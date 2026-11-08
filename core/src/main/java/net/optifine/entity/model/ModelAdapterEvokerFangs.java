package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.effects.EvokerFangsModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EvokerFangsRenderer;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.reflect.Reflector;

public class ModelAdapterEvokerFangs extends ModelAdapterEntity {
    public ModelAdapterEvokerFangs() {
        super(EntityTypes.EVOKER_FANGS, "evoker_fangs", ModelLayers.EVOKER_FANGS);
        this.setAlias("evocation_fangs");
    }

    @Override
    public Model makeModel(ModelPart root) {
        return new EvokerFangsModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("base", "base");
        map.put("upper_jaw", "upper_jaw");
        map.put("lower_jaw", "lower_jaw");
        map.put("root", "root");
        return map;
    }

    @Override
    public IEntityRenderer makeEntityRender(Model modelBase, RendererCache rendererCache, int index) {
        EvokerFangsRenderer render = new EvokerFangsRenderer(this.getContext());
        this.setModel(render, Reflector.RenderEvokerFangs_model, "EvokerFangsRenderer.model", modelBase);
        return render;
    }
}
