package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.HorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterHorse extends ModelAdapterAgeable {
    public ModelAdapterHorse() {
        super(EntityTypes.HORSE, "horse", ModelLayers.HORSE);
    }

    protected ModelAdapterHorse(EntityType type, String name, ModelLayerLocation modelLayer) {
        super(type, name, modelLayer);
    }

    @Override
    public ModelAdapter makeBaby() {
        ModelAdapterHorse ma = new ModelAdapterHorse(this.getEntityType(), "horse_baby", ModelLayers.HORSE_BABY);
        ma.setBaby(true);
        return ma;
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new HorseModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("body", "body");
        map.put("neck", "head_parts");
        map.put("back_right_leg", "right_hind_leg");
        map.put("back_left_leg", "left_hind_leg");
        map.put("front_right_leg", "right_front_leg");
        map.put("front_left_leg", "left_front_leg");
        map.put("tail", "tail");
        map.put("head", "head");
        map.put("mane", "mane");
        map.put("mouth", "upper_mouth");
        map.put("left_ear", "left_ear");
        map.put("right_ear", "right_ear");
        map.put("root", "root");
        return map;
    }

    @Override
    protected AgeableMobRenderer makeAgeableRenderer(Context context) {
        return new HorseRenderer(context);
    }
}
