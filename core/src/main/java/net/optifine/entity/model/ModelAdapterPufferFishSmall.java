package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.fish.PufferfishSmallModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.PufferfishRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.reflect.Reflector;

public class ModelAdapterPufferFishSmall extends ModelAdapterLiving {
    public ModelAdapterPufferFishSmall() {
        super(EntityTypes.PUFFERFISH, "puffer_fish_small", ModelLayers.PUFFERFISH_SMALL);
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new PufferfishSmallModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("body", "body");
        map.put("eye_right", "right_eye");
        map.put("eye_left", "left_eye");
        map.put("fin_right", "right_fin");
        map.put("fin_left", "left_fin");
        map.put("tail", "back_fin");
        map.put("root", "root");
        return map;
    }

    @Override
    protected LivingEntityRenderer makeLivingRenderer(Context context) {
        return new PufferfishRenderer(context);
    }

    @Override
    protected void modifyLivingRenderer(LivingEntityRenderer renderer, Model modelBase) {
        this.setModel(renderer, Reflector.RenderPufferfish_modelSmall, "PufferfishRenderer.smallModel", modelBase);
    }
}
