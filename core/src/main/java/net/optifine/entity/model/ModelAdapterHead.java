package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.skull.SkullModel;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.world.level.block.SkullBlock.Type;
import net.minecraft.world.level.block.SkullBlock.Types;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.optifine.Config;

public class ModelAdapterHead extends ModelAdapterBlockEntity {
   private ModelLayerLocation modelLayer;
   private Types skullBlockType;
   private static Map<String, String> mapParts = makeMapParts();

   public ModelAdapterHead(String name, ModelLayerLocation modelLayer, Types skullBlockType) {
      super(BlockEntityTypes.SKULL, name);
      this.modelLayer = modelLayer;
      this.skullBlockType = skullBlockType;
   }

   @Override
   public Model makeModel() {
      return new SkullModel(bakeModelLayer(this.modelLayer));
   }

   @Override
   public ModelPart getModelRenderer(Model model, String modelPart) {
      return this.getModelRenderer(model.root(), mapParts.get(modelPart));
   }

   @Override
   public String[] getModelRendererNames() {
      return toArray(mapParts.keySet());
   }

   private static Map<String, String> makeMapParts() {
      Map<String, String> map = new LinkedHashMap<>();
      map.put("head", "head");
      map.put("root", "root");
      return map;
   }

   @Override
   public IEntityRenderer makeEntityRender(Model modelBase, RendererCache rendererCache, int index) {
      BlockEntityRenderer renderer = rendererCache.get(BlockEntityTypes.SKULL, index, () -> new SkullBlockRenderer(this.getContext()));
      if (!(renderer instanceof SkullBlockRenderer)) {
         return null;
      } else {
         Map<Type, SkullModelBase> models = RendererUtils.getSkullModelMap();
         if (models == null) {
            Config.warn("Field not found: SkullBlockRenderer.globalModels");
            return null;
         } else {
            models.put(this.skullBlockType, (SkullModelBase)modelBase);
            return renderer;
         }
      }
   }
}
