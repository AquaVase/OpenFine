package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.equipment.ShieldModel;
import net.optifine.entity.model.anim.IRenderResolver;
import net.optifine.entity.model.anim.RenderResolverEntity;

public class ModelAdapterShield extends ModelAdapterVirtual {
    private static Map<String, String> mapParts = makeMapParts();

    public ModelAdapterShield() {
        super("shield");
    }

    @Override
    public Model makeModel() {
        return new ShieldModel(bakeModelLayer(ModelLayers.SHIELD));
    }

    @Override
    public ModelPart getModelRenderer(Model model, String modelPart) {
        return this.getModelRenderer(model.root(), mapParts.get(modelPart));
    }

    @Override
    public String[] getModelRendererNames() {
        return toArray(mapParts.keySet());
    }

    private static Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("plate", "plate");
        map.put("handle", "handle");
        map.put("root", "root");
        return map;
    }

    @Override
    public IEntityRenderer makeEntityRender(Model modelBase, RendererCache rendererCache, int index) {
        final ShieldModel shieldModel = (ShieldModel)modelBase;
        return new VirtualEntityRenderer() {
            @Override
            public void register() {
                CustomStaticModels.setShieldModel(shieldModel);
            }
        };
    }

    @Override
    public IRenderResolver getRenderResolver() {
        return new RenderResolverEntity();
    }
}
