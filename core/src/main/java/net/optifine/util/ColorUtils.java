package net.optifine.util;

import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class ColorUtils {
    public static float getRedFloat(int col) {
        return from8BitChannel(ARGB.red(col));
    }

    public static float getGreenFloat(int col) {
        return from8BitChannel(ARGB.green(col));
    }

    public static float getBlueFloat(int col) {
        return from8BitChannel(ARGB.blue(col));
    }

    public static float from8BitChannel(int val) {
        return val / 255.0F;
    }

    public static Vec3 argbToVec3(int col) {
        float r = getRedFloat(col);
        float g = getGreenFloat(col);
        float b = getBlueFloat(col);
        return new Vec3(r, g, b);
    }

    public static void argbToVec3(int col, Vec3 buf) {
        buf.x = getRedFloat(col);
        buf.y = getGreenFloat(col);
        buf.z = getBlueFloat(col);
    }

    public static Vector3f argbToVec3f(int col) {
        float r = from8BitChannel(ARGB.red(col));
        float g = from8BitChannel(ARGB.green(col));
        float b = from8BitChannel(ARGB.blue(col));
        return new Vector3f(r, g, b);
    }

    public static int vec3ToArgb(Vec3 colVec) {
        return ARGB.colorFromFloat(1.0F, (float)colVec.x, (float)colVec.y, (float)colVec.z);
    }

    public static int vec3fToArgb(Vector3f colVec) {
        return ARGB.colorFromFloat(1.0F, colVec.x, colVec.y, colVec.z);
    }

    public static int combineAlphaRbg(int colAlpha, int colRbg) {
        return colAlpha & 0xFF000000 | colRbg & 16777215;
    }
}
