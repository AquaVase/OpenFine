package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.nautilus.NautilusModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.NautilusRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterNautilus extends ModelAdapterAgeable {
    public ModelAdapterNautilus() {
        super(EntityTypes.NAUTILUS, "nautilus", ModelLayers.NAUTILUS);
    }

    protected ModelAdapterNautilus(EntityType entityType, String name, ModelLayerLocation modelLayer) {
        super(entityType, name, modelLayer);
    }

    @Override
    public ModelAdapter makeBaby() {
        ModelAdapterNautilus ma = new ModelAdapterNautilus(this.getEntityType(), "nautilus_baby", ModelLayers.NAUTILUS_BABY);
        ma.setBaby(true);
        return ma;
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new NautilusModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        return makeStaticMapParts();
    }

    public static Map<String, String> makeStaticMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("body", "body");
        map.put("shell", "shell");
        map.put("upper_mouth", "upper_mouth");
        map.put("inner_mouth", "inner_mouth");
        map.put("lower_mouth", "lower_mouth");
        map.put("root", "root");
        return map;
    }

    @Override
    protected AgeableMobRenderer makeAgeableRenderer(Context context) {
        return new NautilusRenderer(context);
    }
}
