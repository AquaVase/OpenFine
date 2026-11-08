package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.creaking.CreakingModel;
import net.minecraft.client.renderer.entity.CreakingRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterCreaking extends ModelAdapterLiving {
   public ModelAdapterCreaking() {
      super(EntityTypes.CREAKING, "creaking", ModelLayers.CREAKING);
   }

   protected ModelAdapterCreaking(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return new CreakingModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      Map<String, String> map = new LinkedHashMap<>();
      map.put("upper_body", "upper_body");
      map.put("head", "head");
      map.put("body", "body");
      map.put("right_arm", "right_arm");
      map.put("left_arm", "left_arm");
      map.put("right_leg", "right_leg");
      map.put("left_leg", "left_leg");
      map.put("root", "root");
      return map;
   }

   @Override
   protected LivingEntityRenderer makeLivingRenderer(Context context) {
      return new CreakingRenderer(context);
   }
}
