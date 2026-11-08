package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.fish.TropicalFishLargeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.TropicalFishRenderer;
import net.minecraft.client.renderer.entity.layers.TropicalFishPatternLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.reflect.Reflector;

public class ModelAdapterTropicalFishPatternB extends ModelAdapterTropicalFishB {
    public ModelAdapterTropicalFishPatternB() {
        super(EntityTypes.TROPICAL_FISH, "tropical_fish_pattern_b", ModelLayers.TROPICAL_FISH_LARGE_PATTERN);
    }

    @Override
    protected void modifyLivingRenderer(LivingEntityRenderer renderer, Model modelBase) {
        TropicalFishRenderer renderTropicalFish = (TropicalFishRenderer)renderer;

        for (TropicalFishPatternLayer layer : renderTropicalFish.getLayers(TropicalFishPatternLayer.class)) {
            this.setModel(layer, Reflector.TropicalFishPatternLayer_modelB, "TropicalFishPatternLayer.modelB", modelBase);
        }
    }

    @Override
    public boolean setTextureLocation(IEntityRenderer er, Identifier textureLocation) {
        TropicalFishRenderer renderTropicalFish = (TropicalFishRenderer)er;

        for (TropicalFishPatternLayer layer : renderTropicalFish.getLayers(TropicalFishPatternLayer.class)) {
            TropicalFishLargeModel model = (TropicalFishLargeModel)Reflector.TropicalFishPatternLayer_modelB.getValue(layer);
            if (model != null) {
                model.locationTextureCustom = textureLocation;
            }
        }

        return true;
    }
}
