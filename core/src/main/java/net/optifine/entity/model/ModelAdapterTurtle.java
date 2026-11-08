package net.optifine.entity.model;

import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.turtle.AdultTurtleModel;
import net.minecraft.client.model.animal.turtle.BabyTurtleModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.TurtleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterTurtle extends ModelAdapterQuadruped {
   public ModelAdapterTurtle() {
      super(EntityTypes.TURTLE, "turtle", ModelLayers.TURTLE);
   }

   protected ModelAdapterTurtle(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterTurtle ma = new ModelAdapterTurtle(this.getEntityType(), "turtle_baby", ModelLayers.TURTLE_BABY);
      ma.setBaby(true);
      ma.setAlias(this.getName());
      return ma;
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return this.isBaby() ? new BabyTurtleModel(root) : new AdultTurtleModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      Map<String, String> map = super.makeMapParts();
      map.put("body2", "egg_belly");
      return map;
   }

   @Override
   protected AgeableMobRenderer makeAgeableRenderer(Context context) {
      return new TurtleRenderer(context);
   }
}
