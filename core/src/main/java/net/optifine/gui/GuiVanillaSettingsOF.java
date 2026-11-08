package net.optifine.gui;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.TextureFilteringMethod;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class GuiVanillaSettingsOF extends GuiScreenOF {
    private Screen prevScreen;
    private Options settings;
    private final int oldAnisotropyBit;
    private final TextureFilteringMethod oldTextureFiltering;
    private AbstractWidget buttonMaxAnisotropyBit;
    private TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderOptions());

    public GuiVanillaSettingsOF(Screen guiscreen, Options optionsIn) {
        super(Component.translatable("of.options.vanillaTitle"));
        this.prevScreen = guiscreen;
        this.settings = optionsIn;
        this.oldAnisotropyBit = optionsIn.maxAnisotropyBit().get();
        this.oldTextureFiltering = optionsIn.textureFiltering().get();
    }

    @Override
    public void init() {
        this.clearWidgets();
        OptionInstance[] options = new OptionInstance[]{
            this.settings.textureFiltering(), this.settings.maxAnisotropyBit(), this.settings.improvedTransparency(), this.settings.preferredGraphicsBackend()
        };

        for (int i = 0; i < options.length; i++) {
            OptionInstance opt = options[i];
            int x = this.width / 2 - 155 + i % 2 * 160;
            int y = this.height / 6 + 21 * (i / 2) - 12;
            y += 21;
            AbstractWidget guielement = this.addButton(opt.createButton(this.minecraft.options, x, y, 150));
            if (opt == this.settings.maxAnisotropyBit()) {
                this.buttonMaxAnisotropyBit = guielement;
            }

            guielement.setTooltip(null);
        }

        this.addButton(new GuiButtonOF(200, this.width / 2 - 100, this.height / 6 + 168 + 11, I18n.get("gui.done")));
    }

    @Override
    public void actionPerformed(AbstractWidget guiElement) {
        if (guiElement instanceof GuiButtonOF guibutton) {
            if (guibutton.active) {
                if (guibutton.id == 200) {
                    this.minecraft.options.save();
                    this.minecraft.gui.setScreen(this.prevScreen);
                }

                this.minecraft.resizeGui();
            }
        }
    }

    @Override
    public void removed() {
        this.minecraft.options.save();
        if (this.settings.maxAnisotropyBit().get() != this.oldAnisotropyBit || this.settings.textureFiltering().get() != this.oldTextureFiltering) {
            this.minecraft.updateMaxMipLevel(this.settings.mipmapLevels().get());
            this.minecraft.delayTextureReload();
        }

        super.removed();
    }

    @Override
    public void tick() {
        if (this.buttonMaxAnisotropyBit != null) {
            this.buttonMaxAnisotropyBit.active = this.settings.textureFiltering().get() == TextureFilteringMethod.ANISOTROPIC;
        }

        super.tick();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphicsIn, int x, int y, float partialTicks) {
        super.extractRenderState(graphicsIn, x, y, partialTicks);
        drawCenteredString(graphicsIn, this.minecraft.font, this.title, this.width / 2, 15, -1);
        this.tooltipManager.drawTooltips(graphicsIn, x, y, this.getButtonList());
    }
}
