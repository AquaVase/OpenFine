package net.optifine.render;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.function.Supplier;

public class Tesselator {
    private static final int MAX_BYTES = 786432;
    private final ByteBufferBuilder buffer;
    private static Tesselator instance = new Tesselator();

    public static Tesselator getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Tesselator has not been initialized");
        } else {
            return instance;
        }
    }

    public Tesselator(int size) {
        this.buffer = new ByteBufferBuilder(size);
    }

    public Tesselator() {
        this(786432);
    }

    public BufferBuilder begin(PrimitiveTopology mode, VertexFormat format) {
        return new BufferBuilder(this.buffer, mode, format);
    }

    public void clear() {
        this.buffer.clear();
    }

    public void draw(RenderPipeline pipeline, BufferBuilder bufferIn, Supplier<String> labelIn) {
        BufferUploader.draw(pipeline, bufferIn.buildOrThrow(), labelIn);
    }
}
