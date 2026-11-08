package net.optifine.util;

import java.util.HashMap;
import java.util.Map;
import net.optifine.Config;

public class StaticMultiMap {
    public static Map<String, Object> getMap(String mapName) {
        Object mapObj = StaticMap.get(mapName);
        if (mapObj == null) {
            mapObj = new HashMap();
            StaticMap.put(mapName, mapObj);
        }

        if (!(mapObj instanceof Map map)) {
            throw new IllegalArgumentException("Not a map: " + mapObj);
        } else {
            return map;
        }
    }

    public static void put(String mapName, String key, Object value) {
        Map<String, Object> map = getMap(mapName);
        map.put(key, value);
    }

    public static Object get(String mapName, String key) {
        Map<String, Object> map = getMap(mapName);
        return map.get(key);
    }

    public static boolean contains(String mapName, String key) {
        Map<String, Object> map = getMap(mapName);
        return map.containsKey(key);
    }

    public static boolean containsValue(String mapName, Object val) {
        Map<String, Object> map = getMap(mapName);
        return map.containsValue(val);
    }

    public static boolean contains(String mapName, String key, Object value) {
        Object val = get(mapName, key);
        return Config.equals(val, value);
    }
}
