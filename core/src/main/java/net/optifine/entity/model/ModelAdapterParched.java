package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.ParchedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterParched extends ModelAdapterSkeleton {
    public ModelAdapterParched() {
        super(EntityTypes.PARCHED, "parched", ModelLayers.PARCHED);
    }

    public ModelAdapterParched(EntityType type, String name, ModelLayerLocation modelLayer) {
        super(type, name, modelLayer);
    }

    @Override
    protected AgeableMobRenderer makeAgeableRenderer(Context context) {
        return new ParchedRenderer(context);
    }
}
