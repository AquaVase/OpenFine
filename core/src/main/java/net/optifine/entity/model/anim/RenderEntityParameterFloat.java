package net.optifine.entity.model.anim;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.optifine.Config;
import net.optifine.entity.model.CustomEntityModels;
import net.optifine.expr.IExpressionFloat;
import net.optifine.render.RenderState;
import net.optifine.util.MathUtils;
import net.optifine.util.WorldUtils;

public enum RenderEntityParameterFloat implements IExpressionFloat {
   LIMB_SWING("limb_swing"),
   LIMB_SWING_SPEED("limb_speed"),
   AGE("age"),
   HEAD_YAW("head_yaw"),
   HEAD_PITCH("head_pitch"),
   HEALTH("health"),
   HURT_TIME("hurt_time"),
   DEATH_TIME("death_time"),
   IDLE_TIME("idle_time"),
   MAX_HEALTH("max_health"),
   MOVE_FORWARD("move_forward"),
   MOVE_STRAFING("move_strafing"),
   POS_X("pos_x", true),
   POS_Y("pos_y", true),
   POS_Z("pos_z", true),
   ROT_X("rot_x"),
   ROT_Y("rot_y"),
   ID("id", true),
   PLAYER_POS_X("player_pos_x", true),
   PLAYER_POS_Y("player_pos_y", true),
   PLAYER_POS_Z("player_pos_z", true),
   PLAYER_ROT_X("player_rot_x", true),
   PLAYER_ROT_Y("player_rot_y", true),
   FRAME_TIME("frame_time", true),
   FRAME_COUNTER("frame_counter", true),
   ANGER_TIME("anger_time"),
   ANGER_TIME_START("anger_time_start"),
   SWING_PROGRESS("swing_progress"),
   DIMENSION("dimension", true),
   RULE_INDEX("rule_index", true);

   private String name;
   private boolean blockEntity;
   private EntityRenderDispatcher renderManager;
   private static final RenderEntityParameterFloat[] VALUES = values();
   private static Minecraft mc = Minecraft.getInstance();
   private static String KEY_ANGER_TIME_MAX = "ANGER_TIME_MAX";

   RenderEntityParameterFloat(String name) {
      this(name, false);
   }

   RenderEntityParameterFloat(String name, boolean blockEntity) {
      this.name = name;
      this.blockEntity = blockEntity;
      this.renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
   }

   public String getName() {
      return this.name;
   }

   public boolean isBlockEntity() {
      return this.blockEntity;
   }

   @Override
   public float eval() {
      switch (this) {
         case PLAYER_POS_X:
            return Mth.lerp(GameRenderer.getRenderPartialTicks(), (float)mc.player.xo, (float)mc.player.getX());
         case PLAYER_POS_Y:
            return Mth.lerp(GameRenderer.getRenderPartialTicks(), (float)mc.player.yo, (float)mc.player.getY());
         case PLAYER_POS_Z:
            return Mth.lerp(GameRenderer.getRenderPartialTicks(), (float)mc.player.zo, (float)mc.player.getZ());
         case PLAYER_ROT_X:
            return MathUtils.toRad(Mth.lerp(GameRenderer.getRenderPartialTicks(), mc.player.xRotO, mc.player.getXRot()));
         case PLAYER_ROT_Y:
            return MathUtils.toRad(Mth.lerp(GameRenderer.getRenderPartialTicks(), mc.player.yRotO, mc.player.getYRot()));
         case FRAME_TIME:
            return this.getLastFrameTime();
         case FRAME_COUNTER:
            return Config.getWorldRenderer().getFrameCount() % 27720;
         case ANGER_TIME:
         case ANGER_TIME_START:
         case SWING_PROGRESS:
         default:
            BlockEntity blockEntity = RenderState.getBlockEntity();
            if (blockEntity != null) {
               switch (this) {
                  case POS_X:
                     return blockEntity.getBlockPos().getX();
                  case POS_Y:
                     return blockEntity.getBlockPos().getY();
                  case POS_Z:
                     return blockEntity.getBlockPos().getZ();
                  case ROT_X:
                  case ROT_Y:
                  default:
                     break;
                  case ID:
                     return this.toFloat(blockEntity.getBlockPos());
               }
            }

            EntityRenderState ers = RenderState.getEntityRenderState();
            if (ers == null) {
               return 0.0F;
            } else {
               Entity entity = ers.entity;
               if (entity == null) {
                  return 0.0F;
               } else {
                  if (ers instanceof LivingEntityRenderState lers) {
                     switch (this) {
                        case LIMB_SWING:
                           return lers.walkAnimationPos;
                        case LIMB_SWING_SPEED:
                           return lers.walkAnimationSpeed;
                        case AGE:
                           return lers.ageInTicks;
                        case HEAD_YAW:
                           return lers.yRot;
                        case HEAD_PITCH:
                           return lers.xRot;
                        default:
                           if (entity instanceof LivingEntity livingEntity) {
                              switch (this) {
                                 case HEALTH:
                                    return livingEntity.getHealth();
                                 case HURT_TIME:
                                    return livingEntity.hurtTime > 0 ? livingEntity.hurtTime - GameRenderer.getRenderPartialTicks() : 0.0F;
                                 case DEATH_TIME:
                                    return livingEntity.deathTime > 0 ? livingEntity.deathTime + GameRenderer.getRenderPartialTicks() : 0.0F;
                                 case IDLE_TIME:
                                    return livingEntity.getNoActionTime() > 0 ? livingEntity.getNoActionTime() + GameRenderer.getRenderPartialTicks() : 0.0F;
                                 case MAX_HEALTH:
                                    return livingEntity.getMaxHealth();
                                 case MOVE_FORWARD:
                                    return livingEntity.zza;
                                 case MOVE_STRAFING:
                                    return livingEntity.xxa;
                                 case POS_X:
                                 case POS_Y:
                                 case POS_Z:
                                 case ROT_X:
                                 case ROT_Y:
                                 case ID:
                                 case PLAYER_POS_X:
                                 case PLAYER_POS_Y:
                                 case PLAYER_POS_Z:
                                 case PLAYER_ROT_X:
                                 case PLAYER_ROT_Y:
                                 case FRAME_TIME:
                                 case FRAME_COUNTER:
                                 default:
                                    break;
                                 case ANGER_TIME:
                                    if (livingEntity instanceof NeutralMob neutralMob) {
                                       float val = (float)neutralMob.getPersistentAngerEndTime();
                                       float valMax = EntityVariableFloat.getEntityValue(KEY_ANGER_TIME_MAX);
                                       if (val > 0.0F) {
                                          if (val > valMax) {
                                             EntityVariableFloat.setEntityValue(KEY_ANGER_TIME_MAX, val);
                                          }

                                          if (val < valMax) {
                                             val -= GameRenderer.getRenderPartialTicks();
                                          }
                                       } else if (valMax > 0.0F) {
                                          EntityVariableFloat.setEntityValue(KEY_ANGER_TIME_MAX, 0.0F);
                                       }

                                       return val;
                                    }
                                 case ANGER_TIME_START:
                                    return EntityVariableFloat.getEntityValue(KEY_ANGER_TIME_MAX);
                                 case SWING_PROGRESS:
                                    return livingEntity.getAttackAnim(GameRenderer.getRenderPartialTicks());
                              }
                           }
                     }
                  }

                  if (entity instanceof Boat boat) {
                     switch (this) {
                        case LIMB_SWING:
                           float left = boat.getRowingTime(0, GameRenderer.getRenderPartialTicks());
                           float right = boat.getRowingTime(1, GameRenderer.getRenderPartialTicks());
                           return Math.max(left, right);
                        case LIMB_SWING_SPEED:
                           return 1.0F;
                     }
                  }

                  if (entity instanceof AbstractMinecart minecart) {
                     switch (this) {
                        case LIMB_SWING:
                           float posX = Mth.lerp(GameRenderer.getRenderPartialTicks(), (float)minecart.xOld, (float)minecart.getX());
                           float posZ = Mth.lerp(GameRenderer.getRenderPartialTicks(), (float)minecart.zOld, (float)minecart.getZ());
                           BlockState bs = Minecraft.getInstance().level.getBlockState(minecart.blockPosition());
                           if (bs.is(BlockTags.RAILS)) {
                              RailShape rs = bs.getValue(((BaseRailBlock)bs.getBlock()).getShapeProperty());
                              switch (rs) {
                                 case SOUTH_WEST:
                                    return posX + posZ;
                                 case SOUTH_EAST:
                                    return -(posX - posZ);
                                 case NORTH_WEST:
                                    return posX - posZ;
                                 default:
                                    return -(posX + posZ);
                              }
                           }
                        case LIMB_SWING_SPEED:
                           return 1.0F;
                     }
                  }

                  switch (this) {
                     case POS_X:
                        return Mth.lerp(GameRenderer.getRenderPartialTicks(), (float)entity.xo, (float)entity.getX());
                     case POS_Y:
                        return Mth.lerp(GameRenderer.getRenderPartialTicks(), (float)entity.yo, (float)entity.getY());
                     case POS_Z:
                        return Mth.lerp(GameRenderer.getRenderPartialTicks(), (float)entity.zo, (float)entity.getZ());
                     case ROT_X:
                        return MathUtils.toRad(Mth.lerp(GameRenderer.getRenderPartialTicks(), entity.xRotO, entity.getXRot()));
                     case ROT_Y:
                        if (entity instanceof LivingEntity) {
                           return MathUtils.toRad(
                              Mth.lerp(GameRenderer.getRenderPartialTicks(), ((LivingEntity)entity).yBodyRotO, ((LivingEntity)entity).yBodyRot)
                           );
                        }

                        return MathUtils.toRad(Mth.lerp(GameRenderer.getRenderPartialTicks(), entity.yRotO, entity.getYRot()));
                     case ID:
                        return this.toFloat(entity.getUUID());
                     default:
                        return 0.0F;
                  }
               }
            }
         case DIMENSION:
            return WorldUtils.getDimensionId(mc.level);
         case RULE_INDEX:
            return CustomEntityModels.getMatchingRuleIndex();
      }
   }

   float getLastFrameTime() {
      float timeMul = 1.0F;
      Minecraft mc = Minecraft.getInstance();
      IntegratedServer is = mc.getSingleplayerServer();
      if (is != null && mc.isLocalServer()) {
         if (is.isPaused()) {
            return 0.0F;
         }

         TickRateManager trm = is.tickRateManager();
         if (trm.isFrozen()) {
            return 0.0F;
         }

         float tickRate = trm.tickrate();
         if (tickRate > 0.0F && tickRate < 20.0F) {
            timeMul = tickRate / 20.0F;
         }
      }

      return timeMul * (float)Config.getLastFrameTimeMs() / 1000.0F;
   }

   private float toFloat(UUID uuid) {
      int i0 = Long.hashCode(uuid.getLeastSignificantBits());
      int i1 = Long.hashCode(uuid.getMostSignificantBits());
      int id = i0 ^ i1;
      id = Math.abs(id) % 27720 + 1;
      return id;
   }

   private float toFloat(BlockPos pos) {
      int id = Config.getRandom(pos, 0);
      id = Math.abs(id) % 27720 + 1;
      return id;
   }

   public static RenderEntityParameterFloat parse(String str) {
      if (str == null) {
         return null;
      }

      for (int i = 0; i < VALUES.length; i++) {
         RenderEntityParameterFloat type = VALUES[i];
         if (type.getName().equals(str)) {
            return type;
         }
      }

      return null;
   }
}
