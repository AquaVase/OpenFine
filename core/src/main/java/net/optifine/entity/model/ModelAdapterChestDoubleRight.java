package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterChestDoubleRight extends ModelAdapterChest {
    private BlockEntityType blockEntityType;
    private ModelLayerLocation modelLayer;

    public ModelAdapterChestDoubleRight() {
        this(BlockEntityTypes.CHEST, "chest_right", ModelLayers.DOUBLE_CHEST_RIGHT);
    }

    protected ModelAdapterChestDoubleRight(BlockEntityType blockEntityType, String name, ModelLayerLocation modelLayer) {
        super(blockEntityType, name, modelLayer);
        this.blockEntityType = blockEntityType;
        this.modelLayer = modelLayer;
    }

    @Override
    public IEntityRenderer makeEntityRender(Model modelBase, RendererCache rendererCache, int index) {
        BlockEntityRenderer renderer = rendererCache.get(this.blockEntityType, index, () -> new ChestRenderer(this.getBlockEntityContext()));
        if (!Reflector.TileEntityChestRenderer_doubleRightModel.exists()) {
            Config.warn("Field not found: ChestRenderer.doubleRightModel");
            return null;
        } else {
            Reflector.TileEntityChestRenderer_doubleRightModel.setValue(renderer, modelBase);
            return renderer;
        }
    }
}
