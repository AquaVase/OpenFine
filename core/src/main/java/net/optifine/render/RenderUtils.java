package net.optifine.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBuffers;
import net.optifine.Config;

public class RenderUtils {
    private static boolean flushRenderBuffers = true;
    private static long timeCheckMs = 0L;
    private static Minecraft mc = Minecraft.getInstance();

    public static boolean setFlushRenderBuffers(boolean flushRenderBuffers) {
        boolean prev = RenderUtils.flushRenderBuffers;
        RenderUtils.flushRenderBuffers = flushRenderBuffers;
        return prev;
    }

    public static boolean isFlushRenderBuffers() {
        return flushRenderBuffers;
    }

    public static void flushRenderBuffers() {
        if (flushRenderBuffers) {
            RenderBuffers rtb = mc.gameRenderer.renderBuffers();
        }
    }

    public static void finishRenderBuffers() {
    }

    public static boolean isMainFramebuffer(int framebufferIn) {
        return mc.gameRenderer.mainRenderTarget().hasFramebuffer(framebufferIn);
    }

    public static void frameStart() {
        if (System.currentTimeMillis() >= timeCheckMs) {
            timeCheckMs = System.currentTimeMillis() + 3000L;
            if (!flushRenderBuffers) {
                Config.warn("Flush render buffers is disabled!");
            }
        }
    }
}
