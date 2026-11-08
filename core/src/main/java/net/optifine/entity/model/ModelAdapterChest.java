package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterChest extends ModelAdapterBlockEntity {
    private BlockEntityType blockEntityType;
    private ModelLayerLocation modelLayer;
    private static Map<String, String> mapParts = makeMapParts();

    public ModelAdapterChest() {
        this(BlockEntityTypes.CHEST, "chest", ModelLayers.CHEST);
    }

    protected ModelAdapterChest(BlockEntityType blockEntityType, String name, ModelLayerLocation modelLayer) {
        super(blockEntityType, name);
        this.blockEntityType = blockEntityType;
        this.modelLayer = modelLayer;
    }

    @Override
    public Model makeModel() {
        return new ChestModel(bakeModelLayer(this.modelLayer));
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
        map.put("base", "bottom");
        map.put("lid", "lid");
        map.put("knob", "lock");
        map.put("root", "root");
        return map;
    }

    @Override
    public IEntityRenderer makeEntityRender(Model modelBase, RendererCache rendererCache, int index) {
        BlockEntityRenderer renderer = rendererCache.get(this.blockEntityType, index, () -> new ChestRenderer(this.getContext()));
        if (!Reflector.TileEntityChestRenderer_singleModel.exists()) {
            Config.warn("Field not found: ChestRenderer.singleModel");
            return null;
        } else {
            Reflector.TileEntityChestRenderer_singleModel.setValue(renderer, modelBase);
            return renderer;
        }
    }
}
