package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterChickenWarm extends ModelAdapterChicken {
    public ModelAdapterChickenWarm() {
        super(EntityTypes.CHICKEN, "warm_chicken", ModelLayers.CHICKEN, ChickenType.WARM);
    }

    protected ModelAdapterChickenWarm(EntityType entityType, String name, ModelLayerLocation modelLayer, ChickenType type) {
        super(entityType, name, modelLayer, type);
    }

    @Override
    public ModelAdapter makeBaby() {
        ModelAdapterChickenWarm ma = new ModelAdapterChickenWarm(this.getEntityType(), "warm_chicken_baby", ModelLayers.CHICKEN_BABY, ChickenType.WARM);
        ma.setBaby(true);
        ma.setAlias(this.getName());
        return ma;
    }
}
