package net.optifine.util;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.platform.GLX;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryUtil;

public class GlUtil {
    public static ByteBuffer allocateMemory(int sizeIn) {
        return MemoryUtil.memAlloc(sizeIn);
    }

    public static void freeMemory(Buffer bufferIn) {
        MemoryUtil.memFree(bufferIn);
    }

    public static String getGlVendor() {
        return GlStateManager._getString(7936);
    }

    public static String getCpuInfo() {
        return GLX._getCpuInfo();
    }

    public static String getGlRenderer() {
        return GlStateManager._getString(7937);
    }

    public static String getGlVersion() {
        return GlStateManager._getString(7938);
    }
}
