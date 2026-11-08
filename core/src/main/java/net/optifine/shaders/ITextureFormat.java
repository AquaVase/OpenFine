package net.optifine.shaders;

import net.optifine.texture.IColorBlender;

public interface ITextureFormat {
    IColorBlender getColorBlender(ShadersTextureType var1);

    boolean isTextureBlend(ShadersTextureType var1);

    String getMacroName();

    String getMacroVersion();
}
