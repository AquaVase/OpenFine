package net.optifine;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.imageio.ImageIO;
import net.minecraft.client.Options;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.optifine.util.PropertiesOrdered;
import net.optifine.util.ResUtils;
import net.optifine.util.TextureUtils;

public class TextureAnimations {
   private static TextureAnimation[] textureAnimations = null;
   private static int countAnimationsActive = 0;
   private static int frameCountAnimations = 0;

   public static void reset() {
      textureAnimations = null;
   }

   public static void update() {
      textureAnimations = null;
      countAnimationsActive = 0;
      PackResources[] rps = Config.getResourcePacks();
      textureAnimations = getTextureAnimations(rps);
      updateAnimations();
   }

   public static void updateAnimations() {
      if (textureAnimations != null && Config.isAnimatedTextures()) {
         int countActive = 0;

         for (int i = 0; i < textureAnimations.length; i++) {
            TextureAnimation anim = textureAnimations[i];
            anim.updateTexture();
            if (anim.isActive()) {
               countActive++;
            }
         }

         int frameCount = Config.getMinecraft().levelRenderer.getFrameCount();
         if (frameCount != frameCountAnimations) {
            countAnimationsActive = countActive;
            frameCountAnimations = frameCount;
         }

         if (SmartAnimations.isActive()) {
            SmartAnimations.resetTexturesRendered();
         }
      } else {
         countAnimationsActive = 0;
      }
   }

   private static TextureAnimation[] getTextureAnimations(PackResources[] rps) {
      List list = new ArrayList();

      for (int i = 0; i < rps.length; i++) {
         PackResources rp = rps[i];
         TextureAnimation[] tas = getTextureAnimations(rp);
         if (tas != null) {
            list.addAll(Arrays.asList(tas));
         }
      }

      return (TextureAnimation[])list.toArray(new TextureAnimation[list.size()]);
   }

   private static TextureAnimation[] getTextureAnimations(PackResources rp) {
      String[] animPropNames = ResUtils.collectFiles(rp, "optifine/anim/", ".properties", null);
      if (animPropNames.length <= 0) {
         return null;
      }

      List list = new ArrayList();

      for (int i = 0; i < animPropNames.length; i++) {
         String propName = animPropNames[i];
         Config.dbg("Texture animation: " + propName);

         try {
            Identifier propLoc = new Identifier(propName);
            InputStream in = Config.getResourceStream(rp, PackType.CLIENT_RESOURCES, propLoc);
            Properties props = new PropertiesOrdered();
            props.load(in);
            in.close();
            TextureAnimation anim = makeTextureAnimation(props, propLoc);
            if (anim != null) {
               Identifier locDstTex = new Identifier(anim.getDstTex());
               if (!Config.hasResource(rp, locDstTex)) {
                  Config.dbg("Skipped: " + propName + ", target texture not loaded from same resource pack");
               } else {
                  list.add(anim);
               }
            }
         } catch (FileNotFoundException e) {
            Config.warn("File not found: " + e.getMessage());
         } catch (IOException e) {
            e.printStackTrace();
         }
      }

      return (TextureAnimation[])list.toArray(new TextureAnimation[list.size()]);
   }

   private static TextureAnimation makeTextureAnimation(Properties props, Identifier propLoc) {
      String texFrom = props.getProperty("from");
      String texTo = props.getProperty("to");
      int x = Config.parseInt(props.getProperty("x"), -1);
      int y = Config.parseInt(props.getProperty("y"), -1);
      int width = Config.parseInt(props.getProperty("w"), -1);
      int height = Config.parseInt(props.getProperty("h"), -1);
      if (texFrom == null || texTo == null) {
         Config.warn("TextureAnimation: Source or target texture not specified");
         return null;
      }

      if (x >= 0 && y >= 0 && width >= 0 && height >= 0) {
         texFrom = texFrom.trim();
         texTo = texTo.trim();
         String basePath = TextureUtils.getBasePath(propLoc.getPath());
         texFrom = TextureUtils.fixResourcePath(texFrom, basePath);
         texTo = TextureUtils.fixResourcePath(texTo, basePath);
         byte[] imageBytes = getCustomTextureData(texFrom, width);
         if (imageBytes == null) {
            Config.warn("TextureAnimation: Source texture not found: " + texTo);
            return null;
         }

         int countPixels = imageBytes.length / 4;
         int countFrames = countPixels / (width * height);
         int countPixelsAllFrames = countFrames * width * height;
         if (countPixels != countPixelsAllFrames) {
            Config.warn("TextureAnimation: Source texture has invalid number of frames: " + texFrom + ", frames: " + (float)countPixels / (width * height));
            return null;
         }

         Identifier locTexTo = new Identifier(texTo);

         try {
            InputStream inTexTo = Config.getResourceStream(locTexTo);
            if (inTexTo == null) {
               Config.warn("TextureAnimation: Target texture not found: " + texTo);
               return null;
            }

            BufferedImage imgTexTo = readTextureImage(inTexTo);
            if (x + width <= imgTexTo.getWidth() && y + height <= imgTexTo.getHeight()) {
               return new TextureAnimation(texFrom, imageBytes, texTo, locTexTo, x, y, width, height, props);
            }

            Config.warn("TextureAnimation: Animation coordinates are outside the target texture: " + texTo);
            return null;
         } catch (IOException e) {
            Config.warn("TextureAnimation: Target texture not found: " + texTo);
            return null;
         }
      } else {
         Config.warn("TextureAnimation: Invalid coordinates");
         return null;
      }
   }

   private static byte[] getCustomTextureData(String imagePath, int tileWidth) {
      byte[] imageBytes = loadImage(imagePath, tileWidth);
      if (imageBytes == null) {
         imageBytes = loadImage("/anim" + imagePath, tileWidth);
      }

      return imageBytes;
   }

   private static byte[] loadImage(String name, int targetWidth) {
      Options options = Config.getGameSettings();

      try {
         Identifier locRes = new Identifier(name);
         InputStream in = Config.getResourceStream(locRes);
         if (in == null) {
            return null;
         }

         BufferedImage image = readTextureImage(in);
         in.close();
         if (image == null) {
            return null;
         }

         if (targetWidth > 0 && image.getWidth() != targetWidth) {
            double aspectHW = image.getHeight() / image.getWidth();
            int targetHeight = (int)(targetWidth * aspectHW);
            image = scaleBufferedImage(image, targetWidth, targetHeight);
         }

         int width = image.getWidth();
         int height = image.getHeight();
         int[] ai = new int[width * height];
         byte[] byteBuf = new byte[width * height * 4];
         image.getRGB(0, 0, width, height, ai, 0, width);

         for (int l = 0; l < ai.length; l++) {
            int alpha = ai[l] >> 24 & 0xFF;
            int red = ai[l] >> 16 & 0xFF;
            int green = ai[l] >> 8 & 0xFF;
            int blue = ai[l] & 0xFF;
            byteBuf[l * 4 + 0] = (byte)red;
            byteBuf[l * 4 + 1] = (byte)green;
            byteBuf[l * 4 + 2] = (byte)blue;
            byteBuf[l * 4 + 3] = (byte)alpha;
         }

         return byteBuf;
      } catch (FileNotFoundException e) {
         return null;
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   private static BufferedImage readTextureImage(InputStream par1InputStream) throws IOException {
      BufferedImage var2 = ImageIO.read(par1InputStream);
      par1InputStream.close();
      return var2;
   }

   private static BufferedImage scaleBufferedImage(BufferedImage image, int width, int height) {
      BufferedImage scaledImage = new BufferedImage(width, height, 2);
      Graphics2D gr = scaledImage.createGraphics();
      gr.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      gr.drawImage(image, 0, 0, width, height, null);
      return scaledImage;
   }

   public static int getCountAnimations() {
      return textureAnimations == null ? 0 : textureAnimations.length;
   }

   public static int getCountAnimationsActive() {
      return countAnimationsActive;
   }
}
