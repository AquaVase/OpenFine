package net.optifine.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class CollectionUtils {
    public static boolean noneMatch(Set s1, Set s2) {
        return !anyMatch(s1, s2);
    }

    public static boolean anyMatch(Set s1, Set s2) {
        if (!s1.isEmpty() && !s2.isEmpty()) {
            if (s2.size() < s1.size()) {
                Set temp = s1;
                s1 = s2;
                s2 = temp;
            }

            for (Object elem : s1) {
                if (s2.contains(elem)) {
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    public static boolean isMutable(Map map) {
        return map == null ? false : isMutableClass(map.getClass());
    }

    public static boolean isMutable(Collection coll) {
        return coll == null ? false : isMutableClass(coll.getClass());
    }

    private static boolean isMutableClass(Class cls) {
        String name = cls.getName();
        return name.contains("Immutable") ? false : !name.contains("Unmodifiable");
    }

    public static Set getKeysLike(Map map, String keyPart) {
        Set keysLike = new LinkedHashSet();

        for (Object key : map.keySet()) {
            String keyName = String.valueOf(key);
            if (keyName.contains(keyPart)) {
                keysLike.add(key);
            }
        }

        return keysLike;
    }

    public static Map getMapLike(Map map, String keyPart) {
        Map mapLike = new LinkedHashMap();

        for (Object key : map.keySet()) {
            String keyName = String.valueOf(key);
            if (keyName.contains(keyPart)) {
                mapLike.put(key, map.get(key));
            }
        }

        return mapLike;
    }
}
