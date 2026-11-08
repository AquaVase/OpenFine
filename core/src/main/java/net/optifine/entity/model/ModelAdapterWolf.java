package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.wolf.AdultWolfModel;
import net.minecraft.client.model.animal.wolf.BabyWolfModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterWolf extends ModelAdapterAgeable {
    public ModelAdapterWolf() {
        this(EntityTypes.WOLF, "wolf", ModelLayers.WOLF);
    }

    protected ModelAdapterWolf(EntityType entityType, String name, ModelLayerLocation modelLayer) {
        super(entityType, name, modelLayer);
    }

    @Override
    public ModelAdapter makeBaby() {
        ModelAdapterAgeable ma = new ModelAdapterWolf(EntityTypes.WOLF, "wolf_baby", ModelLayers.WOLF_BABY);
        ma.setBaby(true);
        ma.setAlias("wolf");
        return ma;
    }

    @Override
    public Model makeModel(ModelPart root) {
        return this.isBaby() ? new BabyWolfModel(root) : new AdultWolfModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("head", "head");
        map.put("body", "body");
        map.put("leg1", "right_hind_leg");
        map.put("leg2", "left_hind_leg");
        map.put("leg3", "right_front_leg");
        map.put("leg4", "left_front_leg");
        map.put("tail", "tail");
        map.put("mane", "upper_body");
        map.put("root", "root");
        return map;
    }

    @Override
    public AgeableMobRenderer makeAgeableRenderer(Context context) {
        return new WolfRenderer(context);
    }
}
