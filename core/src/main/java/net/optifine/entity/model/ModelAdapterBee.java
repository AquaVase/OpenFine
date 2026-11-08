package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.bee.AdultBeeModel;
import net.minecraft.client.model.animal.bee.BabyBeeModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.BeeRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterBee extends ModelAdapterAgeable {
    public ModelAdapterBee() {
        super(EntityTypes.BEE, "bee", ModelLayers.BEE);
    }

    protected ModelAdapterBee(EntityType entityType, String name, ModelLayerLocation modelLayer) {
        super(entityType, name, modelLayer);
    }

    @Override
    public ModelAdapter makeBaby() {
        ModelAdapterBee ma = new ModelAdapterBee(this.getEntityType(), "bee_baby", ModelLayers.BEE_BABY);
        ma.setBaby(true);
        ma.setAlias(this.getName());
        return ma;
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return this.isBaby() ? new BabyBeeModel(root) : new AdultBeeModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("body", "bone");
        map.put("torso", "body");
        map.put("right_wing", "right_wing");
        map.put("left_wing", "left_wing");
        map.put("front_legs", "front_legs");
        map.put("middle_legs", "middle_legs");
        map.put("back_legs", "back_legs");
        map.put("stinger", "stinger");
        map.put("left_antenna", "left_antenna");
        map.put("right_antenna", "right_antenna");
        map.put("root", "root");
        return map;
    }

    @Override
    protected AgeableMobRenderer makeAgeableRenderer(Context context) {
        return new BeeRenderer(context);
    }
}
