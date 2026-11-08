package net.optifine;

import java.util.Arrays;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class ItemOverrideProperty {
    private Identifier location;
    private float[] values;

    public ItemOverrideProperty(Identifier location, float[] values) {
        this.location = location;
        this.values = (float[])values.clone();
        Arrays.sort(this.values);
    }

    public Integer getValueIndex(ItemStack stack, ClientLevel world, LivingEntity entity) {
        return null;
    }

    public Identifier getLocation() {
        return this.location;
    }

    public float[] getValues() {
        return this.values;
    }

    @Override
    public String toString() {
        return "location: " + this.location + ", values: [" + Config.arrayToString(this.values) + "]";
    }
}
