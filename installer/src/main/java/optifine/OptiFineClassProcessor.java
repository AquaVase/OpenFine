package optifine;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import net.neoforged.neoforgespi.ILaunchContext;
import net.neoforged.neoforgespi.locating.IDiscoveryPipeline;
import net.neoforged.neoforgespi.locating.IModFileCandidateLocator;
import net.neoforged.neoforgespi.locating.IncompatibleFileReporting;
import net.neoforged.neoforgespi.locating.ModFileDiscoveryAttributes;
import net.neoforged.neoforgespi.transformation.ClassProcessor;
import net.neoforged.neoforgespi.transformation.ProcessorName;
import net.neoforged.neoforgespi.transformation.ClassProcessor.ComputeFlags;
import net.neoforged.neoforgespi.transformation.ClassProcessor.SelectionContext;
import net.neoforged.neoforgespi.transformation.ClassProcessor.TransformationContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;

public class OptiFineClassProcessor extends OptiFineBaseTransformerService implements ClassProcessor, IModFileCandidateLocator {
    private static final Logger LOGGER = LogManager.getLogger();
    private OptiFineBaseTransformer transformer;

    public OptiFineClassProcessor() {
        LOGGER.info("OptiFineClassProcessor.init()");

        try {
            super.init();
            this.transformer = new OptiFineBaseTransformer(getOfZipFile());
            OptiFineResourceLocator.setResourceLocator(this.transformer);
        } catch (Exception e) {
            LOGGER.error("Error loading OptiFine ZIP file: " + getOfZipFileUrl(), e);
            throw new RuntimeException("Error loading OptiFine ZIP file: " + getOfZipFileUrl());
        }
    }

    public ProcessorName name() {
        return new ProcessorName("optifine", "class_processor");
    }

    public boolean handlesClass(SelectionContext context) {
        LOGGER.info("OptiFine: handlesClass: " + context.type().getClassName());
        String classPath = this.transformer.getSrgClassPath(context.type().getClassName());
        InputStream in = this.transformer.getOptiFineResourceStream(classPath);
        return in != null;
    }

    public ComputeFlags processClass(TransformationContext context) {
        LOGGER.info("OptiFine: processClass: " + context.type().getClassName());
        ClassNode cn = this.transformer.transform(context.node(), context.type().getClassName());
        copyClassNode(cn, context.node());
        return ComputeFlags.COMPUTE_FRAMES;
    }

    public void findCandidates(ILaunchContext context, IDiscoveryPipeline pipeline) {
        try {
            Map<String, String> map = new HashMap<>();
            map.put("create", "true");
            FileSystem zfs = FileSystems.newFileSystem(getOfZipFilePath().toUri(), map);
            Path pathSrg = zfs.getPath("/srg");
            pipeline.addPath(pathSrg, ModFileDiscoveryAttributes.DEFAULT, IncompatibleFileReporting.WARN_ALWAYS);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static void copyClassNode(ClassNode src, ClassNode dst) {
        resetClassNode(dst);
        src.accept(dst);
    }

    public static void resetClassNode(ClassNode node) {
        node.access = 0;
        node.name = null;
        node.signature = null;
        node.superName = null;
        node.interfaces.clear();
        node.sourceFile = null;
        node.sourceDebug = null;
        node.module = null;
        node.outerClass = null;
        node.outerMethod = null;
        node.outerMethodDesc = null;
        node.visibleAnnotations = null;
        node.invisibleAnnotations = null;
        node.visibleTypeAnnotations = null;
        node.invisibleTypeAnnotations = null;
        node.attrs = null;
        node.innerClasses.clear();
        node.nestHostClass = null;
        node.nestMembers = null;
        node.permittedSubclasses = null;
        node.recordComponents = null;
        node.fields.clear();
        node.methods.clear();
    }
}
