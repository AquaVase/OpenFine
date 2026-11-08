package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.level.block.entity.BlockEntityTypes;

public class ModelAdapterTrappedChestLeft extends ModelAdapterChestDoubleLeft {
    public ModelAdapterTrappedChestLeft() {
        super(BlockEntityTypes.TRAPPED_CHEST, "trapped_chest_left", ModelLayers.DOUBLE_CHEST_LEFT);
    }
}
