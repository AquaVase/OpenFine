package net.optifine.util;

public class MutableInt {
    private int value;

    public MutableInt() {
    }

    public MutableInt(int value) {
        this.value = value;
    }

    public int get() {
        return this.value;
    }

    public void set(int value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return this.value;
    }

    @Override
    public boolean equals(Object obj) {
        return !(obj instanceof MutableInt mi) ? false : this.value == mi.value;
    }

    @Override
    public String toString() {
        return "Mutable(" + this.value + ")";
    }
}
