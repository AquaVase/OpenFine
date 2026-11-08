package net.optifine.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class IteratableOptionOF extends OptionInstance {
    public IteratableOptionOF(String nameIn) {
        super(
            nameIn,
            OptionInstance.noTooltip(),
            (labelIn, valueIn) -> (Boolean)valueIn ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF,
            OptionInstance.BOOLEAN_VALUES,
            false,
            valueIn -> {}
        );
    }

    public void nextOptionValue(int dirIn) {
        Options gameSetings = Minecraft.getInstance().options;
        gameSetings.setOptionValueOF(this, dirIn);
    }

    public Component getOptionText() {
        Options gameSetings = Minecraft.getInstance().options;
        return gameSetings.getKeyComponentOF(this);
    }

    protected Options getOptions() {
        return Minecraft.getInstance().options;
    }
}
