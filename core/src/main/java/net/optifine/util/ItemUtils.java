package net.optifine.util;

import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;
import net.optifine.reflect.Reflector;

public class ItemUtils {
    private static CompoundTag EMPTY_TAG = new CompoundTag();

    public static Item getItem(Identifier loc) {
        return !BuiltInRegistries.ITEM.containsKey(loc) ? null : BuiltInRegistries.ITEM.getValue(loc);
    }

    public static int getId(Item item) {
        return BuiltInRegistries.ITEM.getId(item);
    }

    public static CompoundTag getTag(ItemStack itemStack) {
        if (itemStack == null) {
            return EMPTY_TAG;
        }

        PatchedDataComponentMap components = (PatchedDataComponentMap)Reflector.ItemStack_components.getValue(itemStack);
        return components == null ? EMPTY_TAG : components.getTag();
    }

    public static Equippable getEquippable(Item item) {
        return !item.builtInRegistryHolder().areComponentsBound() ? null : item.components().get(DataComponents.EQUIPPABLE);
    }

    public static String getMaterial(Equippable equip) {
        Optional<ResourceKey<EquipmentAsset>> optAssetKey = equip.assetId();
        return !optAssetKey.isPresent() ? null : optAssetKey.get().identifier().getPath();
    }

    public static String getEquippableMaterial(Item item) {
        Equippable equip = getEquippable(item);
        return equip == null ? null : getMaterial(equip);
    }
}
