package net.optifine.config;

import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.optifine.Lang;

public class IterableOptionBool extends IteratableOptionOF implements IPersitentOption {
    private String resourceKey;
    private ToBooleanFunction<Options> getter;
    private ObjBooleanConsumer<Options> setter;
    private String saveKey;

    public IterableOptionBool(String resourceKey, ToBooleanFunction<Options> getter, ObjBooleanConsumer<Options> setter, String saveKey) {
        super(resourceKey);
        this.resourceKey = resourceKey;
        this.getter = getter;
        this.setter = setter;
        this.saveKey = saveKey;
    }

    @Override
    public void nextOptionValue(int dirIn) {
        Options opts = this.getOptions();
        boolean val = this.getter.applyAsBool(opts);
        val = !val;
        this.setter.accept(opts, val);
    }

    @Override
    public Component getOptionText() {
        Options opts = this.getOptions();
        String optionLabel = Lang.get(this.resourceKey) + ": ";
        boolean val = this.getter.applyAsBool(opts);
        String valueLabel = val ? Lang.getOn() : Lang.getOff();
        String label = optionLabel + valueLabel;
        return Component.literal(label);
    }

    @Override
    public String getSaveKey() {
        return this.saveKey;
    }

    @Override
    public void loadValue(Options opts, String s) {
        boolean val = Boolean.valueOf(s);
        this.setter.accept(opts, val);
    }

    @Override
    public String getSaveText(Options opts) {
        boolean val = this.getter.applyAsBool(opts);
        return Boolean.toString(val);
    }
}
