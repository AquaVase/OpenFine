package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.skull.SkullModel;
import net.minecraft.client.renderer.entity.WitherSkullRenderer;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.reflect.Reflector;

public class ModelAdapterWitherSkull extends ModelAdapterEntity {
    public ModelAdapterWitherSkull() {
        super(EntityTypes.WITHER_SKULL, "wither_skull", ModelLayers.WITHER_SKULL);
    }

    @Override
    public Model makeModel(ModelPart root) {
        return new SkullModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("head", "head");
        map.put("root", "root");
        return map;
    }

    @Override
    public IEntityRenderer makeEntityRender(Model modelBase, RendererCache rendererCache, int index) {
        WitherSkullRenderer render = new WitherSkullRenderer(this.getContext());
        this.setModel(render, Reflector.RenderWitherSkull_model, "WitherSkullRenderer.model", modelBase);
        return render;
    }
}
