package net.optifine.entity.model;

import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EntityType;
import net.optifine.entity.model.anim.IRenderResolver;
import net.optifine.entity.model.anim.RenderResolverEntity;
import net.optifine.reflect.ReflectorField;

public abstract class ModelAdapterEntity extends ModelAdapter {
   private EntityType entityType;
   private ModelLayerLocation modelLayer;
   private Map<String, String> mapParts;

   public ModelAdapterEntity(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name);
      this.entityType = entityType;
      this.modelLayer = modelLayer;
      this.mapParts = this.makeMapParts();
   }

   public EntityType getEntityType() {
      return this.entityType;
   }

   public ModelLayerLocation getModelLayer() {
      return this.modelLayer;
   }

   @Override
   public final Model makeModel() {
      ModelPart root = bakeModelLayer(this.modelLayer);
      return this.makeModel(root);
   }

   protected abstract Model makeModel(ModelPart var1);

   @Override
   public final ModelPart getModelRenderer(Model model, String modelPart) {
      return this.getModelRenderer(model.root(), this.mapParts.get(modelPart));
   }

   @Override
   public final String[] getModelRendererNames() {
      return toArray(this.mapParts.keySet());
   }

   protected abstract Map<String, String> makeMapParts();

   @Override
   public IRenderResolver getRenderResolver() {
      return new RenderResolverEntity();
   }

   public Context getContext() {
      EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      return dispatcher.getContext();
   }

   public void setModel(EntityRenderer renderer, ReflectorField modelField, String fieldName, Model model) {
      if (!modelField.exists()) {
         throw new IllegalArgumentException("Field not found: " + fieldName);
      }

      modelField.setValue(renderer, model);
   }

   public void setModel(RenderLayer layer, ReflectorField modelField, String fieldName, Model model) {
      if (!modelField.exists()) {
         throw new IllegalArgumentException("Field not found: " + fieldName);
      }

      modelField.setValue(layer, model);
   }
}
