package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.fish.PufferfishBigModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.PufferfishRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.reflect.Reflector;

public class ModelAdapterPufferFishBig extends ModelAdapterLiving {
   public ModelAdapterPufferFishBig() {
      super(EntityTypes.PUFFERFISH, "puffer_fish_big", ModelLayers.PUFFERFISH_BIG);
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return new PufferfishBigModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      Map<String, String> map = new LinkedHashMap<>();
      map.put("body", "body");
      map.put("fin_right", "right_blue_fin");
      map.put("fin_left", "left_blue_fin");
      map.put("spikes_front_top", "top_front_fin");
      map.put("spikes_middle_top", "top_middle_fin");
      map.put("spikes_back_top", "top_back_fin");
      map.put("spikes_front_right", "right_front_fin");
      map.put("spikes_front_left", "left_front_fin");
      map.put("spikes_front_bottom", "bottom_front_fin");
      map.put("spikes_middle_bottom", "bottom_middle_fin");
      map.put("spikes_back_bottom", "bottom_back_fin");
      map.put("spikes_back_right", "right_back_fin");
      map.put("spikes_back_left", "left_back_fin");
      map.put("root", "root");
      return map;
   }

   @Override
   protected LivingEntityRenderer makeLivingRenderer(Context context) {
      return new PufferfishRenderer(context);
   }

   @Override
   protected void modifyLivingRenderer(LivingEntityRenderer renderer, Model modelBase) {
      this.setModel(renderer, Reflector.RenderPufferfish_modelBig, "PufferfishRenderer.bigModel", modelBase);
   }
}
