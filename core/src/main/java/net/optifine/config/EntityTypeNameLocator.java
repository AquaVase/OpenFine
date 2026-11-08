package net.optifine.config;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.optifine.util.EntityTypeUtils;

public class EntityTypeNameLocator implements IObjectLocator<String> {
    public String getObject(Identifier loc) {
        EntityType type = EntityTypeUtils.getEntityType(loc);
        return type == null ? null : type.getDescriptionId();
    }

    public static String getEntityTypeName(Entity entity) {
        return entity.getType().getDescriptionId();
    }
}
