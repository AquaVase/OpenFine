package net.optifine.gui;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.optifine.Lang;
import net.optifine.config.Option;

public class GuiQuickInfoOF extends GuiScreenOF {
    private Screen prevScreen;
    private TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderOptions());

    public GuiQuickInfoOF(Screen guiscreen) {
        super(Component.translatable("of.options.quickInfoTitle"));
        this.prevScreen = guiscreen;
    }

    @Override
    public void init() {
        this.clearWidgets();
        OptionInstance[] options = new OptionInstance[]{
            Option.QUICK_INFO,
            Option.QUICK_INFO_FPS,
            Option.QUICK_INFO_CHUNKS,
            Option.QUICK_INFO_ENTITIES,
            Option.QUICK_INFO_PARTICLES,
            Option.QUICK_INFO_UPDATES,
            Option.QUICK_INFO_GPU,
            Option.QUICK_INFO_POS,
            Option.QUICK_INFO_BIOME,
            Option.QUICK_INFO_FACING,
            Option.QUICK_INFO_LIGHT,
            Option.QUICK_INFO_MEMORY,
            Option.QUICK_INFO_NATIVE_MEMORY,
            Option.QUICK_INFO_TARGET_BLOCK,
            Option.QUICK_INFO_TARGET_FLUID,
            Option.QUICK_INFO_TARGET_ENTITY,
            Option.QUICK_INFO_LABELS,
            Option.QUICK_INFO_BACKGROUND
        };

        for (int i = 0; i < options.length; i++) {
            OptionInstance opt = options[i];
            if (opt != null) {
                int x = this.width / 2 - 155 + i % 2 * 160;
                int y = this.height / 6 + 21 * (i / 2) - 12;
                AbstractWidget guielement = this.addButton(opt.createButton(this.minecraft.options, x, y, 150));
                guielement.setTooltip(null);
            }
        }

        this.addButton(new GuiButtonOF(210, this.width / 2 - 155, this.height / 6 + 168 + 11, 70, 20, Lang.get("of.options.animation.allOn")));
        this.addButton(new GuiButtonOF(211, this.width / 2 - 155 + 80, this.height / 6 + 168 + 11, 70, 20, Lang.get("of.options.animation.allOff")));
        this.addButton(new GuiScreenButtonOF(200, this.width / 2 + 5, this.height / 6 + 168 + 11, I18n.get("gui.done")));
        this.updateSubOptions();
    }

    @Override
    public void actionPerformed(AbstractWidget guiElement) {
        this.updateSubOptions();
        if (guiElement instanceof GuiButtonOF guibutton) {
            if (guibutton.active) {
                if (guibutton.id == 200) {
                    this.minecraft.options.save();
                    this.minecraft.gui.setScreen(this.prevScreen);
                }

                if (guibutton.id == 210) {
                    this.minecraft.options.setAllQuickInfos(true);
                }

                if (guibutton.id == 211) {
                    this.minecraft.options.setAllQuickInfos(false);
                }

                this.minecraft.resizeGui();
            }
        }
    }

    private void updateSubOptions() {
        boolean enabled = this.settings.ofQuickInfo;

        for (AbstractWidget aw : this.getButtonList()) {
            if (aw instanceof IOptionControl oc && oc.getControlOption() != Option.QUICK_INFO) {
                aw.active = enabled;
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
