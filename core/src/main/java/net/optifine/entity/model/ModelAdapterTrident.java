package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.projectile.TridentModel;
import net.minecraft.client.renderer.entity.ThrownTridentRenderer;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.reflect.Reflector;

public class ModelAdapterTrident extends ModelAdapterEntity {
    public ModelAdapterTrident() {
        super(EntityTypes.TRIDENT, "trident", ModelLayers.TRIDENT);
    }

    @Override
    public Model makeModel(ModelPart root) {
        return new TridentModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        return makeStaticMapParts();
    }

    public static Map<String, String> makeStaticMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("body", "pole");
        map.put("base", "base");
        map.put("left_spike", "left_spike");
        map.put("middle_spike", "middle_spike");
        map.put("right_spike", "right_spike");
        map.put("root", "root");
        return map;
    }

    @Override
    public IEntityRenderer makeEntityRender(Model modelBase, RendererCache rendererCache, int index) {
        ThrownTridentRenderer render = new ThrownTridentRenderer(this.getContext());
        this.setModel(render, Reflector.RenderTrident_modelTrident, "ThrownTridentRenderer.model", modelBase);
        return render;
    }
}
