package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterBoatPatch extends ModelAdapterEntity {
    public ModelAdapterBoatPatch(EntityType entityType, String prefix, ModelLayerLocation modelLayer) {
        this(entityType, prefix + "_boat_patch", modelLayer, new String[]{"boat_patch"});
    }

    protected ModelAdapterBoatPatch(EntityType entityType, String name, ModelLayerLocation modelLayer, String[] aliases) {
        super(entityType, name, modelLayer);
        this.setAliases(aliases);
    }

    @Override
    public Model makeModel(ModelPart root) {
        root = bakeModelLayer(ModelLayers.BOAT_WATER_PATCH);
        return new Model.Simple(root, locIn -> RenderTypes.waterMask());
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("water_patch", "water_patch");
        map.put("root", "root");
        return map;
    }

    @Override
    public IEntityRenderer makeEntityRender(Model modelBase, RendererCache rendererCache, int index) {
        BoatRenderer customRenderer = new BoatRenderer(this.getEntityContext(), this.getModelLayer());
        BoatRenderer renderer = (BoatRenderer)rendererCache.get(this.getEntityType(), index, () -> customRenderer);
        if (!Reflector.RenderBoat_patchModel.exists()) {
            Config.warn("Field not found: RenderBoat.patchModel");
            return null;
        } else {
            Reflector.RenderBoat_patchModel.setValue(renderer, modelBase);
            return renderer;
        }
    }
}
