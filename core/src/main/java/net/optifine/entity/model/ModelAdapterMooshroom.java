package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.cow.CowModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.MushroomCowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterMooshroom extends ModelAdapterQuadruped {
   public ModelAdapterMooshroom() {
      this(EntityTypes.MOOSHROOM, "mooshroom", ModelLayers.MOOSHROOM);
   }

   protected ModelAdapterMooshroom(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterMooshroom ma = new ModelAdapterMooshroom(this.getEntityType(), "mooshroom_baby", ModelLayers.MOOSHROOM_BABY);
      ma.setBaby(true);
      ma.setAlias(this.getName());
      return ma;
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return new CowModel(root);
   }

   @Override
   protected AgeableMobRenderer makeAgeableRenderer(Context context) {
      return new MushroomCowRenderer(context);
   }
}
