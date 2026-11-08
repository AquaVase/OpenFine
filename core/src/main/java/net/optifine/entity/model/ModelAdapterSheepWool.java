package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.sheep.SheepFurModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.layers.SheepWoolLayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterSheepWool extends ModelAdapterSheep {
   public ModelAdapterSheepWool() {
      super(EntityTypes.SHEEP, "sheep_wool", ModelLayers.SHEEP_WOOL);
   }

   protected ModelAdapterSheepWool(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterSheepWool ma = new ModelAdapterSheepWool(this.getEntityType(), "sheep_baby_wool", ModelLayers.SHEEP_BABY_WOOL);
      ma.setBaby(true);
      ma.setAlias(this.getName());
      return ma;
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return new SheepFurModel(root);
   }

   @Override
   protected void modifyAgeableRenderer(AgeableMobRenderer renderer, Model modelBase) {
      SheepFurModel model = (SheepFurModel)modelBase;

      for (SheepWoolLayer layer : (java.lang.Iterable<SheepWoolLayer>)(java.lang.Iterable<?>)(this.getRenderLayers(renderer, SheepWoolLayer.class))) {
         if (this.isBaby()) {
            layer.babyModel = model;
         } else {
            layer.adultModel = model;
         }
      }
   }
}
