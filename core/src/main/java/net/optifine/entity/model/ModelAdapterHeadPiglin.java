package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.skull.PiglinHeadModel;
import net.minecraft.world.level.block.SkullBlock.Types;

public class ModelAdapterHeadPiglin extends ModelAdapterHead {
    private static Map<String, String> mapParts = makeMapParts();

    public ModelAdapterHeadPiglin() {
        super("head_piglin", null, Types.PIGLIN);
    }

    @Override
    public Model makeModel() {
        return new PiglinHeadModel(bakeModelLayer(ModelLayers.PIGLIN_HEAD));
    }

    @Override
    public ModelPart getModelRenderer(Model model, String modelPart) {
        return this.getModelRenderer(model.root(), mapParts.get(modelPart), () -> super.getModelRenderer(model, modelPart));
    }

    @Override
    public String[] getModelRendererNames() {
        return toArray(super.getModelRendererNames(), mapParts.keySet());
    }

    private static Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("left_ear", "left_ear");
        map.put("right_ear", "right_ear");
        return map;
    }
}
