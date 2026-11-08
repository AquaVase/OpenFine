package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.sheep.SheepFurModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.layers.SheepWoolUndercoatLayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterSheepWoolUndercoat extends ModelAdapterSheep {
   public ModelAdapterSheepWoolUndercoat() {
      super(EntityTypes.SHEEP, "sheep_wool_undercoat", ModelLayers.SHEEP_WOOL_UNDERCOAT);
      this.setAlias("sheep");
   }

   protected ModelAdapterSheepWoolUndercoat(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      return null;
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return new SheepFurModel(root);
   }

   @Override
   protected void modifyAgeableRenderer(AgeableMobRenderer renderer, Model modelBase) {
      SheepFurModel model = (SheepFurModel)modelBase;

      for (SheepWoolUndercoatLayer layer : (java.lang.Iterable<SheepWoolUndercoatLayer>)(java.lang.Iterable<?>)(this.getRenderLayers(renderer, SheepWoolUndercoatLayer.class))) {
         if (!Reflector.SheepWoolUndercoatLayer_model.exists()) {
            Config.warn("Field not found: SheepWoolUndercoatLayer.model");
         }

         Reflector.SheepWoolUndercoatLayer_model.setValue(layer, model);
      }
   }
}
