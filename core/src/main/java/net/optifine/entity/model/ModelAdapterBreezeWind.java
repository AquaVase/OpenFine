package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.breeze.BreezeModel;
import net.minecraft.client.renderer.entity.BreezeRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.BreezeWindLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterBreezeWind extends ModelAdapterBreeze {
    public ModelAdapterBreezeWind() {
        super(EntityTypes.BREEZE, "breeze_wind", ModelLayers.BREEZE_WIND);
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new BreezeModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("body", "body");
        map.put("wind_body", "wind_body");
        map.put("wind_middle", "wind_mid");
        map.put("wind_bottom", "wind_bottom");
        map.put("wind_top", "wind_top");
        map.put("root", "root");
        return map;
    }

    @Override
    protected void modifyLivingRenderer(LivingEntityRenderer renderer, Model modelBase) {
        BreezeRenderer breezeRenderer = (BreezeRenderer)renderer;
        Identifier locTex = modelBase.locationTextureCustom != null ? modelBase.locationTextureCustom : new Identifier("textures/entity/breeze/breeze_wind.png");
        BreezeWindLayer layer = new BreezeWindLayer(breezeRenderer, this.getContext().getModelSet());
        layer.setModel((BreezeModel)modelBase);
        layer.setTextureLocation(locTex);
        renderer.replaceLayer(BreezeWindLayer.class, layer);
    }

    @Override
    public boolean setTextureLocation(IEntityRenderer er, Identifier textureLocation) {
        BreezeRenderer breezeRenderer = (BreezeRenderer)er;

        for (BreezeWindLayer layer : (java.lang.Iterable<BreezeWindLayer>)(java.lang.Iterable<?>)(this.getRenderLayers(breezeRenderer, BreezeWindLayer.class))) {
            layer.setTextureLocation(textureLocation);
        }

        return true;
    }
}
