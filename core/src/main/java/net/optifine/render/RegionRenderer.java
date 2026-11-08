package net.optifine.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.SectionMesh;
import net.optifine.util.PairInt;
import org.joml.Matrix4fc;

public class RegionRenderer {
    private ChunkSectionLayer layer;
    private Map<PairInt, Map<VboRegion, List<SectionMesh.SectionDraw>>> mapPosRegionBuffers = new LinkedHashMap<>(16);
    private int lastRegionX = Integer.MIN_VALUE;
    private int lastRegionZ = Integer.MIN_VALUE;
    private Map<VboRegion, List<SectionMesh.SectionDraw>> lastRegionBuffers;
    private VboRegion lastVboRegion;
    private List<SectionMesh.SectionDraw> lastBuffers;

    public RegionRenderer(ChunkSectionLayer layer) {
        this.layer = layer;
    }

    public void addSection(int regionX, int regionZ, SectionMesh.SectionDraw buffersIn) {
        if (regionX != this.lastRegionX || regionZ != this.lastRegionZ) {
            PairInt pos = PairInt.of(regionX, regionZ);
            this.lastRegionBuffers = this.mapPosRegionBuffers.computeIfAbsent(pos, k -> new LinkedHashMap<>(8));
            this.lastRegionX = regionX;
            this.lastRegionZ = regionZ;
            this.lastVboRegion = null;
        }

        VboRegion vboRegion = buffersIn.data().vboRegion;
        if (vboRegion != this.lastVboRegion) {
            this.lastBuffers = this.lastRegionBuffers.computeIfAbsent(vboRegion, k -> new ArrayList<>());
            this.lastVboRegion = vboRegion;
        }

        this.lastBuffers.add(buffersIn);
    }

    public void reset() {
        this.lastRegionX = Integer.MIN_VALUE;
        this.lastRegionZ = Integer.MIN_VALUE;
        this.lastRegionBuffers = null;
        this.lastVboRegion = null;
        this.lastBuffers = null;
    }

    public void finishPrepare(
        Matrix4fc viewIn,
        double xIn,
        double yIn,
        double zIn,
        List<DynamicUniforms.ChunkSectionInfo> sectionInfos,
        EnumMap<ChunkSectionLayer, Int2ObjectOpenHashMap<List<RenderPass.Draw<GpuBufferSlice[]>>>> mapLayerDraws,
        int atlasWidth,
        int atlasHeight
    ) {
        for (Entry<PairInt, Map<VboRegion, List<SectionMesh.SectionDraw>>> entryPos : this.mapPosRegionBuffers.entrySet()) {
            PairInt pos = entryPos.getKey();
            if (entryPos.getValue() != null) {
                DynamicUniforms.ChunkSectionInfo csi = new DynamicUniforms.ChunkSectionInfo(
                    viewIn, pos.getLeft(), 0, pos.getRight(), 1.0F, atlasWidth, atlasHeight, xIn, yIn, zIn
                );
                int uniformIndex = sectionInfos.size();
                sectionInfos.add(csi);
                RegionRenderData renderData = new RegionRenderData(this, pos);
                BiConsumer<GpuBufferSlice[], RenderPass.UniformUploader> uniformUploader = (buffers2In, uploader2In) -> uploader2In.upload(
                    "ChunkSection", buffers2In[uniformIndex]
                );
                List<RenderPass.Draw<GpuBufferSlice[]>> draws = mapLayerDraws.get(this.layer).computeIfAbsent(0, keyIn -> new ArrayList<>());
                RenderPass.Draw<GpuBufferSlice[]> draw = new RenderPass.Draw<>(0, null, null, null, 0, 0, 0, uniformUploader, renderData, null);
                draws.add(draw);
            }
        }
    }

    public void renderRegion(PairInt positionIn) {
        Map<VboRegion, List<SectionMesh.SectionDraw>> mapRegionBuffers = this.mapPosRegionBuffers.get(positionIn);
        if (this.mapPosRegionBuffers != null) {
            for (Entry<VboRegion, List<SectionMesh.SectionDraw>> entryRegionBuffers : mapRegionBuffers.entrySet()) {
                VboRegion region = entryRegionBuffers.getKey();
                List<SectionMesh.SectionDraw> listBuffers = entryRegionBuffers.getValue();
                if (!listBuffers.isEmpty()) {
                    for (SectionMesh.SectionDraw buffers : listBuffers) {
                        buffers.drawInRegion();
                    }

                    region.finishDraw();
                    listBuffers.clear();
                }
            }
        }
    }

    public void clear() {
        this.reset();
        this.mapPosRegionBuffers.clear();
    }

    @Override
    public String toString() {
        return this.layer + "";
    }
}
