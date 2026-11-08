package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.phantom.PhantomModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.PhantomRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterPhantom extends ModelAdapterLiving {
    public ModelAdapterPhantom() {
        super(EntityTypes.PHANTOM, "phantom", ModelLayers.PHANTOM);
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new PhantomModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("body", "body");
        map.put("head", "head");
        map.put("left_wing", "left_wing_base");
        map.put("left_wing_tip", "left_wing_tip");
        map.put("right_wing", "right_wing_base");
        map.put("right_wing_tip", "right_wing_tip");
        map.put("tail", "tail_base");
        map.put("tail2", "tail_tip");
        map.put("root", "root");
        return map;
    }

    @Override
    protected LivingEntityRenderer makeLivingRenderer(Context context) {
        return new PhantomRenderer(context);
    }
}
