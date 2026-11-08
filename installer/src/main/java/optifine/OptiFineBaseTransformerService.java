package optifine;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OptiFineBaseTransformerService {
    private static final Logger LOGGER = LogManager.getLogger();
    private static URL ofZipFileUrl;
    private static ZipFile ofZipFile;
    private static Path ofZipFilePath;

    public void init() throws IOException {
        ofZipFileUrl = OptiFineBaseTransformerService.class.getProtectionDomain().getCodeSource().getLocation();
        LOGGER.info("OptiFine ZIP file URL: " + ofZipFileUrl);
        URI uri = URI.create(ofZipFileUrl.getPath().replace("file:", ""));
        File file = toFile(uri);
        if (file.getPath().endsWith("!")) {
            file = new File(file.getPath().replace("!", ""));
        }

        ofZipFile = new ZipFile(file);
        LOGGER.info("OptiFine ZIP file: " + file);
        ofZipFilePath = file.toPath();
    }

    public static File toFile(URI uri) {
        if (!"union".equals(uri.getScheme())) {
            return new File(uri.getPath());
        }

        try {
            String path = uri.getPath();
            if (path.contains("#")) {
                path = path.substring(0, path.lastIndexOf("#"));
            }

            File file = new File(path);
            ofZipFileUrl = file.toURI().toURL();
            Map<String, String> map = new HashMap<>();
            map.put("create", "true");
            FileSystems.newFileSystem(URI.create("jar:" + ofZipFileUrl + "!/"), map);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ZipFile getOfZipFile() {
        return ofZipFile;
    }

    public static URL getOfZipFileUrl() {
        return ofZipFileUrl;
    }

    public static Path getOfZipFilePath() {
        return ofZipFilePath;
    }
}
