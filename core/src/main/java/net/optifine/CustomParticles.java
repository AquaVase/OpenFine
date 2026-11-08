package net.optifine;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.optifine.render.RenderEnv;

public class CustomParticles {
    public static boolean isAddParticle(ParticleOptions particleData) {
        if (particleData == ParticleTypes.EXPLOSION_EMITTER && !Config.isAnimatedExplosion()) {
            return false;
        } else if (particleData == ParticleTypes.EXPLOSION && !Config.isAnimatedExplosion()) {
            return false;
        } else if (particleData == ParticleTypes.POOF && !Config.isAnimatedExplosion()) {
            return false;
        } else if (particleData == ParticleTypes.UNDERWATER && !Config.isWaterParticles()) {
            return false;
        } else if (particleData == ParticleTypes.SMOKE && !Config.isAnimatedSmoke()) {
            return false;
        } else if (particleData == ParticleTypes.LARGE_SMOKE && !Config.isAnimatedSmoke()) {
            return false;
        } else if (particleData == ParticleTypes.ENTITY_EFFECT && !Config.isPotionParticles()) {
            return false;
        } else if (particleData == ParticleTypes.EFFECT && !Config.isPotionParticles()) {
            return false;
        } else if (particleData == ParticleTypes.INSTANT_EFFECT && !Config.isPotionParticles()) {
            return false;
        } else if (particleData == ParticleTypes.WITCH && !Config.isPotionParticles()) {
            return false;
        } else if (particleData == ParticleTypes.PORTAL && !Config.isPortalParticles()) {
            return false;
        } else if (particleData == ParticleTypes.FLAME && !Config.isAnimatedFlame()) {
            return false;
        } else if (particleData == ParticleTypes.SOUL_FIRE_FLAME && !Config.isAnimatedFlame()) {
            return false;
        } else if (particleData == ParticleTypes.DUST && !Config.isAnimatedRedstone()) {
            return false;
        } else if (particleData == ParticleTypes.DRIPPING_WATER && !Config.isDrippingWaterLava()) {
            return false;
        } else {
            return particleData == ParticleTypes.DRIPPING_LAVA && !Config.isDrippingWaterLava()
                ? false
                : particleData != ParticleTypes.FIREWORK || Config.isFireworkParticles();
        }
    }

    public static void modifyParticle(ParticleOptions particleData, Particle particle, ClientLevel world, double x, double y, double z, RenderEnv renderEnv) {
        if (particleData == ParticleTypes.BUBBLE) {
            CustomColors.updateWaterFX(particle, world, x, y, z, renderEnv);
        }

        if (particleData == ParticleTypes.SPLASH) {
            CustomColors.updateWaterFX(particle, world, x, y, z, renderEnv);
        }

        if (particleData == ParticleTypes.RAIN) {
            CustomColors.updateWaterFX(particle, world, x, y, z, renderEnv);
        }

        if (particleData == ParticleTypes.MYCELIUM) {
            CustomColors.updateMyceliumFX(particle);
        }

        if (particleData == ParticleTypes.PORTAL) {
            CustomColors.updatePortalFX(particle);
        }

        if (particleData == ParticleTypes.DUST) {
            CustomColors.updateReddustFX(particle, world, x, y, z);
        }

        if (particleData == ParticleTypes.LAVA) {
            CustomColors.updateLavaFX(particle);
        }
    }
}
