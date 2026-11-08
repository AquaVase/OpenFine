package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterPigCold extends ModelAdapterPig {
    public ModelAdapterPigCold() {
        super(EntityTypes.PIG, "cold_pig", ModelLayers.COLD_PIG, PigType.COLD);
    }

    protected ModelAdapterPigCold(EntityType entityType, String name, ModelLayerLocation modelLayer, PigType type) {
        super(entityType, name, modelLayer, type);
    }

    @Override
    public ModelAdapter makeBaby() {
        ModelAdapterPigCold ma = new ModelAdapterPigCold(this.getEntityType(), "cold_pig_baby", ModelLayers.PIG_BABY, PigType.COLD);
        ma.setBaby(true);
        ma.setAlias(this.getName());
        return ma;
    }
}
