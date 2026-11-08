package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.projectile.WindChargeModel;
import net.minecraft.client.renderer.entity.WindChargeRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.reflect.Reflector;

public class ModelAdapterWindCharge extends ModelAdapterEntity {
    public ModelAdapterWindCharge() {
        super(EntityTypes.WIND_CHARGE, "wind_charge", ModelLayers.WIND_CHARGE);
    }

    public ModelAdapterWindCharge(EntityType entityType, String name, ModelLayerLocation modelLayer) {
        super(entityType, name, modelLayer);
    }

    @Override
    public Model makeModel(ModelPart root) {
        return new WindChargeModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("wind", "wind");
        map.put("charge", "wind_charge");
        map.put("root", "root");
        return map;
    }

    @Override
    public IEntityRenderer makeEntityRender(Model modelBase, RendererCache rendererCache, int index) {
        WindChargeRenderer render = new WindChargeRenderer(this.getContext());
        this.setModel(render, Reflector.RenderWindCharge_model, "WindChargeRenderer.model", modelBase);
        return render;
    }
}
