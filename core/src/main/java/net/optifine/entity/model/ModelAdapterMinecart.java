package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.cart.MinecartModel;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.reflect.Reflector;

public class ModelAdapterMinecart extends ModelAdapterEntity {
    public ModelAdapterMinecart() {
        super(EntityTypes.MINECART, "minecart", ModelLayers.MINECART);
    }

    protected ModelAdapterMinecart(EntityType type, String name, ModelLayerLocation modelLayer) {
        super(type, name, modelLayer);
    }

    @Override
    public Model makeModel(ModelPart root) {
        return new MinecartModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("bottom", "bottom");
        map.put("back", "back");
        map.put("front", "front");
        map.put("right", "right");
        map.put("left", "left");
        map.put("root", "root");
        return map;
    }

    @Override
    public IEntityRenderer makeEntityRender(Model modelBase, RendererCache rendererCache, int index) {
        MinecartRenderer render = new MinecartRenderer(this.getContext(), ModelLayers.MINECART);
        this.setModel(render, Reflector.RenderMinecart_modelMinecart, "MinecartRenderer.modelMinecart", modelBase);
        return render;
    }
}
