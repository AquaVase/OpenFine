package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.shulker.ShulkerModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.ShulkerRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterShulker extends ModelAdapterLiving {
    public ModelAdapterShulker() {
        super(EntityTypes.SHULKER, "shulker", ModelLayers.SHULKER);
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new ShulkerModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("base", "base");
        map.put("lid", "lid");
        map.put("head", "head");
        map.put("root", "root");
        return map;
    }

    @Override
    protected LivingEntityRenderer makeLivingRenderer(Context context) {
        return new ShulkerRenderer(context);
    }
}
