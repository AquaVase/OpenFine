package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.panda.PandaModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.PandaRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterPanda extends ModelAdapterQuadruped {
   public ModelAdapterPanda() {
      super(EntityTypes.PANDA, "panda", ModelLayers.PANDA);
   }

   protected ModelAdapterPanda(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterPanda ma = new ModelAdapterPanda(this.getEntityType(), "panda_baby", ModelLayers.PANDA_BABY);
      ma.setBaby(true);
      ma.setAlias(this.getName());
      return ma;
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return new PandaModel(root);
   }

   @Override
   protected AgeableMobRenderer makeAgeableRenderer(Context context) {
      return new PandaRenderer(context);
   }
}
