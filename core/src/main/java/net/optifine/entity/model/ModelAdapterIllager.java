package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.world.entity.EntityType;

public abstract class ModelAdapterIllager extends ModelAdapterLiving {
    public ModelAdapterIllager(EntityType type, String name, ModelLayerLocation modelLayer) {
        super(type, name, modelLayer);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("head", "head");
        map.put("hat", "hat");
        map.put("body", "body");
        map.put("arms", "arms");
        map.put("right_leg", "right_leg");
        map.put("left_leg", "left_leg");
        map.put("nose", "nose");
        map.put("right_arm", "right_arm");
        map.put("left_arm", "left_arm");
        map.put("root", "root");
        return map;
    }
}
