package net.optifine.shaders.vulkan;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDevice;

public class VulkanDescriptorAllocator implements AutoCloseable {
    private static final int SETS_PER_POOL = 128;
    private final VkDevice device;
    private final long layout;
    private final List<VulkanShaderProgram.Binding> bindings;
    private final List<Long> pools = new ArrayList<>();
    private final ArrayDeque<Long> available = new ArrayDeque<>();
    private boolean closed;

    public VulkanDescriptorAllocator(VkDevice device, long layout, List<VulkanShaderProgram.Binding> bindings) {
        this.device = device;
        this.layout = layout;
        this.bindings = List.copyOf(bindings);
    }

    public long acquire() {
        if (this.closed) {
            throw new IllegalStateException("Shader descriptor allocator is closed");
        }
        if (this.available.isEmpty()) {
            this.allocatePool();
        }
        return this.available.removeFirst();
    }

    int getPoolCount() {
        return this.pools.size();
    }

    // Called only after the encoder's retirement queue has waited for GPU completion.
    public void release(long set) {
        if (!this.closed) {
            this.available.addLast(set);
        }
    }

    private void allocatePool() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var counts = new LinkedHashMap<Integer, Integer>();
            for (var binding : this.bindings) {
                counts.merge(binding.descriptorType(), Math.multiplyExact(binding.count(), SETS_PER_POOL), Math::addExact);
            }
            var sizes = VkDescriptorPoolSize.calloc(counts.size(), stack);
            counts.forEach((type, count) -> sizes.get().type(type).descriptorCount(count));
            sizes.flip();
            var info = VkDescriptorPoolCreateInfo.calloc(stack).sType$Default().maxSets(SETS_PER_POOL).pPoolSizes(sizes);
            var pointer = stack.callocLong(1);
            VulkanDeviceProgram.check(VK12.vkCreateDescriptorPool(this.device, info, null, pointer), "create shader descriptor pool");
            long pool = pointer.get(0);
            try {
                var layouts = stack.mallocLong(SETS_PER_POOL);
                for (int i = 0; i < SETS_PER_POOL; i++) {
                    layouts.put(this.layout);
                }
                layouts.flip();
                var allocation = VkDescriptorSetAllocateInfo.calloc(stack).sType$Default().descriptorPool(pool).pSetLayouts(layouts);
                var sets = stack.mallocLong(SETS_PER_POOL);
                VulkanDeviceProgram.check(VK12.vkAllocateDescriptorSets(this.device, allocation, sets), "allocate shader descriptor sets");
                this.pools.add(pool);
                for (int i = 0; i < SETS_PER_POOL; i++) {
                    this.available.addLast(sets.get(i));
                }
            } catch (RuntimeException | Error e) {
                VK12.vkDestroyDescriptorPool(this.device, pool, null);
                throw e;
            }
        }
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.closed = true;
            this.pools.forEach(pool -> VK12.vkDestroyDescriptorPool(this.device, pool, null));
            this.pools.clear();
            this.available.clear();
        }
    }
}
