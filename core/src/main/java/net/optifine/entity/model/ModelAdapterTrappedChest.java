package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.level.block.entity.BlockEntityTypes;

public class ModelAdapterTrappedChest extends ModelAdapterChest {
    public ModelAdapterTrappedChest() {
        super(BlockEntityTypes.TRAPPED_CHEST, "trapped_chest", ModelLayers.CHEST);
    }
}
