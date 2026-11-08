package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.CaveSpiderRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterCaveSpider extends ModelAdapterSpider {
    public ModelAdapterCaveSpider() {
        super(EntityTypes.CAVE_SPIDER, "cave_spider", ModelLayers.CAVE_SPIDER);
    }

    @Override
    protected LivingEntityRenderer makeLivingRenderer(Context context) {
        return new CaveSpiderRenderer(context);
    }
}
