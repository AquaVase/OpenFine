package net.optifine.entity.model;

import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.zombie.GiantZombieModel;
import net.minecraft.client.renderer.entity.GiantMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterGiant extends ModelAdapterLiving {
    public ModelAdapterGiant() {
        super(EntityTypes.GIANT, "giant", ModelLayers.GIANT);
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new GiantZombieModel(root);
    }

    @Override
    protected Map<String, String> makeMapParts() {
        return ModelAdapterHumanoid.makeStaticMapParts();
    }

    @Override
    protected LivingEntityRenderer makeLivingRenderer(Context context) {
        return new GiantMobRenderer(context, 6.0F);
    }
}
