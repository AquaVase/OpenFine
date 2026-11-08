package net.optifine.entity.model;

import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.goat.GoatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.GoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterGoat extends ModelAdapterQuadruped {
   public ModelAdapterGoat() {
      super(EntityTypes.GOAT, "goat", ModelLayers.GOAT);
   }

   protected ModelAdapterGoat(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterGoat ma = new ModelAdapterGoat(this.getEntityType(), "goat_baby", ModelLayers.GOAT_BABY);
      ma.setBaby(true);
      ma.setAlias(this.getName());
      return ma;
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return new GoatModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      Map<String, String> map = super.makeMapParts();
      map.put("left_horn", "left_horn");
      map.put("right_horn", "right_horn");
      map.put("nose", "nose");
      return map;
   }

   @Override
   protected AgeableMobRenderer makeAgeableRenderer(Context context) {
      return new GoatRenderer(context);
   }
}
