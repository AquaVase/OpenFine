package net.optifine.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class GuiScreenOF extends Screen implements IGuiScreenOF {
    protected Font fontRenderer = Minecraft.getInstance().font;
    protected boolean mousePressed = false;
    protected Options settings = Minecraft.getInstance().options;

    public GuiScreenOF(Component title) {
        super(title);
    }

    @Override
    public boolean isMousePressed() {
        return this.mousePressed;
    }

    @Override
    public void setMousePressed(boolean pressed) {
        this.mousePressed = pressed;
    }

    public static void drawString(GuiGraphicsExtractor graphicsIn, Font fontRendererIn, String textIn, int xIn, int yIn, int colorIn) {
        graphicsIn.text(fontRendererIn, textIn, xIn, yIn, colorIn);
    }

    public static void drawCenteredString(GuiGraphicsExtractor graphicsIn, Font fontRendererIn, FormattedCharSequence textIn, int xIn, int yIn, int colorIn) {
        graphicsIn.centeredText(fontRendererIn, textIn, xIn, yIn, colorIn);
    }

    public static void drawCenteredString(GuiGraphicsExtractor graphicsIn, Font fontRendererIn, String textIn, int xIn, int yIn, int colorIn) {
        graphicsIn.centeredText(fontRendererIn, textIn, xIn, yIn, colorIn);
    }

    public static void drawCenteredString(GuiGraphicsExtractor graphicsIn, Font fontRendererIn, Component textIn, int xIn, int yIn, int colorIn) {
        graphicsIn.centeredText(fontRendererIn, textIn, xIn, yIn, colorIn);
    }

    public static boolean hasShiftDown() {
        return Minecraft.getInstance().hasShiftDown();
    }

    public static boolean hasAltDown() {
        return Minecraft.getInstance().hasAltDown();
    }

    protected <T extends GuiEventListener & Renderable & NarratableEntry> T addButton(T buttonIn) {
        this.addRenderableWidget(buttonIn);
        return buttonIn;
    }
}
