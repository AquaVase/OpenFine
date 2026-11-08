package net.optifine.util;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

public class DummyRandomSource implements RandomSource {
    public static DummyRandomSource INSTANCE = new DummyRandomSource();

    @Override
    public RandomSource fork() {
        return this;
    }

    @Override
    public PositionalRandomFactory forkPositional() {
        return DummyRandomFactory.INSTANCE;
    }

    @Override
    public void setSeed(long seed) {
    }

    @Override
    public int nextInt() {
        return 0;
    }

    @Override
    public int nextInt(int max) {
        return 0;
    }

    @Override
    public long nextLong() {
        return 0L;
    }

    @Override
    public boolean nextBoolean() {
        return false;
    }

    @Override
    public float nextFloat() {
        return 0.0F;
    }

    @Override
    public double nextDouble() {
        return 0.0;
    }

    @Override
    public double nextGaussian() {
        return 0.0;
    }
}
