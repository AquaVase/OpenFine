package net.optifine.entity.model;

import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.resources.Identifier;
import net.optifine.entity.model.anim.IRenderResolver;
import net.optifine.entity.model.anim.RenderResolverTileEntity;

public class ModelAdapterBookScreen extends ModelAdapterVirtual {
    private static Map<String, String> mapParts = makeMapParts();

    public ModelAdapterBookScreen() {
        super("enchanting_screen_book");
        this.setAliases("enchanting_book", "book");
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
        return ModelAdapterBook.makeMapParts();
    }

    @Override
    public IEntityRenderer makeEntityRender(Model modelBase, RendererCache rendererCache, int index) {
        final BookModel bookModel = (BookModel)modelBase;
        return new VirtualEntityRenderer() {
            @Override
            public void register() {
                bookModel.locationTextureCustom = this.getLocationTextureCustom();
                CustomStaticModels.setBookModel(bookModel);
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
        return new RenderResolverTileEntity();
    }
}
