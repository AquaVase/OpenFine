package net.optifine.entity.model;

import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.zombie.ZombieVillagerModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterZombieVillagerNoHat extends ModelAdapterZombieVillager {
   public ModelAdapterZombieVillagerNoHat() {
      super(EntityTypes.ZOMBIE_VILLAGER, "zombie_villager_no_hat", ModelLayers.ZOMBIE_VILLAGER_NO_HAT);
      this.setAlias("zombie_villager");
   }

   protected ModelAdapterZombieVillagerNoHat(EntityType type, String name, ModelLayerLocation modelLayer) {
      super(type, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterZombieVillagerNoHat ma = new ModelAdapterZombieVillagerNoHat(
         this.getEntityType(), "zombie_villager_baby_no_hat", ModelLayers.ZOMBIE_VILLAGER_BABY_NO_HAT
      );
      ma.setBaby(true);
      ma.setAlias(this.getName());
      return ma;
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return new ZombieVillagerModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      Map<String, String> map = super.makeMapParts();
      map.remove("headwear");
      return map;
   }

   @Override
   public String[] getIgnoredPartNames() {
      return new String[]{"headwear"};
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
