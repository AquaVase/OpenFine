package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.strider.AdultStriderModel;
import net.minecraft.client.model.monster.strider.BabyStriderModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.StriderRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterStrider extends ModelAdapterAgeable {
   public ModelAdapterStrider() {
      super(EntityTypes.STRIDER, "strider", ModelLayers.STRIDER);
   }

   protected ModelAdapterStrider(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterStrider mb = new ModelAdapterStrider(this.getEntityType(), "strider_baby", ModelLayers.STRIDER_BABY);
      mb.setBaby(true);
      mb.setAlias(this.getName());
      return mb;
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return this.isBaby() ? new BabyStriderModel(root) : new AdultStriderModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      Map<String, String> map = new LinkedHashMap<>();
      map.put("right_leg", "right_leg");
      map.put("left_leg", "left_leg");
      map.put("body", "body");
      map.put("hair_right_bottom", "right_bottom_bristle");
      map.put("hair_right_middle", "right_middle_bristle");
      map.put("hair_right_top", "right_top_bristle");
      map.put("hair_left_top", "left_top_bristle");
      map.put("hair_left_middle", "left_middle_bristle");
      map.put("hair_left_bottom", "left_bottom_bristle");
      map.put("root", "root");
      return map;
   }

   @Override
   protected AgeableMobRenderer makeAgeableRenderer(Context context) {
      return new StriderRenderer(context);
   }
}
