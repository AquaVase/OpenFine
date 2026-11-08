package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.UndeadHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.UndeadHorseRenderer.Type;
import net.minecraft.client.resources.model.EquipmentClientInfo.LayerType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterZombieHorse extends ModelAdapterHorse {
   public ModelAdapterZombieHorse() {
      this(EntityTypes.ZOMBIE_HORSE, "zombie_horse", ModelLayers.ZOMBIE_HORSE);
   }

   protected ModelAdapterZombieHorse(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterZombieHorse ma = new ModelAdapterZombieHorse(this.getEntityType(), "zombie_horse_baby", ModelLayers.ZOMBIE_HORSE_BABY);
      ma.setBaby(true);
      return ma;
   }

   @Override
   protected AgeableMobRenderer makeAgeableRenderer(Context context) {
      return new UndeadHorseRenderer(context, LayerType.ZOMBIE_HORSE_SADDLE, ModelLayers.ZOMBIE_HORSE_SADDLE, Type.ZOMBIE, Type.ZOMBIE_BABY);
   }
}
