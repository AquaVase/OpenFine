package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.HuskRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterHusk extends ModelAdapterZombie {
    public ModelAdapterHusk() {
        super(EntityTypes.HUSK, "husk", ModelLayers.HUSK);
    }

    protected ModelAdapterHusk(EntityType entityType, String name, ModelLayerLocation modelLayer) {
        super(entityType, name, modelLayer);
    }

    @Override
    public ModelAdapter makeBaby() {
        ModelAdapterHusk ma = new ModelAdapterHusk(this.getEntityType(), "husk_baby", ModelLayers.HUSK_BABY);
        ma.setBaby(true);
        ma.setAlias(this.getName());
        return ma;
    }

    @Override
    protected AgeableMobRenderer makeAgeableRenderer(Context context) {
        return new HuskRenderer(context);
    }
}
