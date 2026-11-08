package net.optifine.shaders.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.Vec3i;
import net.optifine.Config;

public class ShaderParser {
    public static Pattern PATTERN_UNIFORM = Pattern.compile("[\\w\\s(,=)]*uniform\\s+\\w+\\s+(\\w+).*");
    public static Pattern PATTERN_ATTRIBUTE = Pattern.compile("\\s*attribute\\s+\\w+\\s+(\\w+).*");
    public static Pattern PATTERN_IN = Pattern.compile("\\s*in\\s+\\w+\\s+(\\w+).*");
    public static Pattern PATTERN_CONST_INT = Pattern.compile("\\s*const\\s+int\\s+(\\w+)\\s*=\\s*([-+.\\w]+)\\s*;.*");
    public static Pattern PATTERN_CONST_IVEC3 = Pattern.compile("\\s*const\\s+ivec3\\s+(\\w+)\\s*=\\s*(.+)\\s*;.*");
    public static Pattern PATTERN_CONST_FLOAT = Pattern.compile("\\s*const\\s+float\\s+(\\w+)\\s*=\\s*([-+.\\w]+)\\s*;.*");
    public static Pattern PATTERN_CONST_VEC2 = Pattern.compile("\\s*const\\s+vec2\\s+(\\w+)\\s*=\\s*(.+)\\s*;.*");
    public static Pattern PATTERN_CONST_VEC4 = Pattern.compile("\\s*const\\s+vec4\\s+(\\w+)\\s*=\\s*(.+)\\s*;.*");
    public static Pattern PATTERN_CONST_BOOL = Pattern.compile("\\s*const\\s+bool\\s+(\\w+)\\s*=\\s*(\\w+)\\s*;.*");
    public static Pattern PATTERN_PROPERTY = Pattern.compile("\\s*(/\\*|//)?\\s*([A-Z]+):\\s*([\\w.,]+)\\s*(\\*/.*|\\s*)");
    public static Pattern PATTERN_EXTENSION = Pattern.compile("\\s*#\\s*extension\\s+(\\w+)\\s*:\\s*(\\w+).*");
    public static Pattern PATTERN_LAYOUT = Pattern.compile("\\s*layout\\s*\\((.*)\\)\\s*(\\w+).*");
    public static Pattern PATTERN_DRAW_BUFFERS = Pattern.compile("[0-9N]+");
    public static Pattern PATTERN_RENDER_TARGETS = Pattern.compile("[0-9N,]+");

    public static ShaderLine parseLine(String line, ShaderType shaderType) {
        Matcher mu = PATTERN_UNIFORM.matcher(line);
        if (mu.matches()) {
            return new ShaderLine(ShaderLine.Type.UNIFORM, mu.group(1), "", line);
        }

        Matcher ma = PATTERN_ATTRIBUTE.matcher(line);
        if (ma.matches()) {
            return new ShaderLine(ShaderLine.Type.ATTRIBUTE, ma.group(1), "", line);
        }

        if (shaderType == ShaderType.VERTEX) {
            Matcher mi = PATTERN_IN.matcher(line);
            if (mi.matches()) {
                return new ShaderLine(ShaderLine.Type.ATTRIBUTE, mi.group(1), "", line);
            }
        }

        Matcher mci = PATTERN_CONST_INT.matcher(line);
        if (mci.matches()) {
            return new ShaderLine(ShaderLine.Type.CONST_INT, mci.group(1), mci.group(2), line);
        }

        Matcher mciv3 = PATTERN_CONST_IVEC3.matcher(line);
        if (mciv3.matches()) {
            return new ShaderLine(ShaderLine.Type.CONST_IVEC3, mciv3.group(1), mciv3.group(2), line);
        }

        Matcher mcf = PATTERN_CONST_FLOAT.matcher(line);
        if (mcf.matches()) {
            return new ShaderLine(ShaderLine.Type.CONST_FLOAT, mcf.group(1), mcf.group(2), line);
        }

        Matcher mcv2 = PATTERN_CONST_VEC2.matcher(line);
        if (mcv2.matches()) {
            return new ShaderLine(ShaderLine.Type.CONST_VEC2, mcv2.group(1), mcv2.group(2), line);
        }

        Matcher mcv4 = PATTERN_CONST_VEC4.matcher(line);
        if (mcv4.matches()) {
            return new ShaderLine(ShaderLine.Type.CONST_VEC4, mcv4.group(1), mcv4.group(2), line);
        }

        Matcher mcb = PATTERN_CONST_BOOL.matcher(line);
        if (mcb.matches()) {
            return new ShaderLine(ShaderLine.Type.CONST_BOOL, mcb.group(1), mcb.group(2), line);
        }

        Matcher mc = PATTERN_PROPERTY.matcher(line);
        if (mc.matches()) {
            return new ShaderLine(ShaderLine.Type.PROPERTY, mc.group(2), mc.group(3), line);
        }

        Matcher mce = PATTERN_EXTENSION.matcher(line);
        if (mce.matches()) {
            return new ShaderLine(ShaderLine.Type.EXTENSION, mce.group(1), mce.group(2), line);
        }

        Matcher ml = PATTERN_LAYOUT.matcher(line);
        return ml.matches() ? new ShaderLine(ShaderLine.Type.LAYOUT, ml.group(2), ml.group(1), line) : null;
    }

    public static int getIndex(String uniform, String prefix, int minIndex, int maxIndex) {
        if (!uniform.startsWith(prefix)) {
            return -1;
        }

        String suffix = uniform.substring(prefix.length());
        int index = Config.parseInt(suffix, -1);
        return index >= minIndex && index <= maxIndex ? index : -1;
    }

    public static int getShadowDepthIndex(String uniform) {
        switch (uniform) {
            case "shadow":
                return 0;
            case "watershadow":
                return 1;
            default:
                return getIndex(uniform, "shadowtex", 0, 1);
        }
    }

    public static int getShadowColorIndex(String uniform) {
        switch (uniform) {
            case "shadowcolor":
                return 0;
            default:
                return getIndex(uniform, "shadowcolor", 0, 1);
        }
    }

    public static int getShadowColorImageIndex(String uniform) {
        return getIndex(uniform, "shadowcolorimg", 0, 1);
    }

    public static int getDepthIndex(String uniform) {
        return getIndex(uniform, "depthtex", 0, 2);
    }

    public static int getColorIndex(String uniform) {
        int gauxIndex = getIndex(uniform, "gaux", 1, 4);
        return gauxIndex > 0 ? gauxIndex + 3 : getIndex(uniform, "colortex", 0, 15);
    }

    public static int getColorImageIndex(String uniform) {
        return getIndex(uniform, "colorimg", 0, 15);
    }

    public static String[] parseDrawBuffers(String str) {
        if (!PATTERN_DRAW_BUFFERS.matcher(str).matches()) {
            return null;
        }

        str = str.trim();
        String[] strs = new String[str.length()];

        for (int i = 0; i < strs.length; i++) {
            strs[i] = String.valueOf(str.charAt(i));
        }

        return strs;
    }

    public static String[] parseRenderTargets(String str) {
        if (!PATTERN_RENDER_TARGETS.matcher(str).matches()) {
            return null;
        }

        str = str.trim();
        return Config.tokenize(str, ",");
    }

    public static Vec3i parseLocalSize(String value) {
        int x = 1;
        int y = 1;
        int z = 1;
        String[] parts = Config.tokenize(value, ",");

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            String[] tokens = Config.tokenize(part, "=");
            if (tokens.length == 2) {
                String name = tokens[0].trim();
                String valStr = tokens[1].trim();
                int val = Config.parseInt(valStr, -1);
                if (val < 1) {
                    return null;
                }

                if (name.equals("local_size_x")) {
                    x = val;
                }

                if (name.equals("local_size_y")) {
                    y = val;
                }

                if (name.equals("local_size_z")) {
                    z = val;
                }
            }
        }

        return x == 1 && y == 1 && z == 1 ? null : new Vec3i(x, y, z);
    }
}
