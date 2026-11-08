package net.optifine.shaders;

public interface IShadersFramebuffer {
    int getGlFramebuffer();

    DrawBuffers getDrawBuffers();

    void setDrawBuffers(DrawBuffers var1);
}
