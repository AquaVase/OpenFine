package net.optifine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final boolean logDetail = System.getProperty("log.detail", "false").equals("true");

    public static void detail(String s) {
        if (logDetail) {
            LOGGER.info("[OptiFine] " + s);
        }
    }

    public static void dbg(String s) {
        LOGGER.info("[OptiFine] " + s);
    }

    public static void warn(String s) {
        LOGGER.warn("[OptiFine] " + s);
    }

    public static void warn(String s, Throwable t) {
        limitStackTrace(t);
        LOGGER.warn("[OptiFine] " + s, t);
    }

    public static void error(String s) {
        LOGGER.error("[OptiFine] " + s);
    }

    public static void error(String s, Throwable t) {
        limitStackTrace(t);
        LOGGER.error("[OptiFine] " + s, t);
    }

    public static void log(String s) {
        dbg(s);
    }

    private static void limitStackTrace(Throwable t) {
        StackTraceElement[] stes = t.getStackTrace();
        if (stes != null) {
            if (stes.length > 35) {
                List<StackTraceElement> listStes = new ArrayList<>(Arrays.asList(stes));
                List<StackTraceElement> listStes2 = new ArrayList<>();
                listStes2.addAll(listStes.subList(0, 30));
                listStes2.add(new StackTraceElement("..", "", null, 0));
                listStes2.addAll(listStes.subList(listStes.size() - 5, listStes.size()));
                StackTraceElement[] stes2 = listStes2.toArray(new StackTraceElement[listStes2.size()]);
                t.setStackTrace(stes2);
            }
        }
    }
}
