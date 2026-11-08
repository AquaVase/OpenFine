package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.parrot.ParrotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterParrot extends ModelAdapterLiving {
    public ModelAdapterParrot() {
        super(EntityTypes.PARROT, "parrot", ModelLayers.PARROT);
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new ParrotModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        return makeStaticMapParts();
    }

    public static Map<String, String> makeStaticMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("body", "body");
        map.put("tail", "tail");
        map.put("left_wing", "left_wing");
        map.put("right_wing", "right_wing");
        map.put("head", "head");
        map.put("left_leg", "left_leg");
        map.put("right_leg", "right_leg");
        map.put("root", "root");
        return map;
    }

    @Override
    protected LivingEntityRenderer makeLivingRenderer(Context context) {
        return new ParrotRenderer(context);
    }
}
