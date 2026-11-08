package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.frog.FrogModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.FrogRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterFrog extends ModelAdapterLiving {
    public ModelAdapterFrog() {
        super(EntityTypes.FROG, "frog", ModelLayers.FROG);
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new FrogModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("body", "body");
        map.put("head", "head");
        map.put("eyes", "eyes");
        map.put("tongue", "tongue");
        map.put("left_arm", "left_arm");
        map.put("right_arm", "right_arm");
        map.put("left_leg", "left_leg");
        map.put("right_leg", "right_leg");
        map.put("croaking_body", "croaking_body");
        map.put("root", "root");
        return map;
    }

    @Override
    protected LivingEntityRenderer makeLivingRenderer(Context context) {
        return new FrogRenderer(context);
    }
}
