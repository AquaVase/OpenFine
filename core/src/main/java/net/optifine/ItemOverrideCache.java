package net.optifine;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.optifine.util.CompoundKey;

public class ItemOverrideCache {
    private ItemOverrideProperty[] itemOverrideProperties;
    private Map<CompoundKey, Integer> mapModelIndexes = new HashMap<>();
    public static final Integer INDEX_NONE = new Integer(-1);

    public ItemOverrideCache(ItemOverrideProperty[] itemOverrideProperties) {
        this.itemOverrideProperties = itemOverrideProperties;
    }

    public Integer getModelIndex(ItemStack stack, ClientLevel world, LivingEntity entity) {
        CompoundKey valueKey = this.getValueKey(stack, world, entity);
        return valueKey == null ? null : this.mapModelIndexes.get(valueKey);
    }

    public void putModelIndex(ItemStack stack, ClientLevel world, LivingEntity entity, Integer index) {
        CompoundKey valueKey = this.getValueKey(stack, world, entity);
        if (valueKey != null) {
            this.mapModelIndexes.put(valueKey, index);
        }
    }

    private CompoundKey getValueKey(ItemStack stack, ClientLevel world, LivingEntity entity) {
        Integer[] indexes = new Integer[this.itemOverrideProperties.length];

        for (int i = 0; i < indexes.length; i++) {
            Integer index = this.itemOverrideProperties[i].getValueIndex(stack, world, entity);
            if (index == null) {
                return null;
            }

            indexes[i] = index;
        }

        return new CompoundKey(indexes);
    }

    @Override
    public String toString() {
        return "properties: " + this.itemOverrideProperties.length + ", modelIndexes: " + this.mapModelIndexes.size();
    }
}
