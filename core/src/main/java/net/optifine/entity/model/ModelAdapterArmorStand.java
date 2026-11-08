package net.optifine.entity.model;

import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.armorstand.ArmorStandModel;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorField;

public class ModelAdapterArmorStand extends ModelAdapterLiving implements IModelAdapterAgeable {
   private boolean baby;

   public ModelAdapterArmorStand() {
      super(EntityTypes.ARMOR_STAND, "armor_stand", ModelLayers.ARMOR_STAND);
   }

   protected ModelAdapterArmorStand(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterArmorStand ma = new ModelAdapterArmorStand(this.getEntityType(), "armor_stand_small", ModelLayers.ARMOR_STAND_SMALL);
      ma.setBaby(true);
      ma.setAlias(this.getName());
      return ma;
   }

   public boolean isBaby() {
      return this.baby;
   }

   public void setBaby(boolean baby) {
      this.baby = baby;
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return new ArmorStandModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      Map<String, String> map = ModelAdapterHumanoid.makeStaticMapParts();
      map.put("right", "right_body_stick");
      map.put("left", "left_body_stick");
      map.put("waist", "shoulder_stick");
      map.put("base", "base_plate");
      map.put("root", "root");
      return map;
   }

   @Override
   protected LivingEntityRenderer makeLivingRenderer(Context context) {
      return new ArmorStandRenderer(context);
   }

   @Override
   protected void modifyLivingRenderer(LivingEntityRenderer renderer, Model modelBase) {
      ReflectorField fieldModel = this.isBaby() ? Reflector.RenderArmorStand_smallModel : Reflector.RenderArmorStand_bigModel;
      this.setModel(renderer, fieldModel, "ArmorstandRenderer.model", modelBase);
      fieldModel.setValue(renderer, modelBase);
      renderer.model = ((ArmorStandModel)modelBase);
   }
}
