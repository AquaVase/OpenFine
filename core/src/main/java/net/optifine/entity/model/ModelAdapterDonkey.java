package net.optifine.entity.model;

import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.equine.DonkeyModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.DonkeyRenderer;
import net.minecraft.client.renderer.entity.DonkeyRenderer.Type;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.resources.model.EquipmentClientInfo.LayerType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterDonkey extends ModelAdapterHorse {
   public ModelAdapterDonkey() {
      super(EntityTypes.DONKEY, "donkey", ModelLayers.DONKEY);
   }

   protected ModelAdapterDonkey(EntityType type, String name, ModelLayerLocation modelLayer) {
      super(type, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterDonkey ma = new ModelAdapterDonkey(this.getEntityType(), "donkey_baby", ModelLayers.DONKEY_BABY);
      ma.setBaby(true);
      return ma;
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return new DonkeyModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      Map<String, String> map = super.makeMapParts();
      map.put("left_chest", "left_chest");
      map.put("right_chest", "right_chest");
      return map;
   }

   @Override
   protected AgeableMobRenderer makeAgeableRenderer(Context context) {
      return new DonkeyRenderer(context, LayerType.DONKEY_SADDLE, ModelLayers.DONKEY_SADDLE, Type.DONKEY, Type.DONKEY_BABY);
   }
}
