package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.crystal.EndCrystalModel;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterEnderCrystal extends ModelAdapterEntity {
    public ModelAdapterEnderCrystal() {
        this("end_crystal");
    }

    protected ModelAdapterEnderCrystal(String name) {
        super(EntityTypes.END_CRYSTAL, name, ModelLayers.END_CRYSTAL);
    }

    @Override
    public Model makeModel(ModelPart root) {
        return new EndCrystalModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("outer_glass", "outer_glass");
        map.put("inner_glass", "inner_glass");
        map.put("cube", "cube");
        map.put("base", "base");
        map.put("root", "root");
        return map;
    }

    @Override
    public IEntityRenderer makeEntityRender(Model modelBase, RendererCache rendererCache, int index) {
        EntityRenderer renderObj = rendererCache.get(EntityTypes.END_CRYSTAL, index, () -> new EndCrystalRenderer(this.getContext()));
        if (!(renderObj instanceof EndCrystalRenderer render)) {
            Config.warn("Not an instance of RenderEnderCrystal: " + renderObj);
            return null;
        } else if (!(modelBase instanceof EndCrystalModel enderCrystalModel)) {
            Config.warn("Not a EnderCrystalModel model: " + modelBase);
            return null;
        } else if (!Reflector.RenderEnderCrystal_model.exists()) {
            Config.warn("Field not found: RenderEnderCrystal.model");
            return null;
        } else {
            Reflector.RenderEnderCrystal_model.setValue(render, modelBase);
            return render;
        }
    }
}
