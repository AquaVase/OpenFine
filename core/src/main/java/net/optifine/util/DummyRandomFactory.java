package net.optifine.util;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

public class DummyRandomFactory implements PositionalRandomFactory {
    public static DummyRandomFactory INSTANCE = new DummyRandomFactory();

    @Override
    public RandomSource fromHashOf(String hash) {
        return DummyRandomSource.INSTANCE;
    }

    @Override
    public RandomSource fromSeed(long seed) {
        return DummyRandomSource.INSTANCE;
    }

    @Override
    public RandomSource at(int x, int y, int z) {
        return DummyRandomSource.INSTANCE;
    }

    @Override
    public void parityConfigString(StringBuilder bug) {
    }
}
