package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterBook extends ModelAdapterBlockEntity {
    private static Map<String, String> mapParts = makeMapParts();

    public ModelAdapterBook() {
        super(BlockEntityTypes.ENCHANTING_TABLE, "enchanting_book");
        this.setAlias("book");
    }

    protected ModelAdapterBook(BlockEntityType tileEntityType, String name) {
        super(tileEntityType, name);
    }

    @Override
    public Model makeModel() {
        return new BookModel(bakeModelLayer(ModelLayers.BOOK));
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
        Map<String, String> map = new LinkedHashMap<>();
        map.put("cover_right", "left_lid");
        map.put("cover_left", "right_lid");
        map.put("pages_right", "left_pages");
        map.put("pages_left", "right_pages");
        map.put("flipping_page_right", "flip_page1");
        map.put("flipping_page_left", "flip_page2");
        map.put("book_spine", "seam");
        map.put("root", "root");
        return map;
    }

    @Override
    public IEntityRenderer makeEntityRender(Model modelBase, RendererCache rendererCache, int index) {
        BlockEntityRenderer renderer = rendererCache.get(BlockEntityTypes.ENCHANTING_TABLE, index, () -> new EnchantTableRenderer(this.getContext()));
        if (!(renderer instanceof EnchantTableRenderer)) {
            return null;
        } else if (!Reflector.TileEntityEnchantmentTableRenderer_modelBook.exists()) {
            Config.warn("Field not found: TileEntityEnchantmentTableRenderer.modelBook");
            return null;
        } else {
            Reflector.setFieldValue(renderer, Reflector.TileEntityEnchantmentTableRenderer_modelBook, modelBase);
            return renderer;
        }
    }
}
