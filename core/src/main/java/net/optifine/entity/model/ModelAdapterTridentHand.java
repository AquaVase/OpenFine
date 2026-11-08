package net.optifine.entity.model;

import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.projectile.TridentModel;
import net.minecraft.resources.Identifier;
import net.optifine.entity.model.anim.IRenderResolver;
import net.optifine.entity.model.anim.RenderResolverEntity;

public class ModelAdapterTridentHand extends ModelAdapterVirtual {
    private static Map<String, String> mapParts = makeMapParts();

    public ModelAdapterTridentHand() {
        super("trident_hand");
        this.setAlias("trident");
    }

    @Override
    public Model makeModel() {
        return new TridentModel(bakeModelLayer(ModelLayers.TRIDENT));
    }

    @Override
    public ModelPart getModelRenderer(Model model, String modelPart) {
        return this.getModelRenderer(model.root(), mapParts.get(modelPart));
    }

    @Override
    public String[] getModelRendererNames() {
        return toArray(mapParts.keySet());
    }

    public static Map<String, String> makeMapParts() {
        return ModelAdapterTrident.makeStaticMapParts();
    }

    @Override
    public IEntityRenderer makeEntityRender(Model modelBase, RendererCache rendererCache, int index) {
        final TridentModel tridentModel = (TridentModel)modelBase;
        return new VirtualEntityRenderer() {
            @Override
            public void register() {
                tridentModel.locationTextureCustom = this.getLocationTextureCustom();
                CustomStaticModels.setTridentModel(tridentModel);
            }
        };
    }

    @Override
    public boolean setTextureLocation(IEntityRenderer er, Identifier textureLocation) {
        VirtualEntityRenderer ver = (VirtualEntityRenderer)er;
        ver.setLocationTextureCustom(textureLocation);
        return true;
    }

    @Override
    public IRenderResolver getRenderResolver() {
        return new RenderResolverEntity();
    }
}
