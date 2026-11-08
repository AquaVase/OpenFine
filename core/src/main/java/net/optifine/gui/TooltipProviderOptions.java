package net.optifine.gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.optifine.Lang;

public class TooltipProviderOptions implements TooltipProvider {
    @Override
    public Rectangle getTooltipBounds(Screen guiScreen, int x, int y) {
        int x1 = guiScreen.width / 2 - 150;
        int y1 = guiScreen.height / 6 - 7;
        if (y <= y1 + 98) {
            y1 += 105;
        }

        int x2 = x1 + 150 + 150;
        int y2 = y1 + 84 + 10;
        return new Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

    @Override
    public boolean isRenderBorder() {
        return false;
    }

    @Override
    public String[] getTooltipLines(AbstractWidget btn, int width) {
        if (!(btn instanceof IOptionControl ctl)) {
            return null;
        } else {
            OptionInstance option = ctl.getControlOption();
            return option == null ? null : getTooltipLines(option.getResourceKey());
        }
    }

    public static String[] getTooltipLines(String key) {
        List<String> list = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            String lineKey = key + ".tooltip." + (i + 1);
            String line = Lang.get(lineKey, null);
            if (line == null) {
                break;
            }

            list.add(line);
        }

        return list.size() <= 0 ? null : list.toArray(new String[list.size()]);
    }
}
