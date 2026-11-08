package net.optifine.util;

public class Mutable<T> {
    private T value;

    public Mutable() {
    }

    public Mutable(T value) {
        this.value = value;
    }

    public T get() {
        return this.value;
    }

    public void set(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Mutable(" + this.value + ")";
    }
}
