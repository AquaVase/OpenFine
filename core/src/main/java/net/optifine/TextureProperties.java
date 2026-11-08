package net.optifine;

import java.util.Properties;
import net.optifine.config.ConfigUtils;
import net.optifine.shaders.ITextureFormat;
import net.optifine.shaders.TextureFormatLabPbr;

public class TextureProperties {
    private static ITextureFormat textureFormat;
    private static final int ALPHA_CUTOUT = 16;
    private static int alphaCutout = 16;

    public static void update() {
        textureFormat = null;
        alphaCutout = 16;

        try {
            Properties props = ConfigUtils.readProperties("optifine/texture.properties");
            if (props == null) {
                return;
            }

            if (Config.isShaders()) {
                textureFormat = parseTextureFormat(props.getProperty("format"));
            }

            alphaCutout = parseAlphaCutout(props.getProperty("alpha.cutout"));
        } catch (Exception e) {
            Config.warn("Error reading texture.properties", e);
        }
    }

    private static int parseAlphaCutout(String prop) {
        if (prop == null) {
            return 16;
        }

        int val = Config.parseInt(prop, 16);
        val = Config.limit(val, 1, 255);
        Config.log("Alpha cutout: " + val);
        return val;
    }

    private static ITextureFormat parseTextureFormat(String formatStr) {
        if (formatStr == null) {
            return null;
        }

        String[] parts = Config.tokenize(formatStr, "/");
        String name = parts[0];
        String ver = parts.length > 1 ? parts[1] : null;
        if (name.equals("lab-pbr")) {
            return new TextureFormatLabPbr(ver);
        }

        Config.warn("Unknown texture format: " + formatStr);
        return null;
    }

    public static ITextureFormat getTextureFormat() {
        return textureFormat;
    }

    public static int getAlphaCutout() {
        return alphaCutout;
    }
}
