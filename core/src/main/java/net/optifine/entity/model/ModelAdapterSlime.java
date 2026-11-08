package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterSlime extends ModelAdapterLiving {
   public ModelAdapterSlime() {
      super(EntityTypes.SLIME, "slime", ModelLayers.SLIME);
   }

   public ModelAdapterSlime(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return new SlimeModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      Map<String, String> map = new LinkedHashMap<>();
      map.put("body", "cube");
      map.put("left_eye", "left_eye");
      map.put("right_eye", "right_eye");
      map.put("mouth", "mouth");
      map.put("root", "root");
      return map;
   }

   @Override
   protected LivingEntityRenderer makeLivingRenderer(Context context) {
      return new SlimeRenderer(context);
   }
}
