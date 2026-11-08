package optifine;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.jar.Manifest;
import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.fml.jarcontents.JarResource;
import net.neoforged.fml.jarcontents.JarResourceVisitor;

public class VirtualJarContents implements JarContents {
   public void close() throws IOException {
   }

   public Optional<String> getChecksum() {
      return Optional.empty();
   }

   public Path getPrimaryPath() {
      return null;
   }

   public Collection<Path> getContentRoots() {
      return null;
   }

   public JarResource get(String relativePath) {
      return null;
   }

   public Optional<URI> findFile(String relativePath) {
      return Optional.empty();
   }

   public InputStream openFile(String relativePath) throws IOException {
      return null;
   }

   public boolean containsFile(String relativePath) {
      return false;
   }

   public Manifest getManifest() {
      return null;
   }

   public void visitContent(String startingFolder, JarResourceVisitor visitor) {
   }
}
