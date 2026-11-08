package net.optifine.shaders.uniform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.LivingEntity;
import net.optifine.expr.IExpressionBool;

public enum ShaderParameterBool implements IExpressionBool {
    IS_ALIVE("is_alive"),
    IS_BURNING("is_burning"),
    IS_CHILD("is_child"),
    IS_GLOWING("is_glowing"),
    IS_HURT("is_hurt"),
    IS_IN_LAVA("is_in_lava"),
    IS_IN_WATER("is_in_water"),
    IS_INVISIBLE("is_invisible"),
    IS_ON_GROUND("is_on_ground"),
    IS_RIDDEN("is_ridden"),
    IS_RIDING("is_riding"),
    IS_SNEAKING("is_sneaking"),
    IS_SPRINTING("is_sprinting"),
    IS_WET("is_wet");

    private String name;
    private EntityRenderDispatcher renderManager;
    private static final ShaderParameterBool[] VALUES = values();

    ShaderParameterBool(String name) {
        this.name = name;
        this.renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
    }

    public String getName() {
        return this.name;
    }

    @Override
    public boolean eval() {
        if (Minecraft.getInstance().getCameraEntity() instanceof LivingEntity entity) {
            switch (this) {
                case IS_ALIVE:
                    return entity.isAlive();
                case IS_BURNING:
                    return entity.isOnFire();
                case IS_CHILD:
                    return entity.isBaby();
                case IS_GLOWING:
                    return entity.isCurrentlyGlowing();
                case IS_HURT:
                    return entity.hurtTime > 0;
                case IS_IN_LAVA:
                    return entity.isInLava();
                case IS_IN_WATER:
                    return entity.isInWater();
                case IS_INVISIBLE:
                    return entity.isInvisible();
                case IS_ON_GROUND:
                    return entity.onGround();
                case IS_RIDDEN:
                    return entity.isVehicle();
                case IS_RIDING:
                    return entity.isPassenger();
                case IS_SNEAKING:
                    return entity.isCrouching();
                case IS_SPRINTING:
                    return entity.isSprinting();
                case IS_WET:
                    return entity.isInWaterOrRain();
            }
        }

        return false;
    }

    public static ShaderParameterBool parse(String str) {
        if (str == null) {
            return null;
        }

        for (int i = 0; i < VALUES.length; i++) {
            ShaderParameterBool type = VALUES[i];
            if (type.getName().equals(str)) {
                return type;
            }
        }

        return null;
    }
}
