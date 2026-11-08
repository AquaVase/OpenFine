package net.optifine.gui;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.optifine.config.Option;

public class GuiDetailSettingsOF extends GuiScreenOF {
    private Screen prevScreen;
    private Options settings;
    private TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderOptions());

    public GuiDetailSettingsOF(Screen guiscreen, Options gamesettings) {
        super(Component.literal(I18n.get("of.options.detailsTitle")));
        this.prevScreen = guiscreen;
        this.settings = gamesettings;
    }

    @Override
    public void init() {
        this.clearWidgets();
        OptionInstance[] options = new OptionInstance[]{
            this.settings.CLOUDS,
            Option.CLOUD_HEIGHT,
            Option.TREES,
            this.settings.CLOUDS_RANGE,
            Option.SKY,
            Option.STARS,
            Option.SUN_MOON,
            this.settings.WEATHER_RADIUS,
            Option.FOG_FANCY,
            Option.FOG_START,
            Option.SHOW_CAPES,
            Option.HELD_ITEM_TOOLTIPS,
            this.settings.CHUNK_FADE,
            Option.SWAMP_COLORS,
            this.settings.VIGNETTE,
            Option.ALTERNATE_BLOCKS,
            this.settings.ENTITY_DISTANCE_SCALING,
            this.settings.BIOME_BLEND_RADIUS
        };

        for (int i = 0; i < options.length; i++) {
            OptionInstance opt = options[i];
            int x = this.width / 2 - 155 + i % 2 * 160;
            int y = this.height / 6 + 21 * (i / 2) - 12;
            AbstractWidget guielement = this.addButton(opt.createButton(this.minecraft.options, x, y, 150));
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
        drawCenteredString(graphicsIn, this.minecraft.font, this.title, this.width / 2, 15, -1);
        this.tooltipManager.drawTooltips(graphicsIn, x, y, this.getButtonList());
    }
}
