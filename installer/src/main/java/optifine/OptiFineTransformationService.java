package optifine;

import cpw.mods.jarhandling.SecureJar;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import cpw.mods.modlauncher.api.IModuleLayerManager.Layer;
import cpw.mods.modlauncher.api.ITransformationService.Resource;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OptiFineTransformationService extends OptiFineBaseTransformerService implements ITransformationService {
   private static final Logger LOGGER = LogManager.getLogger();
   private static OptiFineTransformer transformer;

   public String name() {
      return "OptiFine";
   }

   public void initialize(IEnvironment environment) {
      LOGGER.info("OptiFineTransformationService.initialize");
   }

   public List<Resource> beginScanning(IEnvironment environment) {
      return Collections.emptyList();
   }

   public List<Resource> completeScan(IModuleLayerManager layerManager) {
      List<Resource> list = new ArrayList<>();
      new ArrayList();
      URL urlSrg = this.getClass().getResource("/srg");
      Path pathSrg = Paths.get(URI.create(urlSrg.toString()));
      Resource resSrg = new Resource(Layer.GAME, Collections.singletonList(SecureJar.from(new Path[]{pathSrg})));
      list.add(resSrg);
      return list;
   }

   public void onLoad(IEnvironment env, Set<String> otherServices) throws IncompatibleEnvironmentException {
      LOGGER.info("OptiFineTransformationService.onLoad");

      try {
         super.init();
         transformer = new OptiFineTransformer(getOfZipFile(), env);
         OptiFineResourceLocator.setResourceLocator(transformer);
      } catch (Exception e) {
         LOGGER.error("Error loading OptiFine ZIP file: " + getOfZipFileUrl(), e);
         throw new IncompatibleEnvironmentException("Error loading OptiFine ZIP file: " + getOfZipFileUrl());
      }
   }

   public static Optional<URL> getResourceUrl(String name) {
      if (name.endsWith(".class") && !name.startsWith("optifine/")) {
         name = "srg/" + name;
      }

      if (transformer == null) {
         return Optional.empty();
      }

      ZipEntry ze = getOfZipFile().getEntry(name);
      if (ze == null) {
         return Optional.empty();
      }

      try {
         String ofZipUrlStr = getOfZipFileUrl().toExternalForm();
         URL urlJar = new URL(ofZipUrlStr + name);
         return Optional.of(urlJar);
      } catch (IOException e) {
         LOGGER.error(e);
         return Optional.empty();
      }
   }

   public List<? extends ITransformer<?>> transformers() {
      LOGGER.info("OptiFineTransformationService.transformers");
      List<OptiFineTransformer> list = new ArrayList<>();
      if (transformer != null) {
         list.add(transformer);
      }

      return list;
   }

   public static OptiFineTransformer getTransformer() {
      return transformer;
   }

   public static ZipFile getOfZipFile() {
      return OptiFineBaseTransformerService.getOfZipFile();
   }
}
