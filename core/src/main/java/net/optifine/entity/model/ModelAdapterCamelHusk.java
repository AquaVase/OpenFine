package net.optifine.entity.model;

import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.camel.AdultCamelModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.CamelHuskRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterCamelHusk extends ModelAdapterLiving {
    public ModelAdapterCamelHusk() {
        super(EntityTypes.CAMEL_HUSK, "camel_husk", ModelLayers.CAMEL);
    }

    protected ModelAdapterCamelHusk(EntityType entityType, String name, ModelLayerLocation modelLayer) {
        super(entityType, name, modelLayer);
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new AdultCamelModel(root);
    }

    @Override
    protected Map<String, String> makeMapParts() {
        return ModelAdapterCamel.makeMapPartsStatic();
    }

    @Override
    protected LivingEntityRenderer makeLivingRenderer(Context context) {
        return new CamelHuskRenderer(context);
    }
}
