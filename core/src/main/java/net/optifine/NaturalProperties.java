package net.optifine;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.util.Mth;

public class NaturalProperties {
    public int rotation = 1;
    public boolean flip = false;
    private Map[] quadMaps = new Map[8];

    public NaturalProperties(String type) {
        if (type.equals("4")) {
            this.rotation = 4;
        } else if (type.equals("2")) {
            this.rotation = 2;
        } else if (type.equals("F")) {
            this.flip = true;
        } else if (type.equals("4F")) {
            this.rotation = 4;
            this.flip = true;
        } else if (type.equals("2F")) {
            this.rotation = 2;
            this.flip = true;
        } else {
            Config.warn("NaturalTextures: Unknown type: " + type);
        }
    }

    public boolean isValid() {
        return this.rotation == 2 || this.rotation == 4 ? true : this.flip;
    }

    public synchronized BakedQuad getQuad(BakedQuad quadIn, int rotate, boolean flipU) {
        int index = rotate;
        if (flipU) {
            index |= 4;
        }

        if (index > 0 && index < this.quadMaps.length) {
            Map map = this.quadMaps[index];
            if (map == null) {
                map = new IdentityHashMap(1);
                this.quadMaps[index] = map;
            }

            BakedQuad quad = (BakedQuad)map.get(quadIn);
            if (quad == null) {
                quad = this.makeQuad(quadIn, rotate, flipU);
                map.put(quadIn, quad);
            }

            return quad;
        } else {
            return quadIn;
        }
    }

    private BakedQuad makeQuad(BakedQuad quad, int rotate, boolean flipU) {
        if (!this.isFullSprite(quad)) {
            rotate = 0;
        }

        long[] uvs = this.transformVertexData(quad.getPackedUVs(), rotate, flipU);
        return quad.makeCopy(uvs, quad.sprite());
    }

    private long[] transformVertexData(long[] uvs, int rotate, boolean flipU) {
        long[] uvs2 = (long[])uvs.clone();
        int v2 = 4 - rotate;
        if (flipU) {
            v2 += 3;
        }

        v2 %= 4;

        for (int v = 0; v < 4; v++) {
            uvs2[v2] = uvs[v];
            if (flipU) {
                if (--v2 < 0) {
                    v2 = 3;
                }
            } else if (++v2 > 3) {
                v2 = 0;
            }
        }

        return uvs2;
    }

    private boolean isFullSprite(BakedQuad quad) {
        TextureAtlasSprite sprite = quad.sprite();
        float uMin = sprite.getU0();
        float uMax = sprite.getU1();
        float uSize = uMax - uMin;
        float uDelta = uSize / 256.0F;
        float vMin = sprite.getV0();
        float vMax = sprite.getV1();
        float vSize = vMax - vMin;
        float vDelta = vSize / 256.0F;

        for (int i = 0; i < 4; i++) {
            float u = UVPair.unpackU(quad.packedUV(i));
            float v = UVPair.unpackV(quad.packedUV(i));
            if (!this.equalsDelta(u, uMin, uDelta) && !this.equalsDelta(u, uMax, uDelta)) {
                return false;
            }

            if (!this.equalsDelta(v, vMin, vDelta) && !this.equalsDelta(v, vMax, vDelta)) {
                return false;
            }
        }

        return true;
    }

    private boolean equalsDelta(float x1, float x2, float deltaMax) {
        float deltaAbs = Mth.abs(x1 - x2);
        return deltaAbs < deltaMax;
    }
}
