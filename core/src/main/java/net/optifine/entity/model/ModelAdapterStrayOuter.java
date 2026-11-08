package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.StrayRenderer;
import net.minecraft.client.renderer.entity.layers.SkeletonClothingLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterStrayOuter extends ModelAdapterStray {
    public ModelAdapterStrayOuter() {
        super(EntityTypes.STRAY, "stray_outer", ModelLayers.STRAY_OUTER_LAYER);
    }

    @Override
    protected void modifyAgeableRenderer(AgeableMobRenderer renderer, Model modelBase) {
        StrayRenderer strayRenderer = (StrayRenderer)renderer;
        Identifier STRAY_CLOTHES_LOCATION = new Identifier("textures/entity/skeleton/stray_overlay.png");
        SkeletonClothingLayer layer = new SkeletonClothingLayer<>(
            strayRenderer, this.getContext().getModelSet(), ModelLayers.STRAY_OUTER_LAYER, STRAY_CLOTHES_LOCATION
        );
        layer.layerModel = (SkeletonModel)modelBase;
        strayRenderer.replaceLayer(SkeletonClothingLayer.class, layer);
    }

    @Override
    public boolean setTextureLocation(IEntityRenderer er, Identifier textureLocation) {
        StrayRenderer renderer = (StrayRenderer)er;

        for (SkeletonClothingLayer layer : renderer.getLayers(SkeletonClothingLayer.class)) {
            layer.clothesLocation = textureLocation;
        }

        return true;
    }
}
