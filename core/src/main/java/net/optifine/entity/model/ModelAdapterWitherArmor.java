package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.wither.WitherBossModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.WitherBossRenderer;
import net.minecraft.client.renderer.entity.layers.WitherArmorLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterWitherArmor extends ModelAdapterWither {
    public ModelAdapterWitherArmor() {
        super(EntityTypes.WITHER, "wither_armor", ModelLayers.WITHER_ARMOR);
    }

    @Override
    protected void modifyLivingRenderer(LivingEntityRenderer renderer, Model modelBase) {
        WitherBossRenderer witherRenderer = (WitherBossRenderer)renderer;
        WitherArmorLayer layer = new WitherArmorLayer(witherRenderer, this.getContext().getModelSet());
        layer.model = (WitherBossModel)modelBase;
        renderer.replaceLayer(WitherArmorLayer.class, layer);
    }

    @Override
    public boolean setTextureLocation(IEntityRenderer er, Identifier textureLocation) {
        WitherBossRenderer renderer = (WitherBossRenderer)er;

        for (WitherArmorLayer layer : renderer.getLayers(WitherArmorLayer.class)) {
            layer.customTextureLocation = textureLocation;
        }

        return true;
    }
}
