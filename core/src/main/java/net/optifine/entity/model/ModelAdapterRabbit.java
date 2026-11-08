package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.rabbit.AdultRabbitModel;
import net.minecraft.client.model.animal.rabbit.BabyRabbitModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.RabbitRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterRabbit extends ModelAdapterAgeable {
   public ModelAdapterRabbit() {
      super(EntityTypes.RABBIT, "rabbit", ModelLayers.RABBIT);
   }

   protected ModelAdapterRabbit(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterRabbit ma = new ModelAdapterRabbit(this.getEntityType(), "rabbit_baby", ModelLayers.RABBIT_BABY);
      ma.setBaby(true);
      ma.setAlias(this.getName());
      return ma;
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return this.isBaby() ? new BabyRabbitModel(root) : new AdultRabbitModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      Map<String, String> map = new LinkedHashMap<>();
      map.put("left_foot", "left_hind_foot");
      map.put("right_foot", "right_hind_foot");
      map.put("left_thigh", "left_haunch");
      map.put("right_thigh", "right_haunch");
      map.put("body", "body");
      map.put("left_arm", "left_front_leg");
      map.put("right_arm", "right_front_leg");
      map.put("head", "head");
      map.put("right_ear", "right_ear");
      map.put("left_ear", "left_ear");
      map.put("tail", "tail");
      map.put("nose", "nose");
      map.put("root", "root");
      return map;
   }

   @Override
   protected AgeableMobRenderer makeAgeableRenderer(Context context) {
      return new RabbitRenderer(context);
   }
}
