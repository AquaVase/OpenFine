package net.optifine.util;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;

public class FrameEvent {
    private static Map<String, Integer> mapEventFrames = new HashMap<>();

    public static boolean isActive(String name, int frameInterval) {
        synchronized (mapEventFrames) {
            GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
            if (gameRenderer == null) {
                return false;
            }

            int frameCount = gameRenderer.getFrameCount();
            if (frameCount <= 0) {
                return false;
            }

            Integer frameCountLastObj = mapEventFrames.get(name);
            if (frameCountLastObj == null) {
                frameCountLastObj = new Integer(frameCount);
                mapEventFrames.put(name, frameCountLastObj);
            }

            int frameCountLast = frameCountLastObj;
            if (frameCount > frameCountLast && frameCount < frameCountLast + frameInterval) {
                return false;
            }

            mapEventFrames.put(name, new Integer(frameCount));
            return true;
        }
    }
}
