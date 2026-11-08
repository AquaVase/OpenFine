package net.optifine.config;

import net.minecraft.world.level.Level;

public enum Weather {
    CLEAR,
    RAIN,
    THUNDER;

    public static Weather getWeather(Level world, float partialTicks) {
        float thunderStrength = world.getThunderLevel(partialTicks);
        if (thunderStrength > 0.5F) {
            return THUNDER;
        }

        float rainStrength = world.getRainLevel(partialTicks);
        return rainStrength > 0.5F ? RAIN : CLEAR;
    }
}
