package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;

public abstract class ModelAdapterMultiLiving extends ModelAdapterLiving implements IModelAdapterAgeable {
    private Object type;
    private boolean baby;

    public ModelAdapterMultiLiving(EntityType entityType, String name, ModelLayerLocation modelLayer, Object type) {
        super(entityType, name, modelLayer);
        this.type = type;
    }

    @Override
    public ModelAdapter makeBaby() {
        return null;
    }

    @Override
    protected final void modifyLivingRenderer(LivingEntityRenderer renderer, Model modelBase) {
        this.modifyLivingRenderer(renderer, modelBase, this.type, this.baby);
    }

    protected abstract void modifyLivingRenderer(LivingEntityRenderer var1, Model var2, Object var3, boolean var4);

    public void setBaby(boolean baby) {
        this.baby = baby;
    }

    public boolean isBaby() {
        return this.baby;
    }
}
