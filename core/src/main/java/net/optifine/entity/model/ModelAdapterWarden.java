package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.warden.WardenModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.WardenRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterWarden extends ModelAdapterLiving {
    public ModelAdapterWarden() {
        super(EntityTypes.WARDEN, "warden", ModelLayers.WARDEN);
    }

    protected ModelAdapterWarden(EntityType type, String name, ModelLayerLocation modelLayer) {
        super(type, name, modelLayer);
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new WardenModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("body", "bone");
        map.put("torso", "body");
        map.put("head", "head");
        map.put("right_leg", "right_leg");
        map.put("left_leg", "left_leg");
        map.put("right_arm", "right_arm");
        map.put("left_arm", "left_arm");
        map.put("right_tendril", "right_tendril");
        map.put("left_tendril", "left_tendril");
        map.put("right_ribcage", "right_ribcage");
        map.put("left_ribcage", "left_ribcage");
        map.put("root", "root");
        return map;
    }

    @Override
    protected LivingEntityRenderer makeLivingRenderer(Context context) {
        return new WardenRenderer(context);
    }
}
