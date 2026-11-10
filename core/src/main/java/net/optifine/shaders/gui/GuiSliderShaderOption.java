package net.optifine.shaders.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.optifine.shaders.config.ShaderOption;

public class GuiSliderShaderOption extends GuiButtonShaderOption {
    private float sliderValue;
    public boolean dragging;
    private ShaderOption shaderOption = null;
    private static final Identifier SLIDER_SPRITE = new Identifier("widget/slider");
    private static final Identifier HIGHLIGHTED_SPRITE = new Identifier("widget/slider_highlighted");
    private static final Identifier SLIDER_HANDLE_SPRITE = new Identifier("widget/slider_handle");
    private static final Identifier SLIDER_HANDLE_HIGHLIGHTED_SPRITE = new Identifier("widget/slider_handle_highlighted");

    public GuiSliderShaderOption(int buttonId, int x, int y, int w, int h, ShaderOption shaderOption, String text) {
        super(buttonId, x, y, w, h, shaderOption, text);
        this.sliderValue = 1.0F;
        this.shaderOption = shaderOption;
        this.sliderValue = shaderOption.getIndexNormalized();
        this.setMessage(GuiShaderOptions.getButtonText(shaderOption, this.width));
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphicsIn, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            if (this.dragging && !Minecraft.getInstance().hasShiftDown()) {
                this.sliderValue = (float)(mouseX - (this.getX() + 4)) / (this.width - 8);
                this.sliderValue = Mth.clamp(this.sliderValue, 0.0F, 1.0F);
                this.shaderOption.setIndexNormalized(this.sliderValue);
                this.sliderValue = this.shaderOption.getIndexNormalized();
                this.setMessage(GuiShaderOptions.getButtonText(this.shaderOption, this.width));
            }

            graphicsIn.blitSprite(RenderPipelines.GUI_TEXTURED, this.getSprite(), this.getX(), this.getY(), this.getWidth(), this.getHeight());
            graphicsIn.blitSprite(
                RenderPipelines.GUI_TEXTURED, this.getHandleSprite(), this.getX() + (int)(this.sliderValue * (this.width - 8)), this.getY(), 8, this.getHeight()
            );
            this.extractScrollingStringOverContents(graphicsIn.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE), this.message, 2);
        }
    }

    private Identifier getSprite() {
        return this.isFocused() && !this.dragging ? HIGHLIGHTED_SPRITE : SLIDER_SPRITE;
    }

    private Identifier getHandleSprite() {
        return !this.isHovered && !this.dragging ? SLIDER_HANDLE_SPRITE : SLIDER_HANDLE_HIGHLIGHTED_SPRITE;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent eventIn, boolean doubleIn) {
        if (super.mouseClicked(eventIn, doubleIn)) {
            this.sliderValue = (float)(eventIn.x() - (this.getX() + 4)) / (this.width - 8);
            this.sliderValue = Mth.clamp(this.sliderValue, 0.0F, 1.0F);
            this.shaderOption.setIndexNormalized(this.sliderValue);
            this.setMessage(GuiShaderOptions.getButtonText(this.shaderOption, this.width));
            this.dragging = true;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent eventIn) {
        this.dragging = false;
        return true;
    }

    @Override
    public void valueChanged() {
        this.sliderValue = this.shaderOption.getIndexNormalized();
    }

    @Override
    public boolean isSwitchable() {
        return false;
    }
}
