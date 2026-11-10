package net.optifine.shaders.vulkan;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.vulkan.VulkanDevice;
import com.mojang.blaze3d.vulkan.VulkanGpuBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;

public class VulkanShaderBuffers {
    public static final int USAGE_STORAGE = 1024;
    private final Map<Slot, GpuBufferSlice> buffers = new LinkedHashMap<>();
    private final long storageAlignment;
    private final long uniformAlignment;
    private final long maxStorageRange;
    private final long maxUniformRange;

    public VulkanShaderBuffers(VulkanDevice device) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.calloc(stack);
            VK12.vkGetPhysicalDeviceProperties(device.vkDevice().getPhysicalDevice(), properties);
            this.storageAlignment = properties.limits().minStorageBufferOffsetAlignment();
            this.uniformAlignment = properties.limits().minUniformBufferOffsetAlignment();
            this.maxStorageRange = Integer.toUnsignedLong(properties.limits().maxStorageBufferRange());
            this.maxUniformRange = Integer.toUnsignedLong(properties.limits().maxUniformBufferRange());
        }
    }

    // Bindings borrow buffers. Their owner controls allocation and deferred destruction.
    public void bind(int descriptorType, int set, int binding, int element, GpuBufferSlice buffer) {
        Slot slot = new Slot(descriptorType, set, binding, element);
        checkSlot(slot);
        if (buffer == null) {
            this.buffers.remove(slot);
        } else {
            this.validate(descriptorType, buffer, 0);
            this.buffers.put(slot, buffer);
        }
    }

    public GpuBufferSlice get(int descriptorType, int set, int binding, int element) {
        return this.buffers.get(new Slot(descriptorType, set, binding, element));
    }

    public GpuBufferSlice resolve(VulkanShaderProgram.Binding binding, int element) {
        var first = binding.resources().getFirst().resource();
        for (var reference : binding.resources()) {
            var resource = reference.resource();
            if (resource.originalSet() != first.originalSet() || resource.originalBinding() != first.originalBinding()) {
                throw new IllegalArgumentException("Conflicting explicit buffer slots across stages: " + binding.name());
            }
        }
        GpuBufferSlice slice = this.get(binding.descriptorType(), first.originalSet(), first.originalBinding(), element);
        if (slice == null) {
            throw new IllegalStateException("Missing shader buffer " + binding.name() + " at set " + first.originalSet()
                + ", binding " + first.originalBinding() + ", element " + element);
        }
        this.validate(binding.descriptorType(), slice, first.size());
        return slice;
    }

    public void validate(int descriptorType, GpuBufferSlice slice, long minimumSize) {
        boolean storage = descriptorType == VK12.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER;
        if (!storage && descriptorType != VK12.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER) {
            throw new IllegalArgumentException("Expected uniform or storage buffer descriptor");
        }
        int usage = storage ? USAGE_STORAGE : GpuBuffer.USAGE_UNIFORM;
        long alignment = storage ? this.storageAlignment : this.uniformAlignment;
        long maxRange = storage ? this.maxStorageRange : this.maxUniformRange;
        if (!(slice.buffer() instanceof VulkanGpuBuffer) || slice.buffer().isClosed() || (slice.buffer().usage() & usage) == 0
            || slice.offset() < 0 || slice.length() <= 0 || slice.offset() > slice.buffer().size() - slice.length()
            || slice.offset() % alignment != 0 || slice.length() > maxRange || slice.length() < minimumSize) {
            throw new IllegalArgumentException("Invalid shader buffer range, usage, alignment or lifetime");
        }
    }

    private static void checkSlot(Slot slot) {
        if (slot.set() < 0 || slot.binding() < 0 || slot.element() < 0
            || slot.descriptorType() != VK12.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER && slot.descriptorType() != VK12.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER) {
            throw new IllegalArgumentException("Invalid shader buffer slot: " + slot);
        }
    }

    public void clear() {
        this.buffers.clear();
    }

    private record Slot(int descriptorType, int set, int binding, int element) {
    }
}
