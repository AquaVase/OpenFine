package net.optifine;

import com.mojang.blaze3d.platform.NativeImage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
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
        if (textureAnimations != null) {
            for (TextureAnimation animation : textureAnimations) {
                animation.close();
            }
        }
        textureAnimations = null;
        countAnimationsActive = 0;
    }

    public static void update() {
        reset();
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
                Properties props = new PropertiesOrdered();
                try (InputStream in = Config.getResourceStream(rp, PackType.CLIENT_RESOURCES, propLoc)) {
                    props.load(in);
                }
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

                try (NativeImage imgTexTo = readTextureImage(inTexTo)) {
                    if (x + width <= imgTexTo.getWidth() && y + height <= imgTexTo.getHeight()) {
                        return new TextureAnimation(texFrom, imageBytes, texTo, locTexTo, x, y, width, height, props);
                    }
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
        try {
            Identifier locRes = new Identifier(name);
            InputStream in = Config.getResourceStream(locRes);
            if (in == null) {
                return null;
            }

            try (NativeImage image = readTextureImage(in)) {
                if (targetWidth > 0 && image.getWidth() != targetWidth) {
                    int targetHeight = Math.toIntExact((long)image.getHeight() * targetWidth / image.getWidth());
                    try (NativeImage scaled = TextureUtils.scaleImage(image, targetWidth, targetHeight)) {
                        return getImageBytes(scaled);
                    }
                }
                return getImageBytes(image);
            }
        } catch (FileNotFoundException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static NativeImage readTextureImage(InputStream input) throws IOException {
        return NativeImage.read(input);
    }

    private static byte[] getImageBytes(NativeImage image) {
        byte[] pixels = new byte[Math.multiplyExact(Math.multiplyExact(image.getWidth(), image.getHeight()), 4)];
        image.getPixelBytes().get(pixels);
        return pixels;
    }

    public static int getCountAnimations() {
        return textureAnimations == null ? 0 : textureAnimations.length;
    }

    public static int getCountAnimationsActive() {
        return countAnimationsActive;
    }
}
