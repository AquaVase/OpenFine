package net.optifine.util;

public class PathUtils {
    public static boolean isRelative(String path) {
        return path == null ? false : path.startsWith("./");
    }

    public static String resolveRelative(String path, String basePath) {
        if (path != null && basePath != null) {
            if (path.startsWith("./")) {
                String relPath = path.substring(2);
                if (basePath.endsWith("/")) {
                    path = basePath + relPath;
                } else {
                    path = basePath + "/" + relPath;
                }
            }

            return path;
        } else {
            return path;
        }
    }

    public static String removeLast(String path) {
        if (path == null) {
            return path;
        }

        int pos = path.lastIndexOf(47);
        return pos >= 0 ? path.substring(0, pos) : path;
    }
}
