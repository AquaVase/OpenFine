package net.optifine.shaders.vulkan;

import com.mojang.blaze3d.vulkan.VulkanDevice;
import java.nio.LongBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkSamplerCreateInfo;

public class VulkanTextureSampler implements AutoCloseable {
    private final VulkanDevice device;
    private final long handle;
    private boolean closed;

    public VulkanTextureSampler(VulkanDevice device, int minFilter, int magFilter, boolean compareDepth) {
        this(device, minFilter, magFilter, compareDepth, true);
    }

    public VulkanTextureSampler(VulkanDevice device, int minFilter, int magFilter, boolean compareDepth, boolean clamp) {
        this.device = device;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkSamplerCreateInfo info = VkSamplerCreateInfo.calloc(stack).sType$Default()
                .minFilter(minFilter == 9728 || minFilter == 9984 || minFilter == 9986 ? VK12.VK_FILTER_NEAREST : VK12.VK_FILTER_LINEAR)
                .magFilter(magFilter == 9728 ? VK12.VK_FILTER_NEAREST : VK12.VK_FILTER_LINEAR)
                .mipmapMode(minFilter == 9984 || minFilter == 9985 ? VK12.VK_SAMPLER_MIPMAP_MODE_NEAREST : VK12.VK_SAMPLER_MIPMAP_MODE_LINEAR)
                .addressModeU(clamp ? VK12.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE : VK12.VK_SAMPLER_ADDRESS_MODE_REPEAT)
                .addressModeV(clamp ? VK12.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE : VK12.VK_SAMPLER_ADDRESS_MODE_REPEAT)
                .addressModeW(clamp ? VK12.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE : VK12.VK_SAMPLER_ADDRESS_MODE_REPEAT).minLod(0.0F)
                .maxLod(minFilter >= 9984 && minFilter <= 9987 ? VK12.VK_LOD_CLAMP_NONE : 0.0F)
                .compareEnable(compareDepth).compareOp(VK12.VK_COMPARE_OP_LESS_OR_EQUAL);
            LongBuffer pointer = stack.callocLong(1);
            VulkanDeviceProgram.check(VK12.vkCreateSampler(device.vkDevice(), info, null, pointer), "create shader texture sampler");
            this.handle = pointer.get(0);
        }
    }

    public long getHandle() {
        if (this.closed) {
            throw new IllegalStateException("Shader texture sampler is closed");
        }
        return this.handle;
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.closed = true;
            this.device.createCommandEncoder().queueForDestroy(() -> VK12.vkDestroySampler(this.device.vkDevice(), this.handle, null));
        }
    }
}
