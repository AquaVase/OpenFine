package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.creeper.CreeperModel;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CreeperPowerLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterCreeperCharge extends ModelAdapterCreeper {
    public ModelAdapterCreeperCharge() {
        super(EntityTypes.CREEPER, "creeper_charge", ModelLayers.CREEPER_ARMOR);
    }

    @Override
    protected void modifyLivingRenderer(LivingEntityRenderer renderer, Model modelBase) {
        CreeperRenderer creeperRenderer = (CreeperRenderer)renderer;
        CreeperPowerLayer layer = new CreeperPowerLayer(creeperRenderer, this.getContext().getModelSet());
        layer.model = (CreeperModel)modelBase;
        creeperRenderer.replaceLayer(CreeperPowerLayer.class, layer);
    }

    @Override
    public boolean setTextureLocation(IEntityRenderer er, Identifier textureLocation) {
        LivingEntityRenderer renderer = (LivingEntityRenderer)er;

        for (CreeperPowerLayer layer : (java.lang.Iterable<CreeperPowerLayer>)(java.lang.Iterable<?>)(renderer.getLayers(CreeperPowerLayer.class))) {
            layer.customTextureLocation = textureLocation;
        }

        return true;
    }
}
