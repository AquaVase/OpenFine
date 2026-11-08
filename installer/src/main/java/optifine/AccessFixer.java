package optifine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class AccessFixer {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void fixMemberAccess(ClassNode classOld, ClassNode classNew) {
        List<FieldNode> fieldsOld = classOld.fields;
        List<FieldNode> fieldsNew = classNew.fields;
        Map<String, FieldNode> mapFieldsOld = getMapFields(fieldsOld);

        for (FieldNode fieldNew : fieldsNew) {
            String idNew = fieldNew.name;
            FieldNode fieldOld = mapFieldsOld.get(idNew);
            if (fieldOld != null && fieldNew.access != fieldOld.access) {
                fieldNew.access = combineAccess(fieldNew.access, fieldOld.access);
            }
        }

        List<MethodNode> methodsOld = classOld.methods;
        List<MethodNode> methodsNew = classNew.methods;
        Map<String, MethodNode> mapMethodsOld = getMapMethods(methodsOld);
        Set<String> privateChanged = new HashSet<>();

        for (MethodNode methodNew : methodsNew) {
            String idNew = methodNew.name + methodNew.desc;
            MethodNode methodOld = mapMethodsOld.get(idNew);
            if (methodOld != null && methodNew.access != methodOld.access) {
                int accessPrev = methodNew.access;
                methodNew.access = combineAccess(methodNew.access, methodOld.access);
                if (isSet(accessPrev, 2) && !isSet(methodNew.access, 2) && !isSet(methodNew.access, 8) && !methodNew.name.equals("<init>")) {
                    privateChanged.add(methodNew.name + methodNew.desc);
                }
            }
        }

        if (!privateChanged.isEmpty()) {
            List<MethodInsnNode> changed = new ArrayList<>();
            classNew.methods
                .forEach(
                    mn -> StreamSupport.stream(Spliterators.spliteratorUnknownSize(mn.instructions.iterator(), 16), false)
                        .filter(i -> i.getOpcode() == 183)
                        .map(MethodInsnNode.class::cast)
                        .filter(m -> privateChanged.contains(m.name + m.desc))
                        .forEach(m -> {
                            m.setOpcode(182);
                            changed.add(m);
                        })
                );
        }

        List<InnerClassNode> innerClassesOld = classOld.innerClasses;
        List<InnerClassNode> innerClassesNew = classNew.innerClasses;
        Map<String, InnerClassNode> mapInnerClassesOld = getMapInnerClasses(innerClassesOld);

        for (InnerClassNode innerClassNew : innerClassesNew) {
            String idNew = innerClassNew.name;
            InnerClassNode innerClassOld = mapInnerClassesOld.get(idNew);
            if (innerClassOld != null && innerClassNew.access != innerClassOld.access) {
                int accessNew = combineAccess(innerClassNew.access, innerClassOld.access);
                innerClassNew.access = accessNew;
            }
        }

        if (classNew.access != classOld.access) {
            int accessClassNew = combineAccess(classNew.access, classOld.access);
            classNew.access = accessClassNew;
        }
    }

    private static int combineAccess(int access, int access2) {
        if (access == access2) {
            return access;
        }

        int MASK_ACCESS = 7;
        int accessClean = access & ~MASK_ACCESS;
        if (!isSet(access, 16) || !isSet(access2, 16)) {
            accessClean &= -17;
        }

        if (isSet(access, 1) || isSet(access2, 1)) {
            return accessClean | 1;
        } else if (isSet(access, 4) || isSet(access2, 4)) {
            return accessClean | 4;
        } else if (!isSet(access, MASK_ACCESS) || !isSet(access2, MASK_ACCESS)) {
            return accessClean;
        } else {
            return !isSet(access, 2) && !isSet(access2, 2) ? accessClean : accessClean | 2;
        }
    }

    private static boolean isSet(int access, int flag) {
        return (access & flag) != 0;
    }

    public static Map<String, FieldNode> getMapFields(List<FieldNode> fields) {
        Map<String, FieldNode> map = new LinkedHashMap<>();

        for (FieldNode fieldNode : fields) {
            String id = fieldNode.name;
            map.put(id, fieldNode);
        }

        return map;
    }

    public static Map<String, MethodNode> getMapMethods(List<MethodNode> methods) {
        Map<String, MethodNode> map = new LinkedHashMap<>();

        for (MethodNode methodNode : methods) {
            String id = methodNode.name + methodNode.desc;
            map.put(id, methodNode);
        }

        return map;
    }

    public static Map<String, InnerClassNode> getMapInnerClasses(List<InnerClassNode> innerClasses) {
        Map<String, InnerClassNode> map = new LinkedHashMap<>();

        for (InnerClassNode innerClassNode : innerClasses) {
            String id = innerClassNode.name;
            map.put(id, innerClassNode);
        }

        return map;
    }

    private static String toString(int access) {
        StringBuffer sb = new StringBuffer();
        if (isSet(access, 1)) {
            addToBuffer(sb, "public", " ");
        }

        if (isSet(access, 4)) {
            addToBuffer(sb, "protected", " ");
        }

        if (isSet(access, 2)) {
            addToBuffer(sb, "private", " ");
        }

        if (isSet(access, 16)) {
            addToBuffer(sb, "final", " ");
        }

        return sb.toString();
    }

    private static void addToBuffer(StringBuffer sb, String val, String separator) {
        if (sb.length() > 0) {
            sb.append(separator);
        }

        sb.append(val);
    }
}
