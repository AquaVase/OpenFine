package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterMinecartFurnace extends ModelAdapterMinecart {
    public ModelAdapterMinecartFurnace() {
        super(EntityTypes.FURNACE_MINECART, "furnace_minecart", ModelLayers.FURNACE_MINECART);
        this.setAlias("minecart");
    }
}
