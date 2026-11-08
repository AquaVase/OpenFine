package net.optifine.util;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.optifine.reflect.Reflector;

public class PotionUtils {
    public static MobEffect getPotion(Identifier loc) {
        return !BuiltInRegistries.MOB_EFFECT.containsKey(loc) ? null : BuiltInRegistries.MOB_EFFECT.getValue(loc);
    }

    public static MobEffect getPotion(int potionID) {
        if (potionID == 0) {
            return null;
        }

        int rawId = potionID - 1;
        return BuiltInRegistries.MOB_EFFECT.byId(rawId);
    }

    public static int getId(MobEffect potionIn) {
        int rawId = BuiltInRegistries.MOB_EFFECT.getId(potionIn);
        return rawId < 0 ? -1 : rawId + 1;
    }

    public static String getPotionBaseName(Potion p) {
        return p == null ? null : (String)Reflector.Potion_baseName.getValue(p);
    }

    public static Potion getPotion(ItemStack itemStack) {
        PotionContents pc = itemStack.get(DataComponents.POTION_CONTENTS);
        if (pc == null) {
            return null;
        }

        Optional<Holder<Potion>> opt = pc.potion();
        if (opt.isEmpty()) {
            return null;
        }

        Holder<Potion> holder = opt.get();
        return !holder.isBound() ? null : holder.value();
    }
}
