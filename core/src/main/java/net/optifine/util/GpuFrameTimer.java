package net.optifine.util;

import com.mojang.blaze3d.systems.GpuQueryPool;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.OptionalLong;
import net.optifine.Config;
import org.lwjgl.opengl.GL;

public class GpuFrameTimer {
    private static boolean timerQuerySupported = Config.isVulkan() || GL.getCapabilities().GL_ARB_timer_query;
    private static long lastTimeCpuNs = 0L;
    private static long frameTimeCpuNs = 0L;
    private static GpuFrameTimer.TimerQuery timerQuery;
    private static long frameTimeGpuNs = 0L;
    private static long lastTimeActiveMs = 0L;

    public static void startRender() {
        if (timerQuery != null && timerQuery.hasResult()) {
            long frameTimeNs = timerQuery.getResult();
            frameTimeGpuNs = (frameTimeGpuNs + frameTimeNs) / 2L;
            timerQuery = null;
        }

        if (System.currentTimeMillis() <= lastTimeActiveMs + 1000L) {
            long timeCpuNs = System.nanoTime();
            if (lastTimeCpuNs != 0L) {
                long frameTimeNs = timeCpuNs - lastTimeCpuNs;
                frameTimeCpuNs = (frameTimeCpuNs + frameTimeNs) / 2L;
            }

            lastTimeCpuNs = timeCpuNs;
            if (timerQuery == null && timerQuerySupported) {
                timerQuery = new GpuFrameTimer.TimerQuery();
                timerQuery.start();
            }
        }
    }

    public static void finishRender() {
        if (timerQuery != null) {
            timerQuery.finish();
        }
    }

    public static double getGpuLoad() {
        lastTimeActiveMs = System.currentTimeMillis();
        return Math.max(frameTimeGpuNs, 0L) / Math.max(frameTimeCpuNs, 1.0);
    }

    private static class TimerQuery {
        private GpuQueryPool queries = RenderSystem.getDevice().createTimestampQueryPool(2);
        private boolean[] executed = new boolean[2];
        private long result = -1L;

        public void start() {
            if (!this.executed[0]) {
                RenderSystem.getDevice().createCommandEncoder().writeTimestamp(this.queries, 0);
                this.executed[0] = true;
            }
        }

        public void finish() {
            if (!this.executed[1]) {
                RenderSystem.getDevice().createCommandEncoder().writeTimestamp(this.queries, 1);
                this.executed[1] = true;
            }
        }

        public boolean hasResult() {
            return this.executed[1] && this.queries.getValue(1).isPresent();
        }

        public long getResult() {
            if (this.queries != null) {
                OptionalLong[] values = this.queries.getValues(0, 2);
                if (values[0].isEmpty() || values[1].isEmpty()) {
                    throw new IllegalStateException("GPU frame timestamps are not ready");
                }
                this.result = Math.round((values[1].getAsLong() - values[0].getAsLong())
                    * (double)RenderSystem.getDevice().getDeviceInfo().timestampPeriod());
                this.queries.close();
                this.queries = null;
            }
            return this.result;
        }
    }
}
