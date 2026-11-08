package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterShulkerBox extends ModelAdapterBlockEntity {
    private static Map<String, String> mapParts = makeMapParts();

    public ModelAdapterShulkerBox() {
        super(BlockEntityTypes.SHULKER_BOX, "shulker_box");
    }

    @Override
    public Model makeModel() {
        return new ShulkerBoxRenderer.ShulkerBoxModel(bakeModelLayer(ModelLayers.SHULKER_BOX));
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
        map.put("base", "base");
        map.put("lid", "lid");
        map.put("root", "root");
        return map;
    }

    @Override
    public IEntityRenderer makeEntityRender(Model modelBase, RendererCache rendererCache, int index) {
        BlockEntityRenderer renderer = rendererCache.get(BlockEntityTypes.SHULKER_BOX, index, () -> new ShulkerBoxRenderer(this.getContext()));
        if (!(renderer instanceof ShulkerBoxRenderer)) {
            return null;
        } else if (!Reflector.TileEntityShulkerBoxRenderer_model.exists()) {
            Config.warn("Field not found: ShulkerBoxRenderer.model");
            return null;
        } else {
            Reflector.setFieldValue(renderer, Reflector.TileEntityShulkerBoxRenderer_model, modelBase);
            return renderer;
        }
    }
}
