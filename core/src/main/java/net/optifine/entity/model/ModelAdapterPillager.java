package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.PillagerRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterPillager extends ModelAdapterIllager {
    public ModelAdapterPillager() {
        super(EntityTypes.PILLAGER, "pillager", ModelLayers.PILLAGER);
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new IllagerModel(root);
    }

    @Override
    protected LivingEntityRenderer makeLivingRenderer(Context context) {
        return new PillagerRenderer(context);
    }
}
