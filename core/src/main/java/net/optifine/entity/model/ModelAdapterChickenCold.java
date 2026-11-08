package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterChickenCold extends ModelAdapterChicken {
    public ModelAdapterChickenCold() {
        super(EntityTypes.CHICKEN, "cold_chicken", ModelLayers.COLD_CHICKEN, ChickenType.COLD);
    }

    protected ModelAdapterChickenCold(EntityType entityType, String name, ModelLayerLocation modelLayer, ChickenType type) {
        super(entityType, name, modelLayer, type);
    }

    @Override
    public ModelAdapter makeBaby() {
        ModelAdapterChickenCold ma = new ModelAdapterChickenCold(this.getEntityType(), "cold_chicken_baby", ModelLayers.CHICKEN_BABY, ChickenType.COLD);
        ma.setBaby(true);
        ma.setAlias(this.getName());
        return ma;
    }
}
