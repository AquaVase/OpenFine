package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.projectile.ArrowModel;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterArrow extends ModelAdapterEntity {
    public ModelAdapterArrow() {
        super(EntityTypes.ARROW, "arrow", ModelLayers.ARROW);
    }

    protected ModelAdapterArrow(EntityType entityType, String name, ModelLayerLocation modelLayer) {
        super(entityType, name, modelLayer);
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new ArrowModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("back", "back");
        map.put("cross_1", "cross_1");
        map.put("cross_2", "cross_2");
        map.put("root", "root");
        return map;
    }

    @Override
    public IEntityRenderer makeEntityRender(Model modelBase, RendererCache rendererCache, int index) {
        TippableArrowRenderer render = new TippableArrowRenderer(this.getContext());
        if (!Reflector.ArrowRenderer_model.exists()) {
            Config.warn("Field not found: ArrowRenderer.model");
            return null;
        } else {
            Reflector.setFieldValue(render, Reflector.ArrowRenderer_model, modelBase);
            return render;
        }
    }
}
