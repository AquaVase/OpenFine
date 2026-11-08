package net.optifine.util;

import net.optifine.Config;

public class CompoundKey {
    private Object[] keys;
    private int hashcode = 0;

    public CompoundKey(Object[] keys) {
        this.keys = (Object[])keys.clone();
    }

    public CompoundKey(Object k1, Object k2) {
        this(new Object[]{k1, k2});
    }

    public CompoundKey(Object k1, Object k2, Object k3) {
        this(new Object[]{k1, k2, k3});
    }

    public CompoundKey(Object k1, Object k2, Object k3, Object k4) {
        this(new Object[]{k1, k2, k3, k4});
    }

    @Override
    public int hashCode() {
        if (this.hashcode == 0) {
            this.hashcode = 7;

            for (int i = 0; i < this.keys.length; i++) {
                Object key = this.keys[i];
                if (key != null) {
                    this.hashcode = 31 * this.hashcode + key.hashCode();
                }
            }
        }

        return this.hashcode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (!(obj instanceof CompoundKey ck)) {
            return false;
        } else {
            Object[] ckKeys = ck.getKeys();
            if (ckKeys.length != this.keys.length) {
                return false;
            }

            for (int i = 0; i < this.keys.length; i++) {
                if (!compareKeys(this.keys[i], ckKeys[i])) {
                    return false;
                }
            }

            return true;
        }
    }

    private static boolean compareKeys(Object key1, Object key2) {
        if (key1 == key2) {
            return true;
        } else if (key1 == null) {
            return false;
        } else {
            return key2 == null ? false : key1.equals(key2);
        }
    }

    private Object[] getKeys() {
        return this.keys;
    }

    public Object[] getKeysCopy() {
        return (Object[])this.keys.clone();
    }

    @Override
    public String toString() {
        return "[" + Config.arrayToString(this.keys) + "]";
    }
}
