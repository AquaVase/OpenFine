package net.optifine.util;

import java.util.HashMap;
import java.util.Map;

public class HashMapMap<K1, K2, V> extends AbstractMapMap<K1, K2, V> {
    @Override
    Map makeMap() {
        return new HashMap();
    }
}
