package net.optifine.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class MemberIterator<T, R> implements Iterator<R> {
    private final T[] array;
    private final Function<T, R> getter;
    private int index = 0;

    public MemberIterator(T[] array, Function<T, R> getter) {
        this.array = array;
        this.getter = getter;
    }

    @Override
    public boolean hasNext() {
        return this.index < this.array.length;
    }

    @Override
    public R next() {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        } else {
            return this.getter.apply(this.array[this.index++]);
        }
    }
}
