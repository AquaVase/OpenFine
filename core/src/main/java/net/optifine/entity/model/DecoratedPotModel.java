package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.DecoratedPotRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class DecoratedPotModel<S> extends Model<S> {
    public ModelPart neck;
    public ModelPart frontSide;
    public ModelPart backSide;
    public ModelPart leftSide;
    public ModelPart rightSide;
    public ModelPart top;
    public ModelPart bottom;

    public DecoratedPotModel() {
        super(ModelPart.makeRoot(), RenderTypes::entityCutout);
        BlockEntityRenderDispatcher dispatcher = Config.getMinecraft().getBlockEntityRenderDispatcher();
        DecoratedPotRenderer renderer = new DecoratedPotRenderer(dispatcher.getContext());
        this.neck = (ModelPart)Reflector.TileEntityDecoratedPotRenderer_modelRenderers.getValue(renderer, 0);
        this.frontSide = (ModelPart)Reflector.TileEntityDecoratedPotRenderer_modelRenderers.getValue(renderer, 1);
        this.backSide = (ModelPart)Reflector.TileEntityDecoratedPotRenderer_modelRenderers.getValue(renderer, 2);
        this.leftSide = (ModelPart)Reflector.TileEntityDecoratedPotRenderer_modelRenderers.getValue(renderer, 3);
        this.rightSide = (ModelPart)Reflector.TileEntityDecoratedPotRenderer_modelRenderers.getValue(renderer, 4);
        this.top = (ModelPart)Reflector.TileEntityDecoratedPotRenderer_modelRenderers.getValue(renderer, 5);
        this.bottom = (ModelPart)Reflector.TileEntityDecoratedPotRenderer_modelRenderers.getValue(renderer, 6);
        this.root().addChildModel("neck", this.neck);
        this.root().addChildModel("front", this.frontSide);
        this.root().addChildModel("back", this.backSide);
        this.root().addChildModel("left", this.leftSide);
        this.root().addChildModel("right", this.rightSide);
        this.root().addChildModel("top", this.top);
        this.root().addChildModel("bottom", this.bottom);
    }

    public BlockEntityRenderer updateRenderer(BlockEntityRenderer renderer) {
        if (!Reflector.TileEntityDecoratedPotRenderer_modelRenderers.exists()) {
            Config.warn("Field not found: DecoratedPotRenderer.modelRenderers");
            return null;
        } else {
            Reflector.TileEntityDecoratedPotRenderer_modelRenderers.setValue(renderer, 0, this.neck);
            Reflector.TileEntityDecoratedPotRenderer_modelRenderers.setValue(renderer, 1, this.frontSide);
            Reflector.TileEntityDecoratedPotRenderer_modelRenderers.setValue(renderer, 2, this.backSide);
            Reflector.TileEntityDecoratedPotRenderer_modelRenderers.setValue(renderer, 3, this.leftSide);
            Reflector.TileEntityDecoratedPotRenderer_modelRenderers.setValue(renderer, 4, this.rightSide);
            Reflector.TileEntityDecoratedPotRenderer_modelRenderers.setValue(renderer, 5, this.top);
            Reflector.TileEntityDecoratedPotRenderer_modelRenderers.setValue(renderer, 6, this.bottom);
            return renderer;
        }
    }

    @Override
    public boolean isRenderRoot() {
        return false;
    }
}
