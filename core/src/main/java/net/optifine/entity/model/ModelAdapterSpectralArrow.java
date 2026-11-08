package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.SpectralArrowRenderer;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterSpectralArrow extends ModelAdapterArrow {
    public ModelAdapterSpectralArrow() {
        super(EntityTypes.SPECTRAL_ARROW, "spectral_arrow", ModelLayers.ARROW);
        this.setAlias("arrow");
    }

    @Override
    public IEntityRenderer makeEntityRender(Model modelBase, RendererCache rendererCache, int index) {
        SpectralArrowRenderer render = new SpectralArrowRenderer(this.getContext());
        if (!Reflector.ArrowRenderer_model.exists()) {
            Config.warn("Field not found: ArrowRenderer.model");
            return null;
        } else {
            Reflector.setFieldValue(render, Reflector.ArrowRenderer_model, modelBase);
            return render;
        }
    }
}
