package net.optifine.entity.model;

import java.util.Map;
import net.minecraft.client.model.AdultAndBabyModelPair;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.cow.CowModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.cow.CowVariant.ModelType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterCow extends ModelAdapterMultiLiving {
   public ModelAdapterCow() {
      super(EntityTypes.COW, "cow", ModelLayers.COW, ModelType.NORMAL);
   }

   protected ModelAdapterCow(EntityType entityType, String name, ModelLayerLocation modelLayer, ModelType type) {
      super(entityType, name, modelLayer, type);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterCow ma = new ModelAdapterCow(this.getEntityType(), "cow_baby", ModelLayers.COW_BABY, ModelType.NORMAL);
      ma.setBaby(true);
      ma.setAlias(this.getName());
      return ma;
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return new CowModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      return ModelAdapterQuadruped.makeMapPartsStatic();
   }

   @Override
   protected LivingEntityRenderer makeLivingRenderer(Context context) {
      return new CowRenderer(context);
   }

   @Override
   protected void modifyLivingRenderer(LivingEntityRenderer renderer, Model modelBase, Object type, boolean baby) {
      CowRenderer cowRenderer = (CowRenderer)renderer;
      CowModel cowModel = (CowModel)modelBase;
      ModelType cowType = (ModelType)type;
      Map<ModelType, AdultAndBabyModelPair<CowModel>> models = (Map<ModelType, AdultAndBabyModelPair<CowModel>>)Reflector.CowRenderer_models
         .getValue(cowRenderer);
      if (models == null) {
         Config.warn("CowRenderer.models not found.");
      } else {
         AdultAndBabyModelPair<CowModel> pair = models.get(cowType);
         if (pair == null) {
            Config.warn("CowRenderer.models.pair not found for type: " + cowType);
         } else {
            AdultAndBabyModelPair<CowModel> pair2;
            if (baby) {
               pair2 = new AdultAndBabyModelPair<>(pair.adultModel(), cowModel);
            } else {
               pair2 = new AdultAndBabyModelPair<>(cowModel, pair.babyModel());
            }

            models.put(cowType, pair2);
         }
      }
   }
}
