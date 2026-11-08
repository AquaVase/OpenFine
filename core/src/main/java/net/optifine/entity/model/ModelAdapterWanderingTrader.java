package net.optifine.entity.model;

import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.WanderingTraderRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterWanderingTrader extends ModelAdapterLiving {
    public ModelAdapterWanderingTrader() {
        super(EntityTypes.WANDERING_TRADER, "wandering_trader", ModelLayers.WANDERING_TRADER);
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new VillagerModel(root);
    }

    @Override
    protected Map<String, String> makeMapParts() {
        return ModelAdapterVillager.makeStaticMapParts();
    }

    @Override
    protected LivingEntityRenderer makeLivingRenderer(Context context) {
        return new WanderingTraderRenderer(context);
    }
}
