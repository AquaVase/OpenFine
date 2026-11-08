package net.optifine.entity.model;

import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.camel.CamelModel;
import net.minecraft.client.model.animal.camel.CamelSaddleModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.resources.model.EquipmentClientInfo.LayerType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterCamelSaddle extends ModelAdapterCamel {
   public ModelAdapterCamelSaddle() {
      super(EntityTypes.CAMEL, "camel_saddle", ModelLayers.CAMEL_SADDLE);
   }

   protected ModelAdapterCamelSaddle(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      return null;
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return new CamelSaddleModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      Map<String, String> map = super.makeMapParts();
      return appendSaddleParts(map);
   }

   public static Map<String, String> appendSaddleParts(Map<String, String> map) {
      map.put("saddle", "saddle");
      map.put("reins", "reins");
      map.put("bridle", "bridle");
      return map;
   }

   @Override
   protected void modifyAgeableRenderer(AgeableMobRenderer renderer, Model modelBase) {
      CamelModel camelModel = (CamelModel)modelBase;

      for (SimpleEquipmentLayer layer : (java.lang.Iterable<SimpleEquipmentLayer>)(java.lang.Iterable<?>)(this.getRenderLayers(renderer, SimpleEquipmentLayer.class))) {
         if (layer.getLayerType() == LayerType.CAMEL_SADDLE) {
            if (this.isBaby()) {
               layer.babyModel = camelModel;
            } else {
               layer.adultModel = camelModel;
            }
         }
      }
   }
}
