package net.optifine.entity.model.anim;

import java.util.HashMap;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.optifine.render.RenderState;

public class EntityVariableBool implements IModelVariableBool {
    private String name;

    public EntityVariableBool(String name) {
        this.name = name;
    }

    @Override
    public boolean eval() {
        return this.getValue();
    }

    @Override
    public boolean getValue() {
        SynchedEntityData entityData = this.getEntityData();
        if (entityData == null) {
            return false;
        }

        if (entityData.modelVariables == null) {
            return false;
        }

        Boolean val = (Boolean)entityData.modelVariables.get(this.name);
        return val == null ? false : val;
    }

    @Override
    public void setValue(boolean value) {
        SynchedEntityData entityData = this.getEntityData();
        if (entityData != null) {
            if (entityData.modelVariables == null) {
                entityData.modelVariables = new HashMap<>();
            }

            entityData.modelVariables.put(this.name, value);
        }
    }

    private SynchedEntityData getEntityData() {
        Entity entity = RenderState.getEntity();
        return entity == null ? null : entity.getEntityData();
    }
}
