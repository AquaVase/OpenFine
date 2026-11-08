package net.optifine.entity.model;

import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.equine.EquineSaddleModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.resources.model.EquipmentClientInfo.LayerType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterDonkeySaddle extends ModelAdapterDonkey {
    public ModelAdapterDonkeySaddle() {
        super(EntityTypes.DONKEY, "donkey_saddle", ModelLayers.DONKEY_SADDLE);
    }

    protected ModelAdapterDonkeySaddle(EntityType entityType, String name, ModelLayerLocation modelLayer) {
        super(entityType, name, modelLayer);
    }

    @Override
    public ModelAdapter makeBaby() {
        return null;
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new EquineSaddleModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = super.makeMapParts();
        return ModelAdapterHorseSaddle.appendSaddleParts(map);
    }

    @Override
    protected void modifyAgeableRenderer(AgeableMobRenderer renderer, Model modelBase) {
        EquineSaddleModel saddleModel = (EquineSaddleModel)modelBase;

        for (SimpleEquipmentLayer layer : (java.lang.Iterable<SimpleEquipmentLayer>)(java.lang.Iterable<?>)(this.getRenderLayers(renderer, SimpleEquipmentLayer.class))) {
            if (layer.getLayerType() == LayerType.DONKEY_SADDLE) {
                if (this.isBaby()) {
                    layer.babyModel = saddleModel;
                } else {
                    layer.adultModel = saddleModel;
                }
            }
        }
    }
}
