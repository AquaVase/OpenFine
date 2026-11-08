package net.optifine.entity.model.anim;

import java.util.HashMap;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.optifine.render.RenderState;

public class EntityVariableFloat implements IModelVariableFloat {
    private String name;

    public EntityVariableFloat(String name) {
        this.name = name;
    }

    @Override
    public float eval() {
        return this.getValue();
    }

    @Override
    public float getValue() {
        return getEntityValue(this.name);
    }

    public static float getEntityValue(String key) {
        SynchedEntityData entityData = getEntityData();
        if (entityData == null) {
            return 0.0F;
        }

        if (entityData.modelVariables == null) {
            return 0.0F;
        }

        Float val = (Float)entityData.modelVariables.get(key);
        return val == null ? 0.0F : val;
    }

    @Override
    public void setValue(float value) {
        setEntityValue(this.name, value);
    }

    public static void setEntityValue(String key, float value) {
        SynchedEntityData entityData = getEntityData();
        if (entityData != null) {
            if (entityData.modelVariables == null) {
                entityData.modelVariables = new HashMap<>();
            }

            entityData.modelVariables.put(key, value);
        }
    }

    private static SynchedEntityData getEntityData() {
        Entity entity = RenderState.getEntity();
        return entity == null ? null : entity.getEntityData();
    }
}
