package net.optifine.entity.model;

import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.nautilus.ZombieNautilusCoralModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.ZombieNautilusRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilusVariant.ModelType;

public class ModelAdapterZombieNautilusCoral extends ModelAdapterZombieNautilus {
    public ModelAdapterZombieNautilusCoral() {
        super(EntityTypes.ZOMBIE_NAUTILUS, "zombie_nautilus_coral", ModelLayers.ZOMBIE_NAUTILUS_CORAL, ModelType.WARM);
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new ZombieNautilusCoralModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = super.makeMapParts();
        map.put("corals", "corals");
        return map;
    }

    @Override
    protected LivingEntityRenderer makeLivingRenderer(Context context) {
        return new ZombieNautilusRenderer(context);
    }
}
