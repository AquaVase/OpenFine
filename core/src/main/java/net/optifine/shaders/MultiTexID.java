package net.optifine.shaders;

import com.mojang.blaze3d.opengl.GlTexture;

public class MultiTexID {
    public GlTexture baseTex;
    public GlTexture normTex;
    public GlTexture specTex;
    public int base;
    public int norm;
    public int spec;

    public MultiTexID(GlTexture baseTex, GlTexture normTex, GlTexture specTex) {
        this.baseTex = baseTex;
        this.normTex = normTex;
        this.specTex = specTex;
        this.base = baseTex.getGlTextureId();
        this.norm = normTex.getGlTextureId();
        this.spec = specTex.getGlTextureId();
    }

    @Override
    public String toString() {
        return "base: " + this.base + ", norm: " + this.norm + ", spec: " + this.spec;
    }
}
