package net.optifine.shaders.vulkan;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderPassDescriptor;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vulkan.VulkanConst;
import com.mojang.blaze3d.vulkan.VulkanDevice;
import com.mojang.blaze3d.vulkan.VulkanGpuTexture;
import com.mojang.blaze3d.vulkan.VulkanGpuTextureView;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import net.optifine.shaders.DrawBuffers;
import net.optifine.util.DynamicDimension;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkFormatProperties;

public class VulkanFramebuffer implements AutoCloseable {
    private final GpuDevice device;
    private final String name;
    private final int width;
    private final int height;
    private final Texture[][] colors;
    private final Texture[] depths;
    private final List<Texture> ownedTextures = new ArrayList<>();
    private final boolean[] flipped;
    private final boolean[] changed;
    private final boolean[] depthFilterNearest;
    private final boolean[] depthFilterHardware;
    private final int[] depthMinFilters;
    private final int[] colorMinFilters;
    private final int[] colorMagFilters;
    private final Map<SamplerKey, VulkanTextureSampler> samplers = new LinkedHashMap<>();
    private Texture depthTarget;
    private final boolean separateDepthTarget;
    private boolean drawMain = true;
    private boolean depthAttached = true;
    private boolean closed;

    public VulkanFramebuffer(
        GpuDevice device, String name, int width, int height, int[] formats, int colorCount, int depthCount,
        DynamicDimension[] sizes, boolean separateDepthTarget, boolean[] depthFilterNearest, boolean[] depthFilterHardware, boolean[] colorFilterNearest
    ) {
        this.device = device;
        this.name = name;
        this.width = width;
        this.height = height;
        this.colors = new Texture[2][colorCount];
        this.depths = new Texture[depthCount];
        this.flipped = new boolean[colorCount];
        this.changed = new boolean[colorCount];
        this.depthFilterNearest = depthFilterNearest.clone();
        this.depthFilterHardware = depthFilterHardware.clone();
        this.depthMinFilters = new int[depthCount];
        for (int i = 0; i < depthCount; i++) {
            this.depthMinFilters[i] = depthFilterNearest[i] ? 9728 : 9729;
        }
        this.colorMinFilters = new int[colorCount];
        this.colorMagFilters = new int[colorCount];
        for (int i = 0; i < colorCount; i++) {
            this.colorMinFilters[i] = this.colorMagFilters[i] = colorFilterNearest[i] ? 9728 : 9729;
        }
        this.separateDepthTarget = separateDepthTarget;
        try {
            for (int i = 0; i < colorCount; i++) {
                Dimension size = sizes[i] == null ? new Dimension(width, height) : sizes[i].getDimension(width, height);
                GpuFormat format = getFormat(formats[i]);
                this.colors[0][i] = this.createTexture(name + " color" + i + "/A", format, size.width, size.height);
                this.colors[1][i] = this.createTexture(name + " color" + i + "/B", format, size.width, size.height);
            }
            for (int i = 0; i < depthCount; i++) {
                this.depths[i] = this.createTexture(name + " depth" + i, GpuFormat.D32_FLOAT, width, height);
            }
            if (separateDepthTarget) {
                this.depthTarget = this.createTexture(name + " depthTarget", GpuFormat.D32_FLOAT, width, height);
            } else if (depthCount > 0) {
                this.depthTarget = this.depths[0];
            }
        } catch (RuntimeException | Error e) {
            this.close();
            throw e;
        }
    }

    public static GpuFormat getFormat(int internalFormat) {
        // Unsized legacy formats use their sized equivalents, as with glTexImage2D.
        int sizedFormat = switch (internalFormat) {
            case 6403 -> 33321;
            case 33319 -> 33323;
            case 6407 -> 32849;
            case 6408 -> 32856;
            default -> internalFormat;
        };
        GpuFormat rgbFormat = switch (sizedFormat) {
            case 32849 -> GpuFormat.RGB8_UNORM;
            case 32852 -> GpuFormat.RGB16_UNORM;
            case 36758 -> GpuFormat.RGB8_SNORM;
            case 36762 -> GpuFormat.RGB16_SNORM;
            case 34843 -> GpuFormat.RGB16_FLOAT;
            case 34837 -> GpuFormat.RGB32_FLOAT;
            case 36239 -> GpuFormat.RGB8_SINT;
            case 36221 -> GpuFormat.RGB8_UINT;
            case 36233 -> GpuFormat.RGB16_SINT;
            case 36215 -> GpuFormat.RGB16_UINT;
            default -> null;
        };
        if (rgbFormat != null) {
            return rgbFormat;
        }
        for (GpuFormat format : GpuFormat.values()) {
            if (format.hasColorAspect() && GlConst.toGlInternalId(format) == sizedFormat) {
                return format;
            }
        }

        throw new IllegalArgumentException("Unsupported shader color format: " + internalFormat);
    }

    private Texture createTexture(String label, GpuFormat format, int width, int height) {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Invalid shader attachment size: " + label + ": " + width + "x" + height);
        }

        int levels = 32 - Integer.numberOfLeadingZeros(Math.max(width, height));
        VulkanDevice device = (VulkanDevice)this.device.getBackend();
        GpuFormat actualFormat = getSupportedFormat(device, format);
        int usage = 15;
        if (actualFormat.hasColorAspect()) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                VkFormatProperties properties = VkFormatProperties.calloc(stack);
                VK12.vkGetPhysicalDeviceFormatProperties(device.vkDevice().getPhysicalDevice(), VulkanConst.toVk(actualFormat), properties);
                if ((properties.optimalTilingFeatures() & VK12.VK_FORMAT_FEATURE_STORAGE_IMAGE_BIT) != 0) {
                    usage |= GpuTexture.USAGE_STORAGE_BINDING;
                }
            }
        }
        GpuTexture texture = this.device.createTexture(label, usage, actualFormat, width, height, 1, levels);
        GpuTextureView sampled = null;
        GpuTextureView attachment = null;
        try {
            sampled = new VulkanGpuTextureView(device, (VulkanGpuTexture)texture, 0, levels, format.componentCount() == 3 && actualFormat.componentCount() == 4,
                format.hasDepthAspect());
            attachment = this.device.createTextureView(texture, 0, 1);
            Texture result = new Texture(texture, sampled, attachment);
            this.ownedTextures.add(result);
            return result;
        } catch (RuntimeException | Error e) {
            if (attachment != null) {
                attachment.close();
            }
            if (sampled != null) {
                sampled.close();
            }
            texture.close();
            throw e;
        }
    }

    private static GpuFormat getSupportedFormat(VulkanDevice device, GpuFormat format) {
        if (supportsFormat(device, format)) {
            return format;
        }
        if (format.componentCount() == 3) {
            for (GpuFormat padded : GpuFormat.values()) {
                if (padded.componentCount() == 4 && padded.componentType() == format.componentType() && supportsFormat(device, padded)) {
                    return padded;
                }
            }
        }
        throw new IllegalArgumentException("Vulkan device cannot render and sample shader format " + format);
    }

    private static boolean supportsFormat(VulkanDevice device, GpuFormat format) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkFormatProperties properties = VkFormatProperties.calloc(stack);
            VK12.vkGetPhysicalDeviceFormatProperties(device.vkDevice().getPhysicalDevice(), VulkanConst.toVk(format), properties);
            int required = VK12.VK_FORMAT_FEATURE_SAMPLED_IMAGE_BIT
                | (format.hasDepthAspect() ? VK12.VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT : VK12.VK_FORMAT_FEATURE_COLOR_ATTACHMENT_BIT);
            return (properties.optimalTilingFeatures() & required) == required;
        }
    }

    public Texture getColor(boolean main, int index) {
        this.checkOpen();
        return this.colors[main != this.flipped[index] ? 0 : 1][index];
    }

    public Texture getDepth(int index) {
        this.checkOpen();
        return this.depths[index];
    }

    public Texture getDepthTarget() {
        this.checkOpen();
        return this.depthTarget;
    }

    public void setDrawMain(boolean main) {
        this.drawMain = main;
    }

    public void setColorFiltering(int minFilter, int magFilter) {
        this.checkOpen();
        for (int i = 0; i < this.colorMinFilters.length; i++) {
            this.colorMinFilters[i] = minFilter;
            this.colorMagFilters[i] = magFilter;
        }
    }

    public int getColorCount() {
        return this.flipped.length;
    }

    public void bindColor(int index, int unit) {
        Texture texture = this.getColor(true, index);
        VulkanShaderRuntime.getInstance().bindTexture(unit, texture.sampled(), this.getSampler(this.colorMinFilters[index], this.colorMagFilters[index], false));
    }

    public void generateColorMipmaps(boolean main, int index, int minFilter) {
        VulkanMipmaps.generateColor((VulkanDevice)this.device.getBackend(), this.getColor(main, index).texture());
        this.colorMinFilters[index] = minFilter;
    }

    public void generateDepthMipmaps(int index) {
        VulkanMipmaps.generateDepth((VulkanDevice)this.device.getBackend(), this.getDepth(index).texture());
        this.depthMinFilters[index] = this.depthFilterNearest[index] ? 9984 : 9987;
    }

    public void bindDepth(int index, int unit) {
        Texture texture = this.getDepth(index);
        int filter = this.depthFilterNearest[index] ? 9728 : 9729;
        VulkanShaderRuntime.getInstance().bindTexture(unit, texture.sampled(), this.getSampler(this.depthMinFilters[index], filter, this.depthFilterHardware[index]));
    }

    public void bindImage(int index, boolean main, int unit) {
        VulkanShaderRuntime.getInstance().bindImage(unit, this.getColor(main, index).attachment());
    }

    private long getSampler(int minFilter, int magFilter, boolean compareDepth) {
        SamplerKey key = new SamplerKey(minFilter, magFilter, compareDepth);
        return this.samplers.computeIfAbsent(key,
            value -> new VulkanTextureSampler((VulkanDevice)this.device.getBackend(), value.minFilter(), value.magFilter(), value.compareDepth())).getHandle();
    }

    public void setDepthAttached(boolean attached) {
        this.depthAttached = attached;
    }

    public void flipColor(int index) {
        this.checkOpen();
        this.flipped[index] = !this.flipped[index];
        this.changed[index] = true;
    }

    public void flipDepth(int first, int second) {
        this.checkOpen();
        Texture temp = this.depths[first];
        this.depths[first] = this.depths[second];
        this.depths[second] = temp;
        if (!this.separateDepthTarget) {
            this.depthTarget = this.depths[0];
        }
    }

    public void flipDepthTarget() {
        this.checkOpen();
        if (this.separateDepthTarget) {
            Texture temp = this.depthTarget;
            this.depthTarget = this.depths[0];
            this.depths[0] = temp;
        }
    }

    public RenderPassDescriptor createRenderPassDescriptor(DrawBuffers drawBuffers) {
        this.checkOpen();
        RenderPassDescriptor descriptor = RenderPassDescriptor.create(() -> this.name);
        int width = this.width;
        int height = this.height;
        for (int i = 0; i < drawBuffers.limit(); i++) {
            int attachment = drawBuffers.get(i);
            if (attachment == 0) {
                descriptor.withUnusedColorAttachment();
            } else {
                Texture texture = this.getColor(this.drawMain, attachment - 36064);
                descriptor.withColorAttachment(texture.attachment());
                width = texture.texture().getWidth();
                height = texture.texture().getHeight();
            }
        }
        if (this.depthAttached && this.depthTarget != null) {
            descriptor.withDepthAttachment(this.depthTarget.attachment());
        }
        return descriptor.withRenderArea(new RenderPass.RenderArea(0, 0, width, height));
    }

    public void clearColors(boolean[] clear, Vector4f[] colors) {
        this.checkOpen();
        CommandEncoder encoder = this.device.createCommandEncoder();
        for (int i = 0; i < this.flipped.length; i++) {
            if (clear[i]) {
                if (colors[i] == null) {
                    throw new IllegalArgumentException("Missing clear color for shader attachment " + i);
                }
                encoder.clearColorTexture(this.getColor(true, i).texture(), colors[i]);
                if (this.changed[i]) {
                    encoder.clearColorTexture(this.getColor(false, i).texture(), colors[i]);
                }
            }
        }
    }

    public void clearDepth(double depth) {
        this.checkOpen();
        if (this.depthTarget != null) {
            this.device.createCommandEncoder().clearDepthTexture(this.depthTarget.texture(), depth);
        }
    }

    private void checkOpen() {
        if (this.closed) {
            throw new IllegalStateException("Shader framebuffer is closed: " + this.name);
        }
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.closed = true;
            for (Texture texture : this.ownedTextures) {
                texture.attachment().close();
                texture.sampled().close();
                texture.texture().close();
            }
            this.ownedTextures.clear();
            this.samplers.values().forEach(VulkanTextureSampler::close);
            this.samplers.clear();
        }
    }

    public record Texture(GpuTexture texture, GpuTextureView sampled, GpuTextureView attachment) {
    }

    private record SamplerKey(int minFilter, int magFilter, boolean compareDepth) {
    }
}
