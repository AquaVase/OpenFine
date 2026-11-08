package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.resources.model.EquipmentClientInfo.LayerType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterSkeletonHorseArmor extends ModelAdapterSkeletonHorse {
   public ModelAdapterSkeletonHorseArmor() {
      super(EntityTypes.SKELETON_HORSE, "skeleton_horse_armor", ModelLayers.UNDEAD_HORSE_ARMOR);
   }

   protected ModelAdapterSkeletonHorseArmor(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      return null;
   }

   @Override
   protected void modifyAgeableRenderer(AgeableMobRenderer renderer, Model modelBase) {
      HorseModel horseModel = (HorseModel)modelBase;

      for (SimpleEquipmentLayer layer : (java.lang.Iterable<SimpleEquipmentLayer>)(java.lang.Iterable<?>)(this.getRenderLayers(renderer, SimpleEquipmentLayer.class))) {
         if (layer.getLayerType() == LayerType.HORSE_BODY) {
            if (this.isBaby()) {
               layer.babyModel = horseModel;
            } else {
               layer.adultModel = horseModel;
            }
         }
      }
   }
}
