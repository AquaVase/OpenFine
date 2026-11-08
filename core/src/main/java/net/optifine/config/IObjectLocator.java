package net.optifine.config;

import net.minecraft.resources.Identifier;

public interface IObjectLocator<T> {
    T getObject(Identifier var1);
}
