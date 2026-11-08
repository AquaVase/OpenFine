package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.BoggedRenderer;
import net.minecraft.client.renderer.entity.layers.SkeletonClothingLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterBoggedOuter extends ModelAdapterSkeleton {
    public ModelAdapterBoggedOuter() {
        super(EntityTypes.BOGGED, "bogged_outer", ModelLayers.BOGGED_OUTER_LAYER);
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new SkeletonModel(root);
    }

    @Override
    protected void modifyAgeableRenderer(AgeableMobRenderer renderer, Model modelBase) {
        BoggedRenderer boggedRenderer = (BoggedRenderer)renderer;
        Identifier BOGGED_OUTER_LAYER_LOCATION = Identifier.withDefaultNamespace("textures/entity/skeleton/bogged_overlay.png");
        SkeletonClothingLayer layer = new SkeletonClothingLayer<>(
            boggedRenderer, this.getContext().getModelSet(), ModelLayers.BOGGED_OUTER_LAYER, BOGGED_OUTER_LAYER_LOCATION
        );
        layer.layerModel = (SkeletonModel)modelBase;
        boggedRenderer.replaceLayer(SkeletonClothingLayer.class, layer);
    }

    @Override
    public boolean setTextureLocation(IEntityRenderer er, Identifier textureLocation) {
        BoggedRenderer renderer = (BoggedRenderer)er;

        for (SkeletonClothingLayer layer : renderer.getLayers(SkeletonClothingLayer.class)) {
            layer.clothesLocation = textureLocation;
        }

        return true;
    }
}
