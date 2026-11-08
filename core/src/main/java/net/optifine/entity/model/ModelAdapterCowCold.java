package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.cow.CowVariant.ModelType;

public class ModelAdapterCowCold extends ModelAdapterCow {
   public ModelAdapterCowCold() {
      super(EntityTypes.COW, "cold_cow", ModelLayers.COLD_COW, ModelType.COLD);
   }

   protected ModelAdapterCowCold(EntityType entityType, String name, ModelLayerLocation modelLayer, ModelType type) {
      super(entityType, name, modelLayer, type);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterCowCold ma = new ModelAdapterCowCold(this.getEntityType(), "cold_cow_baby", ModelLayers.COLD_COW_BABY, ModelType.COLD);
      ma.setBaby(true);
      ma.setAlias(this.getName());
      return ma;
   }
}
