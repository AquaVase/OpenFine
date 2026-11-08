package net.optifine.entity.model;

import java.util.Map;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.world.entity.EntityType;

public abstract class ModelAdapterAgeablePlayer extends ModelAdapterAgeableHumanoid {
    protected ModelAdapterAgeablePlayer(EntityType type, String name, ModelLayerLocation modelLayer) {
        super(type, name, modelLayer);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = super.makeMapParts();
        map.put("left_sleeve", "left_sleeve");
        map.put("right_sleeve", "right_sleeve");
        map.put("left_pants", "left_pants");
        map.put("right_pants", "right_pants");
        map.put("jacket", "jacket");
        return map;
    }
}
