package net.optifine.entity.model;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.optifine.util.Either;

public abstract class ModelAdapterVirtual extends ModelAdapter {
    protected ModelAdapterVirtual(String name) {
        super((Either<EntityType, BlockEntityType>)null, name);
    }
}
