package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.sheep.SheepModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.SheepRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterSheep extends ModelAdapterQuadruped {
    public ModelAdapterSheep() {
        super(EntityTypes.SHEEP, "sheep", ModelLayers.SHEEP);
    }

    protected ModelAdapterSheep(EntityType entityType, String name, ModelLayerLocation modelLayer) {
        super(entityType, name, modelLayer);
    }

    @Override
    public ModelAdapter makeBaby() {
        ModelAdapterSheep ma = new ModelAdapterSheep(this.getEntityType(), "sheep_baby", ModelLayers.SHEEP_BABY);
        ma.setBaby(true);
        ma.setAlias(this.getName());
        return ma;
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new SheepModel(root);
    }

    @Override
    protected AgeableMobRenderer makeAgeableRenderer(Context context) {
        return new SheepRenderer(context);
    }
}
