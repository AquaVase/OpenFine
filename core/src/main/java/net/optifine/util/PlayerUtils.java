package net.optifine.util;

import net.minecraft.core.ClientAsset.Texture;
import net.minecraft.resources.Identifier;

public class PlayerUtils {
    public static Identifier getTexturePath(Texture tex) {
        return tex == null ? null : tex.texturePath();
    }
}
