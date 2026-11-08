package net.optifine;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.CrashReport;
import net.minecraft.ReportType;
import net.minecraft.SystemReport;
import net.optifine.http.FileUploadThread;
import net.optifine.http.IFileUploadListener;
import net.optifine.shaders.Shaders;

public class CrashReporter {
    public static void onCrashReport(CrashReport crashReport, SystemReport category) {
        try {
            Throwable cause = crashReport.getException();
            if (cause == null) {
                return;
            }

            if (cause.getClass().getName().contains(".fml.client.SplashProgress")) {
                return;
            }

            if (cause.getClass() == Throwable.class) {
                return;
            }

            if (cause.getClass() == Exception.class && Config.equals(cause.getMessage(), "dummy")) {
                return;
            }

            extendCrashReport(category);
            if (!Config.isTelemetryOn()) {
                return;
            }

            String url = "http://optifine.net/crashReport";
            String reportStr = makeReport(crashReport);
            byte[] content = reportStr.getBytes("ASCII");
            IFileUploadListener listener = new IFileUploadListener() {
                @Override
                public void fileUploadFinished(String url, byte[] contentx, Throwable exception) {
                }
            };
            Map headers = new HashMap();
            headers.put("OF-Version", Config.getVersion());
            headers.put("OF-Summary", makeSummary(crashReport));
            FileUploadThread fut = new FileUploadThread(url, headers, content, listener);
            fut.setPriority(10);
            fut.start();
            Thread.sleep(1000L);
        } catch (Exception e) {
            Config.dbg(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private static String makeReport(CrashReport crashReport) {
        StringBuffer sb = new StringBuffer();
        sb.append("OptiFineVersion: " + Config.getVersion() + "\n");
        sb.append("Summary: " + makeSummary(crashReport) + "\n");
        sb.append("\n");
        sb.append(crashReport.getFriendlyReport(ReportType.CRASH));
        sb.append("\n");
        return sb.toString();
    }

    private static String makeSummary(CrashReport crashReport) {
        Throwable t = crashReport.getException();
        if (t == null) {
            return "Unknown";
        }

        StackTraceElement[] traces = t.getStackTrace();
        String firstTrace = "unknown";
        if (traces.length > 0) {
            firstTrace = traces[0].toString().trim();
        }

        return t.getClass().getName() + ": " + t.getMessage() + " (" + crashReport.getTitle() + ") [" + firstTrace + "]";
    }

    public static void extendCrashReport(SystemReport cat) {
        cat.setDetail("OptiFine Version", Config.getVersion());
        cat.setDetail("OptiFine Build", Config.getBuild());
        if (Config.getGameSettings() != null) {
            cat.setDetail("Render Distance Chunks", Config.getChunkViewDistance() + "");
            cat.setDetail("Mipmaps", Config.getMipmapLevels() + "");
            cat.setDetail("Anisotropic Filtering", Config.getAnisotropicFilterLevel() + "");
            cat.setDetail("Antialiasing", Config.getAntialiasingLevel() + "");
            cat.setDetail("Multitexture", Config.isMultiTexture() + "");
        }

        cat.setDetail("Shaders", Shaders.getShaderPackName() + "");
        cat.setDetail("OpenGlVersion", Config.openGlVersion + "");
        cat.setDetail("OpenGlRenderer", Config.openGlRenderer + "");
        cat.setDetail("OpenGlVendor", Config.openGlVendor + "");
        cat.setDetail("CpuCount", Config.getAvailableProcessors() + "");
    }
}
