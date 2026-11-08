package net.optifine.gui;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.optifine.Config;

public class GuiMessage extends GuiScreenOF {
   private Screen parentScreen;
   private Component messageLine1;
   private Component messageLine2;
   private final List<FormattedCharSequence> listLines2 = Lists.newArrayList();
   protected String confirmButtonText;
   private int ticksUntilEnable;

   public GuiMessage(Screen parentScreen, String line1, String line2) {
      super(Component.translatable("of.options.detailsTitle"));
      this.parentScreen = parentScreen;
      this.messageLine1 = Component.literal(line1);
      this.messageLine2 = Component.literal(line2);
      this.confirmButtonText = I18n.get("gui.done");
   }

   @Override
   public void init() {
      this.addButton(new GuiButtonOF(0, this.width / 2 - 100, this.height / 6 + 96, this.confirmButtonText));
      this.listLines2.clear();
      this.listLines2.addAll(this.minecraft.font.split(this.messageLine2, this.width - 50));
   }

   @Override
   public void actionPerformed(AbstractWidget button) {
      Config.getMinecraft().gui.setScreen(this.parentScreen);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphicsIn, int mouseX, int mouseY, float partialTicks) {
      super.extractRenderState(graphicsIn, mouseX, mouseY, partialTicks);
      drawCenteredString(graphicsIn, this.fontRenderer, this.messageLine1, this.width / 2, 70, -1);
      int var4x = 90;

      for (FormattedCharSequence line : this.listLines2) {
         drawCenteredString(graphicsIn, this.fontRenderer, line, this.width / 2, var4x, -1);
         var4x += 9;
      }
   }

   public void setButtonDelay(int ticksUntilEnable) {
      this.ticksUntilEnable = ticksUntilEnable;

      for (Button var3 : (java.lang.Iterable<Button>)(java.lang.Iterable<?>)(this.getButtonList())) {
         var3.active = false;
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (--this.ticksUntilEnable == 0) {
         for (Button var2 : (java.lang.Iterable<Button>)(java.lang.Iterable<?>)(this.getButtonList())) {
            var2.active = true;
         }
      }
   }
}
