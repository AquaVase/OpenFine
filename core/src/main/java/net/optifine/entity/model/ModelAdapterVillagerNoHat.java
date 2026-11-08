package net.optifine.entity.model;

import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterVillagerNoHat extends ModelAdapterVillager {
   public ModelAdapterVillagerNoHat() {
      super(EntityTypes.VILLAGER, "villager_no_hat", ModelLayers.VILLAGER_NO_HAT);
      this.setAlias("villager");
   }

   protected ModelAdapterVillagerNoHat(EntityType type, String name, ModelLayerLocation modelLayer) {
      super(type, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterVillagerNoHat mb = new ModelAdapterVillagerNoHat(this.getEntityType(), "villager_baby_no_hat", ModelLayers.VILLAGER_BABY_NO_HAT);
      mb.setBaby(true);
      mb.setAlias(this.getName());
      return mb;
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return new VillagerModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      Map<String, String> map = super.makeMapParts();
      map.remove("headwear");
      map.remove("headwear2");
      return map;
   }

   @Override
   public String[] getIgnoredPartNames() {
      return new String[]{"headwear", "headwear2"};
   }

   @Override
   public void finalizeModel(Model model) {
      ModelPart head = this.getModelRenderer(model, "head");
      if (head != null) {
         head.removeCubesDeep();
      }
   }

   @Override
   protected void modifyAgeableRenderer(AgeableMobRenderer renderer, Model modelBase) {
      for (VillagerProfessionLayer layer : (java.lang.Iterable<VillagerProfessionLayer>)(java.lang.Iterable<?>)(this.getRenderLayers(renderer, VillagerProfessionLayer.class))) {
         if (!Reflector.VillagerProfessionLayer_adultModel.exists()) {
            Config.warn("Field not found: VillagerProfessionLayer.adultModel");
         }

         if (!Reflector.VillagerProfessionLayer_babyModel.exists()) {
            Config.warn("Field not found: VillagerProfessionLayer.babyModel");
         }

         if (this.isBaby()) {
            Reflector.VillagerProfessionLayer_babyModel.setValue(layer, modelBase);
         } else {
            Reflector.VillagerProfessionLayer_adultModel.setValue(layer, modelBase);
         }
      }
   }
}
