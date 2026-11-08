package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.leash.LeashKnotModel;
import net.minecraft.client.renderer.entity.LeashKnotRenderer;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.reflect.Reflector;

public class ModelAdapterLeadKnot extends ModelAdapterEntity {
    public ModelAdapterLeadKnot() {
        super(EntityTypes.LEASH_KNOT, "lead_knot", ModelLayers.LEASH_KNOT);
    }

    @Override
    public Model makeModel(ModelPart root) {
        return new LeashKnotModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("knot", "knot");
        map.put("root", "root");
        return map;
    }

    @Override
    public IEntityRenderer makeEntityRender(Model modelBase, RendererCache rendererCache, int index) {
        LeashKnotRenderer render = new LeashKnotRenderer(this.getContext());
        this.setModel(render, Reflector.RenderLeashKnot_leashKnotModel, "LeashKnotRenderer.model", modelBase);
        return render;
    }
}
