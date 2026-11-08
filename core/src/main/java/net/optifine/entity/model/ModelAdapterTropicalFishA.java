package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.fish.TropicalFishSmallModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.TropicalFishRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.reflect.Reflector;

public class ModelAdapterTropicalFishA extends ModelAdapterLiving {
   public ModelAdapterTropicalFishA() {
      super(EntityTypes.TROPICAL_FISH, "tropical_fish_a", ModelLayers.TROPICAL_FISH_SMALL);
   }

   public ModelAdapterTropicalFishA(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return new TropicalFishSmallModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      Map<String, String> map = new LinkedHashMap<>();
      map.put("body", "body");
      map.put("tail", "tail");
      map.put("fin_right", "right_fin");
      map.put("fin_left", "left_fin");
      map.put("fin_top", "top_fin");
      map.put("root", "root");
      return map;
   }

   @Override
   protected LivingEntityRenderer makeLivingRenderer(Context context) {
      return new TropicalFishRenderer(context);
   }

   @Override
   protected void modifyLivingRenderer(LivingEntityRenderer renderer, Model modelBase) {
      this.setModel(renderer, Reflector.RenderTropicalFish_modelA, "TropicalFishRenderer.modelA", modelBase);
   }
}
