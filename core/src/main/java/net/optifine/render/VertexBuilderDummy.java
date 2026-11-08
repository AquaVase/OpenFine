package net.optifine.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;

public class VertexBuilderDummy implements VertexConsumer {
    private RenderTypeFeatureRenderer renderTypeBuffer = null;

    public VertexBuilderDummy(RenderTypeFeatureRenderer renderTypeBuffer) {
        this.renderTypeBuffer = renderTypeBuffer;
    }

    @Override
    public RenderTypeFeatureRenderer getRenderTypeBuffer() {
        return this.renderTypeBuffer;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        return this;
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        return this;
    }

    @Override
    public VertexConsumer setColor(int argb) {
        return null;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        return this;
    }

    @Override
    public VertexConsumer setLineWidth(float widthIn) {
        return this;
    }
}
