package net.optifine.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.optifine.Config;
import net.optifine.util.IntArray;
import net.optifine.util.TextureUtils;

public class MultiTextureBuilder {
    private int vertexCount;
    private ChunkSectionLayer blockLayer;
    private TextureAtlasSprite[] quadSprites;
    private boolean reorderingAllowed;
    private boolean[] drawnIcons = new boolean[256];
    private List<SpriteRenderData> spriteRenderDatas = new ArrayList<>();
    private IntArray vertexPositions = new IntArray(16);
    private IntArray vertexCounts = new IntArray(16);

    public MultiTextureData build(int vertexCountIn, ChunkSectionLayer blockLayerIn, TextureAtlasSprite[] quadSpritesIn, int[] quadOrderingIn) {
        if (quadSpritesIn == null) {
            return null;
        }

        SpriteRenderData[] srds = this.buildRenderDatas(vertexCountIn, blockLayerIn, quadSpritesIn, quadOrderingIn);
        if (srds == null) {
            return null;
        }

        MultiTextureData mtd = new MultiTextureData(srds);
        if (this.blockLayer.translucent()) {
            int countQuads = vertexCountIn / 4;
            TextureAtlasSprite[] quadSpritesNew = Arrays.copyOfRange(quadSpritesIn, 0, countQuads);
            mtd.setResortParameters(vertexCountIn, blockLayerIn, quadSpritesNew);
        }

        return mtd;
    }

    public SpriteRenderData[] buildRenderDatas(int vertexCountIn, ChunkSectionLayer blockLayerIn, TextureAtlasSprite[] quadSpritesIn, int[] quadOrderingIn) {
        if (quadSpritesIn == null) {
            return null;
        }

        this.vertexCount = vertexCountIn;
        this.blockLayer = blockLayerIn;
        this.quadSprites = quadSpritesIn;
        this.reorderingAllowed = !this.blockLayer.translucent();
        int maxTextureIndex = Config.getTextureMapBlocks().getCountRegisteredSprites();
        if (this.drawnIcons.length <= maxTextureIndex) {
            this.drawnIcons = new boolean[maxTextureIndex + 1];
        }

        Arrays.fill(this.drawnIcons, false);
        this.spriteRenderDatas.clear();
        int texSwitch = 0;
        int grassOverlayIndex = -1;
        int countQuads = this.vertexCount / 4;

        for (int i = 0; i < countQuads; i++) {
            int is = quadOrderingIn != null ? quadOrderingIn[i] : i;
            TextureAtlasSprite icon = this.quadSprites[is];
            if (icon != null) {
                int iconIndex = icon.getIndexInMap();
                if (iconIndex >= this.drawnIcons.length) {
                    this.drawnIcons = Arrays.copyOf(this.drawnIcons, iconIndex + 1);
                }

                if (!this.drawnIcons[iconIndex]) {
                    if (icon == TextureUtils.iconGrassSideOverlay) {
                        if (grassOverlayIndex < 0) {
                            grassOverlayIndex = i;
                        }
                    } else {
                        i = this.drawForIcon(icon, i, quadOrderingIn) - 1;
                        texSwitch++;
                        if (this.reorderingAllowed) {
                            this.drawnIcons[iconIndex] = true;
                        }
                    }
                }
            }
        }

        if (grassOverlayIndex >= 0) {
            this.drawForIcon(TextureUtils.iconGrassSideOverlay, grassOverlayIndex, quadOrderingIn);
            texSwitch++;
        }

        return this.spriteRenderDatas.toArray(new SpriteRenderData[this.spriteRenderDatas.size()]);
    }

    private int drawForIcon(TextureAtlasSprite sprite, int startQuadPos, int[] quadOrderingIn) {
        this.vertexPositions.clear();
        this.vertexCounts.clear();
        int firstRegionEnd = -1;
        int lastPos = -1;
        int countQuads = this.vertexCount / 4;

        for (int i = startQuadPos; i < countQuads; i++) {
            int is = quadOrderingIn != null ? quadOrderingIn[i] : i;
            TextureAtlasSprite ts = this.quadSprites[is];
            if (ts == sprite) {
                if (lastPos < 0) {
                    lastPos = i;
                }
            } else if (lastPos >= 0) {
                this.draw(lastPos, i);
                if (!this.reorderingAllowed) {
                    this.spriteRenderDatas.add(new SpriteRenderData(sprite, this.vertexPositions.toIntArray(), this.vertexCounts.toIntArray()));
                    return i;
                }

                lastPos = -1;
                if (firstRegionEnd < 0) {
                    firstRegionEnd = i;
                }
            }
        }

        if (lastPos >= 0) {
            this.draw(lastPos, countQuads);
        }

        if (firstRegionEnd < 0) {
            firstRegionEnd = countQuads;
        }

        this.spriteRenderDatas.add(new SpriteRenderData(sprite, this.vertexPositions.toIntArray(), this.vertexCounts.toIntArray()));
        return firstRegionEnd;
    }

    private void draw(int startQuadVertex, int endQuadVertex) {
        int vxQuadCount = endQuadVertex - startQuadVertex;
        if (vxQuadCount > 0) {
            int startVertex = startQuadVertex * 4;
            int vxCount = vxQuadCount * 4;
            this.vertexPositions.put(startVertex);
            this.vertexCounts.put(vxCount);
        }
    }
}
