package net.optifine.gui;

import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratableEntry.NarrationPriority;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.optifine.render.Tesselator;
import net.optifine.util.TextureUtils;

public abstract class SlotGui extends AbstractContainerEventHandler implements NarratableEntry {
   public static final Identifier WHITE_TEXTURE_LOCATION = TextureUtils.WHITE_TEXTURE_LOCATION;
   public static final Identifier MENU_LIST_BACKGROUND = new Identifier("textures/gui/menu_list_background.png");
   public static final Identifier INWORLD_MENU_LIST_BACKGROUND = new Identifier("textures/gui/inworld_menu_list_background.png");
   protected static final int NO_DRAG = -1;
   protected static final int DRAG_OUTSIDE = -2;
   protected final Minecraft minecraft;
   protected int width;
   protected int height;
   protected int y0;
   protected int y1;
   protected int x1;
   protected int x0;
   protected final int itemHeight;
   protected boolean centerListVertically = true;
   protected int yDrag = -2;
   protected double yScroll;
   protected boolean visible = true;
   protected boolean renderSelection = true;
   protected boolean renderHeader;
   protected int headerHeight;
   private boolean scrolling;

   public SlotGui(Minecraft mcIn, int width, int height, int topIn, int bottomIn, int slotHeightIn) {
      this.minecraft = mcIn;
      this.width = width;
      this.height = height;
      this.y0 = topIn;
      this.y1 = bottomIn;
      this.itemHeight = slotHeightIn;
      this.x0 = 0;
      this.x1 = width;
   }

   public void setRenderSelection(boolean flagIn) {
      this.renderSelection = flagIn;
   }

   protected void setRenderHeader(boolean headerIn, int heightIn) {
      this.renderHeader = headerIn;
      this.headerHeight = heightIn;
      if (!headerIn) {
         this.headerHeight = 0;
      }
   }

   public void setVisible(boolean flagIn) {
      this.visible = flagIn;
   }

   public boolean isVisible() {
      return this.visible;
   }

   protected abstract int getItemCount();

   @Override
   public List<? extends GuiEventListener> children() {
      return Collections.emptyList();
   }

   protected boolean selectItem(int indexIn, int buttonIn, double mouseX, double mouseY) {
      return true;
   }

   protected abstract boolean isSelectedItem(int var1);

   protected int getMaxPosition() {
      return this.getItemCount() * this.itemHeight + this.headerHeight;
   }

   protected abstract void renderBackground();

   protected void updateItemPosition(int index, int xIn, int yIn, float partialTicks) {
   }

   protected abstract void renderItem(GuiGraphicsExtractor var1, int var2, int var3, int var4, int var5, int var6, int var7, float var8);

   protected void renderHeader(GuiGraphicsExtractor graphicsIn, int xIn, int yIn) {
   }

   protected void clickedHeader(int xIn, int yIn) {
   }

   protected void renderDecorations(int mouseX, int mouseY) {
   }

   public int getItemAtPosition(double mouseX, double mouseY) {
      int i = this.x0 + this.width / 2 - this.getRowWidth() / 2;
      int j = this.x0 + this.width / 2 + this.getRowWidth() / 2;
      int k = Mth.floor(mouseY - this.y0) - this.headerHeight + (int)this.yScroll - 4;
      int l = k / this.itemHeight;
      return mouseX < this.getScrollbarPosition() && mouseX >= i && mouseX <= j && l >= 0 && k >= 0 && l < this.getItemCount() ? l : -1;
   }

   protected void capYPosition() {
      this.yScroll = Mth.clamp(this.yScroll, 0.0, this.getMaxScroll());
   }

   public int getMaxScroll() {
      return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
   }

   public void centerScrollOn(int indexIn) {
      this.yScroll = indexIn * this.itemHeight + this.itemHeight / 2 - (this.y1 - this.y0) / 2;
      this.capYPosition();
   }

   public int getScroll() {
      return (int)this.yScroll;
   }

   public boolean isMouseInList(double mouseX, double mouseY) {
      return mouseY >= this.y0 && mouseY <= this.y1 && mouseX >= this.x0 && mouseX <= this.x1;
   }

   public int getScrollBottom() {
      return (int)this.yScroll - this.height - this.headerHeight;
   }

   public void scroll(int deltaIn) {
      this.yScroll += deltaIn;
      this.capYPosition();
      this.yDrag = -2;
   }

   public void extractRenderState(GuiGraphicsExtractor graphicsIn, int mouseX, int mouseY, float partialTicks) {
      if (this.visible) {
         this.renderBackground();
         this.capYPosition();
         Tesselator tessellator = Tesselator.getInstance();
         Identifier menuListBackground = this.minecraft.level != null ? INWORLD_MENU_LIST_BACKGROUND : MENU_LIST_BACKGROUND;
         graphicsIn.blit(menuListBackground, this.x0, this.y0, this.x1, this.y1, 0.0F, 1.0F, 0.0F, 1.0F);
         int k = this.x0 + this.width / 2 - this.getRowWidth() / 2 + 2;
         int l = this.y0 + 4 - (int)this.yScroll;
         if (this.renderHeader) {
            this.renderHeader(graphicsIn, k, l);
         }

         this.renderList(graphicsIn, k, l, mouseX, mouseY, partialTicks);
         int delta = 1;
         graphicsIn.blit(WHITE_TEXTURE_LOCATION, this.x0, this.y0, this.x1, this.y0 + delta, 0.0F, 1.0F, 0.0F, 1.0F, -16777216);
         graphicsIn.blit(WHITE_TEXTURE_LOCATION, this.x0, this.y1 - delta, this.x1, this.y1, 0.0F, 1.0F, 0.0F, 1.0F, -16777216);
         int j1 = this.getMaxScroll();
         if (j1 > 0) {
            int k1 = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / this.getMaxPosition());
            k1 = Mth.clamp(k1, 32, this.y1 - this.y0 - 8);
            int l1 = (int)this.yScroll * (this.y1 - this.y0 - k1) / j1 + this.y0;
            if (l1 < this.y0) {
               l1 = this.y0;
            }

            int sy0 = this.getScrollbarPosition();
            int sy1 = sy0 + 6;
            graphicsIn.blit(WHITE_TEXTURE_LOCATION, sy0, this.y0, sy1, this.y1, 0.0F, 1.0F, 0.0F, 1.0F, -16777216);
            graphicsIn.blit(WHITE_TEXTURE_LOCATION, sy0, l1, sy1, l1 + k1, 0.0F, 1.0F, 0.0F, 1.0F, -8355712);
            graphicsIn.blit(WHITE_TEXTURE_LOCATION, sy0, l1, sy1 - 1, l1 + k1 - 1, 0.0F, 1.0F, 0.0F, 1.0F, -4144960);
         }

         this.renderDecorations(mouseX, mouseY);
      }
   }

   protected void updateScrollingState(double mouseX, double mouseY, int buttonIn) {
      this.scrolling = buttonIn == 0 && mouseX >= this.getScrollbarPosition() && mouseX < this.getScrollbarPosition() + 6;
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent eventIn, boolean doubleIn) {
      double mouseX = eventIn.x();
      double mouseY = eventIn.y();
      int button = eventIn.button();
      this.updateScrollingState(mouseX, mouseY, button);
      if (this.isVisible() && this.isMouseInList(mouseX, mouseY)) {
         int i = this.getItemAtPosition(mouseX, mouseY);
         if (i == -1 && button == 0) {
            this.clickedHeader((int)(mouseX - (this.x0 + this.width / 2 - this.getRowWidth() / 2)), (int)(mouseY - this.y0) + (int)this.yScroll - 4);
            return true;
         }

         if (i != -1 && this.selectItem(i, button, mouseX, mouseY)) {
            if (this.children().size() > i) {
               this.setFocused(this.children().get(i));
            }

            this.setDragging(true);
            return true;
         } else {
            return this.scrolling;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent eventIn) {
      if (this.getFocused() != null) {
         this.getFocused().mouseReleased(eventIn);
      }

      return false;
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent eventIn, double mouseX, double mouseY) {
      double mouseAbsX = eventIn.x();
      double mouseAbsY = eventIn.y();
      double deltaX = mouseX;
      double deltaY = mouseY;
      int buttonIn = eventIn.button();
      if (super.mouseDragged(eventIn, deltaX, deltaY)) {
         return true;
      }

      if (this.isVisible() && buttonIn == 0 && this.scrolling) {
         if (mouseAbsY < this.y0) {
            this.yScroll = 0.0;
         } else if (mouseAbsY > this.y1) {
            this.yScroll = this.getMaxScroll();
         } else {
            double d0 = this.getMaxScroll();
            if (d0 < 1.0) {
               d0 = 1.0;
            }

            int i = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / this.getMaxPosition());
            i = Mth.clamp(i, 32, this.y1 - this.y0 - 8);
            double d1 = d0 / (this.y1 - this.y0 - i);
            if (d1 < 1.0) {
               d1 = 1.0;
            }

            this.yScroll += deltaY * d1;
            this.capYPosition();
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double deltaH, double deltaV) {
      if (!this.isVisible()) {
         return false;
      }

      this.yScroll = this.yScroll - deltaV * this.itemHeight / 2.0;
      return true;
   }

   @Override
   public boolean keyPressed(KeyEvent eventIn) {
      if (!this.isVisible()) {
         return false;
      } else if (super.keyPressed(eventIn)) {
         return true;
      } else if (eventIn.key() == 264) {
         this.moveSelection(1);
         return true;
      } else if (eventIn.key() == 265) {
         this.moveSelection(-1);
         return true;
      } else {
         return false;
      }
   }

   protected void moveSelection(int increment) {
   }

   @Override
   public boolean charTyped(CharacterEvent eventIn) {
      return !this.isVisible() ? false : super.charTyped(eventIn);
   }

   @Override
   public boolean isMouseOver(double mouseX, double mouseY) {
      return this.isMouseInList(mouseX, mouseY);
   }

   public int getRowWidth() {
      return 220;
   }

   protected void renderList(GuiGraphicsExtractor graphicsIn, int xIn, int yIn, int mouseX, int mouseY, float partialTicks) {
      int count = this.getItemCount();
      Tesselator tessellator = Tesselator.getInstance();
      graphicsIn.enableScissor(this.x0, this.y0, this.x1, this.y1);

      for (int i = 0; i < count; i++) {
         int yItem = yIn + i * this.itemHeight + this.headerHeight;
         int h = this.itemHeight - 4;
         if (yItem > this.y1 || yItem + h < this.y0) {
            this.updateItemPosition(i, xIn, yItem, partialTicks);
         }

         if (Boolean.TRUE && this.renderSelection && this.isSelectedItem(i)) {
            int xt = this.x0 + this.width / 2 - this.getRowWidth() / 2;
            int yt = this.x0 + this.width / 2 + this.getRowWidth() / 2;
            float br = this.isFocusedNow() ? 1.0F : 0.5F;
            int col = ARGB.colorFromFloat(1.0F, br, br, br);
            graphicsIn.fill(xt, yItem - 2, yt, yItem + h + 2, col);
            int col2 = ARGB.colorFromFloat(1.0F, 0.0F, 0.0F, 0.0F);
            graphicsIn.fill(xt + 1, yItem - 1, yt - 1, yItem + h + 1, col2);
         }

         if (yItem + this.itemHeight >= this.y0 && yItem <= this.y1) {
            this.renderItem(graphicsIn, i, xIn, yItem, h, mouseX, mouseY, partialTicks);
         }
      }

      graphicsIn.disableScissor();
   }

   protected boolean isFocusedNow() {
      return false;
   }

   protected int getScrollbarPosition() {
      return this.width / 2 + 124;
   }

   public void setLeftPos(int x0In) {
      this.x0 = x0In;
      this.x1 = x0In + this.width;
   }

   public int getItemHeight() {
      return this.itemHeight;
   }

   @Override
   public NarrationPriority narrationPriority() {
      return NarrationPriority.HOVERED;
   }

   @Override
   public void updateNarration(NarrationElementOutput output) {
   }
}
