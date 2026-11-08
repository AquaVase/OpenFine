package net.optifine.shaders.gui;

import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.optifine.Config;
import net.optifine.Lang;
import net.optifine.gui.GuiButtonOF;
import net.optifine.gui.GuiScreenOF;
import net.optifine.gui.TooltipManager;
import net.optifine.gui.TooltipProviderShaderOptions;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.config.ShaderOption;
import net.optifine.shaders.config.ShaderOptionProfile;
import net.optifine.shaders.config.ShaderOptionScreen;

public class GuiShaderOptions extends GuiScreenOF {
    private Screen prevScreen;
    private Options settings;
    private TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderShaderOptions());
    private String screenName = null;
    private String screenText = null;
    private boolean changed = false;
    public static final String OPTION_PROFILE = "<profile>";
    public static final String OPTION_EMPTY = "<empty>";
    public static final String OPTION_REST = "*";

    public GuiShaderOptions(Screen guiscreen, Options gamesettings) {
        super(Component.literal(I18n.get("of.options.shaderOptionsTitle")));
        this.prevScreen = guiscreen;
        this.settings = gamesettings;
    }

    public GuiShaderOptions(Screen guiscreen, Options gamesettings, String screenName) {
        this(guiscreen, gamesettings);
        this.screenName = screenName;
        if (screenName != null) {
            this.screenText = Shaders.translate("screen." + screenName, screenName);
        }
    }

    @Override
    public void init() {
        int baseId = 100;
        int baseX = 0;
        int baseY = 30;
        int stepY = 20;
        int btnWidth = 120;
        int btnHeight = 20;
        int columns = Shaders.getShaderPackColumns(this.screenName, 2);
        ShaderOption[] ops = Shaders.getShaderPackOptions(this.screenName);
        if (ops != null) {
            int colsMin = Mth.ceil(ops.length / 9.0);
            if (columns < colsMin) {
                columns = colsMin;
            }

            for (int i = 0; i < ops.length; i++) {
                ShaderOption so = ops[i];
                if (so != null && so.isVisible()) {
                    int col = i % columns;
                    int row = i / columns;
                    int colWidth = Math.min(this.width / columns, 200);
                    baseX = (this.width - colWidth * columns) / 2;
                    int x = col * colWidth + 5 + baseX;
                    int y = baseY + row * stepY;
                    int w = colWidth - 10;
                    int h = btnHeight;
                    String text = getButtonText(so, w);
                    GuiButtonShaderOption btn;
                    if (Shaders.isShaderPackOptionSlider(so.getName())) {
                        btn = new GuiSliderShaderOption(baseId + i, x, y, w, h, so, text);
                    } else {
                        btn = new GuiButtonShaderOption(baseId + i, x, y, w, h, so, text);
                    }

                    btn.active = so.isEnabled();
                    this.addButton(btn);
                }
            }
        }

        this.addButton(new GuiButtonOF(201, this.width / 2 - btnWidth - 20, this.height / 6 + 168 + 11, btnWidth, btnHeight, I18n.get("controls.reset")));
        this.addButton(new GuiButtonOF(200, this.width / 2 + 20, this.height / 6 + 168 + 11, btnWidth, btnHeight, I18n.get("gui.done")));
    }

    public static String getButtonText(ShaderOption so, int btnWidth) {
        String labelName = so.getNameText();
        if (so instanceof ShaderOptionScreen soScr) {
            return labelName + "...";
        } else {
            Font fr = Config.getMinecraft().font;
            int lenSuffix = fr.width(": " + Lang.getOff()) + 5;

            while (fr.width(labelName) + lenSuffix >= btnWidth && labelName.length() > 0) {
                labelName = labelName.substring(0, labelName.length() - 1);
            }

            String col = so.isChanged() ? so.getValueColor(so.getValue()) : "";
            String labelValue = so.getValueText(so.getValue());
            return labelName + ": " + col + labelValue;
        }
    }

    @Override
    public void actionPerformed(AbstractWidget guiElement) {
        if (guiElement instanceof GuiButtonOF guibutton) {
            if (guibutton.active) {
                if (guibutton.id < 200 && guibutton instanceof GuiButtonShaderOption btnSo) {
                    ShaderOption so = btnSo.getShaderOption();
                    if (so instanceof ShaderOptionScreen) {
                        String screenName = so.getName();
                        GuiShaderOptions scr = new GuiShaderOptions(this, this.settings, screenName);
                        this.minecraft.gui.setScreen(scr);
                        return;
                    }

                    if (hasShiftDown()) {
                        so.resetValue();
                    } else if (btnSo.isSwitchable()) {
                        so.nextValue();
                    }

                    this.updateAllButtons();
                    this.changed = true;
                }

                if (guibutton.id == 201) {
                    ShaderOption[] opts = Shaders.getChangedOptions(Shaders.getShaderPackOptions());

                    for (int i = 0; i < opts.length; i++) {
                        ShaderOption opt = opts[i];
                        opt.resetValue();
                        this.changed = true;
                    }

                    this.updateAllButtons();
                }

                if (guibutton.id == 200) {
                    if (this.changed) {
                        Shaders.saveShaderPackOptions();
                        this.changed = false;
                        Shaders.uninit();
                    }

                    this.minecraft.gui.setScreen(this.prevScreen);
                }
            }
        }
    }

    @Override
    public void removed() {
        if (this.changed) {
            Shaders.saveShaderPackOptions();
            this.changed = false;
            Shaders.uninit();
        }

        super.removed();
    }

    @Override
    public void actionPerformedRightClick(AbstractWidget guiElement) {
        if (guiElement instanceof GuiButtonShaderOption btnSo) {
            ShaderOption so = btnSo.getShaderOption();
            if (hasShiftDown()) {
                so.resetValue();
            } else if (btnSo.isSwitchable()) {
                so.prevValue();
            }

            this.updateAllButtons();
            this.changed = true;
        }
    }

    private void updateAllButtons() {
        for (Button btn : (java.lang.Iterable<Button>)(java.lang.Iterable<?>)(this.getButtonList())) {
            if (btn instanceof GuiButtonShaderOption gbso) {
                ShaderOption opt = gbso.getShaderOption();
                if (opt instanceof ShaderOptionProfile optProf) {
                    optProf.updateProfile();
                }

                gbso.setMessage(getButtonText(opt, gbso.getWidth()));
                gbso.valueChanged();
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphicsIn, int x, int y, float partialTicks) {
        super.extractRenderState(graphicsIn, x, y, partialTicks);
        if (this.screenText != null) {
            drawCenteredString(graphicsIn, this.fontRenderer, this.screenText, this.width / 2, 15, -1);
        } else {
            drawCenteredString(graphicsIn, this.fontRenderer, this.title, this.width / 2, 15, -1);
        }

        this.tooltipManager.drawTooltips(graphicsIn, x, y, this.getButtonList());
    }
}
