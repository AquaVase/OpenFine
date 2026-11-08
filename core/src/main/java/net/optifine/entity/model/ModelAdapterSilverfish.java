package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.silverfish.SilverfishModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.SilverfishRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterSilverfish extends ModelAdapterLiving {
   public ModelAdapterSilverfish() {
      super(EntityTypes.SILVERFISH, "silverfish", ModelLayers.SILVERFISH);
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return new SilverfishModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      Map<String, String> map = new LinkedHashMap<>();

      for (int i = 0; i < 7; i++) {
         map.put("body" + (i + 1), "segment" + i);
      }

      for (int i = 0; i < 3; i++) {
         map.put("wing" + (i + 1), "layer" + i);
      }

      map.put("root", "root");
      return map;
   }

   @Override
   protected LivingEntityRenderer makeLivingRenderer(Context context) {
      return new SilverfishRenderer(context);
   }
}
