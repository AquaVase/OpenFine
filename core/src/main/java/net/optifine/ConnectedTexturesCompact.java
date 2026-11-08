package net.optifine;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.optifine.model.PositionUv;
import net.optifine.render.RenderEnv;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class ConnectedTexturesCompact {
    private static final int COMPACT_NONE = 0;
    private static final int COMPACT_ALL = 1;
    private static final int COMPACT_V = 2;
    private static final int COMPACT_H = 3;
    private static final int COMPACT_HV = 4;

    public static BakedQuad[] getConnectedTextureCtmCompact(int ctmIndex, ConnectedProperties cp, int side, BakedQuad quad, RenderEnv renderEnv) {
        if (cp.ctmTileIndexes != null && ctmIndex >= 0 && ctmIndex < cp.ctmTileIndexes.length) {
            int tileIndex = cp.ctmTileIndexes[ctmIndex];
            if (tileIndex >= 0 && tileIndex <= cp.tileIcons.length) {
                return getQuadsCompact(tileIndex, cp.tileIcons, quad, renderEnv);
            }
        }

        switch (ctmIndex) {
            case 1:
                return getQuadsCompactH(0, 3, cp.tileIcons, side, quad, renderEnv);
            case 2:
                return getQuadsCompact(3, cp.tileIcons, quad, renderEnv);
            case 3:
                return getQuadsCompactH(3, 0, cp.tileIcons, side, quad, renderEnv);
            case 4:
                return getQuadsCompact4(0, 3, 2, 4, cp.tileIcons, side, quad, renderEnv);
            case 5:
                return getQuadsCompact4(3, 0, 4, 2, cp.tileIcons, side, quad, renderEnv);
            case 6:
                return getQuadsCompact4(2, 4, 2, 4, cp.tileIcons, side, quad, renderEnv);
            case 7:
                return getQuadsCompact4(3, 3, 4, 4, cp.tileIcons, side, quad, renderEnv);
            case 8:
                return getQuadsCompact4(4, 1, 4, 4, cp.tileIcons, side, quad, renderEnv);
            case 9:
                return getQuadsCompact4(4, 4, 4, 1, cp.tileIcons, side, quad, renderEnv);
            case 10:
                return getQuadsCompact4(1, 4, 1, 4, cp.tileIcons, side, quad, renderEnv);
            case 11:
                return getQuadsCompact4(1, 1, 4, 4, cp.tileIcons, side, quad, renderEnv);
            case 12:
                return getQuadsCompactV(0, 2, cp.tileIcons, side, quad, renderEnv);
            case 13:
                return getQuadsCompact4(0, 3, 2, 1, cp.tileIcons, side, quad, renderEnv);
            case 14:
                return getQuadsCompactV(3, 1, cp.tileIcons, side, quad, renderEnv);
            case 15:
                return getQuadsCompact4(3, 0, 1, 2, cp.tileIcons, side, quad, renderEnv);
            case 16:
                return getQuadsCompact4(2, 4, 0, 3, cp.tileIcons, side, quad, renderEnv);
            case 17:
                return getQuadsCompact4(4, 2, 3, 0, cp.tileIcons, side, quad, renderEnv);
            case 18:
                return getQuadsCompact4(4, 4, 3, 3, cp.tileIcons, side, quad, renderEnv);
            case 19:
                return getQuadsCompact4(4, 2, 4, 2, cp.tileIcons, side, quad, renderEnv);
            case 20:
                return getQuadsCompact4(1, 4, 4, 4, cp.tileIcons, side, quad, renderEnv);
            case 21:
                return getQuadsCompact4(4, 4, 1, 4, cp.tileIcons, side, quad, renderEnv);
            case 22:
                return getQuadsCompact4(4, 4, 1, 1, cp.tileIcons, side, quad, renderEnv);
            case 23:
                return getQuadsCompact4(4, 1, 4, 1, cp.tileIcons, side, quad, renderEnv);
            case 24:
                return getQuadsCompact(2, cp.tileIcons, quad, renderEnv);
            case 25:
                return getQuadsCompactH(2, 1, cp.tileIcons, side, quad, renderEnv);
            case 26:
                return getQuadsCompact(1, cp.tileIcons, quad, renderEnv);
            case 27:
                return getQuadsCompactH(1, 2, cp.tileIcons, side, quad, renderEnv);
            case 28:
                return getQuadsCompact4(2, 4, 2, 1, cp.tileIcons, side, quad, renderEnv);
            case 29:
                return getQuadsCompact4(3, 3, 1, 4, cp.tileIcons, side, quad, renderEnv);
            case 30:
                return getQuadsCompact4(2, 1, 2, 4, cp.tileIcons, side, quad, renderEnv);
            case 31:
                return getQuadsCompact4(3, 3, 4, 1, cp.tileIcons, side, quad, renderEnv);
            case 32:
                return getQuadsCompact4(1, 1, 1, 4, cp.tileIcons, side, quad, renderEnv);
            case 33:
                return getQuadsCompact4(1, 1, 4, 1, cp.tileIcons, side, quad, renderEnv);
            case 34:
                return getQuadsCompact4(4, 1, 1, 4, cp.tileIcons, side, quad, renderEnv);
            case 35:
                return getQuadsCompact4(1, 4, 4, 1, cp.tileIcons, side, quad, renderEnv);
            case 36:
                return getQuadsCompactV(2, 0, cp.tileIcons, side, quad, renderEnv);
            case 37:
                return getQuadsCompact4(2, 1, 0, 3, cp.tileIcons, side, quad, renderEnv);
            case 38:
                return getQuadsCompactV(1, 3, cp.tileIcons, side, quad, renderEnv);
            case 39:
                return getQuadsCompact4(1, 2, 3, 0, cp.tileIcons, side, quad, renderEnv);
            case 40:
                return getQuadsCompact4(4, 1, 3, 3, cp.tileIcons, side, quad, renderEnv);
            case 41:
                return getQuadsCompact4(1, 2, 4, 2, cp.tileIcons, side, quad, renderEnv);
            case 42:
                return getQuadsCompact4(1, 4, 3, 3, cp.tileIcons, side, quad, renderEnv);
            case 43:
                return getQuadsCompact4(4, 2, 1, 2, cp.tileIcons, side, quad, renderEnv);
            case 44:
                return getQuadsCompact4(1, 4, 1, 1, cp.tileIcons, side, quad, renderEnv);
            case 45:
                return getQuadsCompact4(4, 1, 1, 1, cp.tileIcons, side, quad, renderEnv);
            case 46:
                return getQuadsCompact(4, cp.tileIcons, quad, renderEnv);
            default:
                return getQuadsCompact(0, cp.tileIcons, quad, renderEnv);
        }
    }

    private static BakedQuad[] getQuadsCompactH(int indexLeft, int indexRight, TextureAtlasSprite[] sprites, int side, BakedQuad quad, RenderEnv renderEnv) {
        return getQuadsCompact(ConnectedTexturesCompact.Dir.LEFT, indexLeft, ConnectedTexturesCompact.Dir.RIGHT, indexRight, sprites, side, quad, renderEnv);
    }

    private static BakedQuad[] getQuadsCompactV(int indexUp, int indexDown, TextureAtlasSprite[] sprites, int side, BakedQuad quad, RenderEnv renderEnv) {
        return getQuadsCompact(ConnectedTexturesCompact.Dir.UP, indexUp, ConnectedTexturesCompact.Dir.DOWN, indexDown, sprites, side, quad, renderEnv);
    }

    private static BakedQuad[] getQuadsCompact4(
        int upLeft, int upRight, int downLeft, int downRight, TextureAtlasSprite[] sprites, int side, BakedQuad quad, RenderEnv renderEnv
    ) {
        if (upLeft == upRight) {
            return downLeft == downRight
                ? getQuadsCompact(ConnectedTexturesCompact.Dir.UP, upLeft, ConnectedTexturesCompact.Dir.DOWN, downLeft, sprites, side, quad, renderEnv)
                : getQuadsCompact(
                    ConnectedTexturesCompact.Dir.UP,
                    upLeft,
                    ConnectedTexturesCompact.Dir.DOWN_LEFT,
                    downLeft,
                    ConnectedTexturesCompact.Dir.DOWN_RIGHT,
                    downRight,
                    sprites,
                    side,
                    quad,
                    renderEnv
                );
        } else if (downLeft == downRight) {
            return getQuadsCompact(
                ConnectedTexturesCompact.Dir.UP_LEFT,
                upLeft,
                ConnectedTexturesCompact.Dir.UP_RIGHT,
                upRight,
                ConnectedTexturesCompact.Dir.DOWN,
                downLeft,
                sprites,
                side,
                quad,
                renderEnv
            );
        } else if (upLeft == downLeft) {
            return upRight == downRight
                ? getQuadsCompact(ConnectedTexturesCompact.Dir.LEFT, upLeft, ConnectedTexturesCompact.Dir.RIGHT, upRight, sprites, side, quad, renderEnv)
                : getQuadsCompact(
                    ConnectedTexturesCompact.Dir.LEFT,
                    upLeft,
                    ConnectedTexturesCompact.Dir.UP_RIGHT,
                    upRight,
                    ConnectedTexturesCompact.Dir.DOWN_RIGHT,
                    downRight,
                    sprites,
                    side,
                    quad,
                    renderEnv
                );
        } else {
            return upRight == downRight
                ? getQuadsCompact(
                    ConnectedTexturesCompact.Dir.UP_LEFT,
                    upLeft,
                    ConnectedTexturesCompact.Dir.DOWN_LEFT,
                    downLeft,
                    ConnectedTexturesCompact.Dir.RIGHT,
                    upRight,
                    sprites,
                    side,
                    quad,
                    renderEnv
                )
                : getQuadsCompact(
                    ConnectedTexturesCompact.Dir.UP_LEFT,
                    upLeft,
                    ConnectedTexturesCompact.Dir.UP_RIGHT,
                    upRight,
                    ConnectedTexturesCompact.Dir.DOWN_LEFT,
                    downLeft,
                    ConnectedTexturesCompact.Dir.DOWN_RIGHT,
                    downRight,
                    sprites,
                    side,
                    quad,
                    renderEnv
                );
        }
    }

    private static BakedQuad[] getQuadsCompact(int index, TextureAtlasSprite[] sprites, BakedQuad quad, RenderEnv renderEnv) {
        TextureAtlasSprite sprite = sprites[index];
        return ConnectedTextures.getQuads(sprite, quad, renderEnv);
    }

    private static BakedQuad[] getQuadsCompact(
        ConnectedTexturesCompact.Dir dir1,
        int index1,
        ConnectedTexturesCompact.Dir dir2,
        int index2,
        TextureAtlasSprite[] sprites,
        int side,
        BakedQuad quad,
        RenderEnv renderEnv
    ) {
        BakedQuad quad1 = getQuadCompact(sprites[index1], dir1, side, quad, renderEnv);
        BakedQuad quad2 = getQuadCompact(sprites[index2], dir2, side, quad, renderEnv);
        return renderEnv.getArrayQuadsCtm(quad1, quad2);
    }

    private static BakedQuad[] getQuadsCompact(
        ConnectedTexturesCompact.Dir dir1,
        int index1,
        ConnectedTexturesCompact.Dir dir2,
        int index2,
        ConnectedTexturesCompact.Dir dir3,
        int index3,
        TextureAtlasSprite[] sprites,
        int side,
        BakedQuad quad,
        RenderEnv renderEnv
    ) {
        BakedQuad quad1 = getQuadCompact(sprites[index1], dir1, side, quad, renderEnv);
        BakedQuad quad2 = getQuadCompact(sprites[index2], dir2, side, quad, renderEnv);
        BakedQuad quad3 = getQuadCompact(sprites[index3], dir3, side, quad, renderEnv);
        return renderEnv.getArrayQuadsCtm(quad1, quad2, quad3);
    }

    private static BakedQuad[] getQuadsCompact(
        ConnectedTexturesCompact.Dir dir1,
        int index1,
        ConnectedTexturesCompact.Dir dir2,
        int index2,
        ConnectedTexturesCompact.Dir dir3,
        int index3,
        ConnectedTexturesCompact.Dir dir4,
        int index4,
        TextureAtlasSprite[] sprites,
        int side,
        BakedQuad quad,
        RenderEnv renderEnv
    ) {
        BakedQuad quad1 = getQuadCompact(sprites[index1], dir1, side, quad, renderEnv);
        BakedQuad quad2 = getQuadCompact(sprites[index2], dir2, side, quad, renderEnv);
        BakedQuad quad3 = getQuadCompact(sprites[index3], dir3, side, quad, renderEnv);
        BakedQuad quad4 = getQuadCompact(sprites[index4], dir4, side, quad, renderEnv);
        return renderEnv.getArrayQuadsCtm(quad1, quad2, quad3, quad4);
    }

    private static BakedQuad getQuadCompact(TextureAtlasSprite sprite, ConnectedTexturesCompact.Dir dir, int side, BakedQuad quad, RenderEnv renderEnv) {
        switch (dir) {
            case UP:
                return getQuadCompact(sprite, dir, 0, 0, 16, 8, side, quad, renderEnv);
            case UP_RIGHT:
                return getQuadCompact(sprite, dir, 8, 0, 16, 8, side, quad, renderEnv);
            case RIGHT:
                return getQuadCompact(sprite, dir, 8, 0, 16, 16, side, quad, renderEnv);
            case DOWN_RIGHT:
                return getQuadCompact(sprite, dir, 8, 8, 16, 16, side, quad, renderEnv);
            case DOWN:
                return getQuadCompact(sprite, dir, 0, 8, 16, 16, side, quad, renderEnv);
            case DOWN_LEFT:
                return getQuadCompact(sprite, dir, 0, 8, 8, 16, side, quad, renderEnv);
            case LEFT:
                return getQuadCompact(sprite, dir, 0, 0, 8, 16, side, quad, renderEnv);
            case UP_LEFT:
                return getQuadCompact(sprite, dir, 0, 0, 8, 8, side, quad, renderEnv);
            default:
                return quad;
        }
    }

    private static BakedQuad getQuadCompact(
        TextureAtlasSprite sprite, ConnectedTexturesCompact.Dir dir, int x1, int y1, int x2, int y2, int side, BakedQuad quadIn, RenderEnv renderEnv
    ) {
        Map[][] spriteQuadCompactMaps = ConnectedTextures.getSpriteQuadCompactMaps();
        if (spriteQuadCompactMaps == null) {
            return quadIn;
        }

        int spriteIndex = sprite.getIndexInMap();
        if (spriteIndex >= 0 && spriteIndex < spriteQuadCompactMaps.length) {
            Map[] quadMaps = spriteQuadCompactMaps[spriteIndex];
            if (quadMaps == null) {
                quadMaps = new Map[ConnectedTexturesCompact.Dir.VALUES.length];
                spriteQuadCompactMaps[spriteIndex] = quadMaps;
            }

            Map<BakedQuad, BakedQuad> quadMap = quadMaps[dir.ordinal()];
            if (quadMap == null) {
                quadMap = new IdentityHashMap<>(1);
                quadMaps[dir.ordinal()] = quadMap;
            }

            BakedQuad quad = quadMap.get(quadIn);
            if (quad == null) {
                quad = makeSpriteQuadCompact(quadIn, sprite, side, x1, y1, x2, y2);
                quadMap.put(quadIn, quad);
            }

            return quad;
        } else {
            return quadIn;
        }
    }

    private static BakedQuad makeSpriteQuadCompact(BakedQuad quad, TextureAtlasSprite sprite, int side, int x1, int y1, int x2, int y2) {
        TextureAtlasSprite spriteFrom = quad.sprite();
        PositionUv[] posUvs = new PositionUv[4];

        for (int i = 0; i < posUvs.length; i++) {
            posUvs[i] = fixVertexCompact(quad.position(i), quad.packedUV(i), spriteFrom, sprite, side, x1, y1, x2, y2);
        }

        return quad.makeCopy(posUvs, sprite);
    }

    private static PositionUv fixVertexCompact(
        Vector3fc pos, long pUV, TextureAtlasSprite spriteFrom, TextureAtlasSprite spriteTo, int side, int x1, int y1, int x2, int y2
    ) {
        float u = UVPair.unpackU(pUV);
        float v = UVPair.unpackV(pUV);
        double su16 = spriteFrom.getUnInterpolatedU16(u);
        double sv16 = spriteFrom.getUnInterpolatedV16(v);
        float x = pos.x();
        float y = pos.y();
        float z = pos.z();
        float cu;
        float cv;
        switch (side) {
            case 0:
                cu = x;
                cv = 1.0F - z;
                break;
            case 1:
                cu = x;
                cv = z;
                break;
            case 2:
                cu = 1.0F - x;
                cv = 1.0F - y;
                break;
            case 3:
                cu = x;
                cv = 1.0F - y;
                break;
            case 4:
                cu = z;
                cv = 1.0F - y;
                break;
            case 5:
                cu = 1.0F - z;
                cv = 1.0F - y;
                break;
            default:
                throw new IllegalArgumentException("Unknown side: " + side);
        }

        float k = 0.0F;
        float u16F = 16.0F * (1.0F - k);
        float v16F = 16.0F * (1.0F - k);
        if (su16 < x1) {
            cu = (float)(cu + (x1 - su16) / u16F);
            su16 = x1;
        }

        if (su16 > x2) {
            cu = (float)(cu - (su16 - x2) / u16F);
            su16 = x2;
        }

        if (sv16 < y1) {
            cv = (float)(cv + (y1 - sv16) / v16F);
            sv16 = y1;
        }

        if (sv16 > y2) {
            cv = (float)(cv - (sv16 - y2) / v16F);
            sv16 = y2;
        }

        switch (side) {
            case 0:
                x = cu;
                z = 1.0F - cv;
                break;
            case 1:
                x = cu;
                z = cv;
                break;
            case 2:
                x = 1.0F - cu;
                y = 1.0F - cv;
                break;
            case 3:
                x = cu;
                y = 1.0F - cv;
                break;
            case 4:
                z = cu;
                y = 1.0F - cv;
                break;
            case 5:
                z = 1.0F - cu;
                y = 1.0F - cv;
                break;
            default:
                throw new IllegalArgumentException("Unknown side: " + side);
        }

        float u2 = spriteTo.getU16(su16);
        float v2 = spriteTo.getV16(sv16);
        long pUv2 = UVPair.pack(u2, v2);
        Vector3f pos2 = new Vector3f(x, y, z);
        return new PositionUv(pos2, pUv2);
    }

    private enum Dir {
        UP,
        UP_RIGHT,
        RIGHT,
        DOWN_RIGHT,
        DOWN,
        DOWN_LEFT,
        LEFT,
        UP_LEFT;

        public static final ConnectedTexturesCompact.Dir[] VALUES = values();
    }
}
