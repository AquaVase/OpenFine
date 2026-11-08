package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterBoat extends ModelAdapterEntity {
    public ModelAdapterBoat(EntityType entityType, String prefix, ModelLayerLocation modelLayer) {
        this(entityType, prefix + "_boat", modelLayer, new String[]{"boat"});
    }

    protected ModelAdapterBoat(EntityType entityType, String name, ModelLayerLocation modelLayer, String[] aliases) {
        super(entityType, name, modelLayer);
        this.setAliases(aliases);
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new BoatModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("bottom", "bottom");
        map.put("back", "back");
        map.put("front", "front");
        map.put("right", "right");
        map.put("left", "left");
        map.put("paddle_left", "left_paddle");
        map.put("paddle_right", "right_paddle");
        map.put("root", "root");
        return map;
    }

    @Override
    public IEntityRenderer makeEntityRender(Model modelBase, RendererCache rendererCache, int index) {
        BoatRenderer customRenderer = new BoatRenderer(this.getContext(), this.getModelLayer());
        BoatRenderer renderer = (BoatRenderer)rendererCache.get(this.getEntityType(), index, () -> customRenderer);
        if (!Reflector.RenderBoat_model.exists()) {
            Config.warn("Field not found: RenderBoat.model");
            return null;
        } else {
            Reflector.RenderBoat_model.setValue(renderer, modelBase);
            return renderer;
        }
    }
}
