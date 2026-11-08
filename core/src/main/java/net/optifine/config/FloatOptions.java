package net.optifine.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.optifine.Config;
import net.optifine.Lang;

public class FloatOptions {
    public static Component getTextComponent(OptionInstance option, double val) {
        Options settings = Minecraft.getInstance().options;
        String s = I18n.get(option.getResourceKey()) + ": ";
        if (option == settings.RENDER_DISTANCE) {
            return Options.genericValueLabel(option.getResourceKey(), "options.chunks", (int)val);
        }

        if (option == settings.MIPMAP_LEVELS) {
            if (val >= 4.0) {
                return Options.genericValueLabel(option.getResourceKey(), "of.general.max");
            } else {
                return val == 0.0 ? CommonComponents.optionStatus(option.getCaption(), false) : Options.genericValueLabel(option.getResourceKey(), (int)val);
            }
        } else if (option == settings.BIOME_BLEND_RADIUS) {
            int i = (int)val * 2 + 1;
            return Options.genericValueLabel(option.getResourceKey(), "options.biomeBlendRadius." + i);
        } else {
            String text = getText(option, val);
            return text != null ? Component.literal(text) : null;
        }
    }

    public static String getText(OptionInstance option, double val) {
        String s = I18n.get(option.getResourceKey()) + ": ";
        if (option == Option.AO_LEVEL) {
            return val == 0.0 ? s + I18n.get("options.off") : s + (int)(val * 100.0) + "%";
        }

        if (option == Option.MIPMAP_TYPE) {
            int valInt = (int)val;
            switch (valInt) {
                case 0:
                    return s + Lang.get("of.options.mipmap.nearest");
                case 1:
                    return s + Lang.get("of.options.mipmap.linear");
                case 2:
                    return s + Lang.get("of.options.mipmap.bilinear");
                case 3:
                    return s + Lang.get("of.options.mipmap.trilinear");
                default:
                    return s + "of.options.mipmap.nearest";
            }
        } else if (option == Option.AA_LEVEL) {
            int ofAaLevel = (int)val;
            String suffix = "";
            if (ofAaLevel != Config.getAntialiasingLevel()) {
                suffix = " (" + Lang.get("of.general.restart") + ")";
            }

            return ofAaLevel == 0 ? s + Lang.getOff() + suffix : s + ofAaLevel + suffix;
        } else if (option == Option.AF_LEVEL) {
            int ofAfLevel = (int)val;
            return ofAfLevel == 1 ? s + Lang.getOff() : s + ofAfLevel;
        } else {
            return null;
        }
    }

    public static boolean supportAdjusting(OptionInstance option) {
        Component text = getTextComponent(option, 0.0);
        return text != null;
    }
}
