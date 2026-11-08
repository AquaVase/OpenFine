package net.optifine.entity.model;

import net.minecraft.client.model.animal.squid.SquidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.GlowSquidRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterGlowSquid extends ModelAdapterSquid {
   public ModelAdapterGlowSquid() {
      super(EntityTypes.GLOW_SQUID, "glow_squid", ModelLayers.GLOW_SQUID);
   }

   protected ModelAdapterGlowSquid(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterGlowSquid ma = new ModelAdapterGlowSquid(this.getEntityType(), "glow_squid_baby", ModelLayers.GLOW_SQUID_BABY);
      ma.setBaby(true);
      ma.setAlias(this.getName());
      return ma;
   }

   @Override
   protected AgeableMobRenderer makeAgeableRenderer(Context context) {
      return new GlowSquidRenderer(context, new SquidModel(bakeModelLayer(ModelLayers.GLOW_SQUID)), new SquidModel(bakeModelLayer(ModelLayers.GLOW_SQUID_BABY)));
   }
}
