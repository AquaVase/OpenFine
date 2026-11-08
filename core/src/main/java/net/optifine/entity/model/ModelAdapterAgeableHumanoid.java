package net.optifine.entity.model;

import java.util.Map;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.world.entity.EntityType;

public abstract class ModelAdapterAgeableHumanoid extends ModelAdapterAgeable {
    protected ModelAdapterAgeableHumanoid(EntityType type, String name, ModelLayerLocation modelLayer) {
        super(type, name, modelLayer);
    }

    @Override
    public Map<String, String> makeMapParts() {
        return ModelAdapterHumanoid.makeStaticMapParts();
    }
}
