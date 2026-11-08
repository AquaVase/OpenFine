package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.creeper.CreeperModel;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterCreeper extends ModelAdapterLiving {
   public ModelAdapterCreeper() {
      super(EntityTypes.CREEPER, "creeper", ModelLayers.CREEPER);
   }

   public ModelAdapterCreeper(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return new CreeperModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      Map<String, String> map = new LinkedHashMap<>();
      map.put("head", "head");
      map.put("body", "body");
      map.put("leg1", "right_hind_leg");
      map.put("leg2", "left_hind_leg");
      map.put("leg3", "right_front_leg");
      map.put("leg4", "left_front_leg");
      map.put("root", "root");
      return map;
   }

   @Override
   protected LivingEntityRenderer makeLivingRenderer(Context context) {
      return new CreeperRenderer(context);
   }
}
