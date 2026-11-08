package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.axolotl.AdultAxolotlModel;
import net.minecraft.client.model.animal.axolotl.BabyAxolotlModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.AxolotlRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterAxolotl extends ModelAdapterAgeable {
   public ModelAdapterAxolotl() {
      super(EntityTypes.AXOLOTL, "axolotl", ModelLayers.AXOLOTL);
   }

   protected ModelAdapterAxolotl(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      ModelAdapterAxolotl ma = new ModelAdapterAxolotl(this.getEntityType(), "axolotl_baby", ModelLayers.AXOLOTL_BABY);
      ma.setBaby(true);
      ma.setAlias(this.getName());
      return ma;
   }

   @Override
   protected Model makeModel(ModelPart root) {
      return this.isBaby() ? new BabyAxolotlModel(root) : new AdultAxolotlModel(root);
   }

   @Override
   public Map<String, String> makeMapParts() {
      Map<String, String> map = new LinkedHashMap<>();
      map.put("tail", "tail");
      map.put("leg2", "left_hind_leg");
      map.put("leg1", "right_hind_leg");
      map.put("leg4", "left_front_leg");
      map.put("leg3", "right_front_leg");
      map.put("body", "body");
      map.put("head", "head");
      map.put("top_gills", "top_gills");
      map.put("left_gills", "left_gills");
      map.put("right_gills", "right_gills");
      map.put("root", "root");
      return map;
   }

   @Override
   protected AgeableMobRenderer makeAgeableRenderer(Context context) {
      return new AxolotlRenderer(context);
   }
}
