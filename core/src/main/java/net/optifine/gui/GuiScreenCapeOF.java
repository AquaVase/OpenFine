package net.optifine.gui;

import com.mojang.authlib.exceptions.InvalidCredentialsException;
import java.math.BigInteger;
import java.net.URI;
import java.util.Random;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.optifine.Config;
import net.optifine.Lang;

public class GuiScreenCapeOF extends GuiScreenOF {
    private final Screen parentScreen;
    private String message;
    private long messageHideTimeMs;
    private String linkUrl;
    private GuiButtonOF buttonCopyLink;

    public GuiScreenCapeOF(Screen parentScreenIn) {
        super(Component.literal(I18n.get("of.options.capeOF.title")));
        this.parentScreen = parentScreenIn;
    }

    @Override
    protected void init() {
        int i = 0;
        i += 2;
        this.addButton(new GuiButtonOF(210, this.width / 2 - 155, this.height / 6 + 24 * (i >> 1), 150, 20, I18n.get("of.options.capeOF.openEditor")));
        this.addButton(new GuiButtonOF(220, this.width / 2 - 155 + 160, this.height / 6 + 24 * (i >> 1), 150, 20, I18n.get("of.options.capeOF.reloadCape")));
        i += 6;
        this.buttonCopyLink = new GuiButtonOF(230, this.width / 2 - 100, this.height / 6 + 24 * (i >> 1), 200, 20, I18n.get("of.options.capeOF.copyEditorLink"));
        this.buttonCopyLink.visible = this.linkUrl != null;
        this.addButton(this.buttonCopyLink);
        i += 4;
        this.addButton(new GuiButtonOF(200, this.width / 2 - 100, this.height / 6 + 24 * (i >> 1), I18n.get("gui.done")));
    }

    @Override
    public void actionPerformed(AbstractWidget guiElement) {
        if (guiElement instanceof GuiButtonOF button) {
            if (button.active) {
                if (button.id == 200) {
                    this.minecraft.gui.setScreen(this.parentScreen);
                }

                if (button.id == 210) {
                    try {
                        String userName = this.minecraft.getGameProfile().name();
                        String userId = this.minecraft.getGameProfile().id().toString().replace("-", "");
                        String accessToken = this.minecraft.getUser().getAccessToken();
                        Random r1 = new Random();
                        Random r2 = new Random(System.identityHashCode(new Object()));
                        BigInteger random1Bi = new BigInteger(128, r1);
                        BigInteger random2Bi = new BigInteger(128, r2);
                        BigInteger serverBi = random1Bi.xor(random2Bi);
                        String serverId = serverBi.toString(16);
                        this.minecraft.services().sessionService().joinServer(this.minecraft.getGameProfile().id(), accessToken, serverId);
                        String urlStr = "https://optifine.net/capeChange?u=" + userId + "&n=" + userName + "&s=" + serverId;
                        boolean opened = Config.openWebLink(new URI(urlStr));
                        if (opened) {
                            this.showMessage(Lang.get("of.message.capeOF.openEditor"), 10000L);
                        } else {
                            this.showMessage(Lang.get("of.message.capeOF.openEditorError"), 10000L);
                            this.setLinkUrl(urlStr);
                        }
                    } catch (InvalidCredentialsException e) {
                        Config.showGuiMessage(I18n.get("of.message.capeOF.error1"), I18n.get("of.message.capeOF.error2", e.getMessage()));
                        Config.warn("Mojang authentication failed");
                        Config.warn(e.getClass().getName() + ": " + e.getMessage());
                    } catch (Exception e) {
                        Config.warn("Error opening OptiFine cape link");
                        Config.warn(e.getClass().getName() + ": " + e.getMessage());
                    }
                }

                if (button.id == 220) {
                    this.showMessage(Lang.get("of.message.capeOF.reloadCape"), 15000L);
                    if (this.minecraft.player != null) {
                        long delayMs = 15000L;
                        long reloadTimeMs = System.currentTimeMillis() + delayMs;
                        this.minecraft.player.setReloadCapeTimeMs(reloadTimeMs);
                    }
                }

                if (button.id == 230 && this.linkUrl != null) {
                    this.minecraft.keyboardHandler.setClipboard(this.linkUrl);
                }
            }
        }
    }

    private void showMessage(String msg, long timeMs) {
        this.message = msg;
        this.messageHideTimeMs = System.currentTimeMillis() + timeMs;
        this.setLinkUrl(null);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphicsIn, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(graphicsIn, mouseX, mouseY, partialTicks);
        drawCenteredString(graphicsIn, this.fontRenderer, this.title, this.width / 2, 20, -1);
        if (this.message != null) {
            drawCenteredString(graphicsIn, this.fontRenderer, this.message, this.width / 2, this.height / 6 + 60, -1);
            if (System.currentTimeMillis() > this.messageHideTimeMs) {
                this.message = null;
                this.setLinkUrl(null);
            }
        }
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
        this.buttonCopyLink.visible = linkUrl != null;
    }
}
