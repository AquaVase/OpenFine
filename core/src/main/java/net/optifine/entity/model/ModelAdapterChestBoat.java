package net.optifine.entity.model;

import java.util.Map;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterChestBoat extends ModelAdapterBoat {
    public ModelAdapterChestBoat(EntityType entityType, String prefix, ModelLayerLocation modelLayer) {
        super(entityType, prefix + "_chest_boat", modelLayer, new String[]{"chest_boat"});
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = super.makeMapParts();
        map.put("chest_base", "chest_bottom");
        map.put("chest_lid", "chest_lid");
        map.put("chest_knob", "chest_lock");
        return map;
    }
}
