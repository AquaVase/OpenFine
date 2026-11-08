package net.optifine.util;

import java.util.Iterator;

public class SingleIterable<T> implements Iterable<T>, Iterator<T> {
    private T value;

    public SingleIterable() {
    }

    public SingleIterable(T value) {
        this.value = value;
    }

    @Override
    public Iterator<T> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return this.value != null;
    }

    @Override
    public T next() {
        T ret = this.value;
        this.value = null;
        return ret;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
