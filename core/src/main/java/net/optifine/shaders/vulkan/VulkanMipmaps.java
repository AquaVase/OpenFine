package net.optifine.shaders.vulkan;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderPassDescriptor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vulkan.VulkanCommandEncoder;
import com.mojang.blaze3d.vulkan.VulkanConst;
import com.mojang.blaze3d.vulkan.VulkanDevice;
import com.mojang.blaze3d.vulkan.VulkanGpuTexture;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkFormatProperties;
import org.lwjgl.vulkan.VkImageBlit;

public class VulkanMipmaps {
    public static void generateDepth(VulkanDevice device, GpuTexture texture) {
        if (texture.isClosed() || !texture.getFormat().hasDepthAspect()) {
            throw new IllegalArgumentException("Depth mipmap generation requires an open depth texture");
        }
        if (texture.getMipLevels() == 1) {
            return;
        }
        VulkanShaderRuntime runtime = VulkanShaderRuntime.getInstance();
        int program = runtime.getDepthMipmapProgram();
        VulkanShaderRuntime.TextureBinding previous = runtime.getTexture(0);
        try (VulkanTextureSampler sampler = new VulkanTextureSampler(device, 9729, 9729, false)) {
            runtime.getProgram(program).uniforms().setInt("sourceDepth", 0);
            for (int level = 1; level < texture.getMipLevels(); level++) {
                try (var source = RenderSystem.getDevice().createTextureView(texture, level - 1, 1);
                     var target = RenderSystem.getDevice().createTextureView(texture, level, 1)) {
                    runtime.bindTexture(0, source, sampler.getHandle());
                    var descriptor = RenderPassDescriptor.create(() -> "OptiFine depth mipmap").withDepthAttachment(target)
                        .withRenderArea(new RenderPass.RenderArea(0, 0, texture.getWidth(level), texture.getHeight(level)));
                    runtime.drawComposite(program, descriptor, true);
                }
            }
        } finally {
            if (previous == null) {
                runtime.unbindTexture(0);
            } else {
                runtime.bindTexture(0, previous.view(), previous.sampler());
            }
        }
    }

    public static void generateColor(VulkanDevice device, GpuTexture texture) {
        if (texture.isClosed() || !texture.getFormat().hasColorAspect()
            || (texture.usage() & (GpuTexture.USAGE_COPY_SRC | GpuTexture.USAGE_COPY_DST)) != (GpuTexture.USAGE_COPY_SRC | GpuTexture.USAGE_COPY_DST)) {
            throw new IllegalArgumentException("Color mipmap generation requires an open color texture with copy source and destination usage");
        }
        if (texture.getMipLevels() == 1) {
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkFormatProperties properties = VkFormatProperties.calloc(stack);
            VK12.vkGetPhysicalDeviceFormatProperties(device.vkDevice().getPhysicalDevice(), VulkanConst.toVk(texture.getFormat()), properties);
            int required = VK12.VK_FORMAT_FEATURE_BLIT_SRC_BIT | VK12.VK_FORMAT_FEATURE_BLIT_DST_BIT | VK12.VK_FORMAT_FEATURE_SAMPLED_IMAGE_FILTER_LINEAR_BIT;
            if ((properties.optimalTilingFeatures() & required) != required) {
                throw new UnsupportedOperationException("Linear mipmap generation is not supported for " + texture.getFormat());
            }
            VulkanCommandEncoder encoder = device.createCommandEncoder();
            VkCommandBuffer commands = encoder.allocateAndBeginTransientCommandBuffer();
            long image = ((VulkanGpuTexture)texture).vkImage();
            VkImageBlit.Buffer region = VkImageBlit.calloc(1, stack);
            region.srcSubresource().aspectMask(VK12.VK_IMAGE_ASPECT_COLOR_BIT).baseArrayLayer(0).layerCount(1);
            region.dstSubresource().aspectMask(VK12.VK_IMAGE_ASPECT_COLOR_BIT).baseArrayLayer(0).layerCount(1);
            for (int level = 1; level < texture.getMipLevels(); level++) {
                VulkanCommandEncoder.memoryBarrier(commands, stack);
                region.srcSubresource().mipLevel(level - 1);
                region.dstSubresource().mipLevel(level);
                region.srcOffsets(1).set(texture.getWidth(level - 1), texture.getHeight(level - 1), 1);
                region.dstOffsets(1).set(texture.getWidth(level), texture.getHeight(level), 1);
                VK12.vkCmdBlitImage(commands, image, VK12.VK_IMAGE_LAYOUT_GENERAL, image, VK12.VK_IMAGE_LAYOUT_GENERAL, region, VK12.VK_FILTER_LINEAR);
            }
            VulkanCommandEncoder.memoryBarrier(commands, stack);
            VulkanDeviceProgram.check(VK12.vkEndCommandBuffer(commands), "end mipmap command buffer");
            encoder.execute(commands);
        }
    }
}
