package net.optifine.entity.model;

import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.nautilus.NautilusModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.ZombieNautilusRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilusVariant.ModelType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterZombieNautilus extends ModelAdapterMultiLiving {
    public ModelAdapterZombieNautilus() {
        super(EntityTypes.ZOMBIE_NAUTILUS, "zombie_nautilus", ModelLayers.ZOMBIE_NAUTILUS, ModelType.NORMAL);
    }

    protected ModelAdapterZombieNautilus(EntityType entityType, String name, ModelLayerLocation modelLayer, ModelType type) {
        super(entityType, name, modelLayer, type);
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new NautilusModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        return ModelAdapterNautilus.makeStaticMapParts();
    }

    @Override
    protected LivingEntityRenderer makeLivingRenderer(Context context) {
        return new ZombieNautilusRenderer(context);
    }

    @Override
    protected void modifyLivingRenderer(LivingEntityRenderer renderer, Model modelBase, Object type, boolean baby) {
        ZombieNautilusRenderer znRenderer = (ZombieNautilusRenderer)renderer;
        NautilusModel nautilusModel = (NautilusModel)modelBase;
        ModelType znType = (ModelType)type;
        Map<ModelType, NautilusModel> models = (Map<ModelType, NautilusModel>)Reflector.ZombieNautilusRenderer_models.getValue(znRenderer);
        if (models == null) {
            Config.warn("ZombieNautilusRenderer.models not found.");
        } else {
            models.put(znType, nautilusModel);
        }
    }
}
