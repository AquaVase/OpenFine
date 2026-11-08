package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.level.block.entity.BlockEntityTypes;

public class ModelAdapterTrappedChestRight extends ModelAdapterChestDoubleRight {
    public ModelAdapterTrappedChestRight() {
        super(BlockEntityTypes.TRAPPED_CHEST, "trapped_chest_right", ModelLayers.DOUBLE_CHEST_RIGHT);
    }
}
