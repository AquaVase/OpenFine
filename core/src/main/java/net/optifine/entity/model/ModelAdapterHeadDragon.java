package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.skull.DragonHeadModel;
import net.minecraft.world.level.block.SkullBlock.Types;

public class ModelAdapterHeadDragon extends ModelAdapterHead {
   private static Map<String, String> mapParts = makeMapParts();

   public ModelAdapterHeadDragon() {
      super("head_dragon", null, Types.DRAGON);
   }

   @Override
   public Model makeModel() {
      return new DragonHeadModel(bakeModelLayer(ModelLayers.DRAGON_SKULL));
   }

   @Override
   public ModelPart getModelRenderer(Model model, String modelPart) {
      return this.getModelRenderer(model.root(), mapParts.get(modelPart), () -> super.getModelRenderer(model, modelPart));
   }

   @Override
   public String[] getModelRendererNames() {
      return toArray(super.getModelRendererNames(), mapParts.keySet());
   }

   private static Map<String, String> makeMapParts() {
      Map<String, String> map = new LinkedHashMap<>();
      map.put("jaw", "jaw");
      return map;
   }
}
