package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.ghast.HappyGhastHarnessModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.resources.model.EquipmentClientInfo.LayerType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterHappyGhastHarness extends ModelAdapterHappyGhast {
   public ModelAdapterHappyGhastHarness() {
      super(EntityTypes.HAPPY_GHAST, "happy_ghast_harness", ModelLayers.HAPPY_GHAST_HARNESS);
   }

   protected ModelAdapterHappyGhastHarness(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      return null;
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return new HappyGhastHarnessModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      Map<String, String> map = new LinkedHashMap<>();
      map.put("body", "harness");
      map.put("goggles", "goggles");
      map.put("root", "root");
      return map;
   }

   @Override
   protected void modifyAgeableRenderer(AgeableMobRenderer renderer, Model modelBase) {
      HappyGhastHarnessModel model = (HappyGhastHarnessModel)modelBase;

      for (SimpleEquipmentLayer layer : (java.lang.Iterable<SimpleEquipmentLayer>)(java.lang.Iterable<?>)(this.getRenderLayers(renderer, SimpleEquipmentLayer.class))) {
         if (layer.getLayerType() == LayerType.HAPPY_GHAST_BODY) {
            if (this.isBaby()) {
               layer.babyModel = model;
            } else {
               layer.adultModel = model;
            }
         }
      }
   }
}
