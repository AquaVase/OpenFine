package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ambient.BatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.BatRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterBat extends ModelAdapterLiving {
   public ModelAdapterBat() {
      super(EntityTypes.BAT, "bat", ModelLayers.BAT);
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return new BatModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      Map<String, String> map = new LinkedHashMap<>();
      map.put("head", "head");
      map.put("body", "body");
      map.put("right_wing", "right_wing");
      map.put("left_wing", "left_wing");
      map.put("outer_right_wing", "right_wing_tip");
      map.put("outer_left_wing", "left_wing_tip");
      map.put("feet", "feet");
      map.put("root", "root");
      return map;
   }

   @Override
   protected LivingEntityRenderer makeLivingRenderer(Context context) {
      return new BatRenderer(context);
   }
}
