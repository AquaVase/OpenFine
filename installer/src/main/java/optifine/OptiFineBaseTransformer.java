package optifine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class OptiFineBaseTransformer implements IResourceProvider, IOptiFineResourceLocator {
   private static final Logger LOGGER = LogManager.getLogger();
   private ZipFile ofZipFile;
   private String forgeJarUrlStr;
   private Map<String, String> patchMap = null;
   private Pattern[] patterns = null;
   public static final String PREFIX_SRG = "srg/";
   public static final String SUFFIX_CLASS = ".class";
   public static final String PREFIX_PATCH_SRG = "patch/srg/";
   public static final String SUFFIX_CLASS_XDELTA = ".class.xdelta";
   public static final String PREFIX_OPTIFINE = "optifine/";

   public OptiFineBaseTransformer(ZipFile ofZipFile) {
      this.ofZipFile = ofZipFile;

      try {
         this.patchMap = Patcher.getConfigurationMap(ofZipFile);
         this.patterns = Patcher.getConfigurationPatterns(this.patchMap);
      } catch (IOException e) {
         e.printStackTrace();
      }

      try {
         Class cls = Class.forName("net.minecraft.client.Minecraft");
         URL forgeJarUrl = cls.getProtectionDomain().getCodeSource().getLocation();
         this.forgeJarUrlStr = forgeJarUrl.toString();
         LOGGER.info("Forge JAR URL: " + this.forgeJarUrlStr);
      } catch (Exception e) {
         LOGGER.info("Forge JAR not available");
      }
   }

   public ClassNode transform(ClassNode input, String className) {
      ClassNode classNode = input;
      String classPath = this.getSrgClassPath(className);
      byte[] bytes = this.getOptiFineResource(classPath);
      if (bytes != null) {
         InputStream in = new ByteArrayInputStream(bytes);
         ClassNode classNodeNew = this.loadClass(in);
         if (classNodeNew != null) {
            this.debugClass(classNodeNew);
            AccessFixer.fixMemberAccess(classNode, classNodeNew);
            classNode = classNodeNew;
         }
      }

      return classNode;
   }

   private void debugClass(ClassNode classNode) {
   }

   private ClassNode loadClass(InputStream in) {
      try {
         ClassReader cr = new ClassReader(in);
         ClassNode cn = new ClassNode(393216);
         cr.accept(cn, 0);
         return cn;
      } catch (IOException e) {
         e.printStackTrace();
         return null;
      }
   }

   protected String[] getResourceNames(String prefix, String suffix) {
      List<String> list = new ArrayList<>();
      Enumeration<? extends ZipEntry> entries = this.ofZipFile.entries();

      while (entries.hasMoreElements()) {
         ZipEntry zipEntry = entries.nextElement();
         String name = zipEntry.getName();
         if (name.startsWith(prefix) && name.endsWith(suffix)) {
            list.add(name);
         }
      }

      return list.toArray(new String[list.size()]);
   }

   private byte[] getOptiFineResource(String name) {
      try {
         InputStream in = this.getOptiFineResourceStream(name);
         if (in == null) {
            return null;
         }

         byte[] bytes = Utils.readAll(in);
         in.close();
         return bytes;
      } catch (IOException e) {
         e.printStackTrace();
         return null;
      }
   }

   @Override
   public synchronized InputStream getOptiFineResourceStream(String name) {
      name = Utils.removePrefix(name, "/");
      InputStream in = this.getOptiFineResourceStreamZip(name);
      if (in != null) {
         return in;
      }

      in = this.getOptiFineResourceStreamPatched(name);
      return in != null ? in : null;
   }

   @Override
   public InputStream getResourceStream(String path) {
      path = Utils.removePrefix(path, "/");

      try {
         Enumeration<URL> urlsEnum = ClassLoader.getSystemClassLoader().getResources(path);

         while (urlsEnum.hasMoreElements()) {
            URL url = urlsEnum.nextElement();
            if (this.forgeJarUrlStr == null || !url.getPath().startsWith(this.forgeJarUrlStr)) {
               return url.openStream();
            }
         }

         return null;
      } catch (IOException e) {
         return null;
      }
   }

   public synchronized InputStream getOptiFineResourceStreamZip(String name) {
      if (this.ofZipFile == null) {
         return null;
      }

      name = Utils.removePrefix(name, "/");
      ZipEntry ze = this.ofZipFile.getEntry(name);
      if (ze == null) {
         return null;
      }

      try {
         return this.ofZipFile.getInputStream(ze);
      } catch (IOException e) {
         e.printStackTrace();
         return null;
      }
   }

   public synchronized byte[] getOptiFineResourceZip(String name) {
      InputStream in = this.getOptiFineResourceStreamZip(name);
      if (in == null) {
         return null;
      }

      try {
         return Utils.readAll(in);
      } catch (IOException e) {
         return null;
      }
   }

   public synchronized InputStream getOptiFineResourceStreamPatched(String name) {
      byte[] bytes = this.getOptiFineResourcePatched(name);
      return bytes == null ? null : new ByteArrayInputStream(bytes);
   }

   public synchronized byte[] getOptiFineResourcePatched(String name) {
      if (this.patterns != null && this.patchMap != null) {
         name = Utils.removePrefix(name, "/");
         String patchName = "patch/" + name + ".xdelta";
         byte[] bytes = this.getOptiFineResourceZip(patchName);
         if (bytes == null) {
            return null;
         }

         try {
            byte[] bytesPatched = Patcher.applyPatch(name, bytes, this.patterns, this.patchMap, this);
            String fullMd5Name = "patch/" + name + ".md5";
            byte[] bytesMd5 = this.getOptiFineResourceZip(fullMd5Name);
            if (bytesMd5 != null) {
               String md5Str = new String(bytesMd5, "ASCII");
               byte[] md5Mod = HashUtils.getHashMd5(bytesPatched);
               String md5ModStr = HashUtils.toHexString(md5Mod);
               if (!md5Str.equals(md5ModStr)) {
                  throw new IOException("MD5 not matching, name: " + name + ", saved: " + md5Str + ", patched: " + md5ModStr);
               }
            }

            return bytesPatched;
         } catch (Exception e) {
            e.printStackTrace();
            return null;
         }
      } else {
         return null;
      }
   }

   public String getSrgClassPath(String className) {
      return "srg/" + className.replace('.', '/') + ".class";
   }
}
