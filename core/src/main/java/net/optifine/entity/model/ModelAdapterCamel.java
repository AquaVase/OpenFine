package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.camel.AdultCamelModel;
import net.minecraft.client.model.animal.camel.BabyCamelModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.CamelRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterCamel extends ModelAdapterAgeable {
   public ModelAdapterCamel() {
      super(EntityTypes.CAMEL, "camel", ModelLayers.CAMEL);
   }

   protected ModelAdapterCamel(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterCamel ma = new ModelAdapterCamel(this.getEntityType(), "camel_baby", ModelLayers.CAMEL_BABY);
      ma.setBaby(true);
      ma.setAlias(this.getName());
      return ma;
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return this.isBaby() ? new BabyCamelModel(root) : new AdultCamelModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      return makeMapPartsStatic();
   }

   public static Map<String, String> makeMapPartsStatic() {
      Map<String, String> map = new LinkedHashMap<>();
      map.put("body", "body");
      map.put("hump", "hump");
      map.put("tail", "tail");
      map.put("head", "head");
      map.put("left_ear", "left_ear");
      map.put("right_ear", "right_ear");
      map.put("back_left_leg", "left_hind_leg");
      map.put("back_right_leg", "right_hind_leg");
      map.put("front_left_leg", "left_front_leg");
      map.put("front_right_leg", "right_front_leg");
      map.put("root", "root");
      return map;
   }

   @Override
   protected AgeableMobRenderer makeAgeableRenderer(Context context) {
      return new CamelRenderer(context);
   }
}
