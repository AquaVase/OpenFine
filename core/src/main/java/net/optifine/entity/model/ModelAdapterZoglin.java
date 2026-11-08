package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.ZoglinRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterZoglin extends ModelAdapterHoglin {
    public ModelAdapterZoglin() {
        this(EntityTypes.ZOGLIN, "zoglin", ModelLayers.ZOGLIN);
    }

    protected ModelAdapterZoglin(EntityType entityType, String name, ModelLayerLocation modelLayer) {
        super(entityType, name, modelLayer);
    }

    @Override
    public ModelAdapter makeBaby() {
        ModelAdapterAgeable ma = new ModelAdapterZoglin(this.getEntityType(), "zoglin_baby", ModelLayers.ZOGLIN_BABY);
        ma.setBaby(true);
        ma.setAlias(this.getName());
        return ma;
    }

    @Override
    protected AgeableMobRenderer makeAgeableRenderer(Context context) {
        return new ZoglinRenderer(context);
    }
}
