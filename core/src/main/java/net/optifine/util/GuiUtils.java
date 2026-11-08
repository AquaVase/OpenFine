package net.optifine.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

public class GuiUtils {
    public static int getWidth(AbstractWidget widget) {
        return widget.getWidth();
    }

    public static int getHeight(AbstractWidget widget) {
        return widget.getHeight();
    }

    public static List<AbstractWidget> getButtonList(Screen screen) {
        List<AbstractWidget> buttons = new ArrayList<>();

        for (GuiEventListener gel : screen.children()) {
            if (gel instanceof AbstractWidget) {
                buttons.add((AbstractWidget)gel);
            }
        }

        return buttons;
    }
}
