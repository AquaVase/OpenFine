package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterMinecartCommandBlock extends ModelAdapterMinecart {
    public ModelAdapterMinecartCommandBlock() {
        super(EntityTypes.COMMAND_BLOCK_MINECART, "command_block_minecart", ModelLayers.COMMAND_BLOCK_MINECART);
        this.setAlias("minecart");
    }
}
