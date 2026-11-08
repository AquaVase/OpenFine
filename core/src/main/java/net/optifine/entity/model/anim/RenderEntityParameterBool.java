package net.optifine.entity.model.anim;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.optifine.expr.IExpressionBool;
import net.optifine.render.RenderState;
import net.optifine.util.BlockUtils;

public enum RenderEntityParameterBool implements IExpressionBool {
    IS_ALIVE("is_alive"),
    IS_AGGRESSIVE("is_aggressive"),
    IS_BURNING("is_burning"),
    IS_CHILD("is_child"),
    IS_GLOWING("is_glowing"),
    IS_HURT("is_hurt"),
    IS_IN_HAND("is_in_hand", true),
    IS_IN_ITEM_FRAME("is_in_item_frame", true),
    IS_IN_GROUND("is_in_ground"),
    IS_IN_GUI("is_in_gui", true),
    IS_IN_LAVA("is_in_lava"),
    IS_IN_WATER("is_in_water", true),
    IS_INVISIBLE("is_invisible"),
    IS_ON_GROUND("is_on_ground"),
    IS_ON_HEAD("is_on_head", true),
    IS_ON_SHOULDER("is_on_shoulder"),
    IS_RIDDEN("is_ridden"),
    IS_RIDING("is_riding"),
    IS_SITTING("is_sitting"),
    IS_SNEAKING("is_sneaking"),
    IS_SPRINTING("is_sprinting"),
    IS_TAMED("is_tamed"),
    IS_WET("is_wet");

    private String name;
    private boolean blockEntity;
    private Minecraft mc;
    private static final RenderEntityParameterBool[] VALUES = values();

    RenderEntityParameterBool(String name) {
        this(name, false);
    }

    RenderEntityParameterBool(String name, boolean blockEntity) {
        this.name = name;
        this.blockEntity = blockEntity;
        this.mc = Minecraft.getInstance();
    }

    public String getName() {
        return this.name;
    }

    public boolean isBlockEntity() {
        return this.blockEntity;
    }

    @Override
    public boolean eval() {
        switch (this) {
            case IS_IN_HAND:
                return ItemInHandRenderer.isRenderItemHand() && !RenderState.isRenderItemHead();
            case IS_IN_ITEM_FRAME:
                return ItemFrameRenderer.isRenderItemFrame();
            case IS_IN_GROUND:
            case IS_IN_LAVA:
            case IS_IN_WATER:
            case IS_INVISIBLE:
            case IS_ON_GROUND:
            default:
                BlockEntity blockEntity = RenderState.getBlockEntity();
                if (blockEntity != null) {
                    switch (this) {
                        case IS_IN_WATER:
                            return BlockUtils.isPropertyTrue(blockEntity.getBlockState(), BlockStateProperties.WATERLOGGED);
                    }
                }

                Entity entity = RenderState.getEntity();
                if (entity == null) {
                    return false;
                } else {
                    if (entity instanceof LivingEntity livingEntity) {
                        switch (this) {
                            case IS_CHILD:
                                return livingEntity.isBaby();
                            case IS_HURT:
                                return livingEntity.hurtTime > 0;
                            case IS_ON_SHOULDER:
                                return entity == this.mc.player.entityShoulderLeft || entity == this.mc.player.entityShoulderRight;
                        }
                    }

                    if (entity instanceof Mob mob) {
                        switch (this) {
                            case IS_AGGRESSIVE:
                                return mob.isAggressive();
                        }
                    }

                    if (entity instanceof TamableAnimal tamable) {
                        switch (this) {
                            case IS_SITTING:
                                return tamable.isInSittingPose();
                            case IS_TAMED:
                                return tamable.isTame();
                        }
                    }

                    if (entity instanceof Fox fox) {
                        switch (this) {
                            case IS_SITTING:
                                return fox.isSitting();
                        }
                    }

                    if (entity instanceof AbstractArrow arrowEntity) {
                        switch (this) {
                            case IS_IN_GROUND:
                                if (arrowEntity.tickCount == 0 && arrowEntity.xo == 0.0 && arrowEntity.yo == 0.0 && arrowEntity.zo == 0.0) {
                                    return true;
                                }

                                return Arrow.isInGround(arrowEntity);
                        }
                    }

                    switch (this) {
                        case IS_ALIVE:
                            return entity.isAlive();
                        case IS_AGGRESSIVE:
                        case IS_CHILD:
                        case IS_HURT:
                        case IS_IN_HAND:
                        case IS_IN_ITEM_FRAME:
                        case IS_IN_GROUND:
                        case IS_IN_GUI:
                        case IS_ON_HEAD:
                        case IS_ON_SHOULDER:
                        case IS_SITTING:
                        case IS_TAMED:
                        default:
                            return false;
                        case IS_BURNING:
                            return entity.isOnFire();
                        case IS_GLOWING:
                            return entity.isCurrentlyGlowing();
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
            case IS_IN_GUI:
                return ItemFeatureRenderer.isRenderItemGui();
            case IS_ON_HEAD:
                return RenderState.isRenderItemHead();
        }
    }

    public static RenderEntityParameterBool parse(String str) {
        if (str == null) {
            return null;
        }

        for (int i = 0; i < VALUES.length; i++) {
            RenderEntityParameterBool type = VALUES[i];
            if (type.getName().equals(str)) {
                return type;
            }
        }

        return null;
    }
}
