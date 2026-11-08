package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.sniffer.SnifferModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.SnifferRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterSniffer extends ModelAdapterAgeable {
   public ModelAdapterSniffer() {
      super(EntityTypes.SNIFFER, "sniffer", ModelLayers.SNIFFER);
   }

   protected ModelAdapterSniffer(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterSniffer ma = new ModelAdapterSniffer(this.getEntityType(), "sniffer_baby", ModelLayers.SNIFFER_BABY);
      ma.setBaby(true);
      ma.setAlias(this.getName());
      return ma;
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return new SnifferModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      Map<String, String> map = new LinkedHashMap<>();
      map.put("body", "body");
      map.put("back_left_leg", "left_hind_leg");
      map.put("back_right_leg", "right_hind_leg");
      map.put("middle_left_leg", "left_mid_leg");
      map.put("middle_right_leg", "right_mid_leg");
      map.put("front_left_leg", "left_front_leg");
      map.put("front_right_leg", "right_front_leg");
      map.put("head", "head");
      map.put("left_ear", "left_ear");
      map.put("right_ear", "right_ear");
      map.put("nose", "nose");
      map.put("lower_beak", "lower_beak");
      map.put("root", "root");
      return map;
   }

   @Override
   protected AgeableMobRenderer makeAgeableRenderer(Context context) {
      return new SnifferRenderer(context);
   }
}
