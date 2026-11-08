package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.LecternRenderer;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterBookLectern extends ModelAdapterBook {
    public ModelAdapterBookLectern() {
        super(BlockEntityTypes.LECTERN, "lectern_book");
        this.setAliases("enchanting_book", "book");
    }

    @Override
    public IEntityRenderer makeEntityRender(Model modelBase, RendererCache rendererCache, int index) {
        BlockEntityRenderer renderer = rendererCache.get(BlockEntityTypes.LECTERN, index, () -> new LecternRenderer(this.getContext()));
        if (!(renderer instanceof LecternRenderer)) {
            return null;
        } else if (!Reflector.TileEntityLecternRenderer_modelBook.exists()) {
            Config.warn("Field not found: TileEntityLecternRenderer.modelBook");
            return null;
        } else {
            Reflector.setFieldValue(renderer, Reflector.TileEntityLecternRenderer_modelBook, modelBase);
            return renderer;
        }
    }
}
