package net.optifine.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier;
import net.minecraft.server.packs.resources.PreparableReloadListener.SharedState;
import net.minecraft.util.Util;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class FontUtils {
    public static Properties readFontProperties(Identifier locationFontTexture) {
        String fontFileName = locationFontTexture.getPath();
        Properties props = new PropertiesOrdered();
        String suffix = ".png";
        if (!fontFileName.endsWith(suffix)) {
            return props;
        }

        String fileName = fontFileName.substring(0, fontFileName.length() - suffix.length()) + ".properties";

        try {
            Identifier locProp = new Identifier(locationFontTexture.getNamespace(), fileName);
            InputStream in = Config.getResourceStream(Config.getResourceManager(), locProp);
            if (in == null) {
                return props;
            }

            Config.log("Loading " + fileName);
            props.load(in);
            in.close();
        } catch (FileNotFoundException var7) {
        } catch (IOException e) {
            e.printStackTrace();
        }

        return props;
    }

    public static Int2ObjectMap<Float> readCustomCharWidths(Properties props) {
        Int2ObjectMap<Float> map = new Int2ObjectOpenHashMap<>();

        for (String key : (java.lang.Iterable<String>)(java.lang.Iterable<?>)(props.keySet())) {
            String prefix = "width.";
            if (key.startsWith(prefix)) {
                String numStr = key.substring(prefix.length());
                int num = Config.parseInt(numStr, -1);
                if (num >= 0) {
                    String value = props.getProperty(key);
                    float width = Config.parseFloat(value, -1.0F);
                    if (width >= 0.0F) {
                        char ch = (char)num;
                        map.put(ch, new Float(width));
                    }
                }
            }
        }

        return map;
    }

    public static float readFloat(Properties props, String key, float defOffset) {
        String str = props.getProperty(key);
        if (str == null) {
            return defOffset;
        } else {
            float offset = Config.parseFloat(str, Float.MIN_VALUE);
            if (offset == Float.MIN_VALUE) {
                Config.warn("Invalid value for " + key + ": " + str);
                return defOffset;
            } else {
                return offset;
            }
        }
    }

    public static boolean readBoolean(Properties props, String key, boolean defVal) {
        String str = props.getProperty(key);
        if (str == null) {
            return defVal;
        } else {
            String strLow = str.toLowerCase().trim();
            if (strLow.equals("true") || strLow.equals("on")) {
                return true;
            } else if (!strLow.equals("false") && !strLow.equals("off")) {
                Config.warn("Invalid value for " + key + ": " + str);
                return defVal;
            } else {
                return false;
            }
        }
    }

    public static Identifier getHdFontLocation(Identifier fontLoc) {
        if (!Config.isCustomFonts()) {
            return fontLoc;
        }

        if (fontLoc == null) {
            return fontLoc;
        }

        if (!Config.isMinecraftThread()) {
            return fontLoc;
        }

        String fontName = fontLoc.getPath();
        String texturesStr = "textures/";
        String optifineStr = "optifine/";
        if (!fontName.startsWith(texturesStr)) {
            return fontLoc;
        }

        fontName = fontName.substring(texturesStr.length());
        fontName = optifineStr + fontName;
        Identifier fontLocHD = new Identifier(fontLoc.getNamespace(), fontName);
        return Config.hasResource(Config.getResourceManager(), fontLocHD) ? fontLocHD : fontLoc;
    }

    public static void reloadFonts() {
        PreparationBarrier barrier = new PreparationBarrier() {
            @Override
            public <T> CompletableFuture<T> wait(T x) {
                return CompletableFuture.completedFuture(x);
            }
        };
        Executor ex = Util.backgroundExecutor();
        Minecraft mc = Minecraft.getInstance();
        FontManager frm = (FontManager)Reflector.getFieldValue(mc, Reflector.Minecraft_fontResourceManager);
        if (frm != null) {
            SharedState state = new SharedState(Config.getResourceManager());
            frm.reload(state, ex, barrier, mc);
        }
    }
}
