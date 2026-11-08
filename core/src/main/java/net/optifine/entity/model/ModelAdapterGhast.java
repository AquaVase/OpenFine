package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.ghast.GhastModel;
import net.minecraft.client.renderer.entity.GhastRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterGhast extends ModelAdapterLiving {
    public ModelAdapterGhast() {
        super(EntityTypes.GHAST, "ghast", ModelLayers.GHAST);
    }

    protected ModelAdapterGhast(EntityType entityType, String name, ModelLayerLocation modelLayer) {
        super(entityType, name, modelLayer);
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new GhastModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        return makeStaticMapParts();
    }

    public static Map<String, String> makeStaticMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("body", "body");

        for (int i = 0; i < 9; i++) {
            map.put("tentacle" + (i + 1), "tentacle" + i);
        }

        map.put("root", "root");
        return map;
    }

    @Override
    protected LivingEntityRenderer makeLivingRenderer(Context context) {
        return new GhastRenderer(context);
    }
}
