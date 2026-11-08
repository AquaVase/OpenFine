package net.optifine.render;

import com.mojang.blaze3d.IndexType;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.ScissorState;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class BufferUploader {
    private static final Vector3f VEC3_ZERO = new Vector3f();
    private static final Matrix4f MAT4_ZERO = new Matrix4f();
    private static GpuBuffer immediateDrawVertexBuffer;
    private static GpuBuffer immediateDrawIndexBuffer;

    public static void draw(RenderPipeline pipelineIn, MeshData dataIn, Supplier<String> labelIn) {
        draw(pipelineIn, dataIn, labelIn, (GpuTextureView)null, (GpuSampler)null);
    }

    public static void draw(RenderPipeline pipelineIn, MeshData dataIn, Supplier<String> labelIn, Identifier locationTextureIn, FilterMode filterIn) {
        AbstractTexture tex = Minecraft.getInstance().getTextureManager().getTexture(locationTextureIn);
        GpuTextureView texView = tex.getTextureView();
        draw(pipelineIn, dataIn, labelIn, texView, filterIn);
    }

    public static void draw(RenderPipeline pipelineIn, MeshData dataIn, Supplier<String> labelIn, GpuTextureView textureIn, FilterMode filterIn) {
        GpuSampler sampler = RenderSystem.getSamplerCache().getRepeat(filterIn);
        draw(pipelineIn, dataIn, labelIn, textureIn, sampler);
    }

    public static void draw(RenderPipeline pipelineIn, MeshData dataIn, Supplier<String> labelIn, GpuTextureView textureIn, GpuSampler samplerIn) {
        GpuBufferSlice gpubufferslice = RenderSystem.getDynamicUniforms()
            .writeTransform(RenderSystem.getModelViewMatrixCopy(), RenderSystem.getShaderColor(), VEC3_ZERO, MAT4_ZERO);
        MeshData meshdata = dataIn;

        try {
            GpuBuffer vertexGpuBuffer = uploadImmediateVertexBuffer(dataIn.vertexBuffer());
            GpuBuffer indexGpuBuffer;
            IndexType vertexformat$indextype;
            if (dataIn.indexBuffer() == null) {
                RenderSystem.AutoStorageIndexBuffer rendersystem$autostorageindexbuffer = RenderSystem.getSequentialBuffer(dataIn.drawState().primitiveTopology());
                indexGpuBuffer = rendersystem$autostorageindexbuffer.getBuffer(dataIn.drawState().indexCount());
                vertexformat$indextype = rendersystem$autostorageindexbuffer.type();
            } else {
                indexGpuBuffer = uploadImmediateIndexBuffer(dataIn.indexBuffer());
                vertexformat$indextype = dataIn.drawState().indexType();
            }

            RenderTarget rendertarget = Minecraft.getInstance().gameRenderer.mainRenderTarget();
            GpuTextureView gputextureview = RenderSystem.outputColorTextureOverride != null
                ? RenderSystem.outputColorTextureOverride
                : rendertarget.getColorTextureView();
            GpuTextureView gputextureview1 = rendertarget.useDepth
                ? (RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : rendertarget.getDepthTextureView())
                : null;

            try (RenderPass renderpass = RenderSystem.getDevice()
                    .createCommandEncoder()
                    .createRenderPass(labelIn, gputextureview, Optional.empty(), gputextureview1, OptionalDouble.empty())) {
                renderpass.setPipeline(pipelineIn);
                ScissorState scissorstate = RenderSystem.getScissorStateForRenderTypeDraws();
                if (scissorstate.enabled()) {
                    renderpass.enableScissor(scissorstate.x(), scissorstate.y(), scissorstate.width(), scissorstate.height());
                }

                RenderSystem.bindDefaultUniforms(renderpass);
                renderpass.setUniform("DynamicTransforms", gpubufferslice);
                renderpass.setVertexBuffer(0, vertexGpuBuffer.slice());
                if (textureIn != null && samplerIn != null) {
                    renderpass.bindTexture("Sampler0", textureIn, samplerIn);
                }

                renderpass.setIndexBuffer(indexGpuBuffer, vertexformat$indextype);
                renderpass.drawIndexed(dataIn.drawState().indexCount(), 1, 0, 0, 0);
            }
        } catch (Throwable throwable2) {
            if (dataIn != null) {
                try {
                    meshdata.close();
                } catch (Throwable throwable) {
                    throwable2.addSuppressed(throwable);
                }
            }

            throw throwable2;
        }

        if (dataIn != null) {
            dataIn.close();
        }
    }

    public static void draw(MeshData dataIn) {
        MeshData.DrawState drawState = dataIn.drawState();
        VertexFormat vertexFormat = drawState.format();
        int indexCount = drawState.indexCount();
        PrimitiveTopology drawMode = drawState.primitiveTopology();
        GlBuffer vertexBuffer = (GlBuffer)uploadImmediateVertexBuffer(dataIn.vertexBuffer());
        VertexFormat[] vertexFormats = new VertexFormat[]{vertexFormat};
        GpuBufferSlice[] vertexBuffers = new GpuBufferSlice[]{vertexBuffer.slice()};
        RenderSystem.getGlDevice().vertexArrayCache().bindVertexArray(vertexFormats, vertexBuffers, null);
        RenderSystem.AutoStorageIndexBuffer autoIndexBuffer = RenderSystem.getSequentialBuffer(drawMode);
        GlBuffer indexBuffer = (GlBuffer)autoIndexBuffer.getBuffer(indexCount);
        IndexType indexType = autoIndexBuffer.type();
        GlStateManager._glBindBuffer(34963, indexBuffer.getHandle());
        GlStateManager._drawElements(GlConst.toGl(drawMode), indexCount, GlConst.toGl(indexType), 0L);
        dataIn.close();
    }

    private static GpuBuffer uploadToBuffer(GpuBuffer target, ByteBuffer buffer, @GpuBuffer.Usage int usage, Supplier<String> label) {
        GpuDevice device = RenderSystem.getDevice();
        if (target == null) {
            target = device.createBuffer(label, usage, buffer);
        } else {
            CommandEncoder encoder = device.createCommandEncoder();
            if (target.size() < buffer.remaining()) {
                target.close();
                target = device.createBuffer(label, usage, buffer);
            } else {
                encoder.writeToBuffer(target.slice(), buffer);
            }
        }

        return target;
    }

    public static GpuBuffer uploadImmediateVertexBuffer(ByteBuffer buffer) {
        immediateDrawVertexBuffer = uploadToBuffer(immediateDrawVertexBuffer, buffer, 40, () -> "Immediate vertex buffer");
        return immediateDrawVertexBuffer;
    }

    public static GpuBuffer uploadImmediateIndexBuffer(ByteBuffer buffer) {
        immediateDrawIndexBuffer = uploadToBuffer(immediateDrawIndexBuffer, buffer, 72, () -> "Immediate index buffer");
        return immediateDrawIndexBuffer;
    }

    public static String getSamplerName(int indexIn) {
        switch (indexIn) {
            case 0:
                return "Sampler0";
            case 1:
                return "Sampler1";
            case 2:
                return "Sampler2";
            case 3:
                return "Sampler3";
            case 4:
                return "Sampler4";
            case 5:
                return "Sampler5";
            case 6:
                return "Sampler6";
            case 7:
                return "Sampler7";
            case 8:
                return "Sampler8";
            case 9:
                return "Sampler9";
            case 10:
                return "Sampler10";
            case 11:
                return "Sampler11";
            default:
                return "Sampler" + indexIn;
        }
    }
}
