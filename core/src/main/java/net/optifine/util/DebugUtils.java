package net.optifine.util;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.SectionMesh;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.optifine.Config;
import org.joml.Matrix4f;
import org.joml.Runtime;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class DebugUtils {
    private static FloatBuffer floatBuffer16 = BufferUtils.createFloatBuffer(16);
    private static float[] floatArray16 = new float[16];
    private static String[] FRAME_UNIFORMS = new String[0];
    private static NumberFormat NUMBER_FORMAT = new DecimalFormat(" 0.00E0;-", new DecimalFormatSymbols(Locale.ROOT));
    private static Pattern PATTERN_EXP = Pattern.compile("E(\\d)");

    public static String getGlModelView() {
        floatBuffer16.clear();
        GL11.glGetFloatv(2982, floatBuffer16);
        floatBuffer16.get(floatArray16);
        float[] floatArray16T = transposeMat4(floatArray16);
        return getMatrix4(floatArray16T);
    }

    public static String getGlProjection() {
        floatBuffer16.clear();
        GL11.glGetFloatv(2983, floatBuffer16);
        floatBuffer16.get(floatArray16);
        float[] floatArray16T = transposeMat4(floatArray16);
        return getMatrix4(floatArray16T);
    }

    private static float[] transposeMat4(float[] arr) {
        float[] arrT = new float[16];

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                arrT[x * 4 + y] = arr[y * 4 + x];
            }
        }

        return arrT;
    }

    public static String getMatrix4(Matrix4f mat) {
        MathUtils.write(mat, floatArray16);
        return getMatrix4(floatArray16);
    }

    public static String getMatrix3(float[] fs) {
        return getMatrix3(fs, "\n");
    }

    public static String getMatrix3(float[] fs, String lineSep) {
        return getMatrix(fs, 3, lineSep);
    }

    public static String getMatrix4(FloatBuffer fb) {
        fb.clear();
        fb.get(floatArray16);
        fb.clear();
        return getMatrix4(floatArray16, "\n");
    }

    public static String getMatrix4(float[] fs) {
        return getMatrix4(fs, "\n");
    }

    public static String getMatrix4(float[] fs, String lineSep) {
        return getMatrix(fs, 4, lineSep);
    }

    public static String getMatrix(float[] fs, int cols, String lineSep) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < fs.length; i++) {
            String str = format(fs[i]);
            if (i > 0) {
                if (i % cols == 0) {
                    sb.append(lineSep);
                } else {
                    sb.append(" ");
                }
            }

            str = StrUtils.fillLeft(str, 5, ' ');
            sb.append(str);
        }

        return sb.toString();
    }

    private static String format(double val) {
        String str = Runtime.format(val, NUMBER_FORMAT);
        str = PATTERN_EXP.matcher(str).replaceAll("E+$1");
        return str.replace('E', 'e');
    }

    public static void debugVboMemory(SectionRenderDispatcher.RenderSection[] renderChunks) {
        if (TimedEvent.isActive("DbgVbos", 3000L)) {
            int sum = 0;
            int countChunks = 0;
            int countVbos = 0;
            int countLayers = 0;

            for (int i = 0; i < renderChunks.length; i++) {
                SectionRenderDispatcher.RenderSection renderChunk = renderChunks[i];
                SectionMesh sectionMesh = renderChunk.getSectionMesh();
                int sumPre = sum;

                for (ChunkSectionLayer rt : SectionRenderDispatcher.BLOCK_RENDER_LAYERS) {
                    if (renderChunk.getSectionMesh().isLayerUsed(rt)) {
                        countLayers++;
                    }
                }

                if (sum > sumPre) {
                    countChunks++;
                }
            }

            Config.dbg("VRAM: " + sum / 1048576 + " MB, vbos: " + countVbos + ", layers: " + countLayers + ", chunks: " + countChunks);
            Config.dbg("VBOs: " + GpuMemory.getBufferAllocated() / 1048576L + " MB");
        }
    }

    public static void debugTextures() {
        Config.dbg(" *** TEXTURES ***");
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        long sum = 0L;
        Collection<Identifier> locations = textureManager.getTextureLocations();
        List<Identifier> list = new ArrayList<>(locations);
        Collections.sort(list);

        for (Identifier loc : list) {
            AbstractTexture texture = textureManager.getTexture(loc);
            long size = GpuMemory.getTextureSize(texture);
            if (Config.isShaders()) {
                size *= 3L;
            }

            Config.dbg(loc + " = " + size);
            sum += size;
        }

        Config.dbg("All: " + sum);
    }

    public static String getClassName(Object obj) {
        return obj == null ? "null" : obj.getClass().getName();
    }

    public static void waitDebug(int sec) {
        System.out.print("Waiting: ");

        for (int i = 0; i < sec; i++) {
            System.out.print(sec - i + " ");
            Config.sleep(1000L);
        }
    }

    public static void waitRenderDoc() {
        System.out.print("Waiting for RenderDoc: ");

        for (int i = 1; i < 3600; i++) {
            HMODULE hModule = Kernel32.INSTANCE.GetModuleHandle("renderdoc.dll");
            if (hModule != null) {
                break;
            }

            if (i % 100 == 0) {
                System.out.println(".");
            } else {
                System.out.print(".");
            }

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException ignored) {
                break;
            }
        }

        System.out.println("");
    }
}
