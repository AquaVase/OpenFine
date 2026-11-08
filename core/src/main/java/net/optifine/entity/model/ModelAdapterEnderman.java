package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterEnderman extends ModelAdapterHumanoid {
    public ModelAdapterEnderman() {
        super(EntityTypes.ENDERMAN, "enderman", ModelLayers.ENDERMAN);
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new EndermanModel(root);
    }

    @Override
    protected LivingEntityRenderer makeLivingRenderer(Context context) {
        return new EndermanRenderer(context);
    }
}
