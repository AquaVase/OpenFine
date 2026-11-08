package net.optifine.gui;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.optifine.config.Option;

public class GuiPerformanceSettingsOF extends GuiScreenOF {
    private Screen prevScreen;
    private Options settings;
    private TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderOptions());

    public GuiPerformanceSettingsOF(Screen guiscreen, Options gamesettings) {
        super(Component.literal(I18n.get("of.options.performanceTitle")));
        this.prevScreen = guiscreen;
        this.settings = gamesettings;
    }

    @Override
    public void init() {
        this.clearWidgets();
        OptionInstance[] options = new OptionInstance[]{
            Option.RENDER_REGIONS,
            Option.FAST_RENDER,
            Option.SMART_ANIMATIONS,
            Option.FAST_MATH,
            Option.SMOOTH_FPS,
            Option.SMOOTH_WORLD,
            Option.CHUNK_UPDATES,
            Option.CHUNK_UPDATES_DYNAMIC,
            Option.LAZY_CHUNK_LOADING,
            this.settings.PRIORITIZE_CHUNK_UPDATES
        };

        for (int i = 0; i < options.length; i++) {
            OptionInstance option = options[i];
            int x = this.width / 2 - 155 + i % 2 * 160;
            int y = this.height / 6 + 21 * (i / 2) - 12;
            AbstractWidget guielement = this.addButton(option.createButton(this.minecraft.options, x, y, 150));
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
            }
        }
    }

    @Override
    public void removed() {
        this.minecraft.options.save();
        super.removed();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphicsIn, int x, int y, float partialTicks) {
        super.extractRenderState(graphicsIn, x, y, partialTicks);
        drawCenteredString(graphicsIn, this.fontRenderer, this.title, this.width / 2, 15, -1);
        this.tooltipManager.drawTooltips(graphicsIn, x, y, this.getButtonList());
    }
}
