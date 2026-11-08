package net.optifine.util;

import java.util.ArrayList;
import java.util.List;

public class MemoryMonitor {
    private static long startTimeMs = System.currentTimeMillis();
    private static long startMemory = getMemoryUsed();
    private static long lastTimeMs = startTimeMs;
    private static long lastMemory = startMemory;
    private static boolean gcEvent = false;
    private static long memBytesSec = 0L;
    private static long memBytesSecAvg = 0L;
    private static List<Long> listMemBytesSec = new ArrayList<>();
    private static long gcBytesSec = 0L;
    private static long MB = 1048576L;

    public static void update() {
        long timeMs = System.currentTimeMillis();
        long memory = getMemoryUsed();
        gcEvent = memory < lastMemory;
        if (gcEvent) {
            gcBytesSec = memBytesSec;
            startTimeMs = timeMs;
            startMemory = memory;
        }

        long timeDiffMs = timeMs - startTimeMs;
        long memoryDiff = memory - startMemory;
        double timeDiffSec = timeDiffMs / 1000.0;
        if (memoryDiff >= 0L && timeDiffSec > 0.0) {
            memBytesSec = (long)(memoryDiff / timeDiffSec);
            listMemBytesSec.add(memBytesSec);
            if (timeMs / 1000L != lastTimeMs / 1000L) {
                long sumBytes = 0L;

                for (Long bytes : listMemBytesSec) {
                    sumBytes += bytes;
                }

                memBytesSecAvg = sumBytes / listMemBytesSec.size();
                listMemBytesSec.clear();
            }
        }

        lastTimeMs = timeMs;
        lastMemory = memory;
    }

    private static long getMemoryUsed() {
        Runtime r = Runtime.getRuntime();
        return r.totalMemory() - r.freeMemory();
    }

    public static long getStartTimeMs() {
        return startTimeMs;
    }

    public static long getStartMemoryMb() {
        return startMemory / MB;
    }

    public static boolean isGcEvent() {
        return gcEvent;
    }

    public static long getAllocationRateMb() {
        return memBytesSec / MB;
    }

    public static long getAllocationRateAvgMb() {
        return memBytesSecAvg / MB;
    }

    public static long getGcRateMb() {
        return gcBytesSec / MB;
    }
}
