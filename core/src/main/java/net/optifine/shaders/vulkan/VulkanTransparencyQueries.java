package net.optifine.shaders.vulkan;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vulkan.VulkanDevice;
import java.nio.LongBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkQueryPoolCreateInfo;

public class VulkanTransparencyQueries implements AutoCloseable {
    private static final int CAPACITY = 4096;
    private static VulkanTransparencyQueries active;
    private final VulkanDevice device;
    private final long pool;
    private final int[] layers = new int[CAPACITY];
    private int count;
    private int layer;
    private boolean overflow;

    public VulkanTransparencyQueries() {
        this.device = (VulkanDevice)RenderSystem.getDevice().getBackend();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer result = stack.mallocLong(1);
            int status = VK12.vkCreateQueryPool(this.device.vkDevice(), VkQueryPoolCreateInfo.calloc(stack)
                .sType$Default().queryType(VK12.VK_QUERY_TYPE_OCCLUSION).queryCount(CAPACITY), null, result);
            if (status != VK12.VK_SUCCESS) {
                throw new IllegalStateException("Cannot create transparency query pool: " + status);
            }
            this.pool = result.get(0);
        }
    }

    public void reset() {
        VK12.vkResetQueryPool(this.device.vkDevice(), this.pool, 0, CAPACITY);
        this.count = 0;
        this.overflow = false;
    }

    public void beginLayer(int layer) {
        this.layer = layer;
        active = this;
    }

    public void endLayer() {
        active = null;
    }

    public static Query beginPass(VkCommandBuffer commandBuffer) {
        VulkanTransparencyQueries current = active;
        if (current == null) {
            return null;
        }
        if (current.count == CAPACITY) {
            current.overflow = true;
            return null;
        }
        int index = current.count++;
        current.layers[index] = current.layer;
        VK12.vkCmdBeginQuery(commandBuffer, current.pool, index, 0);
        return new Query(current.pool, index);
    }

    public static void endPass(VkCommandBuffer commandBuffer, Query query) {
        if (query != null) {
            VK12.vkCmdEndQuery(commandBuffer, query.pool(), query.index());
        }
    }

    public int activeLayers(int maximum) {
        if (this.count == 0) {
            return 0;
        }
        var encoder = this.device.createCommandEncoder();
        try (var fence = encoder.createFence()) {
            encoder.submit();
            if (!fence.awaitCompletion(5000000000L)) {
                throw new IllegalStateException("Timed out reading transparency occupancy");
            }
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer results = stack.mallocLong(this.count);
            int status = VK12.vkGetQueryPoolResults(this.device.vkDevice(), this.pool, 0, this.count,
                results, Long.BYTES, VK12.VK_QUERY_RESULT_64_BIT);
            if (status != VK12.VK_SUCCESS || this.overflow) {
                return maximum;
            }
            int used = 0;
            for (int i = 0; i < this.count; i++) {
                if (results.get(i) != 0) {
                    used = Math.max(used, this.layers[i] + 1);
                }
            }
            return used;
        }
    }

    @Override
    public void close() {
        VK12.vkDestroyQueryPool(this.device.vkDevice(), this.pool, null);
    }

    public record Query(long pool, int index) {
    }
}
