package net.optifine.shaders;

import java.nio.FloatBuffer;
import java.util.Locale;
import net.optifine.Config;
import net.optifine.shaders.uniform.ShaderUniformBase;
import net.optifine.util.DebugUtils;

public class ShadersDebug {
    public static boolean enabled = false;

    public static void onBeginRender() {
        if (enabled) {
            Config.dbg("===== START =====");
        }
    }

    public static void onRenderStage(RenderStage stage) {
        if (enabled) {
            Config.dbg("=== Stage: " + stage + " ====");
        }
    }

    public static void onUseProgram(Program program) {
        if (enabled) {
            Config.dbg("=== Program " + program + " ===");
        }
    }

    public static void onUniform1f(ShaderUniformBase uniform, float v0) {
        if (enabled) {
            Config.dbg(String.format(Locale.ROOT, "%s: %.2f", uniform.getName(), v0));
        }
    }

    public static void onUniform2f(ShaderUniformBase uniform, float v0, float v1) {
        if (enabled) {
            Config.dbg(String.format(Locale.ROOT, "%s: %.2f, %.2f", uniform.getName(), v0, v1));
        }
    }

    public static void onUniform4f(ShaderUniformBase uniform, float v0, float v1, float v2, float v3) {
        if (enabled) {
            Config.dbg(String.format(Locale.ROOT, "%s: %.2f, %.2f, %.2f, %.2f", uniform.getName(), v0, v1, v2, v3));
        }
    }

    public static void onUniform3f(ShaderUniformBase uniform, float v0, float v1, float v2) {
        if (enabled) {
            Config.dbg(String.format(Locale.ROOT, "%s: %.2f, %.2f, %.2f", uniform.getName(), v0, v1, v2));
        }
    }

    public static void onUniform1i(ShaderUniformBase uniform, int v0) {
        if (enabled) {
            Config.dbg(String.format(Locale.ROOT, "%s: %d", uniform.getName(), v0));
        }
    }

    public static void onUniform2i(ShaderUniformBase uniform, int v0, int v1) {
        if (enabled) {
            Config.dbg(String.format(Locale.ROOT, "%s: %d, %d", uniform.getName(), v0, v1));
        }
    }

    public static void onUniform4i(ShaderUniformBase uniform, int v0, int v1, int v2, int v3) {
        if (enabled) {
            Config.dbg(String.format(Locale.ROOT, "%s: %d, %d, %d, %d", uniform.getName(), v0, v1, v2, v3));
        }
    }

    public static void onUniformM3(ShaderUniformBase uniform, boolean transpose, FloatBuffer matrix) {
        if (enabled) {
            float[] fs = new float[matrix.limit()];
            matrix.get(fs);
            matrix.clear();
            String str = DebugUtils.getMatrix3(fs, " |");
            Config.dbg(uniform.getName() + ", transpose: " + transpose);
            Config.dbg(str);
        }
    }

    public static void onUniformM4(ShaderUniformBase uniform, boolean transpose, FloatBuffer matrix) {
        if (enabled) {
            float[] buf = new float[matrix.limit()];
            matrix.get(buf);
            matrix.clear();
            String str = DebugUtils.getMatrix4(buf, " |");
            Config.dbg(uniform.getName() + ", transpose: " + transpose);
            Config.dbg(str);
        }
    }

    public static void onEndRender() {
        if (enabled) {
            Config.dbg("=== END ===");
        }
    }
}
