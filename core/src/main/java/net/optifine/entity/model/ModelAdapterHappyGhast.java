package net.optifine.entity.model;

import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.ghast.HappyGhastModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.HappyGhastRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterHappyGhast extends ModelAdapterAgeable {
   public ModelAdapterHappyGhast() {
      super(EntityTypes.HAPPY_GHAST, "happy_ghast", ModelLayers.HAPPY_GHAST);
   }

   protected ModelAdapterHappyGhast(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterHappyGhast ma = new ModelAdapterHappyGhast.Baby(this.getEntityType(), "happy_ghast_baby", ModelLayers.HAPPY_GHAST_BABY);
      ma.setBaby(true);
      return ma;
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return new HappyGhastModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      return ModelAdapterGhast.makeStaticMapParts();
   }

   @Override
   protected AgeableMobRenderer makeAgeableRenderer(Context context) {
      return new HappyGhastRenderer(context);
   }

   static class Baby extends ModelAdapterHappyGhast {
      public Baby(EntityType entityType, String name, ModelLayerLocation modelLayer) {
         super(entityType, name, modelLayer);
      }

      @Override
      public Map<String, String> makeMapParts() {
         Map<String, String> map = super.makeMapParts();
         map.put("inner_body", "inner_body");
         return map;
      }
   }
}
