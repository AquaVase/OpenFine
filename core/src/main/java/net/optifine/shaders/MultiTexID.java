package net.optifine.shaders;

import com.mojang.blaze3d.textures.GpuTexture;

public class MultiTexID {
    public GpuTexture baseTex;
    public GpuTexture normTex;
    public GpuTexture specTex;
    public int base;
    public int norm;
    public int spec;

    public MultiTexID(GpuTexture baseTex, GpuTexture normTex, GpuTexture specTex) {
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
