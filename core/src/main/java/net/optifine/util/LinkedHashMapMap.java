package net.optifine.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class LinkedHashMapMap<K1, K2, V> extends AbstractMapMap<K1, K2, V> {
    @Override
    Map makeMap() {
        return new LinkedHashMap();
    }
}
