package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.StrayRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterStray extends ModelAdapterSkeleton {
   public ModelAdapterStray() {
      super(EntityTypes.STRAY, "stray", ModelLayers.STRAY);
   }

   public ModelAdapterStray(EntityType type, String name, ModelLayerLocation modelLayer) {
      super(type, name, modelLayer);
   }

   @Override
   protected AgeableMobRenderer makeAgeableRenderer(Context context) {
      return new StrayRenderer(context);
   }
}
