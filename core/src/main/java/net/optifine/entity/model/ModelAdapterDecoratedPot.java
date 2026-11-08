package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.DecoratedPotRenderer;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.optifine.Config;

public class ModelAdapterDecoratedPot extends ModelAdapterBlockEntity {
    public ModelAdapterDecoratedPot() {
        super(BlockEntityTypes.DECORATED_POT, "decorated_pot");
    }

    @Override
    public Model makeModel() {
        return new DecoratedPotModel();
    }

    @Override
    public ModelPart getModelRenderer(Model model, String modelPart) {
        if (!(model instanceof DecoratedPotModel modelDecoratedPot)) {
            return null;
        } else if (modelPart.equals("neck")) {
            return modelDecoratedPot.neck;
        } else if (modelPart.equals("front")) {
            return modelDecoratedPot.frontSide;
        } else if (modelPart.equals("back")) {
            return modelDecoratedPot.backSide;
        } else if (modelPart.equals("left")) {
            return modelDecoratedPot.leftSide;
        } else if (modelPart.equals("right")) {
            return modelDecoratedPot.rightSide;
        } else if (modelPart.equals("top")) {
            return modelDecoratedPot.top;
        } else {
            return modelPart.equals("bottom") ? modelDecoratedPot.bottom : null;
        }
    }

    @Override
    public String[] getModelRendererNames() {
        return new String[]{"neck", "front", "back", "left", "right", "top", "bottom"};
    }

    @Override
    public IEntityRenderer makeEntityRender(Model model, RendererCache rendererCache, int index) {
        BlockEntityRenderer renderer = rendererCache.get(BlockEntityTypes.DECORATED_POT, index, () -> new DecoratedPotRenderer(this.getContext()));
        if (!(renderer instanceof DecoratedPotRenderer)) {
            return null;
        } else if (!(model instanceof DecoratedPotModel decoratedPotModel)) {
            Config.warn("Not a decorated pot model: " + model);
            return null;
        } else {
            return decoratedPotModel.updateRenderer(renderer);
        }
    }
}
