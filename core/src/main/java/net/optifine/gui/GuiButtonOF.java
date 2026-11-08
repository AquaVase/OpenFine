package net.optifine.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.CreateNarration;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.network.chat.Component;

public class GuiButtonOF extends Button {
    public final int id;

    public GuiButtonOF(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, OnPress pressable, CreateNarration narrationIn) {
        super(x, y, widthIn, heightIn, Component.literal(buttonText), pressable, narrationIn);
        this.id = buttonId;
    }

    public GuiButtonOF(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        this(buttonId, x, y, widthIn, heightIn, buttonText, btn -> {}, DEFAULT_NARRATION);
    }

    public GuiButtonOF(int buttonId, int x, int y, String buttonText) {
        this(buttonId, x, y, 200, 20, buttonText, btn -> {}, DEFAULT_NARRATION);
    }

    public void setMessage(String messageIn) {
        super.setMessage(Component.literal(messageIn));
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphicsIn, int mouseX, int mouseY, float partialTicks) {
        this.extractDefaultSprite(graphicsIn);
        this.extractDefaultLabel(graphicsIn.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
    }
}
