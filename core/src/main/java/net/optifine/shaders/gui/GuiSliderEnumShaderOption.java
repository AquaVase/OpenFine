package net.optifine.shaders.gui;

import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.optifine.shaders.config.EnumShaderOption;

public class GuiSliderEnumShaderOption extends GuiButtonEnumShaderOption {
    private static final Identifier SLIDER_SPRITE = new Identifier("widget/slider");
    private static final Identifier HIGHLIGHTED_SPRITE = new Identifier("widget/slider_highlighted");
    private static final Identifier HANDLE_SPRITE = new Identifier("widget/slider_handle");
    private static final Identifier HANDLE_HIGHLIGHTED_SPRITE = new Identifier("widget/slider_handle_highlighted");
    private final float[] values;
    private final Consumer<Float> onApply;
    private float appliedValue;
    private int valueIndex;
    private boolean dragging;
    private boolean keyboardChanging;

    public GuiSliderEnumShaderOption(EnumShaderOption option, int x, int y, int width, int height, float[] values, float value, Consumer<Float> onApply) {
        super(option, x, y, width, height);
        this.values = values;
        this.onApply = onApply;
        this.appliedValue = value;
        this.valueIndex = GuiShaders.getValueIndex(value, values);
        this.updateButtonText();
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        boolean highlighted = this.active && (this.isHovered || this.dragging || this.isFocused());
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.isFocused() && !this.dragging ? HIGHLIGHTED_SPRITE : SLIDER_SPRITE,
            this.getX(), this.getY(), this.width, this.height);
        int handleX = this.getX() + Math.round((float)this.valueIndex / (this.values.length - 1) * (this.width - 8));
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, highlighted ? HANDLE_HIGHLIGHTED_SPRITE : HANDLE_SPRITE,
            handleX, this.getY(), 8, this.height);
        this.extractScrollingStringOverContents(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE), this.getMessage(), 2);
    }

    @Override
    protected boolean isValidClickButton(MouseButtonInfo info) {
        return info.button() == 0;
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (!this.active || event.button() != 0) {
            return;
        }
        this.dragging = true;
        this.keyboardChanging = false;
        this.setValueFromMouse(event);
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dx, double dy) {
        if (this.dragging) {
            this.setValueFromMouse(event);
        }
    }

    private void setValueFromMouse(MouseButtonEvent event) {
        if ((event.modifiers() & 1) != 0) {
            this.valueIndex = GuiShaders.getValueIndex(this.getEnumShaderOption().getDefaultFloat(), this.values);
        } else {
            double value = (event.x() - (this.getX() + 4)) / Math.max(1, this.width - 8);
            this.valueIndex = (int)Math.round(Mth.clamp(value, 0.0, 1.0) * (this.values.length - 1));
        }
        this.updateButtonText();
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        if (this.dragging && event.button() == 0) {
            this.setValueFromMouse(event);
            this.dragging = false;
            this.applyValue();
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!this.active || this.dragging || event.key() != 263 && event.key() != 262) {
            return false;
        }
        this.valueIndex = Mth.clamp(this.valueIndex + (event.key() == 263 ? -1 : 1), 0, this.values.length - 1);
        this.keyboardChanging = true;
        this.updateButtonText();
        return true;
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (this.keyboardChanging && (event.key() == 263 || event.key() == 262)) {
            this.keyboardChanging = false;
            this.applyValue();
            return true;
        }
        return false;
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            this.dragging = false;
            this.keyboardChanging = false;
            this.valueIndex = GuiShaders.getValueIndex(this.appliedValue, this.values);
            this.updateButtonText();
        }
    }

    private void applyValue() {
        float value = this.values[this.valueIndex];
        if (this.appliedValue != value) {
            this.appliedValue = value;
            this.onApply.accept(value);
        }
    }

    @Override
    public void updateButtonText() {
        EnumShaderOption option = this.getEnumShaderOption();
        float value = this.values[this.valueIndex];
        String text = option == EnumShaderOption.HAND_DEPTH_MUL
            ? GuiShaders.toStringHandDepth(value, option.getDefaultFloat()) : GuiShaders.toStringQuality(value, option.getDefaultFloat());
        this.setMessage(I18n.get(option.getResourceKey()) + ": " + text);
    }
}
