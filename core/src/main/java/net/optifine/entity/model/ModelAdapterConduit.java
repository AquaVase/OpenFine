package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.ConduitRenderer;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.optifine.Config;

public class ModelAdapterConduit extends ModelAdapterBlockEntity {
    public ModelAdapterConduit() {
        super(BlockEntityTypes.CONDUIT, "conduit");
    }

    @Override
    public Model makeModel() {
        return new ConduitModel();
    }

    @Override
    public ModelPart getModelRenderer(Model model, String modelPart) {
        if (!(model instanceof ConduitModel modelConduit)) {
            return null;
        } else if (modelPart.equals("eye")) {
            return modelConduit.eye;
        } else if (modelPart.equals("wind")) {
            return modelConduit.wind;
        } else if (modelPart.equals("base")) {
            return modelConduit.base;
        } else {
            return modelPart.equals("cage") ? modelConduit.cage : null;
        }
    }

    @Override
    public String[] getModelRendererNames() {
        return new String[]{"eye", "wind", "base", "cage"};
    }

    @Override
    public IEntityRenderer makeEntityRender(Model modelBase, RendererCache rendererCache, int index) {
        BlockEntityRenderer renderer = rendererCache.get(BlockEntityTypes.CONDUIT, index, () -> new ConduitRenderer(this.getContext()));
        if (!(renderer instanceof ConduitRenderer)) {
            return null;
        } else if (!(modelBase instanceof ConduitModel conduitModel)) {
            Config.warn("Not a conduit model: " + modelBase);
            return null;
        } else {
            return conduitModel.updateRenderer(renderer);
        }
    }
}
