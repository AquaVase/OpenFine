package net.optifine.entity.model;

import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.skeleton.BoggedModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.BoggedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterBogged extends ModelAdapterSkeleton {
    public ModelAdapterBogged() {
        super(EntityTypes.BOGGED, "bogged", ModelLayers.BOGGED);
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new BoggedModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = super.makeMapParts();
        map.put("mushrooms", "mushrooms");
        return map;
    }

    @Override
    protected AgeableMobRenderer makeAgeableRenderer(Context context) {
        return new BoggedRenderer(context);
    }
}
