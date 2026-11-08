package net.optifine.util;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.FontTexture;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.optifine.Config;
import net.optifine.shaders.SimpleShaderTexture;

public class GpuMemory {
    private static long bufferAllocated = 0L;
    private static long textureAllocated = 0L;
    private static long textureAllocatedUpdateTime = 0L;

    public static synchronized void bufferAllocated(long size) {
        bufferAllocated += size;
    }

    public static synchronized void bufferFreed(long size) {
        bufferAllocated -= size;
    }

    public static long getBufferAllocated() {
        return bufferAllocated;
    }

    public static long getTextureAllocated() {
        if (System.currentTimeMillis() > textureAllocatedUpdateTime) {
            textureAllocated = calculateTextureAllocated();
            textureAllocatedUpdateTime = System.currentTimeMillis() + 1000L;
        }

        return textureAllocated;
    }

    private static long calculateTextureAllocated() {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        long sum = 0L;

        for (AbstractTexture texture : textureManager.getTextures()) {
            long size = getTextureSize(texture);
            if (Config.isShaders()) {
                size *= 3L;
            }

            sum += size;
        }

        return sum;
    }

    public static long getTextureSize(AbstractTexture texture) {
        if (texture instanceof DynamicTexture dt) {
            NativeImage img = dt.getPixels();
            if (img != null) {
                return img.getSize();
            }
        }

        if (texture instanceof FontTexture ft) {
            return 262144L;
        } else if (texture instanceof SimpleShaderTexture sst) {
            return sst.getSize();
        } else if (texture instanceof SimpleTexture st) {
            return st.size;
        } else {
            return texture instanceof TextureAtlas ta ? ta.getWidth() * ta.getHeight() * 4 : 0L;
        }
    }
}
