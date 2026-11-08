package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.llama.LlamaSpitModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.LlamaSpitRenderer;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.reflect.Reflector;

public class ModelAdapterLlamaSpit extends ModelAdapterEntity {
    public ModelAdapterLlamaSpit() {
        super(EntityTypes.LLAMA_SPIT, "llama_spit", ModelLayers.LLAMA_SPIT);
    }

    @Override
    public Model makeModel(ModelPart root) {
        return new LlamaSpitModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("body", "main");
        map.put("root", "root");
        return map;
    }

    @Override
    public IEntityRenderer makeEntityRender(Model modelBase, RendererCache rendererCache, int index) {
        LlamaSpitRenderer render = new LlamaSpitRenderer(this.getContext());
        this.setModel(render, Reflector.RenderLlamaSpit_model, "LlamaSpitRenderer.model", modelBase);
        return render;
    }
}
