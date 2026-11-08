package net.optifine.config;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.optifine.util.ItemUtils;

public class ItemLocator implements IObjectLocator<Item> {
    public Item getObject(Identifier loc) {
        return ItemUtils.getItem(loc);
    }
}
