package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.layers.LlamaDecorLayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorField;

public class ModelAdapterTraderLlamaDecor extends ModelAdapterTraderLlama {
    public ModelAdapterTraderLlamaDecor() {
        super(EntityTypes.TRADER_LLAMA, "trader_llama_decor", ModelLayers.LLAMA_DECOR);
    }

    protected ModelAdapterTraderLlamaDecor(EntityType entityType, String name, ModelLayerLocation modelLayer) {
        super(entityType, name, modelLayer);
    }

    @Override
    public ModelAdapter makeBaby() {
        ModelAdapterTraderLlamaDecor ma = new ModelAdapterTraderLlamaDecor(this.getEntityType(), "trader_llama_baby_decor", ModelLayers.LLAMA_BABY_DECOR);
        ma.setBaby(true);
        ma.setAlias(this.getName());
        return ma;
    }

    @Override
    protected void modifyAgeableRenderer(AgeableMobRenderer renderer, Model modelBase) {
        ReflectorField field = this.isBaby() ? Reflector.LlamaDecorLayer_babyModel : Reflector.LlamaDecorLayer_adultModel;
        this.setLayerModel(renderer, LlamaDecorLayer.class, field, "LlamaDecorLayer_model", modelBase);
    }
}
