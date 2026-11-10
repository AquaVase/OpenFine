package optifine;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import optifine.xdelta.GDiffPatcher;
import optifine.xdelta.PatchException;

public class Patcher {
    public static final String CONFIG_FILE = "patch.cfg";
    public static final String CONFIG_FILE2 = "patch2.cfg";
    public static final String CONFIG_FILE3 = "patch3.cfg";
    public static final String PREFIX_PATCH = "patch/";
    public static final String SUFFIX_DELTA = ".xdelta";
    public static final String SUFFIX_MD5 = ".md5";

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            Utils.dbg("Usage: Patcher <base.jar> <diff.jar> <mod.jar>");
        } else {
            File baseFile = new File(args[0]);
            File diffFile = new File(args[1]);
            File modFile = new File(args[2]);
            if (baseFile.getName().equals("AUTO")) {
                baseFile = Differ.detectBaseFile(diffFile);
            }

            if (!baseFile.exists() || !baseFile.isFile()) {
                throw new IOException("Base file not found: " + baseFile);
            }

            if (diffFile.exists() && diffFile.isFile()) {
                process(baseFile, diffFile, modFile);
            } else {
                throw new IOException("Diff file not found: " + modFile);
            }
        }
    }

    public static void process(File baseFile, File diffFile, File modFile) throws Exception {
        try (ZipFile diffZip = new ZipFile(diffFile);
             ZipFile baseZip = new ZipFile(baseFile);
             ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(modFile)))) {
            PatchConfiguration configuration = new PatchConfiguration(getConfigurationMap(diffZip));
            ZipResourceProvider zrp = new ZipResourceProvider(baseZip);
            Enumeration<? extends ZipEntry> diffZipEntries = diffZip.entries();
            byte[] copyBuffer = new byte[8192];

            while (diffZipEntries.hasMoreElements()) {
                ZipEntry diffZipEntry = diffZipEntries.nextElement();
                String name = diffZipEntry.getName();
                if (name.startsWith(PREFIX_PATCH) && name.endsWith(SUFFIX_MD5)) {
                    continue;
                }
                if (name.startsWith(PREFIX_PATCH) && name.endsWith(SUFFIX_DELTA)) {
                    byte[] bytes = Utils.readAll(diffZip.getInputStream(diffZipEntry));
                    name = name.substring(PREFIX_PATCH.length(), name.length() - SUFFIX_DELTA.length());
                    byte[] bytesMod = applyPatch(name, bytes, configuration, zrp);
                    String nameMd5 = PREFIX_PATCH + name + SUFFIX_MD5;
                    ZipEntry diffZipEntryMd5 = diffZip.getEntry(nameMd5);
                    if (diffZipEntryMd5 != null) {
                        byte[] md5 = Utils.readAll(diffZip.getInputStream(diffZipEntryMd5));
                        String md5Str = new String(md5, "ASCII");
                        byte[] md5Mod = HashUtils.getHashMd5(bytesMod);
                        String md5ModStr = HashUtils.toHexString(md5Mod);
                        if (!md5Str.equals(md5ModStr)) {
                            throw new Exception("MD5 not matching, name: " + name + ", saved: " + md5Str + ", patched: " + md5ModStr);
                        }
                    }

                    zipOut.putNextEntry(new ZipEntry(name));
                    zipOut.write(bytesMod);
                    zipOut.closeEntry();
                    Utils.dbg("Mod: " + name);
                } else {
                    zipOut.putNextEntry(new ZipEntry(name));
                    try (InputStream in = diffZip.getInputStream(diffZipEntry)) {
                        int length;
                        while ((length = in.read(copyBuffer)) >= 0) {
                            zipOut.write(copyBuffer, 0, length);
                        }
                    }
                    zipOut.closeEntry();
                    Utils.dbg("Same: " + name);
                }
            }
        }
    }

    public static byte[] applyPatch(String name, byte[] bytesDiff, Pattern[] patterns, Map<String, String> cfgMap, IResourceProvider resourceProvider) throws IOException, PatchException {
        name = Utils.removePrefix(name, "/");
        String baseName = getPatchBase(name, patterns, cfgMap);
        if (baseName == null) {
            throw new IOException("No patch base, name: " + name + ", patterns: " + Utils.arrayToCommaSeparatedString(patterns));
        }

        return applyPatch(bytesDiff, baseName, resourceProvider);
    }

    public static byte[] applyPatch(String name, byte[] bytesDiff, PatchConfiguration configuration, IResourceProvider resourceProvider)
        throws IOException, PatchException {
        name = Utils.removePrefix(name, "/");
        String baseName = configuration.getPatchBase(name);
        if (baseName == null) {
            throw new IOException("No patch base, name: " + name);
        }
        return applyPatch(bytesDiff, baseName, resourceProvider);
    }

    private static byte[] applyPatch(byte[] bytesDiff, String baseName, IResourceProvider resourceProvider)
        throws IOException, PatchException {
        InputStream baseIn = resourceProvider.getResourceStream(baseName);
        if (baseIn == null) {
            throw new IOException("Base resource not found: " + baseName);
        }

        byte[] baseBytes = Utils.readAll(baseIn);
        InputStream patchStream = new ByteArrayInputStream(bytesDiff);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new GDiffPatcher(baseBytes, patchStream, outputStream);
        outputStream.close();
        return outputStream.toByteArray();
    }

    public static Pattern[] getConfigurationPatterns(Map<String, String> cfgMap) {
        String[] cfgKeys = cfgMap.keySet().toArray(new String[0]);
        Pattern[] patterns = new Pattern[cfgKeys.length];

        for (int i = 0; i < cfgKeys.length; i++) {
            String key = cfgKeys[i];
            patterns[i] = Pattern.compile(key);
        }

        return patterns;
    }

    public static Map<String, String> getConfigurationMap(ZipFile modZip) throws IOException {
        Map<String, String> cfgMap = getConfigurationMap(modZip, "patch.cfg");
        Map<String, String> cfgMap2 = getConfigurationMap(modZip, "patch2.cfg");
        Map<String, String> cfgMap3 = getConfigurationMap(modZip, "patch3.cfg");
        cfgMap.putAll(cfgMap2);
        cfgMap.putAll(cfgMap3);
        return cfgMap;
    }

    public static Map<String, String> getConfigurationMap(ZipFile modZip, String pathConfig) throws IOException {
        Map<String, String> cfgMap = new LinkedHashMap<>();
        if (modZip == null) {
            return cfgMap;
        }

        ZipEntry entryPatch = modZip.getEntry(pathConfig);
        if (entryPatch == null) {
            return cfgMap;
        }

        InputStream inPatch = modZip.getInputStream(entryPatch);
        String[] lines = Utils.readLines(inPatch, "ASCII");
        inPatch.close();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.startsWith("#") && line.length() > 0) {
                String[] parts = Utils.tokenize(line, "=");
                if (parts.length != 2) {
                    throw new IOException("Invalid patch configuration: " + line);
                }

                String key = parts[0].trim();
                String val = parts[1].trim();
                cfgMap.put(key, val);
            }
        }

        return cfgMap;
    }

    public static String getPatchBase(String name, Pattern[] patterns, Map<String, String> cfgMap) {
        name = Utils.removePrefix(name, "/");

        for (int i = 0; i < patterns.length; i++) {
            Pattern pattern = patterns[i];
            Matcher matcher = pattern.matcher(name);
            if (matcher.matches()) {
                return resolvePatchBase(name, cfgMap.get(pattern.pattern()), matcher);
            }
        }

        return null;
    }

    static String resolvePatchBase(String name, String base, Matcher matcher) {
        if (base != null && base.trim().equals("*")) {
            return name;
        }
        if (matcher != null) {
            for (int g = 1; g <= matcher.groupCount(); g++) {
                base = base.replace("$" + g, matcher.group(g));
            }
        }
        return base;
    }
}
