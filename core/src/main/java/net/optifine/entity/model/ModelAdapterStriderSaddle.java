package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.strider.StriderModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.resources.model.EquipmentClientInfo.LayerType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterStriderSaddle extends ModelAdapterStrider {
    public ModelAdapterStriderSaddle() {
        super(EntityTypes.STRIDER, "strider_saddle", ModelLayers.STRIDER_SADDLE);
    }

    private ModelAdapterStriderSaddle(EntityType entityType, String name, ModelLayerLocation modelLayer) {
        super(entityType, name, modelLayer);
    }

    @Override
    public ModelAdapter makeBaby() {
        return null;
    }

    @Override
    protected void modifyAgeableRenderer(AgeableMobRenderer renderer, Model modelBase) {
        StriderModel striderModel = (StriderModel)modelBase;

        for (SimpleEquipmentLayer layer : (java.lang.Iterable<SimpleEquipmentLayer>)(java.lang.Iterable<?>)(this.getRenderLayers(renderer, SimpleEquipmentLayer.class))) {
            if (layer.getLayerType() == LayerType.STRIDER_SADDLE) {
                if (this.isBaby()) {
                    layer.babyModel = striderModel;
                } else {
                    layer.adultModel = striderModel;
                }
            }
        }
    }
}
