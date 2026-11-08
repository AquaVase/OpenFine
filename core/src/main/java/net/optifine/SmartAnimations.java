package net.optifine;

import java.util.BitSet;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.optifine.shaders.Shaders;

public class SmartAnimations {
    private static boolean active;
    private static BitSet spritesRendered = new BitSet();
    private static BitSet texturesRendered = new BitSet();

    public static boolean isActive() {
        return active && !Shaders.isShadowPass;
    }

    public static void update() {
        active = Config.getGameSettings().ofSmartAnimations;
    }

    public static void spriteRendered(TextureAtlasSprite sprite) {
        if (sprite.isTerrain()) {
            int animationIndex = sprite.getAnimationIndex();
            if (animationIndex >= 0) {
                spritesRendered.set(animationIndex);
            }
        }
    }

    public static void spritesRendered(BitSet animationIndexes) {
        if (animationIndexes != null) {
            spritesRendered.or(animationIndexes);
        }
    }

    public static boolean isSpriteRendered(TextureAtlasSprite sprite) {
        if (!sprite.isTerrain()) {
            return true;
        }

        int animationIndex = sprite.getAnimationIndex();
        return animationIndex < 0 ? false : spritesRendered.get(animationIndex);
    }

    public static void resetSpritesRendered(TextureAtlas atlasTexture) {
        if (atlasTexture.isTerrain()) {
            spritesRendered.clear();
        }
    }

    public static void textureRendered(int textureId) {
        if (textureId >= 0) {
            texturesRendered.set(textureId);
        }
    }

    public static boolean isTextureRendered(int texId) {
        return texId < 0 ? false : texturesRendered.get(texId);
    }

    public static void resetTexturesRendered() {
        texturesRendered.clear();
    }
}
