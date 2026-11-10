package net.optifine.render;

import com.mojang.blaze3d.IndexType;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.VertexArrayCache;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.RenderPassBackend;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.level.ChunkPos;
import net.optifine.Config;
import net.optifine.util.GpuMemory;
import net.optifine.util.LinkedList;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;

public class VboRegion {
    private ChunkPos chunkPos;
    private ChunkSectionLayer layer;
    private int glArrayObjectId;
    private int glBufferId;
    private GpuBuffer gpuBuffer;
    private final boolean vulkan = Config.isVulkan();
    private int capacity = 4096;
    private int positionTop = 0;
    private int sizeUsed;
    private LinkedList<VboRange> rangeList = new LinkedList<>();
    private VboRange compactRangeLast;
    private final ConcurrentLinkedQueue<VboRange> pendingReleases = new ConcurrentLinkedQueue<>();
    private PointerBuffer bufferIndexVertex;
    private IntBuffer bufferCountVertex;
    private IntBuffer bufferDrawParameters;
    private final int vertexBytes = DefaultVertexFormat.BLOCK.getVertexSize();
    private PrimitiveTopology drawMode = PrimitiveTopology.QUADS;

    public VboRegion(ChunkPos chunkPos, ChunkSectionLayer layer) {
        this.chunkPos = chunkPos;
        this.layer = layer;
        if (this.vulkan) {
            this.gpuBuffer = this.createBuffer(this.capacity);
            GpuMemory.bufferAllocated(this.toBytes(this.capacity));
            RenderSystem.getSequentialBuffer(this.drawMode).ensureStorage(this.drawMode.indexCount(this.capacity));
            return;
        }

        this.glArrayObjectId = GlStateManager._glGenVertexArrays();
        this.bindVertexArray();
        RenderSystem.getGlDevice().debugLabels().applyLabelVertexArray(this.glArrayObjectId, "VboRegion " + this.toString());
        unbindVertexArray();
        this.glBufferId = GlStateManager._glGenBuffers();
        this.bindBuffer();
        long capacityBytes = this.toBytes(this.capacity);
        GlStateManager._glBufferData(GlStateManager.GL_ARRAY_BUFFER, capacityBytes, GlStateManager.GL_STATIC_DRAW);
        RenderSystem.getGlDevice().debugLabels().applyLabelBuffer(this.glBufferId, "VboRegion " + this.toString() + ", " + this.capacity);
        GpuMemory.bufferAllocated(capacityBytes);
        this.unbindBuffer();
    }

    public void bufferData(ByteBuffer data, VboRange range) {
        if (this.glBufferId >= 0) {
            int posOld = range.getPosition();
            int sizeOld = range.getSize();
            int sizeNew = this.toVertex(data.limit());
            if (sizeNew <= 0) {
                if (posOld >= 0) {
                    range.setPosition(-1);
                    range.setSize(0);
                    this.rangeList.remove(range.getNode());
                    this.sizeUsed -= sizeOld;
                    if (this.rangeList.isEmpty()) {
                        this.positionTop = 0;
                        this.compactRangeLast = null;
                    }
                }
            } else {
                if (sizeNew > sizeOld) {
                    range.setPosition(this.positionTop);
                    range.setSize(sizeNew);
                    this.positionTop += sizeNew;
                    if (posOld >= 0) {
                        this.rangeList.remove(range.getNode());
                    }

                    this.rangeList.addLast(range.getNode());
                }

                range.setSize(sizeNew);
                this.sizeUsed += sizeNew - sizeOld;
                this.checkVboSize(range.getPositionNext());
                long positionBytes = this.toBytes(range.getPosition());
                if (this.vulkan) {
                    RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.gpuBuffer.slice(positionBytes, data.remaining()), data);
                } else {
                    this.bindVertexArray();
                    this.bindBuffer();
                    GlStateManager._glBufferSubData(GlStateManager.GL_ARRAY_BUFFER, positionBytes, data);
                    this.unbindBuffer();
                    unbindVertexArray();
                }
                if (this.positionTop > this.sizeUsed * 11 / 10) {
                    this.compactRanges(1);
                }
            }
        }
    }

    private void compactRanges(int countMax) {
        if (!this.rangeList.isEmpty()) {
            VboRange range = this.compactRangeLast;
            if (range == null || !this.rangeList.contains(range.getNode())) {
                range = this.rangeList.getFirst().getItem();
            }

            int posCompact = range.getPosition();
            VboRange rangePrev = range.getPrev();
            if (rangePrev == null) {
                posCompact = 0;
            } else {
                posCompact = rangePrev.getPositionNext();
            }

            int count = 0;

            while (range != null && count < countMax) {
                count++;
                if (range.getPosition() == posCompact) {
                    posCompact += range.getSize();
                    range = range.getNext();
                } else {
                    int sizeFree = range.getPosition() - posCompact;
                    if (range.getSize() <= sizeFree) {
                        this.copyVboData(range.getPosition(), posCompact, range.getSize());
                        range.setPosition(posCompact);
                        posCompact += range.getSize();
                        range = range.getNext();
                    } else {
                        this.checkVboSize(this.positionTop + range.getSize());
                        this.copyVboData(range.getPosition(), this.positionTop, range.getSize());
                        range.setPosition(this.positionTop);
                        this.positionTop = this.positionTop + range.getSize();
                        VboRange rangeNext = range.getNext();
                        this.rangeList.remove(range.getNode());
                        this.rangeList.addLast(range.getNode());
                        range = rangeNext;
                    }
                }
            }

            if (range == null) {
                this.positionTop = this.rangeList.getLast().getItem().getPositionNext();
            }

            this.compactRangeLast = range;
        }
    }

    private void checkRanges() {
        int count = 0;
        int size = 0;

        for (VboRange range = this.rangeList.getFirst().getItem(); range != null; range = range.getNext()) {
            count++;
            size += range.getSize();
            if (range.getPosition() < 0 || range.getSize() <= 0 || range.getPositionNext() > this.positionTop) {
                throw new RuntimeException("Invalid range: " + range);
            }

            VboRange rangePrev = range.getPrev();
            if (rangePrev != null && range.getPosition() < rangePrev.getPositionNext()) {
                throw new RuntimeException("Invalid range: " + range);
            }

            VboRange rangeNext = range.getNext();
            if (rangeNext != null && range.getPositionNext() > rangeNext.getPosition()) {
                throw new RuntimeException("Invalid range: " + range);
            }
        }

        if (count != this.rangeList.getSize()) {
            throw new RuntimeException("Invalid count: " + count + " <> " + this.rangeList.getSize());
        }

        if (size != this.sizeUsed) {
            throw new RuntimeException("Invalid size: " + size + " <> " + this.sizeUsed);
        }
    }

    private void checkVboSize(int sizeMin) {
        if (this.capacity < sizeMin) {
            this.expandVbo(sizeMin);
        }
    }

    private void copyVboData(int posFrom, int posTo, int size) {
        long posFromBytes = this.toBytes(posFrom);
        long posToBytes = this.toBytes(posTo);
        long sizeBytes = this.toBytes(size);
        if (this.vulkan) {
            RenderSystem.getDevice().createCommandEncoder().copyToBuffer(
                this.gpuBuffer.slice(posFromBytes, sizeBytes), this.gpuBuffer.slice(posToBytes, sizeBytes)
            );
            return;
        }

        GlStateManager._glBindBuffer(GlStateManager.GL_COPY_READ_BUFFER, this.glBufferId);
        GlStateManager._glBindBuffer(GlStateManager.GL_COPY_WRITE_BUFFER, this.glBufferId);
        GlStateManager.copyBufferSubData(GlStateManager.GL_COPY_READ_BUFFER, GlStateManager.GL_COPY_WRITE_BUFFER, posFromBytes, posToBytes, sizeBytes);
        Config.checkGlError("Copy VBO range");
        GlStateManager._glBindBuffer(GlStateManager.GL_COPY_READ_BUFFER, 0);
        GlStateManager._glBindBuffer(GlStateManager.GL_COPY_WRITE_BUFFER, 0);
    }

    private void expandVbo(int sizeMin) {
        int capacityNew = this.capacity * 6 / 4;

        while (capacityNew < sizeMin) {
            capacityNew = capacityNew * 6 / 4;
        }

        long capacityBytes = this.toBytes(this.capacity);
        long capacityNewBytes = this.toBytes(capacityNew);
        if (this.vulkan) {
            GpuBuffer bufferNew = this.createBuffer(capacityNew);
            RenderSystem.getDevice().createCommandEncoder().copyToBuffer(this.gpuBuffer.slice(), bufferNew.slice(0L, capacityBytes));
            this.gpuBuffer.close();
            this.gpuBuffer = bufferNew;
            GpuMemory.bufferAllocated(capacityNewBytes);
            GpuMemory.bufferFreed(capacityBytes);
            this.capacity = capacityNew;
            RenderSystem.getSequentialBuffer(this.drawMode).ensureStorage(this.drawMode.indexCount(this.capacity));
            return;
        }

        int glBufferIdNew = GlStateManager._glGenBuffers();
        GlStateManager._glBindBuffer(GlStateManager.GL_ARRAY_BUFFER, glBufferIdNew);
        GlStateManager._glBufferData(GlStateManager.GL_ARRAY_BUFFER, capacityNewBytes, GlStateManager.GL_STATIC_DRAW);
        RenderSystem.getGlDevice().debugLabels().applyLabelBuffer(glBufferIdNew, "VboRegion " + this.toString() + ", " + capacityNewBytes);
        GpuMemory.bufferAllocated(capacityNewBytes);
        Config.checkGlError("Expand VBO");
        GlStateManager._glBindBuffer(GlStateManager.GL_ARRAY_BUFFER, 0);
        GlStateManager._glBindBuffer(GlStateManager.GL_COPY_READ_BUFFER, this.glBufferId);
        GlStateManager._glBindBuffer(GlStateManager.GL_COPY_WRITE_BUFFER, glBufferIdNew);
        GlStateManager.copyBufferSubData(GlStateManager.GL_COPY_READ_BUFFER, GlStateManager.GL_COPY_WRITE_BUFFER, 0L, 0L, capacityBytes);
        Config.checkGlError("Copy VBO: " + capacityNewBytes);
        GlStateManager._glBindBuffer(GlStateManager.GL_COPY_READ_BUFFER, 0);
        GlStateManager._glBindBuffer(GlStateManager.GL_COPY_WRITE_BUFFER, 0);
        GlStateManager._glDeleteBuffers(this.glBufferId);
        GpuMemory.bufferFreed(capacityBytes);
        this.glBufferId = glBufferIdNew;
        this.capacity = capacityNew;
    }

    public void bindVertexArray() {
        GlStateManager._glBindVertexArray(this.glArrayObjectId);
    }

    private GpuBuffer createBuffer(int capacity) {
        return RenderSystem.getDevice().createBuffer(() -> "VboRegion " + this + ", " + capacity,
            GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_SRC | GpuBuffer.USAGE_COPY_DST, this.toBytes(capacity));
    }

    public void bindBuffer() {
        GlStateManager._glBindBuffer(GlStateManager.GL_ARRAY_BUFFER, this.glBufferId);
    }

    public void drawArrays(PrimitiveTopology drawMode, VboRange range) {
        if (this.isClosed()) {
            return;
        }
        if (this.drawMode != drawMode) {
            if (this.bufferIndexVertex != null && this.bufferIndexVertex.position() > 0) {
                throw new IllegalArgumentException("Mixed region draw modes: " + this.drawMode + " != " + drawMode);
            }

            this.drawMode = drawMode;
        }

        this.ensureDrawCapacity();
        int indexSize = 4;
        int pos = drawMode.indexCount(range.getPosition()) * indexSize;
        this.bufferIndexVertex.put(pos);
        int count = drawMode.indexCount(range.getSize());
        this.bufferCountVertex.put(count);
    }

    private void ensureDrawCapacity() {
        if (this.bufferIndexVertex != null && this.bufferIndexVertex.hasRemaining()) {
            return;
        }
        int capacity = this.bufferIndexVertex == null ? 64 : Math.multiplyExact(this.bufferIndexVertex.capacity(), 2);
        PointerBuffer indices = MemoryUtil.memAllocPointer(capacity);
        IntBuffer counts;
        try {
            counts = MemoryUtil.memAllocInt(capacity);
        } catch (Throwable throwable) {
            MemoryUtil.memFree(indices);
            throw throwable;
        }
        if (this.bufferIndexVertex != null) {
            indices.put(this.bufferIndexVertex.duplicate().flip());
            counts.put(this.bufferCountVertex.duplicate().flip());
            MemoryUtil.memFree(this.bufferIndexVertex);
            MemoryUtil.memFree(this.bufferCountVertex);
        }
        this.bufferIndexVertex = indices;
        this.bufferCountVertex = counts;
    }

    public void prepareDraw() {
        if (this.isClosed()) {
            return;
        }
        VboRange range;
        while ((range = this.pendingReleases.poll()) != null) {
            this.bufferData(SectionDrawData.emptyBuffer, range);
        }

        if (this.vulkan && this.positionTop > this.sizeUsed * 11 / 10) {
            this.compactRanges(1);
        }
    }

    public void releaseRange(VboRange range) {
        if (!this.isClosed()) {
            this.pendingReleases.add(range);
        }
    }

    public boolean isClosed() {
        return this.glBufferId < 0;
    }

    public void finishDraw() {
        this.bindVertexArray();
        this.bindBuffer();
        VertexArrayCache.Emulated.setupCombinedAttributes(this.layer.pipeline().getVertexFormatBindings(), true, null);
        RenderSystem.AutoStorageIndexBuffer autoIndexBuffer = RenderSystem.getSequentialBuffer(this.drawMode);
        int indexCount = this.drawMode.indexCount(this.positionTop);
        indexCount = Math.max(indexCount, 65536);
        GpuBuffer indexBuffer = autoIndexBuffer.getBuffer(indexCount);
        IndexType indexType = autoIndexBuffer.type();
        GlStateManager._glBindBuffer(34963, ((GlBuffer)indexBuffer).getHandle());
        this.bufferIndexVertex.flip();
        this.bufferCountVertex.flip();
        GlStateManager.glMultiDrawElements(GlConst.toGl(this.drawMode), this.bufferCountVertex, GlConst.toGl(indexType), this.bufferIndexVertex);
        this.bufferIndexVertex.limit(this.bufferIndexVertex.capacity());
        this.bufferCountVertex.limit(this.bufferCountVertex.capacity());
        if (this.positionTop > this.sizeUsed * 11 / 10) {
            this.compactRanges(1);
        }
    }

    public void finishDraw(RenderPassBackend pass) {
        if (!this.vulkan) {
            this.finishDraw();
            return;
        }

        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(this.drawMode);
        int indexCount = this.drawMode.indexCount(this.positionTop);
        if (!indices.hasStorage(indexCount)) {
            throw new IllegalStateException("Region indices must be uploaded before the render pass");
        }

        pass.setVertexBuffer(0, this.gpuBuffer.slice());
        pass.setIndexBuffer(indices.getBuffer(indexCount), indices.type());
        this.bufferIndexVertex.flip();
        this.bufferCountVertex.flip();
        try {
            var info = RenderSystem.getDevice().getDeviceInfo();
            int drawCount = this.bufferCountVertex.limit();
            if (info.features().multiDrawDirectInterleaved() && drawCount > 1) {
                int batchSize = Math.min(drawCount, info.limits().maxMultiDrawDirectInterleavedDrawCount());
                if (this.bufferDrawParameters == null || this.bufferDrawParameters.capacity() < batchSize * 3) {
                    IntBuffer parameters = MemoryUtil.memAllocInt(batchSize * 3);
                    MemoryUtil.memFree(this.bufferDrawParameters);
                    this.bufferDrawParameters = parameters;
                }

                for (int start = 0; start < drawCount; start += batchSize) {
                    this.bufferDrawParameters.clear();
                    int end = Math.min(start + batchSize, drawCount);
                    for (int i = start; i < end; i++) {
                        this.bufferDrawParameters.put((int)(this.bufferIndexVertex.get(i) / 4L));
                        this.bufferDrawParameters.put(this.bufferCountVertex.get(i));
                        this.bufferDrawParameters.put(0);
                    }

                    this.bufferDrawParameters.flip();
                    pass.multiDrawIndexed(this.bufferDrawParameters, 1, 0, end - start);
                }
            } else {
                for (int i = 0; i < drawCount; i++) {
                    pass.drawIndexed(this.bufferCountVertex.get(i), 1, (int)(this.bufferIndexVertex.get(i) / 4L), 0, 0);
                }
            }
        } finally {
            this.bufferIndexVertex.clear();
            this.bufferCountVertex.clear();
        }
    }

    public void unbindBuffer() {
        GlStateManager._glBindBuffer(GlStateManager.GL_ARRAY_BUFFER, 0);
    }

    public static void unbindVertexArray() {
        GlStateManager._glBindVertexArray(0);
    }

    public void deleteGlBuffers() {
        this.pendingReleases.clear();
        MemoryUtil.memFree(this.bufferIndexVertex);
        MemoryUtil.memFree(this.bufferCountVertex);
        MemoryUtil.memFree(this.bufferDrawParameters);
        this.bufferIndexVertex = null;
        this.bufferCountVertex = null;
        this.bufferDrawParameters = null;
        if (this.vulkan) {
            if (this.gpuBuffer != null) {
                this.gpuBuffer.close();
                this.gpuBuffer = null;
                GpuMemory.bufferFreed(this.toBytes(this.capacity));
            }

            this.glBufferId = -1;
            return;
        }

        if (this.glArrayObjectId >= 0) {
            GlStateManager.deleteVertexArrays(this.glArrayObjectId);
            this.glArrayObjectId = -1;
        }

        if (this.glBufferId >= 0) {
            GlStateManager._glDeleteBuffers(this.glBufferId);
            GpuMemory.bufferFreed(this.toBytes(this.capacity));
            this.glBufferId = -1;
        }
    }

    private long toBytes(int vertex) {
        return (long)vertex * this.vertexBytes;
    }

    private int toVertex(long bytes) {
        return (int)(bytes / this.vertexBytes);
    }

    public int getPositionTop() {
        return this.positionTop;
    }

    @Override
    public String toString() {
        return this.chunkPos + ", " + this.layer;
    }
}
