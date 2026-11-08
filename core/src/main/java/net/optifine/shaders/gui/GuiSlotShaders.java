package net.optifine.shaders.gui;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.optifine.Config;
import net.optifine.Lang;
import net.optifine.gui.SlotGui;
import net.optifine.shaders.IShaderPack;
import net.optifine.shaders.Shaders;
import net.optifine.util.ResUtils;

class GuiSlotShaders extends SlotGui {
    private List<String> shaderslist;
    private int selectedIndex;
    private long lastClicked = Long.MIN_VALUE;
    private long lastClickedCached = 0L;
    final GuiShaders shadersGui;

    public GuiSlotShaders(GuiShaders par1GuiShaders, int width, int height, int top, int bottom, int slotHeight) {
        super(par1GuiShaders.getMc(), width, height, top, bottom, slotHeight);
        this.shadersGui = par1GuiShaders;
        this.updateList("");
        this.yScroll = 0.0;
        int posYSelected = this.selectedIndex * slotHeight;
        int wMid = (bottom - top) / 2;
        if (posYSelected > wMid) {
            this.scroll(posYSelected - wMid);
        }
    }

    @Override
    public int getRowWidth() {
        return this.width - 20;
    }

    public void updateList(String filter) {
        List<String> list = Shaders.listOfShaders();
        String filterLow = filter.trim().toLowerCase();
        if (filterLow.length() > 0) {
            Iterator it = list.iterator();

            while (it.hasNext()) {
                String name = (String)it.next();
                if (!Shaders.isSystemShaderPack(name) && !Config.equals(name, Shaders.currentShaderName)) {
                    String nameLow = name.toLowerCase();
                    if (!nameLow.contains(filterLow)) {
                        it.remove();
                    }
                }
            }
        }

        if (this.shaderslist == null) {
            this.shaderslist = new ArrayList<>();
        }

        int countPrev = this.shaderslist.size();
        this.shaderslist = list;
        this.selectedIndex = 0;
        int i = 0;

        for (int n = this.shaderslist.size(); i < n; i++) {
            if (this.shaderslist.get(i).equals(Shaders.currentShaderName)) {
                this.selectedIndex = i;
                break;
            }
        }

        if (this.shaderslist.size() != countPrev) {
            this.yScroll = 0.0;
            this.centerScrollOn(this.selectedIndex);
        }
    }

    @Override
    protected int getItemCount() {
        return this.shaderslist.size();
    }

    @Override
    protected boolean selectItem(int index, int buttons, double x, double y) {
        if (index == this.selectedIndex && this.lastClicked == this.lastClickedCached) {
            return false;
        }

        String name = this.shaderslist.get(index);
        IShaderPack sp = Shaders.getShaderPack(name);
        if (!this.checkCompatible(sp, index)) {
            return false;
        }

        this.selectIndex(index);
        return true;
    }

    private void selectIndex(int index) {
        this.selectedIndex = index;
        this.lastClickedCached = this.lastClicked;
        Shaders.setShaderPack(this.shaderslist.get(index));
        Shaders.uninit();
        this.shadersGui.updateButtons();
    }

    private boolean checkCompatible(IShaderPack sp, int index) {
        if (sp == null) {
            return true;
        }

        InputStream in = sp.getResourceAsStream("/shaders/shaders.properties");
        Properties props = ResUtils.readProperties(in, "Shaders");
        if (props == null) {
            return true;
        }

        String keyVer = "version.26.2";
        String relMin = props.getProperty(keyVer);
        if (relMin == null) {
            return true;
        }

        relMin = relMin.trim();
        String rel = "K2_pre5";
        int res = Config.compareRelease(rel, relMin);
        if (res >= 0) {
            return true;
        }

        String verMin = ("HD_U_" + relMin).replace('_', ' ');
        String msg1 = I18n.get("of.message.shaders.nv1", verMin);
        String msg2 = I18n.get("of.message.shaders.nv2");
        int theIndex = index;
        BooleanConsumer callback = result -> {
            if (result) {
                this.selectIndex(theIndex);
            }

            this.minecraft.gui.setScreen(this.shadersGui);
        };
        ConfirmScreen guiYesNo = new ConfirmScreen(callback, Component.literal(msg1), Component.literal(msg2));
        this.minecraft.gui.setScreen(guiYesNo);
        return false;
    }

    @Override
    protected boolean isSelectedItem(int index) {
        return index == this.selectedIndex;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x0 + this.width - 6;
    }

    @Override
    public int getItemHeight() {
        return this.getItemCount() * 18;
    }

    @Override
    protected void renderBackground() {
    }

    @Override
    protected void renderItem(GuiGraphicsExtractor graphicsIn, int index, int posX, int posY, int contentY, int mouseX, int mouseY, float partialTicks) {
        String label = this.shaderslist.get(index);
        if (label.equals("OFF")) {
            label = Lang.get("of.options.shaders.packNone");
        } else if (label.equals("(debug)")) {
            label = Lang.get("of.options.shaders.packDefault");
        } else if (!label.toLowerCase().endsWith(".zip")) {
            label = "<" + label + ">";
        }

        this.shadersGui.drawCenteredString(graphicsIn, label, this.width / 2, posY + 1, -2039584);
    }

    public int getSelectedIndex() {
        return this.selectedIndex;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaH, double deltaV) {
        return super.mouseScrolled(mouseX, mouseY, deltaH, deltaV * 3.0);
    }
}
