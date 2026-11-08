package net.optifine.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

public class EntityTypeUtils {
    public static EntityType getEntityType(Identifier loc) {
        return !BuiltInRegistries.ENTITY_TYPE.containsKey(loc) ? null : BuiltInRegistries.ENTITY_TYPE.getValue(loc);
    }
}
