package net.optifine.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.SectionMesh;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.optifine.Config;
import org.joml.Matrix4fc;

public class RegionLayerRenderer {
    private RegionRenderer[] renderers = makeRenderers();

    private static RegionRenderer[] makeRenderers() {
        RegionRenderer[] rs = new RegionRenderer[2];

        for (int i = 0; i < rs.length; i++) {
            rs[i] = new RegionRenderer(ChunkSectionLayer.VALUES[i]);
        }

        return rs;
    }

    public static boolean canRenderLayer(ChunkSectionLayer layerIn) {
        return layerIn == null ? false : layerIn.ordinal() < 2;
    }

    public void addSection(ChunkSectionLayer layerIn, int regionX, int regionZ, SectionMesh.SectionDraw buffersIn) {
        int layer = layerIn.ordinal();
        this.renderers[layer].addSection(regionX, regionZ, buffersIn);
    }

    public void finishPrepare(
        Matrix4fc viewIn,
        double xIn,
        double yIn,
        double zIn,
        List<DynamicUniforms.ChunkSectionInfo> transforms,
        EnumMap<ChunkSectionLayer, Int2ObjectOpenHashMap<List<RenderPass.Draw<GpuBufferSlice[]>>>> mapLayerDraws
    ) {
        TextureAtlas textureMap = Config.getTextureMapBlocks();
        int atlasWidth = textureMap.atlasWidth;
        int atlasHeight = textureMap.atlasHeight;

        for (int i = 0; i < this.renderers.length; i++) {
            this.renderers[i].finishPrepare(viewIn, xIn, yIn, zIn, transforms, mapLayerDraws, atlasWidth, atlasHeight);
        }
    }

    public void reset() {
        for (int i = 0; i < this.renderers.length; i++) {
            this.renderers[i].reset();
        }
    }

    public void clear() {
        for (int i = 0; i < this.renderers.length; i++) {
            this.renderers[i].clear();
        }
    }
}
