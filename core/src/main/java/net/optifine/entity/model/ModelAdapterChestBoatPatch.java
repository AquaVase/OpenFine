package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterChestBoatPatch extends ModelAdapterBoatPatch {
    public ModelAdapterChestBoatPatch(EntityType entityType, String prefix, ModelLayerLocation modelLayer) {
        super(entityType, prefix + "_chest_boat_patch", modelLayer, new String[]{"chest_boat_patch"});
    }
}
