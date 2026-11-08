package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterSpider extends ModelAdapterLiving {
    public ModelAdapterSpider() {
        super(EntityTypes.SPIDER, "spider", ModelLayers.SPIDER);
    }

    protected ModelAdapterSpider(EntityType type, String name, ModelLayerLocation modelLayer) {
        super(type, name, modelLayer);
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new SpiderModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("head", "head");
        map.put("neck", "body0");
        map.put("body", "body1");
        map.put("leg1", "right_hind_leg");
        map.put("leg2", "left_hind_leg");
        map.put("leg3", "right_middle_hind_leg");
        map.put("leg4", "left_middle_hind_leg");
        map.put("leg5", "right_middle_front_leg");
        map.put("leg6", "left_middle_front_leg");
        map.put("leg7", "right_front_leg");
        map.put("leg8", "left_front_leg");
        map.put("root", "root");
        return map;
    }

    @Override
    protected LivingEntityRenderer makeLivingRenderer(Context context) {
        return new SpiderRenderer(context);
    }
}
