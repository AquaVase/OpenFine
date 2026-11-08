package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.golem.IronGolemModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterIronGolem extends ModelAdapterLiving {
    public ModelAdapterIronGolem() {
        super(EntityTypes.IRON_GOLEM, "iron_golem", ModelLayers.IRON_GOLEM);
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new IronGolemModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("head", "head");
        map.put("body", "body");
        map.put("right_arm", "right_arm");
        map.put("left_arm", "left_arm");
        map.put("left_leg", "left_leg");
        map.put("right_leg", "right_leg");
        map.put("root", "root");
        return map;
    }

    @Override
    protected LivingEntityRenderer makeLivingRenderer(Context context) {
        return new IronGolemRenderer(context);
    }
}
