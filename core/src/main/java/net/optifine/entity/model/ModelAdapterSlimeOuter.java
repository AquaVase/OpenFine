package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterSlimeOuter extends ModelAdapterSlime {
    public ModelAdapterSlimeOuter() {
        super(EntityTypes.SLIME, "slime_outer", ModelLayers.SLIME_OUTER);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("body", "cube");
        map.put("root", "root");
        return map;
    }

    @Override
    protected void modifyLivingRenderer(LivingEntityRenderer renderer, Model modelBase) {
        SlimeRenderer slimeRenderer = (SlimeRenderer)renderer;
        SlimeOuterLayer layer = new SlimeOuterLayer(slimeRenderer, this.getContext().getModelSet());
        layer.model = (SlimeModel)modelBase;
        slimeRenderer.replaceLayer(SlimeOuterLayer.class, layer);
    }

    @Override
    public boolean setTextureLocation(IEntityRenderer er, Identifier textureLocation) {
        SlimeRenderer renderer = (SlimeRenderer)er;

        for (SlimeOuterLayer layer : renderer.getLayers(SlimeOuterLayer.class)) {
            layer.customTextureLocation = textureLocation;
        }

        return true;
    }
}
