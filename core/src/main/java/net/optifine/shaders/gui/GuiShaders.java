package net.optifine.shaders.gui;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.optifine.Config;
import net.optifine.Lang;
import net.optifine.gui.GuiButtonOF;
import net.optifine.gui.GuiScreenOF;
import net.optifine.gui.TooltipManager;
import net.optifine.gui.TooltipProviderEnumShaderOptions;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersTex;
import net.optifine.shaders.config.EnumShaderOption;

public class GuiShaders extends GuiScreenOF {
   protected Screen parentGui;
   private TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderEnumShaderOptions());
   private int updateTimer = -1;
   private GuiSlotShaders shaderList;
   private EditBox filterField;
   private boolean saved = false;
   private static float[] QUALITY_MULTIPLIERS = new float[]{
      0.5F, 0.6F, 0.6666667F, 0.75F, 0.8333333F, 0.9F, 1.0F, 1.1666666F, 1.3333334F, 1.5F, 1.6666666F, 1.8F, 2.0F
   };
   private static String[] QUALITY_MULTIPLIER_NAMES = new String[]{
      "0.5x", "0.6x", "0.66x", "0.75x", "0.83x", "0.9x", "1x", "1.16x", "1.33x", "1.5x", "1.66x", "1.8x", "2x"
   };
   private static float QUALITY_MULTIPLIER_DEFAULT = 1.0F;
   private static float[] HAND_DEPTH_VALUES = new float[]{0.0625F, 0.125F, 0.25F};
   private static String[] HAND_DEPTH_NAMES = new String[]{"0.5x", "1x", "2x"};
   private static float HAND_DEPTH_DEFAULT = 0.125F;
   public static final int EnumOS_UNKNOWN = 0;
   public static final int EnumOS_WINDOWS = 1;
   public static final int EnumOS_OSX = 2;
   public static final int EnumOS_SOLARIS = 3;
   public static final int EnumOS_LINUX = 4;

   public GuiShaders(Screen par1GuiScreen, Options par2GameSettings) {
      super(Component.literal(I18n.get("of.options.shadersTitle")));
      this.parentGui = par1GuiScreen;
   }

   @Override
   public void init() {
      if (Shaders.shadersConfig == null) {
         Shaders.loadConfig();
      }

      int btnWidth = 120;
      int btnHeight = 20;
      int btnX = this.width - btnWidth - 10;
      int baseY = 30;
      int stepY = 20;
      int shaderListLeft = 10;
      int shaderListBottom = this.height - 65;
      int shaderListWidth = this.width - btnWidth - 30;
      this.filterField = new EditBox(this.font, shaderListLeft, shaderListBottom, shaderListWidth, 15, Component.empty());
      this.addButton(this.filterField);
      this.shaderList = new GuiSlotShaders(this, shaderListWidth, this.height, baseY, shaderListBottom, 16);
      this.shaderList.setLeftPos(shaderListLeft);
      this.filterField.setResponder(x -> this.requestUpdate());
      this.addWidget(this.shaderList);
      this.addButton(new GuiButtonEnumShaderOption(EnumShaderOption.ANTIALIASING, btnX, 0 * stepY + baseY, btnWidth, btnHeight));
      this.addButton(new GuiButtonEnumShaderOption(EnumShaderOption.NORMAL_MAP, btnX, 1 * stepY + baseY, btnWidth, btnHeight));
      this.addButton(new GuiButtonEnumShaderOption(EnumShaderOption.SPECULAR_MAP, btnX, 2 * stepY + baseY, btnWidth, btnHeight));
      this.addButton(new GuiButtonEnumShaderOption(EnumShaderOption.RENDER_RES_MUL, btnX, 3 * stepY + baseY, btnWidth, btnHeight));
      this.addButton(new GuiButtonEnumShaderOption(EnumShaderOption.SHADOW_RES_MUL, btnX, 4 * stepY + baseY, btnWidth, btnHeight));
      this.addButton(new GuiButtonEnumShaderOption(EnumShaderOption.HAND_DEPTH_MUL, btnX, 5 * stepY + baseY, btnWidth, btnHeight));
      this.addButton(new GuiButtonEnumShaderOption(EnumShaderOption.OLD_HAND_LIGHT, btnX, 6 * stepY + baseY, btnWidth, btnHeight));
      this.addButton(new GuiButtonEnumShaderOption(EnumShaderOption.OLD_LIGHTING, btnX, 7 * stepY + baseY, btnWidth, btnHeight));
      int btnFolderWidth = Math.min(150, (shaderListWidth - 10) / 2);
      int xFolder = shaderListLeft + (shaderListWidth - 10) / 4 - btnFolderWidth / 2;
      int yFolder = this.height - 25;
      this.addButton(new GuiButtonOF(201, xFolder, yFolder, btnFolderWidth - 22 + 1, btnHeight, Lang.get("of.options.shaders.shadersFolder")));
      this.addButton(new GuiButtonDownloadShaders(210, xFolder + btnFolderWidth - 22 - 1, yFolder));
      int xDone = shaderListLeft + 10 + (shaderListWidth - 10) * 3 / 4 - btnFolderWidth / 2;
      this.addButton(new GuiButtonOF(202, xDone, this.height - 25, btnFolderWidth, btnHeight, I18n.get("gui.done")));
      this.addButton(new GuiButtonOF(203, btnX, this.height - 25, btnWidth, btnHeight, Lang.get("of.options.shaders.shaderOptions")));
      this.setFocused(this.shaderList);
      this.updateButtons();
   }

   public void updateButtons() {
      boolean shaderActive = Config.isShaders();

      for (AbstractWidget guiElement : this.getButtonList()) {
         if (guiElement instanceof GuiButtonOF button
            && button.id != 201
            && button.id != 202
            && button.id != 210
            && button.id != EnumShaderOption.ANTIALIASING.ordinal()) {
            button.active = shaderActive;
         }
      }
   }

   @Override
   public void actionPerformed(AbstractWidget button) {
      this.actionPerformed(button, false);
   }

   @Override
   public void actionPerformedRightClick(AbstractWidget button) {
      this.actionPerformed(button, true);
   }

   private void actionPerformed(AbstractWidget guiElement, boolean rightClick) {
      if (guiElement.active) {
         if (guiElement instanceof GuiButtonEnumShaderOption gbeso) {
            EnumShaderOption eso = gbeso.getEnumShaderOption();
            switch (eso) {
               case ANTIALIASING:
                  Shaders.nextAntialiasingLevel(!rightClick);
                  if (hasShiftDown()) {
                     Shaders.configAntialiasingLevel = 0;
                  }

                  Shaders.uninit();
                  break;
               case NORMAL_MAP:
                  Shaders.configNormalMap = !Shaders.configNormalMap;
                  if (hasShiftDown()) {
                     Shaders.configNormalMap = true;
                  }

                  Shaders.uninit();
                  this.minecraft.delayTextureReload();
                  break;
               case SPECULAR_MAP:
                  Shaders.configSpecularMap = !Shaders.configSpecularMap;
                  if (hasShiftDown()) {
                     Shaders.configSpecularMap = true;
                  }

                  Shaders.uninit();
                  this.minecraft.delayTextureReload();
                  break;
               case RENDER_RES_MUL:
                  Shaders.configRenderResMul = this.getNextValue(
                     Shaders.configRenderResMul, QUALITY_MULTIPLIERS, QUALITY_MULTIPLIER_DEFAULT, !rightClick, hasShiftDown()
                  );
                  Shaders.uninit();
                  Shaders.scheduleResize();
                  break;
               case SHADOW_RES_MUL:
                  Shaders.configShadowResMul = this.getNextValue(
                     Shaders.configShadowResMul, QUALITY_MULTIPLIERS, QUALITY_MULTIPLIER_DEFAULT, !rightClick, hasShiftDown()
                  );
                  Shaders.uninit();
                  Shaders.scheduleResizeShadow();
                  break;
               case HAND_DEPTH_MUL:
                  Shaders.configHandDepthMul = this.getNextValue(Shaders.configHandDepthMul, HAND_DEPTH_VALUES, HAND_DEPTH_DEFAULT, !rightClick, hasShiftDown());
                  Shaders.uninit();
                  break;
               case OLD_HAND_LIGHT:
                  Shaders.configOldHandLight.nextValue(!rightClick);
                  if (hasShiftDown()) {
                     Shaders.configOldHandLight.resetValue();
                  }

                  Shaders.uninit();
                  break;
               case OLD_LIGHTING:
                  Shaders.configOldLighting.nextValue(!rightClick);
                  if (hasShiftDown()) {
                     Shaders.configOldLighting.resetValue();
                  }

                  Shaders.updateBlockLightLevel();
                  Shaders.uninit();
                  this.minecraft.delayTextureReload();
                  break;
               case TWEAK_BLOCK_DAMAGE:
                  Shaders.configTweakBlockDamage = !Shaders.configTweakBlockDamage;
                  break;
               case CLOUD_SHADOW:
                  Shaders.configCloudShadow = !Shaders.configCloudShadow;
                  break;
               case TEX_MIN_FIL_B:
                  Shaders.configTexMinFilB = (Shaders.configTexMinFilB + 1) % 3;
                  Shaders.configTexMinFilN = Shaders.configTexMinFilS = Shaders.configTexMinFilB;
                  gbeso.setMessage("Tex Min: " + Shaders.texMinFilDesc[Shaders.configTexMinFilB]);
                  ShadersTex.updateTextureMinMagFilter();
                  break;
               case TEX_MAG_FIL_N:
                  Shaders.configTexMagFilN = (Shaders.configTexMagFilN + 1) % 2;
                  gbeso.setMessage("Tex_n Mag: " + Shaders.texMagFilDesc[Shaders.configTexMagFilN]);
                  ShadersTex.updateTextureMinMagFilter();
                  break;
               case TEX_MAG_FIL_S:
                  Shaders.configTexMagFilS = (Shaders.configTexMagFilS + 1) % 2;
                  gbeso.setMessage("Tex_s Mag: " + Shaders.texMagFilDesc[Shaders.configTexMagFilS]);
                  ShadersTex.updateTextureMinMagFilter();
                  break;
               case SHADOW_CLIP_FRUSTRUM:
                  Shaders.configShadowClipFrustrum = !Shaders.configShadowClipFrustrum;
                  gbeso.setMessage("ShadowClipFrustrum: " + toStringOnOff(Shaders.configShadowClipFrustrum, Boolean.valueOf(eso.getValueDefault())));
                  ShadersTex.updateTextureMinMagFilter();
            }

            gbeso.updateButtonText();
         } else if (!rightClick) {
            if (guiElement instanceof GuiButtonOF button) {
               switch (button.id) {
                  case 201:
                     switch (getOSType()) {
                        case 1:
                           String var2x = String.format("cmd.exe /C start \"Open file\" \"%s\"", Shaders.shaderPacksDir.getAbsolutePath());

                           try {
                              Runtime.getRuntime().exec(var2x);
                              return;
                           } catch (IOException var6) {
                              var6.printStackTrace();
                              break;
                           }
                        case 2:
                           try {
                              Runtime.getRuntime().exec(new String[]{"/usr/bin/open", Shaders.shaderPacksDir.getAbsolutePath()});
                              return;
                           } catch (IOException var7) {
                              var7.printStackTrace();
                           }
                     }

                     boolean var8 = false;

                     try {
                        URI uri = new File(this.minecraft.gameDirectory, "shaderpacks").toURI();
                        Util.getPlatform().openUri(uri);
                     } catch (Throwable var5) {
                        var5.printStackTrace();
                        var8 = true;
                     }

                     if (var8) {
                        Config.dbg("Opening via system class!");
                        Util.getPlatform().openUri("file://" + Shaders.shaderPacksDir.getAbsolutePath());
                     }
                     break;
                  case 202:
                     Shaders.storeConfig();
                     this.saved = true;
                     this.minecraft.gui.setScreen(this.parentGui);
                     break;
                  case 203:
                     GuiShaderOptions gui = new GuiShaderOptions(this, Config.getGameSettings());
                     Config.getMinecraft().gui.setScreen(gui);
                  case 204:
                  case 205:
                  case 206:
                  case 207:
                  case 208:
                  case 209:
                  default:
                     break;
                  case 210:
                     try {
                        URI uri = new URI("http://optifine.net/shaderPacks");
                        Util.getPlatform().openUri(uri);
                     } catch (Throwable throwable) {
                        throwable.printStackTrace();
                     }
               }
            }
         }
      }
   }

   @Override
   public void removed() {
      if (!this.saved) {
         Shaders.storeConfig();
         this.saved = true;
      }

      super.removed();
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphicsIn, int mouseX, int mouseY, float partialTicks) {
      super.extractBackground(graphicsIn, mouseX, mouseY, partialTicks);
      this.shaderList.extractRenderState(graphicsIn, mouseX, mouseY, partialTicks);
      if (this.updateTimer <= 0) {
         this.shaderList.updateList(this.filterField.getValue());
         this.updateTimer = 20;
      }

      drawCenteredString(graphicsIn, this.fontRenderer, this.title, this.width / 2, 15, -1);
      String info = "OpenGL: " + Shaders.glVersionString + ", " + Shaders.glVendorString + ", " + Shaders.glRendererString;
      int infoWidth = this.fontRenderer.width(info);
      if (infoWidth < this.width - 20) {
         drawCenteredString(graphicsIn, this.fontRenderer, info, this.width / 2, this.height - 40, -6250336);
      } else {
         drawString(graphicsIn, this.fontRenderer, info, 10, this.height - 40, -6250336);
      }

      super.extractRenderState(graphicsIn, mouseX, mouseY, partialTicks);
      this.tooltipManager.drawTooltips(graphicsIn, mouseX, mouseY, this.getButtonList());
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor graphicsIn, int mouseX, int mouseY, float partialTicks) {
   }

   @Override
   public void tick() {
      super.tick();
      if (this.updateTimer > 0) {
         this.updateTimer--;
      }
   }

   public void requestUpdate() {
      if (this.updateTimer > 3) {
         this.updateTimer = 3;
      }
   }

   public Minecraft getMc() {
      return this.minecraft;
   }

   public void drawCenteredString(GuiGraphicsExtractor graphicsIn, String text, int x, int y, int color) {
      drawCenteredString(graphicsIn, this.fontRenderer, text, x, y, color);
   }

   public static String toStringOnOff(boolean value, boolean defValue) {
      String str = value ? Lang.getOn() : Lang.getOff();
      String color = "";
      if (value != defValue) {
         color = value ? "Â§a" : "Â§c";
      }

      return color + str;
   }

   public static String toStringAa(int value, int valueDef) {
      String text = Lang.getOff();
      if (value == 2) {
         text = "FXAA 2x";
      }

      if (value == 4) {
         text = "FXAA 4x";
      }

      String color = value != valueDef ? "Â§a" : "";
      return color + text;
   }

   public static String toStringValue(float val, float[] values, String[] names) {
      int index = getValueIndex(val, values);
      return names[index];
   }

   private float getNextValue(float val, float[] values, float valDef, boolean forward, boolean reset) {
      if (reset) {
         return valDef;
      }

      int index = getValueIndex(val, values);
      if (forward) {
         if (++index >= values.length) {
            index = 0;
         }
      } else if (--index < 0) {
         index = values.length - 1;
      }

      return values[index];
   }

   public static int getValueIndex(float val, float[] values) {
      for (int i = 0; i < values.length; i++) {
         float value = values[i];
         if (value >= val) {
            return i;
         }
      }

      return values.length - 1;
   }

   public static String toStringQuality(float val, float valDef) {
      String color = val != valDef ? "Â§a" : "";
      return color + toStringValue(val, QUALITY_MULTIPLIERS, QUALITY_MULTIPLIER_NAMES);
   }

   public static String toStringHandDepth(float val, float valDef) {
      String color = val != valDef ? "Â§a" : "";
      return color + toStringValue(val, HAND_DEPTH_VALUES, HAND_DEPTH_NAMES);
   }

   public static int getOSType() {
      String osName = System.getProperty("os.name").toLowerCase();
      if (osName.contains("win")) {
         return 1;
      } else if (osName.contains("mac")) {
         return 2;
      } else if (osName.contains("solaris")) {
         return 3;
      } else if (osName.contains("sunos")) {
         return 3;
      } else if (osName.contains("linux")) {
         return 4;
      } else {
         return osName.contains("unix") ? 4 : 0;
      }
   }
}
