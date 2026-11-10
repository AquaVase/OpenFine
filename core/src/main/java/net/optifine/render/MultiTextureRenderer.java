package net.optifine.render;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import java.nio.IntBuffer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.optifine.Config;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersTex;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;

public class MultiTextureRenderer {
    private static PointerBuffer bufferPositions;
    private static IntBuffer bufferCounts;
    private static IntBuffer bufferBaseVertices;
    private static boolean shaders;

    public static void draw(PrimitiveTopology drawMode, int baseVertexIn, int firstIndexIn, int indexTypeIn, MultiTextureData multiTextureData) {
        shaders = Config.isShaders();
        SpriteRenderData[] srds = multiTextureData.getSpriteRenderDatas();

        for (int i = 0; i < srds.length; i++) {
            SpriteRenderData srd = srds[i];
            draw(drawMode, baseVertexIn, firstIndexIn, indexTypeIn, srd);
        }
    }

    private static void draw(PrimitiveTopology drawMode, int baseVertexIn, int firstIndexIn, int indexTypeIn, SpriteRenderData srd) {
        TextureAtlasSprite sprite = srd.getSprite();
        int[] positions = srd.getPositions();
        int[] counts = srd.getCounts();
        sprite.bindSpriteTexture();
        if (shaders) {
            int normalTex = sprite.spriteNormal != null ? sprite.spriteNormal.getSpriteTextureId() : 0;
            int specularTex = sprite.spriteSpecular != null ? sprite.spriteSpecular.getSpriteTextureId() : 0;
            TextureAtlas at = sprite.getTextureAtlas();
            ShadersTex.bindNSTextures(normalTex, specularTex, at.isNormalBlend(), at.isSpecularBlend(), at.isMipmaps());
            if (Shaders.uniform_spriteBounds.isDefined()) {
                Shaders.uniform_spriteBounds.setValue(sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());
            }
        }

        prepareDrawParameters(drawMode, baseVertexIn, firstIndexIn, indexTypeIn, positions, counts);
        GlStateManager.glMultiDrawElementsBaseVertex(GlConst.toGl(drawMode), bufferCounts, indexTypeIn, bufferPositions, bufferBaseVertices);
    }

    private static void prepareDrawParameters(PrimitiveTopology drawMode, int baseVertexIn, int firstIndexIn, int indexTypeIn, int[] positions, int[] counts) {
        int drawCount = Math.min(positions.length, counts.length);
        if (bufferPositions == null || bufferPositions.capacity() < drawCount) {
            int size = Math.max(64, Mth.smallestEncompassingPowerOfTwo(drawCount));
            PointerBuffer newPositions = MemoryUtil.memAllocPointer(size);
            IntBuffer newCounts = null;
            IntBuffer newBaseVertices;
            try {
                newCounts = MemoryUtil.memAllocInt(size);
                newBaseVertices = MemoryUtil.memAllocInt(size);
            } catch (Throwable throwable) {
                MemoryUtil.memFree(newPositions);
                MemoryUtil.memFree(newCounts);
                throw throwable;
            }
            releaseBuffers();
            bufferPositions = newPositions;
            bufferCounts = newCounts;
            bufferBaseVertices = newBaseVertices;
        }

        bufferPositions.clear();
        bufferCounts.clear();
        bufferBaseVertices.clear();
        int indexSize = getIndexSize(indexTypeIn);

        for (int i = 0; i < drawCount; i++) {
            bufferPositions.put((firstIndexIn + drawMode.indexCount(positions[i])) * indexSize);
            bufferCounts.put(drawMode.indexCount(counts[i]));
            bufferBaseVertices.put(baseVertexIn);
        }

        bufferPositions.flip();
        bufferCounts.flip();
        bufferBaseVertices.flip();
    }

    public static void releaseBuffers() {
        MemoryUtil.memFree(bufferPositions);
        MemoryUtil.memFree(bufferCounts);
        MemoryUtil.memFree(bufferBaseVertices);
        bufferPositions = null;
        bufferCounts = null;
        bufferBaseVertices = null;
    }

    private static int getIndexSize(int indexTypeIn) {
        if (indexTypeIn == 5125) {
            return 4;
        } else {
            return indexTypeIn == 5123 ? 2 : 1;
        }
    }
}
