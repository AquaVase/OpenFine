package net.optifine.util;

import org.lwjgl.opengl.ARBTimerQuery;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL32C;

public class GpuFrameTimer {
    private static boolean timerQuerySupported = GL.getCapabilities().GL_ARB_timer_query;
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
        private int[] queries = new int[2];
        private boolean[] executed = new boolean[2];
        private long result = -1L;

        public TimerQuery() {
            GL32C.glGenQueries(this.queries);
        }

        public void start() {
            if (!this.executed[0]) {
                ARBTimerQuery.glQueryCounter(this.queries[0], 36392);
                this.executed[0] = true;
            }
        }

        public void finish() {
            if (!this.executed[1]) {
                ARBTimerQuery.glQueryCounter(this.queries[1], 36392);
                this.executed[1] = true;
            }
        }

        public boolean hasResult() {
            int res = GL32C.glGetQueryObjecti(this.queries[1], 34919);
            return res == 1;
        }

        public long getResult() {
            if (this.queries[1] > 0) {
                long timeNs0 = ARBTimerQuery.glGetQueryObjectui64(this.queries[0], 34918);
                long timeNs1 = ARBTimerQuery.glGetQueryObjectui64(this.queries[1], 34918);
                this.result = timeNs1 - timeNs0;
                GL32C.glDeleteQueries(this.queries);
                this.queries[0] = 0;
                this.queries[1] = 0;
            }

            return this.result;
        }
    }
}
