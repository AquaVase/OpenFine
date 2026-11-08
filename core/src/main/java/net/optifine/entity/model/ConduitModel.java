package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.ConduitRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ConduitModel<S> extends Model<S> {
    public ModelPart eye;
    public ModelPart wind;
    public ModelPart base;
    public ModelPart cage;

    public ConduitModel() {
        super(ModelPart.makeRoot(), RenderTypes::entityCutout);
        BlockEntityRenderDispatcher dispatcher = Config.getMinecraft().getBlockEntityRenderDispatcher();
        ConduitRenderer renderer = new ConduitRenderer(dispatcher.getContext());
        this.eye = (ModelPart)Reflector.TileEntityConduitRenderer_modelRenderers.getValue(renderer, 0);
        this.wind = (ModelPart)Reflector.TileEntityConduitRenderer_modelRenderers.getValue(renderer, 1);
        this.base = (ModelPart)Reflector.TileEntityConduitRenderer_modelRenderers.getValue(renderer, 2);
        this.cage = (ModelPart)Reflector.TileEntityConduitRenderer_modelRenderers.getValue(renderer, 3);
        this.root().addChildModel("eye", this.eye);
        this.root().addChildModel("wind", this.wind);
        this.root().addChildModel("shell", this.base);
        this.root().addChildModel("cage", this.cage);
    }

    public BlockEntityRenderer updateRenderer(BlockEntityRenderer renderer) {
        if (!Reflector.TileEntityConduitRenderer_modelRenderers.exists()) {
            Config.warn("Field not found: TileEntityConduitRenderer.modelRenderers");
            return null;
        } else {
            Reflector.TileEntityConduitRenderer_modelRenderers.setValue(renderer, 0, this.eye);
            Reflector.TileEntityConduitRenderer_modelRenderers.setValue(renderer, 1, this.wind);
            Reflector.TileEntityConduitRenderer_modelRenderers.setValue(renderer, 2, this.base);
            Reflector.TileEntityConduitRenderer_modelRenderers.setValue(renderer, 3, this.cage);
            return renderer;
        }
    }

    @Override
    public boolean isRenderRoot() {
        return false;
    }
}
