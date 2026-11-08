package optifine;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TargetType;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import cpw.mods.modlauncher.api.IEnvironment.Keys;
import cpw.mods.modlauncher.api.ITransformer.Target;
import cpw.mods.modlauncher.api.TypesafeMap.Key;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;

public class OptiFineTransformer extends OptiFineBaseTransformer implements ITransformer<ClassNode> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final boolean hasTargetPreClass;

    public OptiFineTransformer(ZipFile ofZipFile, IEnvironment env) {
        super(ofZipFile);
        this.hasTargetPreClass = hasTargetPreClass(env);
        if (this.hasTargetPreClass) {
            LOGGER.info("Target.PRE_CLASS is available");
        } else {
            LOGGER.info("Target.PRE_CLASS is not available");
        }
    }

    public TransformerVoteResult castVote(ITransformerVotingContext context) {
        return TransformerVoteResult.YES;
    }

    public TargetType<ClassNode> getTargetType() {
        return TargetType.PRE_CLASS;
    }

    public Set<Target<ClassNode>> targets() {
        Set<Target<ClassNode>> set = new HashSet<>();
        String[] names = this.getResourceNames("srg/", ".class");
        String[] namesPatch = this.getResourceNames("patch/srg/", ".class.xdelta");
        names = (String[])Utils.addObjectsToArray(names, namesPatch);

        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            name = Utils.removePrefix(name, new String[]{"srg/", "patch/srg/"});
            name = Utils.removeSuffix(name, new String[]{".class", ".class.xdelta"});
            if (!name.startsWith("net/optifine/")) {
                Target itt = this.getTargetClass(name);
                set.add(itt);
            }
        }

        LOGGER.info("Targets: " + set.size());
        return set;
    }

    private Target getTargetClass(String name) {
        return this.hasTargetPreClass ? this.getTargetPreClass(name) : Target.targetClass(name);
    }

    private Target getTargetPreClass(String name) {
        return Target.targetPreClass(name);
    }

    private static boolean hasTargetPreClass(IEnvironment env) {
        Optional<String> mlVer = env.getProperty((Key)Keys.MLSPEC_VERSION.get());
        if (!mlVer.isPresent()) {
            return false;
        }

        String[] parts = Utils.tokenize(mlVer.get(), ".");
        if (parts.length <= 0) {
            return false;
        }

        String majorStr = parts[0];
        int major = Utils.parseInt(majorStr, -1);
        return major >= 7;
    }

    public ClassNode transform(ClassNode input, ITransformerVotingContext context) {
        return super.transform(input, context.getClassName());
    }
}
