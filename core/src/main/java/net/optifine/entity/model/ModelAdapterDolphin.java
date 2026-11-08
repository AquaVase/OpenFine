package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.dolphin.DolphinModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.DolphinRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterDolphin extends ModelAdapterAgeable {
    public ModelAdapterDolphin() {
        super(EntityTypes.DOLPHIN, "dolphin", ModelLayers.DOLPHIN);
    }

    protected ModelAdapterDolphin(EntityType entityType, String name, ModelLayerLocation modelLayer) {
        super(entityType, name, modelLayer);
    }

    @Override
    public ModelAdapter makeBaby() {
        ModelAdapterDolphin ma = new ModelAdapterDolphin(this.getEntityType(), "dolphin_baby", ModelLayers.DOLPHIN_BABY);
        ma.setBaby(true);
        ma.setAlias(this.getName());
        return ma;
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new DolphinModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("body", "body");
        map.put("back_fin", "back_fin");
        map.put("left_fin", "left_fin");
        map.put("right_fin", "right_fin");
        map.put("tail", "tail");
        map.put("tail_fin", "tail_fin");
        map.put("head", "head");
        map.put("root", "root");
        return map;
    }

    @Override
    protected AgeableMobRenderer makeAgeableRenderer(Context context) {
        return new DolphinRenderer(context);
    }
}
