package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.boat.RaftModel;
import net.minecraft.client.renderer.entity.RaftRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.reflect.Reflector;

public class ModelAdapterRaft extends ModelAdapterEntity {
    public ModelAdapterRaft() {
        this(EntityTypes.BAMBOO_RAFT, "raft", ModelLayers.BAMBOO_RAFT);
    }

    protected ModelAdapterRaft(EntityType entityType, String name, ModelLayerLocation modelLayer) {
        super(entityType, name, modelLayer);
    }

    @Override
    public Model makeModel(ModelPart root) {
        return new RaftModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("bottom", "bottom");
        map.put("paddle_left", "left_paddle");
        map.put("paddle_right", "right_paddle");
        map.put("root", "root");
        return map;
    }

    @Override
    public IEntityRenderer makeEntityRender(Model modelBase, RendererCache rendererCache, int index) {
        RaftRenderer renderer = new RaftRenderer(this.getContext(), this.getModelLayer());
        this.setModel(renderer, Reflector.RenderRaft_model, "RaftRenderer.model", modelBase);
        return renderer;
    }
}
