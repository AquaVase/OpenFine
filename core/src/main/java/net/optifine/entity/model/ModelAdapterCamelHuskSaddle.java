package net.optifine.entity.model;

import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.camel.CamelModel;
import net.minecraft.client.model.animal.camel.CamelSaddleModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.resources.model.EquipmentClientInfo.LayerType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterCamelHuskSaddle extends ModelAdapterCamelHusk {
    public ModelAdapterCamelHuskSaddle() {
        super(EntityTypes.CAMEL_HUSK, "camel_husk_saddle", ModelLayers.CAMEL_HUSK_SADDLE);
    }

    protected ModelAdapterCamelHuskSaddle(EntityType entityType, String name, ModelLayerLocation modelLayer) {
        super(entityType, name, modelLayer);
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new CamelSaddleModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = super.makeMapParts();
        return ModelAdapterCamelSaddle.appendSaddleParts(map);
    }

    @Override
    protected void modifyLivingRenderer(LivingEntityRenderer renderer, Model modelBase) {
        CamelModel camelModel = (CamelModel)modelBase;

        for (SimpleEquipmentLayer layer : (java.lang.Iterable<SimpleEquipmentLayer>)(java.lang.Iterable<?>)(this.getRenderLayers(renderer, SimpleEquipmentLayer.class))) {
            if (layer.getLayerType() == LayerType.CAMEL_HUSK_SADDLE) {
                layer.adultModel = camelModel;
            }
        }
    }
}
