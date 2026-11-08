package net.optifine.entity.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.banner.BannerModel;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.optifine.reflect.Reflector;

public class ModelAdapterBannerBase extends ModelAdapterBlockEntity {
    private boolean standing;
    private Map<String, String> mapParts;

    public ModelAdapterBannerBase(boolean standing) {
        super(BlockEntityTypes.BANNER, "banner_base" + (standing ? "" : "_wall"));
        this.standing = standing;
        if (!standing) {
            this.setAliases("banner_base", "banner");
        } else {
            this.setAliases("banner");
        }

        this.mapParts = this.makeMapParts();
    }

    @Override
    public Model makeModel() {
        return new BannerModel(bakeModelLayer(this.standing ? ModelLayers.STANDING_BANNER : ModelLayers.WALL_BANNER));
    }

    @Override
    public ModelPart getModelRenderer(Model model, String modelPart) {
        return this.getModelRenderer(model.root(), this.mapParts.get(modelPart));
    }

    @Override
    public String[] getModelRendererNames() {
        return toArray(this.mapParts.keySet());
    }

    @Override
    public String[] getIgnoredPartNames() {
        List<String> names = new ArrayList<>(getAllBannerPartNames());
        names.removeAll(this.mapParts.keySet());
        return toArray(names);
    }

    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        if (this.standing) {
            map.put("stand", "pole");
        }

        map.put("top", "bar");
        map.put("root", "root");
        return map;
    }

    public static List<String> getAllBannerPartNames() {
        return List.of("slate", "stand", "top");
    }

    @Override
    public IEntityRenderer makeEntityRender(Model model, RendererCache rendererCache, int index) {
        BlockEntityRenderer renderer = rendererCache.get(BlockEntityTypes.BANNER, index, () -> new BannerRenderer(this.getContext()));
        BannerModel bannerModel = (BannerModel)model;
        if (!Reflector.TileEntityBannerRenderer_bannerModels.exists()) {
            throw new IllegalArgumentException("Field not found: BannerRenderer.bannerModels");
        }

        int fieldIndex = this.standing ? 0 : 1;
        Reflector.TileEntityBannerRenderer_bannerModels.setValue(renderer, fieldIndex, bannerModel);
        return renderer;
    }
}
