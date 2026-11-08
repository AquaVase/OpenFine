package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.fox.AdultFoxModel;
import net.minecraft.client.model.animal.fox.BabyFoxModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.FoxRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterFox extends ModelAdapterAgeable {
   public ModelAdapterFox() {
      super(EntityTypes.FOX, "fox", ModelLayers.FOX);
   }

   protected ModelAdapterFox(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterFox ma = new ModelAdapterFox(this.getEntityType(), "fox_baby", ModelLayers.FOX_BABY);
      ma.setBaby(true);
      ma.setAlias(this.getName());
      return ma;
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return this.isBaby() ? new BabyFoxModel(root) : new AdultFoxModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      new LinkedHashMap();
      Map<String, String> map = new LinkedHashMap();
      map.put("head", "head");
      map.put("body", "body");
      map.put("leg1", "right_hind_leg");
      map.put("leg2", "left_hind_leg");
      map.put("leg3", "right_front_leg");
      map.put("leg4", "left_front_leg");
      map.put("tail", "tail");
      map.put("root", "root");
      return map;
   }

   @Override
   protected AgeableMobRenderer makeAgeableRenderer(Context context) {
      return new FoxRenderer(context);
   }
}
