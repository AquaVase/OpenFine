package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.llama.LlamaModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.LlamaRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterLlama extends ModelAdapterAgeable {
   public ModelAdapterLlama() {
      this(EntityTypes.LLAMA, "llama", ModelLayers.LLAMA);
   }

   protected ModelAdapterLlama(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterLlama ma = new ModelAdapterLlama(this.getEntityType(), "llama_baby", ModelLayers.LLAMA_BABY);
      ma.setBaby(true);
      ma.setAlias(this.getName());
      return ma;
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return new LlamaModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      Map<String, String> map = new LinkedHashMap<>();
      map.put("head", "head");
      map.put("body", "body");
      map.put("leg1", "right_hind_leg");
      map.put("leg2", "left_hind_leg");
      map.put("leg3", "right_front_leg");
      map.put("leg4", "left_front_leg");
      map.put("chest_right", "right_chest");
      map.put("chest_left", "left_chest");
      map.put("root", "root");
      return map;
   }

   @Override
   protected AgeableMobRenderer makeAgeableRenderer(Context context) {
      return new LlamaRenderer(context, ModelLayers.LLAMA, ModelLayers.LLAMA_BABY);
   }
}
