package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterSkeleton extends ModelAdapterAgeableHumanoid {
    public ModelAdapterSkeleton() {
        super(EntityTypes.SKELETON, "skeleton", ModelLayers.SKELETON);
    }

    protected ModelAdapterSkeleton(EntityType type, String name, ModelLayerLocation modelLayer) {
        super(type, name, modelLayer);
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new SkeletonModel(root);
    }

    @Override
    protected AgeableMobRenderer makeAgeableRenderer(Context context) {
        return new SkeletonRenderer(context);
    }
}
