package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.feline.AdultCatModel;
import net.minecraft.client.model.animal.feline.BabyCatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.layers.CatCollarLayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterCatCollar extends ModelAdapterCat {
    public ModelAdapterCatCollar() {
        super(EntityTypes.CAT, "cat_collar", ModelLayers.CAT_COLLAR);
        this.setAlias("cat");
    }

    protected ModelAdapterCatCollar(EntityType entityType, String name, ModelLayerLocation modelLayer) {
        super(entityType, name, modelLayer);
    }

    @Override
    public ModelAdapter makeBaby() {
        ModelAdapterCatCollar ma = new ModelAdapterCatCollar(this.getEntityType(), "cat_baby_collar", ModelLayers.CAT_BABY_COLLAR);
        ma.setBaby(true);
        ma.setAliases(this.getName(), "cat");
        return ma;
    }

    @Override
    protected void modifyAgeableRenderer(AgeableMobRenderer renderer, Model modelBase) {
        for (CatCollarLayer layer : (java.lang.Iterable<CatCollarLayer>)(java.lang.Iterable<?>)(this.getRenderLayers(renderer, CatCollarLayer.class))) {
            if (this.isBaby()) {
                layer.babyModel = (BabyCatModel)modelBase;
            } else {
                layer.adultModel = (AdultCatModel)modelBase;
            }
        }
    }
}
