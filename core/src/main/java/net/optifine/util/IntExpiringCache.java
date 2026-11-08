package net.optifine.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;

public abstract class IntExpiringCache<T> {
    private final int intervalMs;
    private long timeCheckMs;
    private Int2ObjectOpenHashMap<IntExpiringCache.Wrapper<T>> map = new Int2ObjectOpenHashMap<>();

    public IntExpiringCache(int intervalMs) {
        this.intervalMs = intervalMs;
    }

    public T get(int key) {
        long timeNowMs = System.currentTimeMillis();
        if (!this.map.isEmpty() && timeNowMs >= this.timeCheckMs) {
            this.timeCheckMs = timeNowMs + this.intervalMs;
            long timeMinMs = timeNowMs - this.intervalMs;
            IntSet keys = this.map.keySet();
            IntIterator it = keys.iterator();

            while (it.hasNext()) {
                int k = it.nextInt();
                if (k != key) {
                    IntExpiringCache.Wrapper<T> w = this.map.get(k);
                    if (w.getAccessTimeMs() <= timeMinMs) {
                        it.remove();
                    }
                }
            }
        }

        IntExpiringCache.Wrapper<T> w = this.map.get(key);
        if (w == null) {
            T obj = this.make();
            w = new IntExpiringCache.Wrapper<>(obj);
            this.map.put(key, w);
        }

        w.setAccessTimeMs(timeNowMs);
        return w.getValue();
    }

    protected abstract T make();

    public static class Wrapper<T> {
        private final T value;
        private long accessTimeMs;

        public Wrapper(T value) {
            this.value = value;
        }

        public T getValue() {
            return this.value;
        }

        public long getAccessTimeMs() {
            return this.accessTimeMs;
        }

        public void setAccessTimeMs(long accessTimeMs) {
            this.accessTimeMs = accessTimeMs;
        }
    }
}
