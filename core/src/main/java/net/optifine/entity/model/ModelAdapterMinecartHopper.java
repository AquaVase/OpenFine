package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterMinecartHopper extends ModelAdapterMinecart {
    public ModelAdapterMinecartHopper() {
        super(EntityTypes.HOPPER_MINECART, "hopper_minecart", ModelLayers.HOPPER_MINECART);
        this.setAlias("minecart");
    }
}
