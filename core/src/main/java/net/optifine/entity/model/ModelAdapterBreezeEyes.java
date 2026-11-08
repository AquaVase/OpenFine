package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.breeze.BreezeModel;
import net.minecraft.client.renderer.entity.BreezeRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.BreezeEyesLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterBreezeEyes extends ModelAdapterBreeze {
    public ModelAdapterBreezeEyes() {
        super(EntityTypes.BREEZE, "breeze_eyes", ModelLayers.BREEZE_EYES);
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new BreezeModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("body", "body");
        map.put("head", "head");
        map.put("eyes", "eyes");
        map.put("root", "root");
        return map;
    }

    @Override
    protected void modifyLivingRenderer(LivingEntityRenderer renderer, Model modelBase) {
        BreezeRenderer breezeRenderer = (BreezeRenderer)renderer;
        Identifier locTex = modelBase.locationTextureCustom != null ? modelBase.locationTextureCustom : new Identifier("textures/entity/breeze/breeze_eyes.png");
        BreezeEyesLayer layer = new BreezeEyesLayer(breezeRenderer, this.getContext().getModelSet());
        layer.setModel((BreezeModel)modelBase);
        layer.setTextureLocation(locTex);
        renderer.replaceLayer(BreezeEyesLayer.class, layer);
    }

    @Override
    public boolean setTextureLocation(IEntityRenderer er, Identifier textureLocation) {
        BreezeRenderer breezeRenderer = (BreezeRenderer)er;

        for (BreezeEyesLayer layer : (java.lang.Iterable<BreezeEyesLayer>)(java.lang.Iterable<?>)(this.getRenderLayers(breezeRenderer, BreezeEyesLayer.class))) {
            layer.setTextureLocation(textureLocation);
        }

        return true;
    }
}
