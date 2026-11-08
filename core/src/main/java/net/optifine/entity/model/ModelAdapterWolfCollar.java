package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.wolf.WolfModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.layers.WolfCollarLayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterWolfCollar extends ModelAdapterWolf {
   public ModelAdapterWolfCollar() {
      this(EntityTypes.WOLF, "wolf_collar", ModelLayers.WOLF);
      this.setAlias("wolf");
   }

   protected ModelAdapterWolfCollar(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterAgeable ma = new ModelAdapterWolfCollar(this.getEntityType(), "wolf_baby_collar", ModelLayers.WOLF_BABY);
      ma.setBaby(true);
      ma.setAliases(this.getName(), "wolf");
      return ma;
   }

   @Override
   protected void modifyAgeableRenderer(AgeableMobRenderer renderer, Model model) {
      WolfModel wolfModel = (WolfModel)model;

      for (WolfCollarLayer layer : (java.lang.Iterable<WolfCollarLayer>)(java.lang.Iterable<?>)(this.getRenderLayers(renderer, WolfCollarLayer.class))) {
         if (this.isBaby()) {
            layer.babyModel = wolfModel;
         } else {
            layer.adultModel = wolfModel;
         }
      }
   }
}
