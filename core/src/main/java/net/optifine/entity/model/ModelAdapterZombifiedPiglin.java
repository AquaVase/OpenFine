package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.piglin.AdultZombifiedPiglinModel;
import net.minecraft.client.model.monster.piglin.BabyZombifiedPiglinModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.ZombifiedPiglinRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterZombifiedPiglin extends ModelAdapterPiglin {
    public ModelAdapterZombifiedPiglin() {
        super(EntityTypes.ZOMBIFIED_PIGLIN, "zombified_piglin", ModelLayers.ZOMBIFIED_PIGLIN);
    }

    protected ModelAdapterZombifiedPiglin(EntityType entityType, String name, ModelLayerLocation modelLayer) {
        super(entityType, name, modelLayer);
    }

    @Override
    public ModelAdapter makeBaby() {
        ModelAdapterZombifiedPiglin ma = new ModelAdapterZombifiedPiglin(this.getEntityType(), "zombified_piglin_baby", ModelLayers.ZOMBIFIED_PIGLIN_BABY);
        ma.setBaby(true);
        ma.setAlias(this.getName());
        return ma;
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return this.isBaby() ? new BabyZombifiedPiglinModel(root) : new AdultZombifiedPiglinModel(root);
    }

    @Override
    protected AgeableMobRenderer makeAgeableRenderer(Context context) {
        return new ZombifiedPiglinRenderer(
            context, ModelLayers.ZOMBIFIED_PIGLIN, ModelLayers.ZOMBIFIED_PIGLIN_BABY, ModelLayers.ZOMBIFIED_PIGLIN_ARMOR, ModelLayers.ZOMBIFIED_PIGLIN_BABY_ARMOR
        );
    }
}
