package net.optifine.entity.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.optifine.reflect.Reflector;

public class ModelAdapterBannerFlag extends ModelAdapterBlockEntity {
    private boolean standing;
    private Map<String, String> mapParts = this.makeMapParts();

    public ModelAdapterBannerFlag(boolean standing) {
        super(BlockEntityTypes.BANNER, "banner_flag" + (standing ? "" : "_wall"));
        this.standing = standing;
        if (!standing) {
            this.setAliases("banner_flag", "banner");
        } else {
            this.setAlias("banner");
        }
    }

    @Override
    public Model makeModel() {
        return new BannerFlagModel(bakeModelLayer(this.standing ? ModelLayers.STANDING_BANNER_FLAG : ModelLayers.WALL_BANNER_FLAG));
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
        List<String> names = new ArrayList<>(ModelAdapterBannerBase.getAllBannerPartNames());
        names.removeAll(this.mapParts.keySet());
        return toArray(names);
    }

    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("slate", "flag");
        map.put("root", "root");
        return map;
    }

    @Override
    public IEntityRenderer makeEntityRender(Model model, RendererCache rendererCache, int index) {
        BlockEntityRenderer renderer = rendererCache.get(BlockEntityTypes.BANNER, index, () -> new BannerRenderer(this.getContext()));
        BannerFlagModel bannerFlagModel = (BannerFlagModel)model;
        if (!Reflector.TileEntityBannerRenderer_bannerFlagModels.exists()) {
            throw new IllegalArgumentException("Field not found: BannerRenderer.bannerFlagModels");
        }

        int fieldIndex = this.standing ? 0 : 1;
        Reflector.TileEntityBannerRenderer_bannerFlagModels.setValue(renderer, fieldIndex, bannerFlagModel);
        return renderer;
    }
}
