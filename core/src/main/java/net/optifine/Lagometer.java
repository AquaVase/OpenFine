package net.optifine;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Mth;
import net.optifine.reflect.Reflector;
import net.optifine.render.ProjectionMatrixBuffer;
import net.optifine.render.Tesselator;
import net.optifine.util.MathUtils;
import net.optifine.util.MemoryMonitor;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public class Lagometer {
    private static Minecraft mc;
    public static boolean active = false;
    public static Lagometer.TimerNano timerTick = new Lagometer.TimerNano();
    public static Lagometer.TimerNano timerScheduledExecutables = new Lagometer.TimerNano();
    public static Lagometer.TimerNano timerChunkUpload = new Lagometer.TimerNano();
    public static Lagometer.TimerNano timerChunkUpdate = new Lagometer.TimerNano();
    public static Lagometer.TimerNano timerVisibility = new Lagometer.TimerNano();
    public static Lagometer.TimerNano timerTerrain = new Lagometer.TimerNano();
    public static Lagometer.TimerNano timerServer = new Lagometer.TimerNano();
    private static long[] timesFrame = new long[512];
    private static long[] timesTick = new long[512];
    private static long[] timesScheduledExecutables = new long[512];
    private static long[] timesChunkUpload = new long[512];
    private static long[] timesChunkUpdate = new long[512];
    private static long[] timesVisibility = new long[512];
    private static long[] timesTerrain = new long[512];
    private static long[] timesServer = new long[512];
    private static boolean[] gcs = new boolean[512];
    private static int numRecordedFrameTimes = 0;
    private static long prevFrameTimeNano = -1L;
    private static long renderTimeNano = 0L;
    private static final ProjectionMatrixBuffer projectionMatrixBuffer = new ProjectionMatrixBuffer("lagometer");
    private static boolean renderLines;

    public static void updateLagometer() {
        if (mc == null) {
            mc = Minecraft.getInstance();
        }

        if (mc.debugEntries.isOverlayVisible() && mc.getDebugOverlay().isRenderFpsCharts()) {
            active = true;
            long timeNowNano = System.nanoTime();
            if (prevFrameTimeNano == -1L) {
                prevFrameTimeNano = timeNowNano;
            } else {
                int frameIndex = numRecordedFrameTimes & timesFrame.length - 1;
                numRecordedFrameTimes++;
                boolean gc = MemoryMonitor.isGcEvent();
                timesFrame[frameIndex] = timeNowNano - prevFrameTimeNano - renderTimeNano;
                timesTick[frameIndex] = timerTick.timeNano;
                timesScheduledExecutables[frameIndex] = timerScheduledExecutables.timeNano;
                timesChunkUpload[frameIndex] = timerChunkUpload.timeNano;
                timesChunkUpdate[frameIndex] = timerChunkUpdate.timeNano;
                timesVisibility[frameIndex] = timerVisibility.timeNano;
                timesTerrain[frameIndex] = timerTerrain.timeNano;
                timesServer[frameIndex] = timerServer.timeNano;
                gcs[frameIndex] = gc;
                timerTick.reset();
                timerScheduledExecutables.reset();
                timerVisibility.reset();
                timerChunkUpdate.reset();
                timerChunkUpload.reset();
                timerTerrain.reset();
                timerServer.reset();
                prevFrameTimeNano = System.nanoTime();
            }
        } else {
            active = false;
            prevFrameTimeNano = -1L;
        }
    }

    public static void renderLagometer(GuiGraphicsExtractor graphicsIn, int scaleFactor) {
        int displayWidth = mc.getWindow().getWidth();
        int displayHeight = mc.getWindow().getHeight();
        int y60 = displayHeight - 80;
        int y30 = displayHeight - 160;
        String str30 = Config.isShowFrameTime() ? "33" : "30";
        String str60 = Config.isShowFrameTime() ? "17" : "60";
        Matrix3x2fStack guiMatrixStack = graphicsIn.pose();
        guiMatrixStack.pushMatrix();
        guiMatrixStack.scale(1.0F / scaleFactor, 1.0F / scaleFactor);
        graphicsIn.text(mc.font, str30, 1, y30, -3881788, false);
        graphicsIn.text(mc.font, str60, 1, y60, -3881788, false);
        guiMatrixStack.popMatrix();
        float lumMem = 1.0F - (float)((System.currentTimeMillis() - MemoryMonitor.getStartTimeMs()) / 1000.0);
        lumMem = Config.limit(lumMem, 0.0F, 1.0F);
        int memColR = (int)Mth.lerp(lumMem, 180.0F, 255.0F);
        int memColG = (int)Mth.lerp(lumMem, 110.0F, 155.0F);
        int memColB = (int)Mth.lerp(lumMem, 15.0F, 20.0F);
        int colMem = memColR << 16 | memColG << 8 | memColB;
        int posX = 512 / scaleFactor + 2;
        int posY = displayHeight / scaleFactor - 8;
        graphicsIn.fill(posX - 1, posY - 1, posX + 50, posY + 10, -1605349296);
        graphicsIn.text(mc.font, " " + MemoryMonitor.getGcRateMb() + " MB/s", posX, posY, colMem);
        renderLines = true;
    }

    public static void renderLines(GuiRenderer rendererIn) {
        if (renderLines) {
            long timeRenderStartNano = System.nanoTime();
            GlStateManager._clear(256);
            RenderSystem.backupProjectionMatrix();
            int displayWidth = mc.getWindow().getWidth();
            int displayHeight = mc.getWindow().getHeight();
            float guiFarPlane = Reflector.ForgeHooksClient_getGuiFarPlane.exists() ? Reflector.ForgeHooksClient_getGuiFarPlane.callFloat() : 21000.0F;
            Matrix4f matrix4f = MathUtils.makeOrtho(0.0F, displayWidth, displayHeight, 0.0F, 1000.0F, guiFarPlane);
            RenderSystem.setProjectionMatrix(projectionMatrixBuffer.getBuffer(matrix4f), ProjectionType.ORTHOGRAPHIC);
            Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.pushMatrix();
            modelViewStack.translate(0.0F, 0.0F, -0.95F * guiFarPlane);
            GlStateManager.disableTexture();
            GlStateManager._depthMask(false);
            GlStateManager._disableCull();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            Tesselator tess = Tesselator.getInstance();
            BufferBuilder buffer = tess.begin(PrimitiveTopology.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);

            for (int frameNum = 0; frameNum < timesFrame.length; frameNum++) {
                int lum = (frameNum - numRecordedFrameTimes & timesFrame.length - 1) * 100 / timesFrame.length;
                lum += 155;
                float baseHeight = displayHeight;
                long heightFrame = 0L;
                if (gcs[frameNum]) {
                    heightFrame = renderTime(frameNum, timesFrame[frameNum], lum, lum / 2, 0, baseHeight, buffer);
                } else {
                    heightFrame = renderTime(frameNum, timesFrame[frameNum], lum, lum, lum, baseHeight, buffer);
                    baseHeight -= (float)renderTime(frameNum, timesServer[frameNum], lum / 2, lum / 2, lum / 2, baseHeight, buffer);
                    baseHeight -= (float)renderTime(frameNum, timesTerrain[frameNum], 0, lum, 0, baseHeight, buffer);
                    baseHeight -= (float)renderTime(frameNum, timesVisibility[frameNum], lum, lum, 0, baseHeight, buffer);
                    baseHeight -= (float)renderTime(frameNum, timesChunkUpdate[frameNum], lum, 0, 0, baseHeight, buffer);
                    baseHeight -= (float)renderTime(frameNum, timesChunkUpload[frameNum], lum, 0, lum, baseHeight, buffer);
                    baseHeight -= (float)renderTime(frameNum, timesScheduledExecutables[frameNum], 0, 0, lum, baseHeight, buffer);
                    baseHeight -= (float)renderTime(frameNum, timesTick[frameNum], 0, lum, lum, baseHeight, buffer);
                }
            }

            renderTimeDivider(0, timesFrame.length, 33333333L, 196, 196, 196, displayHeight, buffer);
            renderTimeDivider(0, timesFrame.length, 16666666L, 196, 196, 196, displayHeight, buffer);
            tess.draw(RenderPipelines.LINES, buffer, () -> "Lagometer");
            GlStateManager._enableCull();
            GlStateManager._depthMask(true);
            GlStateManager.enableTexture();
            modelViewStack.popMatrix();
            RenderSystem.restoreProjectionMatrix();
            renderTimeNano = System.nanoTime() - timeRenderStartNano;
            renderLines = false;
        }
    }

    private static long renderTime(int frameNum, long time, int r, int g, int b, float baseHeight, BufferBuilder buffer) {
        long heightTime = time / 200000L;
        if (heightTime < 3L) {
            return 0L;
        }

        buffer.addVertex(frameNum + 0.5F, baseHeight - (float)heightTime + 0.5F, 0.0F).setColor(r, g, b, 255).setNormal(0.0F, 1.0F, 0.0F).setLineWidth(1.0F);
        buffer.addVertex(frameNum + 0.5F, baseHeight + 0.5F, 0.0F).setColor(r, g, b, 255).setNormal(0.0F, 1.0F, 0.0F).setLineWidth(1.0F);
        return heightTime;
    }

    private static long renderTimeDivider(int frameStart, int frameEnd, long time, int r, int g, int b, float baseHeight, BufferBuilder buffer) {
        long heightTime = time / 200000L;
        if (heightTime < 3L) {
            return 0L;
        }

        buffer.addVertex(frameStart + 0.5F, baseHeight - (float)heightTime + 0.5F, 0.0F).setColor(r, g, b, 255).setNormal(1.0F, 0.0F, 0.0F).setLineWidth(1.0F);
        buffer.addVertex(frameEnd + 0.5F, baseHeight - (float)heightTime + 0.5F, 0.0F).setColor(r, g, b, 255).setNormal(1.0F, 0.0F, 0.0F).setLineWidth(1.0F);
        return heightTime;
    }

    public static boolean isActive() {
        return active;
    }

    public static class TimerNano {
        public long timeStartNano = 0L;
        public long timeNano = 0L;

        public void start() {
            if (Lagometer.active) {
                if (this.timeStartNano == 0L) {
                    this.timeStartNano = System.nanoTime();
                }
            }
        }

        public void end() {
            if (Lagometer.active) {
                if (this.timeStartNano != 0L) {
                    this.timeNano = this.timeNano + (System.nanoTime() - this.timeStartNano);
                    this.timeStartNano = 0L;
                }
            }
        }

        private void reset() {
            this.timeNano = 0L;
            this.timeStartNano = 0L;
        }
    }
}
