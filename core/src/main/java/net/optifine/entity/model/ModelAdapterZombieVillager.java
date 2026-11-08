package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.zombie.ZombieVillagerModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.ZombieVillagerRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterZombieVillager extends ModelAdapterAgeableHumanoid {
    public ModelAdapterZombieVillager() {
        super(EntityTypes.ZOMBIE_VILLAGER, "zombie_villager", ModelLayers.ZOMBIE_VILLAGER);
    }

    protected ModelAdapterZombieVillager(EntityType entityType, String name, ModelLayerLocation modelLayer) {
        super(entityType, name, modelLayer);
    }

    @Override
    public ModelAdapter makeBaby() {
        ModelAdapterZombieVillager ma = new ModelAdapterZombieVillager(this.getEntityType(), "zombie_villager_baby", ModelLayers.ZOMBIE_VILLAGER_BABY);
        ma.setBaby(true);
        ma.setAlias(this.getName());
        return ma;
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new ZombieVillagerModel(root);
    }

    @Override
    protected AgeableMobRenderer makeAgeableRenderer(Context context) {
        return new ZombieVillagerRenderer(context);
    }
}
