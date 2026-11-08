package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterMinecartMobSpawner extends ModelAdapterMinecart {
    public ModelAdapterMinecartMobSpawner() {
        super(EntityTypes.SPAWNER_MINECART, "spawner_minecart", ModelLayers.SPAWNER_MINECART);
        this.setAlias("minecart");
    }
}
