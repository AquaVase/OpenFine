package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.world.entity.EntityType;

public abstract class ModelAdapterQuadruped extends ModelAdapterAgeable {
    public ModelAdapterQuadruped(EntityType type, String name, ModelLayerLocation modelLayer) {
        super(type, name, modelLayer);
    }

    @Override
    public Map<String, String> makeMapParts() {
        return makeMapPartsStatic();
    }

    public static Map<String, String> makeMapPartsStatic() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("head", "head");
        map.put("body", "body");
        map.put("leg1", "right_hind_leg");
        map.put("leg2", "left_hind_leg");
        map.put("leg3", "right_front_leg");
        map.put("leg4", "left_front_leg");
        map.put("root", "root");
        return map;
    }
}
