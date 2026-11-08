package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.entity.EvokerRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterEvoker extends ModelAdapterIllager {
    public ModelAdapterEvoker() {
        super(EntityTypes.EVOKER, "evoker", ModelLayers.EVOKER);
        this.setAlias("evocation_illager");
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new IllagerModel(root);
    }

    @Override
    protected LivingEntityRenderer makeLivingRenderer(Context context) {
        return new EvokerRenderer(context);
    }
}
