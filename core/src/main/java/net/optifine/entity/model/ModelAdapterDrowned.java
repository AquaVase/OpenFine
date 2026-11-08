package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.zombie.DrownedModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.DrownedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterDrowned extends ModelAdapterZombie {
   public ModelAdapterDrowned() {
      super(EntityTypes.DROWNED, "drowned", ModelLayers.DROWNED);
   }

   public ModelAdapterDrowned(EntityType type, String name, ModelLayerLocation modelLayer) {
      super(type, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterDrowned ma = new ModelAdapterDrowned(this.getEntityType(), "drowned_baby", ModelLayers.DROWNED_BABY);
      ma.setBaby(true);
      ma.setAlias(this.getName());
      return ma;
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return new DrownedModel(root);
   }

   @Override
   protected AgeableMobRenderer makeAgeableRenderer(Context context) {
      return new DrownedRenderer(context);
   }
}
