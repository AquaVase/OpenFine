package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.DonkeyRenderer;
import net.minecraft.client.renderer.entity.DonkeyRenderer.Type;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.resources.model.EquipmentClientInfo.LayerType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterMule extends ModelAdapterDonkey {
   public ModelAdapterMule() {
      super(EntityTypes.MULE, "mule", ModelLayers.MULE);
   }

   protected ModelAdapterMule(EntityType type, String name, ModelLayerLocation modelLayer) {
      super(type, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterMule ma = new ModelAdapterMule(this.getEntityType(), "mule_baby", ModelLayers.MULE_BABY);
      ma.setBaby(true);
      return ma;
   }

   @Override
   protected AgeableMobRenderer makeAgeableRenderer(Context context) {
      return new DonkeyRenderer(context, LayerType.DONKEY_SADDLE, ModelLayers.DONKEY_SADDLE, Type.DONKEY, Type.DONKEY_BABY);
   }
}
