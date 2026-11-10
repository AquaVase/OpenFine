package net.optifine.shaders.vulkan;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vulkan.VulkanConst;
import com.mojang.blaze3d.vulkan.VulkanDevice;
import com.mojang.blaze3d.vulkan.VulkanGpuTexture;
import com.mojang.blaze3d.vulkan.VulkanGpuTextureView;
import java.nio.ByteBuffer;
import net.optifine.texture.InternalFormat;
import net.optifine.texture.PixelFormat;
import net.optifine.texture.PixelType;
import net.optifine.texture.TextureType;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkFormatProperties;

public class VulkanRawTexture implements AutoCloseable {
    private VulkanGpuTexture texture;
    private VulkanGpuTextureView view;
    private VulkanTextureSampler sampler;

    public VulkanRawTexture(
        TextureType type, InternalFormat internalFormat, int width, int height, int depth,
        PixelFormat pixelFormat, PixelType pixelType, ByteBuffer data, boolean blur, boolean clamp
    ) {
        RenderSystem.assertOnRenderThread();
        int imageType = switch (type) {
            case TEXTURE_1D -> VK12.VK_IMAGE_TYPE_1D;
            case TEXTURE_2D, TEXTURE_RECTANGLE -> VK12.VK_IMAGE_TYPE_2D;
            case TEXTURE_3D -> VK12.VK_IMAGE_TYPE_3D;
        };
        int actualHeight = type == TextureType.TEXTURE_1D ? 1 : height;
        int actualDepth = type == TextureType.TEXTURE_3D ? depth : 1;
        VulkanDevice device = (VulkanDevice)RenderSystem.getDevice().getBackend();
        GpuFormat requested = switch (internalFormat) {
            case R3_G3_B2, RGB5_A1, RGB9_E5 -> GpuFormat.RGBA32_FLOAT;
            default -> VulkanFramebuffer.getFormat(internalFormat.getId());
        };
        GpuFormat format = supportedFormat(device, requested, blur);
        ByteBuffer pixels = VulkanRawTextureData.convert(format, internalFormat, pixelFormat, pixelType, data, width, actualHeight, actualDepth);
        try {
            this.texture = new VulkanGpuTexture(device, GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_SRC | GpuTexture.USAGE_COPY_DST,
                "OptiFine raw " + type, format, width, actualHeight, actualDepth, 1, imageType);
            this.view = new VulkanGpuTextureView(device, this.texture, 0, 1, requested.componentCount() == 3 && format.componentCount() == 4, false);
            this.sampler = new VulkanTextureSampler(device, blur ? 9729 : 9728, blur ? 9729 : 9728, false,
                clamp || type == TextureType.TEXTURE_RECTANGLE);
            int sliceBytes = Math.multiplyExact(Math.multiplyExact(width, actualHeight), format.blockSize());
            for (int z = 0; z < actualDepth; z++) {
                device.createCommandEncoder().writeToTexture(this.texture, pixels.slice(z * sliceBytes, sliceBytes), 0, z, 0, 0, width, actualHeight);
            }
        } catch (RuntimeException | Error e) {
            this.close();
            throw e;
        } finally {
            MemoryUtil.memFree(pixels);
        }
    }

    private static GpuFormat supportedFormat(VulkanDevice device, GpuFormat requested, boolean linear) {
        if (supports(device, requested, linear)) {
            return requested;
        }
        if (requested.componentCount() == 3) {
            for (GpuFormat padded : GpuFormat.values()) {
                if (padded.componentCount() == 4 && padded.componentType() == requested.componentType() && supports(device, padded, linear)) {
                    return padded;
                }
            }
        }
        throw new UnsupportedOperationException("Vulkan device cannot sample raw texture format " + requested + " with linear=" + linear);
    }

    private static boolean supports(VulkanDevice device, GpuFormat format, boolean linear) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkFormatProperties properties = VkFormatProperties.calloc(stack);
            VK12.vkGetPhysicalDeviceFormatProperties(device.vkDevice().getPhysicalDevice(), VulkanConst.toVk(format), properties);
            int required = VK12.VK_FORMAT_FEATURE_SAMPLED_IMAGE_BIT | VK12.VK_FORMAT_FEATURE_TRANSFER_DST_BIT | VK12.VK_FORMAT_FEATURE_TRANSFER_SRC_BIT;
            if (linear) {
                required |= VK12.VK_FORMAT_FEATURE_SAMPLED_IMAGE_FILTER_LINEAR_BIT;
            }
            return (properties.optimalTilingFeatures() & required) == required;
        }
    }

    public void bind(int unit) {
        if (this.view == null || this.view.isClosed()) {
            throw new IllegalStateException("Raw shader texture is closed");
        }
        VulkanShaderRuntime.getInstance().bindTexture(unit, this.view, this.sampler.getHandle());
    }

    @Override
    public void close() {
        if (this.sampler != null) {
            this.sampler.close();
        }
        if (this.view != null) {
            this.view.close();
        }
        if (this.texture != null) {
            this.texture.close();
        }
    }
}
