package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.feline.AdultCatModel;
import net.minecraft.client.model.animal.feline.BabyCatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterCat extends ModelAdapterOcelot {
   public ModelAdapterCat() {
      super(EntityTypes.CAT, "cat", ModelLayers.CAT);
   }

   protected ModelAdapterCat(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterCat ma = new ModelAdapterCat(this.getEntityType(), "cat_baby", ModelLayers.CAT_BABY);
      ma.setBaby(true);
      ma.setAlias(this.getName());
      return ma;
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return this.isBaby() ? new BabyCatModel(root) : new AdultCatModel(root);
   }

   @Override
   protected AgeableMobRenderer makeAgeableRenderer(Context context) {
      return new CatRenderer(context);
   }
}
