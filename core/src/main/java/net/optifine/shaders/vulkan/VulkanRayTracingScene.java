package net.optifine.shaders.vulkan;

import com.mojang.blaze3d.vulkan.VulkanDevice;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.optifine.Config;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.KHRAccelerationStructure;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkAccelerationStructureBuildGeometryInfoKHR;
import org.lwjgl.vulkan.VkAccelerationStructureBuildRangeInfoKHR;
import org.lwjgl.vulkan.VkAccelerationStructureBuildSizesInfoKHR;
import org.lwjgl.vulkan.VkAccelerationStructureCreateInfoKHR;
import org.lwjgl.vulkan.VkAccelerationStructureDeviceAddressInfoKHR;
import org.lwjgl.vulkan.VkAccelerationStructureGeometryKHR;
import org.lwjgl.vulkan.VkAccelerationStructureInstanceKHR;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkBufferDeviceAddressInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkMemoryAllocateFlagsInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryBarrier;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDeviceAccelerationStructurePropertiesKHR;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties2;

// Experimental block broad phase. Sundial still resolves texture alpha and exact voxel shapes.
public class VulkanRayTracingScene implements AutoCloseable {
    public static final int DIAMETER = 64;
    private static volatile long worldRevision;
    private final VulkanDevice device;
    private Snapshot snapshot;
    private ClientLevel world;
    private long revision = -1;
    private long nextScan;
    private int originX;
    private int originY;
    private int originZ;
    private int[] cells;
    private final int[] scanCells = new int[DIAMETER * DIAMETER * DIAMETER];
    private int builds;

    public VulkanRayTracingScene(VulkanDevice device) {
        if (!VulkanRayTracingFeatures.isEnabled(device.vkDevice())) {
            throw new IllegalStateException("Hardware ray query is not enabled on this Vulkan device");
        }
        this.device = device;
    }

    public static void invalidate() {
        worldRevision++;
    }

    public void prepare() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            this.close();
            return;
        }
        var camera = mc.gameRenderer.mainCamera().position();
        int x = Math.floorDiv((int)Math.floor(camera.x()), 16) * 16 - 24;
        int y = Math.floorDiv((int)Math.floor(camera.y()), 16) * 16 - 24;
        int z = Math.floorDiv((int)Math.floor(camera.z()), 16) * 16 - 24;
        long currentRevision = worldRevision;
        long now = System.nanoTime();
        boolean sameWorld = this.world == mc.level;
        boolean sameOrigin = this.originX == x && this.originY == y && this.originZ == z;
        if (sameWorld && sameOrigin && this.revision == currentRevision && now < this.nextScan && this.snapshot != null) {
            return;
        }
        // Also detects chunk arrival/unload. Never asks the server to generate a chunk.
        int[] found = this.scanCells;
        int count = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dz = 0; dz < DIAMETER; dz++) {
            for (int dx = 0; dx < DIAMETER; dx++) {
                if (!mc.level.hasChunk((x + dx) >> 4, (z + dz) >> 4)) {
                    continue;
                }
                for (int dy = 0; dy < DIAMETER; dy++) {
                    pos.set(x + dx, y + dy, z + dz);
                    if (!mc.level.getBlockState(pos).isAir()) {
                        found[count++] = dx | dy << 6 | dz << 12;
                    }
                }
            }
        }
        this.nextScan = now + 1000000000L;
        this.revision = currentRevision;
        if (sameWorld && sameOrigin && this.cells != null && Arrays.equals(this.cells, 0, this.cells.length, found, 0, count) && this.snapshot != null) {
            return;
        }
        found = Arrays.copyOf(found, count);
        Snapshot replacement = this.build(x, y, z, found);
        Snapshot previous = this.snapshot;
        this.snapshot = replacement;
        this.world = mc.level;
        this.originX = x;
        this.originY = y;
        this.originZ = z;
        this.cells = found;
        this.builds++;
        if (previous != null) {
            this.device.createCommandEncoder().queueForDestroy(previous::close);
        }
        Config.dbg("Hardware RT scene built: " + count + " block AABBs, origin=" + x + "," + y + "," + z + ", builds=" + this.builds);
    }

    public void updateUniforms(VulkanProgramUniforms uniforms) {
        if (uniforms.contains("ofRtSceneRelativeOrigin")) {
            var camera = Minecraft.getInstance().gameRenderer.mainCamera().position();
            // Subtract doubles before converting to float, independently of OptiFine's camera rebasing.
            uniforms.setFloat("ofRtSceneRelativeOrigin", (float)(this.originX - camera.x()),
                (float)(this.originY - camera.y()), (float)(this.originZ - camera.z()));
        }
    }

    public long accelerationStructure() {
        if (this.snapshot == null) {
            throw new IllegalStateException("Hardware RT scene was not prepared before rendering");
        }
        return this.snapshot.topLevel;
    }

    public long metadataBuffer() {
        this.accelerationStructure();
        return this.snapshot.metadata.buffer;
    }

    public long metadataSize() {
        return this.snapshot.metadata.size;
    }

    public int getBuildCount() {
        return this.builds;
    }

    private Snapshot build(int x, int y, int z, int[] cells) {
        Snapshot result = new Snapshot();
        List<Buffer> temporary = new ArrayList<>();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int inputUsage = KHRAccelerationStructure.VK_BUFFER_USAGE_ACCELERATION_STRUCTURE_BUILD_INPUT_READ_ONLY_BIT_KHR;
            Buffer boxes = this.buffer(Math.max(24L, cells.length * 24L), inputUsage, true);
            temporary.add(boxes);
            result.metadata = this.buffer(16L + cells.length * 16L, VK12.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT, true);
            result.buffers.add(result.metadata);
            ByteBuffer bounds = boxes.map(stack);
            ByteBuffer metadata = result.metadata.map(stack);
            metadata.putInt(x).putInt(y).putInt(z).putInt(DIAMETER);
            for (int cell : cells) {
                int dx = cell & 63;
                int dy = cell >> 6 & 63;
                int dz = cell >> 12 & 63;
                bounds.putFloat(dx).putFloat(dy).putFloat(dz).putFloat(dx + 1).putFloat(dy + 1).putFloat(dz + 1);
                metadata.putInt(dx).putInt(dy).putInt(dz).putInt(0);
            }
            boxes.unmap();
            result.metadata.unmap();
            var properties = VkPhysicalDeviceAccelerationStructurePropertiesKHR.calloc(stack).sType$Default();
            VK11.vkGetPhysicalDeviceProperties2(this.device.vkDevice().getPhysicalDevice(),
                VkPhysicalDeviceProperties2.calloc(stack).sType$Default().pNext(properties.address()));
            int alignment = properties.minAccelerationStructureScratchOffsetAlignment();
            var encoder = this.device.createCommandEncoder();
            VkCommandBuffer commands = encoder.allocateAndBeginTransientCommandBuffer();
            barrier(commands, stack, VK12.VK_PIPELINE_STAGE_HOST_BIT, KHRAccelerationStructure.VK_PIPELINE_STAGE_ACCELERATION_STRUCTURE_BUILD_BIT_KHR,
                VK12.VK_ACCESS_HOST_WRITE_BIT, KHRAccelerationStructure.VK_ACCESS_ACCELERATION_STRUCTURE_READ_BIT_KHR);
            var geometry = VkAccelerationStructureGeometryKHR.calloc(1, stack).sType$Default()
                .geometryType(KHRAccelerationStructure.VK_GEOMETRY_TYPE_AABBS_KHR);
            geometry.geometry().aabbs().sType$Default().stride(24).data().deviceAddress(boxes.address(stack));
            result.bottomLevel = this.buildStructure(commands, geometry, cells.length,
                KHRAccelerationStructure.VK_ACCELERATION_STRUCTURE_TYPE_BOTTOM_LEVEL_KHR, alignment, result, temporary, stack);
            barrier(commands, stack, KHRAccelerationStructure.VK_PIPELINE_STAGE_ACCELERATION_STRUCTURE_BUILD_BIT_KHR,
                KHRAccelerationStructure.VK_PIPELINE_STAGE_ACCELERATION_STRUCTURE_BUILD_BIT_KHR,
                KHRAccelerationStructure.VK_ACCESS_ACCELERATION_STRUCTURE_WRITE_BIT_KHR,
                KHRAccelerationStructure.VK_ACCESS_ACCELERATION_STRUCTURE_READ_BIT_KHR);
            var addressInfo = VkAccelerationStructureDeviceAddressInfoKHR.calloc(stack).sType$Default().accelerationStructure(result.bottomLevel);
            long bottomAddress = KHRAccelerationStructure.vkGetAccelerationStructureDeviceAddressKHR(this.device.vkDevice(), addressInfo);
            Buffer instances = this.buffer(64, inputUsage, true);
            temporary.add(instances);
            ByteBuffer instance = instances.map(stack);
            // VkAccelerationStructureInstanceKHR: 3x4 identity, packed custom index/mask and SBT offset/flags, BLAS address.
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 4; col++) {
                    instance.putFloat(row == col ? 1.0F : 0.0F);
                }
            }
            instance.putInt(0xFF000000).putInt(0).putLong(bottomAddress);
            instances.unmap();
            geometry = VkAccelerationStructureGeometryKHR.calloc(1, stack).sType$Default()
                .geometryType(KHRAccelerationStructure.VK_GEOMETRY_TYPE_INSTANCES_KHR);
            geometry.geometry().instances().sType$Default().data().deviceAddress(instances.address(stack));
            result.topLevel = this.buildStructure(commands, geometry, 1,
                KHRAccelerationStructure.VK_ACCELERATION_STRUCTURE_TYPE_TOP_LEVEL_KHR, alignment, result, temporary, stack);
            barrier(commands, stack, KHRAccelerationStructure.VK_PIPELINE_STAGE_ACCELERATION_STRUCTURE_BUILD_BIT_KHR,
                VK12.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT | VK12.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
                KHRAccelerationStructure.VK_ACCESS_ACCELERATION_STRUCTURE_WRITE_BIT_KHR,
                KHRAccelerationStructure.VK_ACCESS_ACCELERATION_STRUCTURE_READ_BIT_KHR);
            check(VK12.vkEndCommandBuffer(commands), "end RT build commands");
            encoder.execute(commands);
            encoder.queueForDestroy(() -> temporary.forEach(Buffer::close));
            return result;
        } catch (RuntimeException | Error e) {
            // Commands may already refer to these allocations; retire through the normal frame fence.
            this.device.createCommandEncoder().queueForDestroy(() -> {
                result.close();
                temporary.forEach(Buffer::close);
            });
            throw e;
        }
    }

    private long buildStructure(VkCommandBuffer commands, VkAccelerationStructureGeometryKHR.Buffer geometry, int count, int type,
        int alignment, Snapshot result, List<Buffer> temporary, MemoryStack stack) {
        var info = VkAccelerationStructureBuildGeometryInfoKHR.calloc(1, stack).sType$Default().type(type)
            .flags(KHRAccelerationStructure.VK_BUILD_ACCELERATION_STRUCTURE_PREFER_FAST_TRACE_BIT_KHR)
            .mode(KHRAccelerationStructure.VK_BUILD_ACCELERATION_STRUCTURE_MODE_BUILD_KHR)
            .geometryCount(geometry.remaining()).pGeometries(geometry);
        var sizes = VkAccelerationStructureBuildSizesInfoKHR.calloc(stack).sType$Default();
        KHRAccelerationStructure.vkGetAccelerationStructureBuildSizesKHR(this.device.vkDevice(),
            KHRAccelerationStructure.VK_ACCELERATION_STRUCTURE_BUILD_TYPE_DEVICE_KHR, info.get(0), stack.ints(count), sizes);
        Buffer storage = this.buffer(sizes.accelerationStructureSize(), KHRAccelerationStructure.VK_BUFFER_USAGE_ACCELERATION_STRUCTURE_STORAGE_BIT_KHR, false);
        result.buffers.add(storage);
        var create = VkAccelerationStructureCreateInfoKHR.calloc(stack).sType$Default().buffer(storage.buffer).size(storage.size).type(type);
        var pointer = stack.mallocLong(1);
        check(KHRAccelerationStructure.vkCreateAccelerationStructureKHR(this.device.vkDevice(), create, null, pointer), "create RT acceleration structure");
        long structure = pointer.get(0);
        result.structures.add(structure);
        Buffer scratch = this.buffer(sizes.buildScratchSize() + alignment, VK12.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT, false);
        temporary.add(scratch);
        long address = scratch.address(stack);
        address = (address + alignment - 1) & -(long)alignment;
        info.dstAccelerationStructure(structure).scratchData().deviceAddress(address);
        var range = VkAccelerationStructureBuildRangeInfoKHR.calloc(stack).primitiveCount(count);
        KHRAccelerationStructure.vkCmdBuildAccelerationStructuresKHR(commands, info, stack.pointers(range.address()));
        return structure;
    }

    private Buffer buffer(long size, int usage, boolean host) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var pointer = stack.mallocLong(1);
            var create = VkBufferCreateInfo.calloc(stack).sType$Default().size(Math.max(16, size))
                .usage(usage | VK12.VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT).sharingMode(VK12.VK_SHARING_MODE_EXCLUSIVE);
            check(VK12.vkCreateBuffer(this.device.vkDevice(), create, null, pointer), "create RT buffer");
            long buffer = pointer.get(0);
            long memory = 0;
            try {
                var requirements = VkMemoryRequirements.calloc(stack);
                VK12.vkGetBufferMemoryRequirements(this.device.vkDevice(), buffer, requirements);
                var properties = VkPhysicalDeviceMemoryProperties.calloc(stack);
                VK12.vkGetPhysicalDeviceMemoryProperties(this.device.vkDevice().getPhysicalDevice(), properties);
                int wanted = host ? VK12.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK12.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT : VK12.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
                int index = -1;
                for (int i = 0; i < properties.memoryTypeCount(); i++) {
                    if ((requirements.memoryTypeBits() & 1 << i) != 0 && (properties.memoryTypes(i).propertyFlags() & wanted) == wanted) {
                        index = i;
                        break;
                    }
                }
                if (index < 0) {
                    throw new IllegalStateException("No compatible memory for RT buffer");
                }
                var flags = VkMemoryAllocateFlagsInfo.calloc(stack).sType$Default().flags(VK12.VK_MEMORY_ALLOCATE_DEVICE_ADDRESS_BIT);
                var allocation = VkMemoryAllocateInfo.calloc(stack).sType$Default().pNext(flags.address())
                    .allocationSize(requirements.size()).memoryTypeIndex(index);
                check(VK12.vkAllocateMemory(this.device.vkDevice(), allocation, null, pointer), "allocate RT memory");
                memory = pointer.get(0);
                check(VK12.vkBindBufferMemory(this.device.vkDevice(), buffer, memory, 0), "bind RT memory");
                return new Buffer(buffer, memory, Math.max(16, size));
            } catch (RuntimeException | Error e) {
                VK12.vkDestroyBuffer(this.device.vkDevice(), buffer, null);
                if (memory != 0) {
                    VK12.vkFreeMemory(this.device.vkDevice(), memory, null);
                }
                throw e;
            }
        }
    }

    private static void barrier(VkCommandBuffer commands, MemoryStack stack, int sourceStage, int targetStage, int sourceAccess, int targetAccess) {
        var barrier = VkMemoryBarrier.calloc(1, stack).sType$Default().srcAccessMask(sourceAccess).dstAccessMask(targetAccess);
        VK12.vkCmdPipelineBarrier(commands, sourceStage, targetStage, 0, barrier, null, null);
    }

    private static void check(int result, String action) {
        if (result != VK12.VK_SUCCESS) {
            throw new IllegalStateException("Failed to " + action + ": VkResult=" + result);
        }
    }

    @Override
    public void close() {
        if (this.snapshot != null) {
            Snapshot old = this.snapshot;
            this.device.createCommandEncoder().queueForDestroy(old::close);
            this.snapshot = null;
        }
        this.world = null;
        this.cells = null;
    }

    private class Buffer implements AutoCloseable {
        private final long buffer;
        private final long memory;
        private final long size;

        Buffer(long buffer, long memory, long size) {
            this.buffer = buffer;
            this.memory = memory;
            this.size = size;
        }

        long address(MemoryStack stack) {
            return VK12.vkGetBufferDeviceAddress(device.vkDevice(), VkBufferDeviceAddressInfo.calloc(stack).sType$Default().buffer(this.buffer));
        }

        ByteBuffer map(MemoryStack stack) {
            var pointer = stack.mallocPointer(1);
            check(VK12.vkMapMemory(device.vkDevice(), this.memory, 0, this.size, 0, pointer), "map RT buffer");
            return MemoryUtil.memByteBuffer(pointer.get(0), Math.toIntExact(this.size));
        }

        void unmap() {
            VK12.vkUnmapMemory(device.vkDevice(), this.memory);
        }

        @Override
        public void close() {
            VK12.vkDestroyBuffer(device.vkDevice(), this.buffer, null);
            VK12.vkFreeMemory(device.vkDevice(), this.memory, null);
        }
    }

    private class Snapshot implements AutoCloseable {
        private final List<Buffer> buffers = new ArrayList<>();
        private final List<Long> structures = new ArrayList<>();
        private Buffer metadata;
        private long bottomLevel;
        private long topLevel;

        @Override
        public void close() {
            for (long structure : this.structures.reversed()) {
                KHRAccelerationStructure.vkDestroyAccelerationStructureKHR(device.vkDevice(), structure, null);
            }
            this.buffers.forEach(Buffer::close);
        }
    }
}
