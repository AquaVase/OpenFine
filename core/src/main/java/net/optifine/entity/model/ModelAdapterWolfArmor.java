package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.layers.WolfArmorLayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorField;

public class ModelAdapterWolfArmor extends ModelAdapterWolf {
    public ModelAdapterWolfArmor() {
        this(EntityTypes.WOLF, "wolf_armor", ModelLayers.WOLF_ARMOR);
    }

    protected ModelAdapterWolfArmor(EntityType entityType, String name, ModelLayerLocation modelLayer) {
        super(entityType, name, modelLayer);
    }

    @Override
    public ModelAdapter makeBaby() {
        return null;
    }

    @Override
    protected void modifyAgeableRenderer(AgeableMobRenderer renderer, Model modelBase) {
        ReflectorField field = Reflector.WolfArmorLayer_adultModel;
        this.setLayerModel(renderer, WolfArmorLayer.class, field, "WolfArmorLayer.model", modelBase);
    }
}
