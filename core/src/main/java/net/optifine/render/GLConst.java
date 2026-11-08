package net.optifine.render;

import com.mojang.blaze3d.platform.NativeImage;

public class GLConst {
    public static final int GL_TEXTURE_2D = 3553;
    public static final int GL_UNSIGNED_BYTE = 5121;
    public static final int GL_TEXTURE_MIN_FILTER = 10241;
    public static final int GL_TEXTURE_MAG_FILTER = 10240;
    public static final int GL_NEAREST = 9728;
    public static final int GL_LINEAR = 9729;
    public static final int GL_NEAREST_MIPMAP_NEAREST = 9984;
    public static final int GL_LINEAR_MIPMAP_NEAREST = 9985;
    public static final int GL_NEAREST_MIPMAP_LINEAR = 9986;
    public static final int GL_LINEAR_MIPMAP_LINEAR = 9987;
    public static final int GL_TEXTURE_MAX_ANISOTROPY_EXT = 34046;
    public static final int GL_FRAMEBUFFER = 36160;
    public static final int GL_RENDERBUFFER = 36161;
    public static final int GL_COLOR_ATTACHMENT0 = 36064;
    public static final int GL_DEPTH_ATTACHMENT = 36096;
    public static final int GL_FRAMEBUFFER_COMPLETE = 36053;
    public static final int GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT = 36054;
    public static final int GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = 36055;
    public static final int GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER = 36059;
    public static final int GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER = 36060;
    public static int GL_EXP = 2048;
    public static int GL_EXP2 = 2049;
    public static final int GL_TEXTURE_MIN_LOD = 33082;
    public static final int GL_TEXTURE_MAX_LOD = 33083;
    public static final int GL_TEXTURE_BASE_LEVEL = 33084;
    public static final int GL_TEXTURE_MAX_LEVEL = 33085;
    public static final int GL_TEXTURE_LOD_BIAS = 34049;
    public static final int GL_TEXTURE_WRAP_S = 10242;
    public static final int GL_TEXTURE_WRAP_T = 10243;
    public static final int GL_CLAMP_TO_EDGE = 33071;
    public static final int GL_REPEAT = 10497;
    public static final int GL_TEXTURE_BUFFER = 35882;
    public static final int GL_TEXTURE_BINDING_BUFFER = 35884;

    public static int toGl(NativeImage.Format format) {
        return switch (format) {
            case RGBA -> 6408;
            case RGB -> 6407;
            case LUMINANCE_ALPHA -> 33319;
            case LUMINANCE -> 6403;
        };
    }
}
