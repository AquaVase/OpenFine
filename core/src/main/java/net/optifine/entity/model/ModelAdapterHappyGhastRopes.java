package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.ghast.HappyGhastModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.HappyGhastRenderer;
import net.minecraft.client.renderer.entity.layers.RopesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterHappyGhastRopes extends ModelAdapterHappyGhast {
    public ModelAdapterHappyGhastRopes() {
        super(EntityTypes.HAPPY_GHAST, "happy_ghast_ropes", ModelLayers.HAPPY_GHAST_ROPES);
    }

    protected ModelAdapterHappyGhastRopes(EntityType entityType, String name, ModelLayerLocation modelLayer) {
        super(entityType, name, modelLayer);
    }

    @Override
    public ModelAdapter makeBaby() {
        return null;
    }

    @Override
    protected void modifyAgeableRenderer(AgeableMobRenderer renderer, Model modelBase) {
        HappyGhastModel model = (HappyGhastModel)modelBase;

        for (RopesLayer layer : (java.lang.Iterable<RopesLayer>)(java.lang.Iterable<?>)(this.getRenderLayers(renderer, RopesLayer.class))) {
            if (!Reflector.RopesLayer_adultModel.exists()) {
                Config.warn("RopesLayer.adultModel not found");
                return;
            }

            Reflector.setFieldValue(layer, Reflector.RopesLayer_adultModel, model);
        }
    }

    @Override
    public boolean setTextureLocation(IEntityRenderer er, Identifier textureLocation) {
        HappyGhastRenderer renderer = (HappyGhastRenderer)er;

        for (RopesLayer layer : (java.lang.Iterable<RopesLayer>)(java.lang.Iterable<?>)(this.getRenderLayers(renderer, RopesLayer.class))) {
            if (!Reflector.RopesLayer_ropes.exists()) {
                Config.warn("Field not found: RopesLayer.ropes");
            }

            RenderType ropes = RenderTypes.entityCutout(textureLocation);
            Reflector.RopesLayer_ropes.setValue(layer, ropes);
        }

        return true;
    }
}
