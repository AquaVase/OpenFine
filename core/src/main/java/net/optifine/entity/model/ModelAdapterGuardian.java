package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.guardian.GuardianModel;
import net.minecraft.client.renderer.entity.GuardianRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterGuardian extends ModelAdapterLiving {
   public ModelAdapterGuardian() {
      super(EntityTypes.GUARDIAN, "guardian", ModelLayers.GUARDIAN);
   }

   public ModelAdapterGuardian(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return new GuardianModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      Map<String, String> map = new LinkedHashMap<>();
      map.put("body", "head");
      map.put("eye", "eye");

      for (int i = 0; i < 12; i++) {
         map.put("spine" + (i + 1), "spike" + i);
      }

      for (int i = 0; i < 3; i++) {
         map.put("tail" + (i + 1), "tail" + i);
      }

      map.put("root", "root");
      return map;
   }

   @Override
   protected LivingEntityRenderer makeLivingRenderer(Context context) {
      return new GuardianRenderer(context);
   }
}
