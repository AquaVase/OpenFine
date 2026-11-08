package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.warden.WardenModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LivingEntityEmissiveLayer;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterWardenLayer extends ModelAdapterWarden {
    public ModelAdapterWardenLayer(ModelLayerLocation modelLayerIn) {
        super(EntityTypes.WARDEN, "warden_" + modelLayerIn.layer(), modelLayerIn);
        this.setAlias("warden");
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new WardenModel(root);
    }

    @Override
    protected void modifyLivingRenderer(LivingEntityRenderer renderer, Model modelBase) {
        for (LivingEntityEmissiveLayer layer : (java.lang.Iterable<LivingEntityEmissiveLayer>)(java.lang.Iterable<?>)(this.getRenderLayers(renderer, LivingEntityEmissiveLayer.class))) {
            WardenModel layerModel = (WardenModel)Reflector.LivingEntityEmissiveLayer_model.getValue(layer);
            if (layerModel == null) {
                Config.warn("Warden layer model not found");
            } else {
                ModelLayerLocation modelLoc = layerModel.root().getModelLayerLocation();
                if (modelLoc == null) {
                    Config.warn("Warden layer model location not found");
                } else if (modelLoc == this.getModelLayer()) {
                    Reflector.LivingEntityEmissiveLayer_model.setValue(layer, modelBase);
                }
            }
        }
    }
}
