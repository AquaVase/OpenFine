package net.optifine.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.optifine.Config;

public class StaticMap {
    private static final Map<String, Object> MAP = Collections.synchronizedMap(new HashMap<>());

    public static boolean contains(String key) {
        return MAP.containsKey(key);
    }

    public static boolean contains(String key, Object value) {
        if (!MAP.containsKey(key)) {
            return false;
        }

        Object val = MAP.get(key);
        return Config.equals(val, value);
    }

    public static Object get(String key) {
        return MAP.get(key);
    }

    public static void put(String key, Object val) {
        MAP.put(key, val);
    }

    public static void remove(String key) {
        MAP.remove(key);
    }

    public static int getInt(String key, int def) {
        return !(MAP.get(key) instanceof Integer valInt) ? def : valInt;
    }

    public static int putInt(String key, int val) {
        int valPrev = getInt(key, 0);
        Integer valObj = val;
        MAP.put(key, valObj);
        return valPrev;
    }

    public static long getLong(String key, long def) {
        return !(MAP.get(key) instanceof Long valLong) ? def : valLong;
    }

    public static void putLong(String key, long val) {
        Long valObj = val;
        MAP.put(key, valObj);
    }

    public static long putLong(String key, long val, long def) {
        long valPrev = getLong(key, def);
        Long valObj = val;
        MAP.put(key, valObj);
        return valPrev;
    }

    public static long addLong(String key, long val, long def) {
        long valMap = getLong(key, def);
        valMap += val;
        putLong(key, valMap);
        return valMap;
    }

    public static <T> List<T> getAutoList(String key) {
        List list = (List)get(key);
        if (list == null) {
            list = new ArrayList();
            put(key, list);
        }

        return list;
    }

    public static <T> Set<T> getAutoSet(String key) {
        Set set = (Set)get(key);
        if (set == null) {
            set = new HashSet();
            put(key, set);
        }

        return set;
    }

    public static <K, V> Map<K, V> getAutoMap(String key) {
        Map map = (Map)get(key);
        if (map == null) {
            map = new HashMap();
            put(key, map);
        }

        return map;
    }
}
