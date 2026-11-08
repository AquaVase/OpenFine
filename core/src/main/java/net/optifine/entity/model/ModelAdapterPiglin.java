package net.optifine.entity.model;

import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.piglin.AdultPiglinModel;
import net.minecraft.client.model.monster.piglin.BabyPiglinModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.PiglinRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterPiglin extends ModelAdapterAgeablePlayer {
   public ModelAdapterPiglin() {
      super(EntityTypes.PIGLIN, "piglin", ModelLayers.PIGLIN);
   }

   protected ModelAdapterPiglin(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterPiglin ma = new ModelAdapterPiglin(this.getEntityType(), "piglin_baby", ModelLayers.PIGLIN_BABY);
      ma.setBaby(true);
      ma.setAlias(this.getName());
      return ma;
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return this.isBaby() ? new BabyPiglinModel(root) : new AdultPiglinModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      Map<String, String> map = super.makeMapParts();
      map.put("left_ear", "left_ear");
      map.put("right_ear", "right_ear");
      return map;
   }

   @Override
   protected AgeableMobRenderer makeAgeableRenderer(Context context) {
      return new PiglinRenderer(context, ModelLayers.PIGLIN, ModelLayers.PIGLIN_BABY, ModelLayers.PIGLIN_ARMOR, ModelLayers.PIGLIN_BABY_ARMOR);
   }
}
