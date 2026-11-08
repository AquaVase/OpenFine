package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorField;

public class ModelAdapterDrownedOuter extends ModelAdapterDrowned {
    public ModelAdapterDrownedOuter() {
        super(EntityTypes.DROWNED, "drowned_outer", ModelLayers.DROWNED_OUTER_LAYER);
    }

    protected ModelAdapterDrownedOuter(EntityType entityType, String name, ModelLayerLocation modelLayer) {
        super(entityType, name, modelLayer);
    }

    @Override
    public ModelAdapter makeBaby() {
        ModelAdapterDrownedOuter ma = new ModelAdapterDrownedOuter(this.getEntityType(), "drowned_baby_outer", ModelLayers.DROWNED_BABY_OUTER_LAYER);
        ma.setBaby(true);
        ma.setAlias(this.getName());
        return ma;
    }

    @Override
    protected void modifyAgeableRenderer(AgeableMobRenderer renderer, Model modelBase) {
        ReflectorField field = this.isBaby() ? Reflector.DrownedOuterLayer_babyModel : Reflector.DrownedOuterLayer_adultModel;
        this.setLayerModel(renderer, DrownedOuterLayer.class, field, "DrownedOuterLayer.model", modelBase);
    }
}
