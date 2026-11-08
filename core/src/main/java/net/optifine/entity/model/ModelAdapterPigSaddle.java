package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.pig.PigModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.resources.model.EquipmentClientInfo.LayerType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterPigSaddle extends ModelAdapterPig {
    public ModelAdapterPigSaddle() {
        super(EntityTypes.PIG, "pig_saddle", ModelLayers.PIG_SADDLE, null);
    }

    protected ModelAdapterPigSaddle(EntityType entityType, String name, ModelLayerLocation modelLayer, PigType type) {
        super(entityType, name, modelLayer, type);
    }

    @Override
    public ModelAdapter makeBaby() {
        return null;
    }

    @Override
    protected void modifyLivingRenderer(LivingEntityRenderer renderer, Model model, Object type, boolean baby) {
        PigModel pigModel = (PigModel)model;

        for (SimpleEquipmentLayer layer : (java.lang.Iterable<SimpleEquipmentLayer>)(java.lang.Iterable<?>)(this.getRenderLayers(renderer, SimpleEquipmentLayer.class))) {
            if (layer.getLayerType() == LayerType.PIG_SADDLE) {
                if (this.isBaby()) {
                    layer.babyModel = pigModel;
                } else {
                    layer.adultModel = pigModel;
                }
            }
        }
    }
}
