package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterMinecartTnt extends ModelAdapterMinecart {
    public ModelAdapterMinecartTnt() {
        super(EntityTypes.TNT_MINECART, "tnt_minecart", ModelLayers.TNT_MINECART);
        this.setAlias("minecart");
    }

    @Override
    public IEntityRenderer makeEntityRender(Model modelBase, RendererCache rendererCache, int index) {
        TntMinecartRenderer render = new TntMinecartRenderer(this.getContext());
        if (!Reflector.RenderMinecart_modelMinecart.exists()) {
            Config.warn("Field not found: RenderMinecart.modelMinecart");
            return null;
        } else {
            Reflector.setFieldValue(render, Reflector.RenderMinecart_modelMinecart, modelBase);
            return render;
        }
    }
}
