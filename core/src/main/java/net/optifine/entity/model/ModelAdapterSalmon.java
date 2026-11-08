package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.fish.SalmonModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.SalmonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorField;

public class ModelAdapterSalmon extends ModelAdapterLiving {
   public ModelAdapterSalmon() {
      this(EntityTypes.SALMON, "salmon", ModelLayers.SALMON);
   }

   protected ModelAdapterSalmon(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return new SalmonModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      Map<String, String> map = new LinkedHashMap<>();
      map.put("body_front", "body_front");
      map.put("body_back", "body_back");
      map.put("head", "head");
      map.put("fin_back_1", "top_front_fin");
      map.put("fin_back_2", "top_back_fin");
      map.put("tail", "back_fin");
      map.put("fin_right", "right_fin");
      map.put("fin_left", "left_fin");
      map.put("root", "root");
      return map;
   }

   @Override
   protected LivingEntityRenderer makeLivingRenderer(Context context) {
      return new SalmonRenderer(context);
   }

   @Override
   protected void modifyLivingRenderer(LivingEntityRenderer renderer, Model modelBase) {
      ReflectorField modelField = this.getModelField();
      this.setModel(renderer, modelField, "SalmonRenderer.model", modelBase);
   }

   protected ReflectorField getModelField() {
      return Reflector.SalmonRenderer_modelMedium;
   }
}
