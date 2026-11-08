package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterVillager extends ModelAdapterAgeable {
    public ModelAdapterVillager() {
        super(EntityTypes.VILLAGER, "villager", ModelLayers.VILLAGER);
    }

    protected ModelAdapterVillager(EntityType type, String name, ModelLayerLocation modelLayer) {
        super(type, name, modelLayer);
    }

    @Override
    public ModelAdapter makeBaby() {
        ModelAdapterVillager mb = new ModelAdapterVillager(this.getEntityType(), "villager_baby", ModelLayers.VILLAGER_BABY);
        mb.setBaby(true);
        mb.setAlias(this.getName());
        return mb;
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new VillagerModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        return makeStaticMapParts();
    }

    public static Map<String, String> makeStaticMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("head", "head");
        map.put("headwear", "hat");
        map.put("headwear2", "hat_rim");
        map.put("body", "body");
        map.put("bodywear", "jacket");
        map.put("arms", "arms");
        map.put("right_leg", "right_leg");
        map.put("left_leg", "left_leg");
        map.put("nose", "nose");
        map.put("root", "root");
        return map;
    }

    @Override
    protected AgeableMobRenderer makeAgeableRenderer(Context context) {
        return new VillagerRenderer(context);
    }
}
