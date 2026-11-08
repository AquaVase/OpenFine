package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.polarbear.PolarBearModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.PolarBearRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterPolarBear extends ModelAdapterQuadruped {
   public ModelAdapterPolarBear() {
      super(EntityTypes.POLAR_BEAR, "polar_bear", ModelLayers.POLAR_BEAR);
   }

   protected ModelAdapterPolarBear(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterPolarBear ma = new ModelAdapterPolarBear(this.getEntityType(), "polar_bear_baby", ModelLayers.POLAR_BEAR_BABY);
      ma.setBaby(true);
      ma.setAlias(this.getName());
      return ma;
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return new PolarBearModel(root);
   }

   @Override
   protected AgeableMobRenderer makeAgeableRenderer(Context context) {
      return new PolarBearRenderer(context);
   }
}
