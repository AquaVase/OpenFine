package net.optifine.shaders;

import com.mojang.blaze3d.opengl.GlStateManager;

public class GlState {
    private static IShadersFramebuffer activeFramebuffer;

    public static void bindFramebuffer(IShadersFramebuffer framebufferIn) {
        activeFramebuffer = framebufferIn;
        GlStateManager._glBindFramebuffer(36160, activeFramebuffer.getGlFramebuffer());
    }

    public static void unbindFramebuffer() {
        activeFramebuffer = null;
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    public static IShadersFramebuffer getFramebuffer() {
        return activeFramebuffer;
    }

    public static void setDrawBuffers(DrawBuffers drawBuffers) {
        activeFramebuffer.setDrawBuffers(drawBuffers);
    }

    public static DrawBuffers getDrawBuffers() {
        return activeFramebuffer.getDrawBuffers();
    }
}
