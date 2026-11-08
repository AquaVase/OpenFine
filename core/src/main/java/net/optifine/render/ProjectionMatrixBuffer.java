package net.optifine.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

public class ProjectionMatrixBuffer implements AutoCloseable {
    private final GpuBuffer buffer;
    private final GpuBufferSlice bufferSlice;

    public ProjectionMatrixBuffer(String nameIn) {
        GpuDevice gpudevice = RenderSystem.getDevice();
        this.buffer = gpudevice.createBuffer(() -> "Projection matrix UBO: " + nameIn, 136, RenderSystem.PROJECTION_MATRIX_UBO_SIZE);
        this.bufferSlice = this.buffer.slice(0L, RenderSystem.PROJECTION_MATRIX_UBO_SIZE);
    }

    public GpuBufferSlice getBuffer(Matrix4f matrixIn) {
        try (MemoryStack memorystack = MemoryStack.stackPush()) {
            ByteBuffer bytebuffer = Std140Builder.onStack(memorystack, RenderSystem.PROJECTION_MATRIX_UBO_SIZE).putMat4f(matrixIn).get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.buffer.slice(), bytebuffer);
        }

        this.bufferSlice.setData(matrixIn);
        return this.bufferSlice;
    }

    @Override
    public void close() {
        this.buffer.close();
    }
}
