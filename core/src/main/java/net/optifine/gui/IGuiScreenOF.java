package net.optifine.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.MouseButtonEvent;
import net.optifine.Config;
import net.optifine.util.GuiUtils;

public interface IGuiScreenOF extends ContainerEventHandler {
    default List<AbstractWidget> getButtonList() {
        List<AbstractWidget> buttons = new ArrayList<>();

        for (GuiEventListener gel : this.children()) {
            if (gel instanceof AbstractWidget) {
                buttons.add((AbstractWidget)gel);
            }
        }

        return buttons;
    }

    default void actionPerformed(AbstractWidget button) {
    }

    default void actionPerformedRightClick(AbstractWidget button) {
    }

    boolean isMousePressed();

    void setMousePressed(boolean var1);

    @Override
    default boolean mouseClicked(MouseButtonEvent eventIn, boolean doubleIn) {
        AbstractWidget btn = getSelectedButton((int)eventIn.x(), (int)eventIn.y(), this.getButtonList());
        boolean ret = ContainerEventHandler.super.mouseClicked(eventIn, doubleIn);
        this.setMousePressed(true);
        if (btn != null && btn.active) {
            if (eventIn.button() == 1 && btn instanceof IOptionControl ioc && ioc.getControlOption() == Config.getGameSettings().GUI_SCALE) {
                btn.playDownSound(Config.getMinecraft().getSoundManager());
            }

            if (eventIn.button() == 0) {
                this.actionPerformed(btn);
            } else if (eventIn.button() == 1) {
                this.actionPerformedRightClick(btn);
            }

            return true;
        } else {
            return ret;
        }
    }

    @Override
    default boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        boolean ret = ContainerEventHandler.super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
        AbstractWidget btn = getSelectedButton((int)mouseX, (int)mouseY, this.getButtonList());
        if (btn != null && btn.active && btn instanceof IOptionControl) {
            this.actionPerformed(btn);
            return true;
        } else {
            return ret;
        }
    }

    @Override
    default boolean mouseReleased(MouseButtonEvent eventIn) {
        if (!this.isMousePressed()) {
            return false;
        }

        this.setMousePressed(false);
        this.setDragging(false);
        return this.getFocused() != null && this.getFocused().mouseReleased(eventIn) ? true : ContainerEventHandler.super.mouseReleased(eventIn);
    }

    @Override
    default boolean mouseDragged(MouseButtonEvent eventIn, double mouseX, double mouseY) {
        return !this.isMousePressed() ? false : ContainerEventHandler.super.mouseDragged(eventIn, mouseX, mouseY);
    }

    static AbstractWidget getSelectedButton(int x, int y, List<AbstractWidget> listButtons) {
        for (int i = 0; i < listButtons.size(); i++) {
            AbstractWidget btn = listButtons.get(i);
            if (btn.visible) {
                int btnWidth = GuiUtils.getWidth(btn);
                int btnHeight = GuiUtils.getHeight(btn);
                if (x >= btn.getX() && y >= btn.getY() && x < btn.getX() + btnWidth && y < btn.getY() + btnHeight) {
                    return btn;
                }
            }
        }

        return null;
    }
}
