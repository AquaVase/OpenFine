package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.cow.CowVariant.ModelType;

public class ModelAdapterCowWarm extends ModelAdapterCow {
    public ModelAdapterCowWarm() {
        super(EntityTypes.COW, "warm_cow", ModelLayers.WARM_COW, ModelType.WARM);
    }

    protected ModelAdapterCowWarm(EntityType entityType, String name, ModelLayerLocation modelLayer, ModelType type) {
        super(entityType, name, modelLayer, type);
    }

    @Override
    public ModelAdapter makeBaby() {
        ModelAdapterCowWarm ma = new ModelAdapterCowWarm(this.getEntityType(), "warm_cow_baby", ModelLayers.WARM_COW_BABY, ModelType.WARM);
        ma.setBaby(true);
        ma.setAlias(this.getName());
        return ma;
    }
}
