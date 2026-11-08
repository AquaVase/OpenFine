package net.optifine.render;

import java.util.List;
import net.minecraft.client.renderer.rendertype.RenderSetup;

public class RenderStateManager {
    private static boolean cacheEnabled;

    public static void setupRenderStates(List renderStates) {
        if (cacheEnabled) {
            setupCached(renderStates);
        }
    }

    public static void clearRenderStates(List renderStates) {
        if (cacheEnabled) {
            clearCached(renderStates);
        }
    }

    private static void setupCached(List renderStates) {
    }

    private static void clearCached(List renderStates) {
    }

    private static void setupCached(RenderSetup state, int index) {
    }

    private static void clearCached(RenderSetup state, int index) {
    }

    public static void enableCache() {
        if (!cacheEnabled) {
            cacheEnabled = true;
        }
    }

    public static void flushCache() {
        if (cacheEnabled) {
            disableCache();
            enableCache();
        }
    }

    public static void disableCache() {
        if (cacheEnabled) {
            cacheEnabled = false;
        }
    }
}
