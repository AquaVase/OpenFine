package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterTraderLlama extends ModelAdapterLlama {
    public ModelAdapterTraderLlama() {
        this(EntityTypes.TRADER_LLAMA, "trader_llama", ModelLayers.TRADER_LLAMA);
    }

    protected ModelAdapterTraderLlama(EntityType entityType, String name, ModelLayerLocation modelLayer) {
        super(entityType, name, modelLayer);
    }

    @Override
    public ModelAdapter makeBaby() {
        ModelAdapterTraderLlama ma = new ModelAdapterTraderLlama(this.getEntityType(), "trader_llama_baby", ModelLayers.TRADER_LLAMA_BABY);
        ma.setBaby(true);
        ma.setAlias(this.getName());
        return ma;
    }
}
