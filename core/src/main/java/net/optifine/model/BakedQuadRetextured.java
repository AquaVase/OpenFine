package net.optifine.model;

import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;

public class BakedQuadRetextured {
    public static BakedQuad make(BakedQuad quad, TextureAtlasSprite spriteIn) {
        return make(quad, spriteIn, quad.materialInfo().layer());
    }

    public static BakedQuad make(BakedQuad quad, TextureAtlasSprite spriteIn, ChunkSectionLayer layerIn) {
        long[] uvs = quad.getPackedUVs();

        for (int i = 0; i < uvs.length; i++) {
            uvs[i] = remapUv(quad.packedUV(i), quad.sprite(), spriteIn);
        }

        return quad.makeCopy(uvs, spriteIn, layerIn);
    }

    private static long remapUv(long pUv, TextureAtlasSprite sprite, TextureAtlasSprite spriteNew) {
        float u = UVPair.unpackU(pUv);
        float v = UVPair.unpackV(pUv);
        float u2 = spriteNew.getU16(sprite.getUnInterpolatedU16(u));
        float v2 = spriteNew.getV16(sprite.getUnInterpolatedV16(v));
        return UVPair.pack(u2, v2);
    }
}
