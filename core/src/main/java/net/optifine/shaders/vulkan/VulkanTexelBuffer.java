package net.optifine.shaders.vulkan;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.vulkan.VulkanConst;
import com.mojang.blaze3d.vulkan.VulkanDevice;
import com.mojang.blaze3d.vulkan.VulkanGpuBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkBufferViewCreateInfo;
import org.lwjgl.vulkan.VkFormatProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;

// The view borrows the buffer; both must remain open until the draw is recorded.
public class VulkanTexelBuffer implements AutoCloseable {
    public static final int USAGE_STORAGE_TEXEL = 2048;
    private final VulkanDevice device;
    private final GpuBufferSlice buffer;
    private final boolean storage;
    private final boolean atomic;
    private final long view;
    private boolean closed;

    public VulkanTexelBuffer(VulkanDevice device, GpuBufferSlice buffer, GpuFormat format, boolean storage) {
        this.device = device;
        this.buffer = buffer;
        this.storage = storage;
        int usage = storage ? USAGE_STORAGE_TEXEL : GpuBuffer.USAGE_UNIFORM_TEXEL_BUFFER;
        if (!(buffer.buffer() instanceof VulkanGpuBuffer) || buffer.buffer().isClosed() || (buffer.buffer().usage() & usage) == 0
            || buffer.offset() < 0 || buffer.length() <= 0 || buffer.offset() > buffer.buffer().size() - buffer.length() || !format.hasColorAspect()) {
            throw new IllegalArgumentException("Invalid shader texel buffer usage, range or lifetime");
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int vkFormat = VulkanConst.toVk(format);
            var properties = VkPhysicalDeviceProperties.calloc(stack);
            VK12.vkGetPhysicalDeviceProperties(device.vkDevice().getPhysicalDevice(), properties);
            var features = VkFormatProperties.calloc(stack);
            VK12.vkGetPhysicalDeviceFormatProperties(device.vkDevice().getPhysicalDevice(), vkFormat, features);
            int required = storage ? VK12.VK_FORMAT_FEATURE_STORAGE_TEXEL_BUFFER_BIT : VK12.VK_FORMAT_FEATURE_UNIFORM_TEXEL_BUFFER_BIT;
            int texelSize = format.blockSize();
            if ((features.bufferFeatures() & required) == 0 || buffer.offset() % properties.limits().minTexelBufferOffsetAlignment() != 0
                || buffer.length() % texelSize != 0 || buffer.length() / texelSize > Integer.toUnsignedLong(properties.limits().maxTexelBufferElements())) {
                throw new IllegalArgumentException("Unsupported shader texel buffer format, alignment or element count");
            }
            this.atomic = (features.bufferFeatures() & VK12.VK_FORMAT_FEATURE_STORAGE_TEXEL_BUFFER_ATOMIC_BIT) != 0;
            var info = VkBufferViewCreateInfo.calloc(stack).sType$Default().buffer(((VulkanGpuBuffer)buffer.buffer()).vkBuffer())
                .format(vkFormat).offset(buffer.offset()).range(buffer.length());
            var pointer = stack.callocLong(1);
            VulkanDeviceProgram.check(VK12.vkCreateBufferView(device.vkDevice(), info, null, pointer), "create shader texel buffer view");
            this.view = pointer.get(0);
        }
    }

    public long getView() {
        if (this.isClosed()) {
            throw new IllegalStateException("Shader texel buffer view or buffer is closed");
        }
        return this.view;
    }

    public boolean isStorage() {
        return this.storage;
    }

    public boolean supportsAtomics() {
        return this.atomic;
    }

    public boolean isClosed() {
        return this.closed || this.buffer.buffer().isClosed();
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.closed = true;
            this.device.createCommandEncoder().queueForDestroy(() -> VK12.vkDestroyBufferView(this.device.vkDevice(), this.view, null));
        }
    }
}
