package net.optifine.entity.model;

import java.util.Map;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterChestRaft extends ModelAdapterRaft {
    public ModelAdapterChestRaft() {
        super(EntityTypes.BAMBOO_CHEST_RAFT, "chest_raft", ModelLayers.BAMBOO_CHEST_RAFT);
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
