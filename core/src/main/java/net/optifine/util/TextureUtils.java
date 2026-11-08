package net.optifine.util;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.minecraft.client.renderer.entity.layers.MushroomCowMushroomLayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.optifine.BetterGrass;
import net.optifine.BetterSnow;
import net.optifine.Config;
import net.optifine.ConnectedTextures;
import net.optifine.CustomBlockLayers;
import net.optifine.CustomColors;
import net.optifine.CustomGuis;
import net.optifine.CustomItems;
import net.optifine.CustomLoadingScreens;
import net.optifine.CustomPanorama;
import net.optifine.CustomSky;
import net.optifine.EmissiveTextures;
import net.optifine.Lang;
import net.optifine.NaturalTextures;
import net.optifine.RandomEntities;
import net.optifine.SmartAnimations;
import net.optifine.SmartLeaves;
import net.optifine.TextureAnimations;
import net.optifine.TextureProperties;
import net.optifine.entity.model.CustomEntityModels;
import net.optifine.reflect.ReflectorForge;
import net.optifine.render.GLConst;
import net.optifine.shaders.MultiTexID;
import net.optifine.shaders.Shaders;
import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class TextureUtils {
    private static final String texGrassTop = "grass_block_top";
    private static final String texGrassSide = "grass_block_side";
    private static final String texGrassSideOverlay = "grass_block_side_overlay";
    private static final String texSnow = "snow";
    private static final String texGrassSideSnowed = "grass_block_snow";
    private static final String texMyceliumSide = "mycelium_side";
    private static final String texMyceliumTop = "mycelium_top";
    private static final String texWaterStill = "water_still";
    private static final String texWaterFlow = "water_flow";
    private static final String texLavaStill = "lava_still";
    private static final String texLavaFlow = "lava_flow";
    private static final String texFireLayer0 = "fire_0";
    private static final String texFireLayer1 = "fire_1";
    private static final String texSoulFireLayer0 = "soul_fire_0";
    private static final String texSoulFireLayer1 = "soul_fire_1";
    private static final String texCampFire = "campfire_fire";
    private static final String texCampFireLogLit = "campfire_log_lit";
    private static final String texSoulCampFire = "soul_campfire_fire";
    private static final String texSoulCampFireLogLit = "soul_campfire_log_lit";
    private static final String texPortal = "nether_portal";
    private static final String texGlass = "glass";
    private static final String texGlassPaneTop = "glass_pane_top";
    public static TextureAtlasSprite iconGrassTop;
    public static TextureAtlasSprite iconGrassSide;
    public static TextureAtlasSprite iconGrassSideOverlay;
    public static TextureAtlasSprite iconSnow;
    public static TextureAtlasSprite iconGrassSideSnowed;
    public static TextureAtlasSprite iconMyceliumSide;
    public static TextureAtlasSprite iconMyceliumTop;
    public static TextureAtlasSprite iconWaterStill;
    public static TextureAtlasSprite iconWaterFlow;
    public static TextureAtlasSprite iconLavaStill;
    public static TextureAtlasSprite iconLavaFlow;
    public static TextureAtlasSprite iconFireLayer0;
    public static TextureAtlasSprite iconFireLayer1;
    public static TextureAtlasSprite iconSoulFireLayer0;
    public static TextureAtlasSprite iconSoulFireLayer1;
    public static TextureAtlasSprite iconCampFire;
    public static TextureAtlasSprite iconCampFireLogLit;
    public static TextureAtlasSprite iconSoulCampFire;
    public static TextureAtlasSprite iconSoulCampFireLogLit;
    public static TextureAtlasSprite iconPortal;
    public static TextureAtlasSprite iconGlass;
    public static TextureAtlasSprite iconGlassPaneTop;
    public static final String SPRITE_PREFIX_BLOCKS = "minecraft:block/";
    public static final String SPRITE_PREFIX_ITEMS = "minecraft:item/";
    public static final Identifier LOCATION_SPRITE_EMPTY = new Identifier("optifine/ctm/default/empty");
    public static final Identifier LOCATION_TEXTURE_EMPTY = new Identifier("optifine/ctm/default/empty.png");
    public static final Identifier WHITE_TEXTURE_LOCATION = new Identifier("optifine/textures/white16.png");
    private static IntBuffer staticBuffer = Config.createDirectIntBuffer(256);
    private static int glMaximumTextureSize = -1;
    private static Map<Integer, String> mapTextureAllocations = new HashMap<>();
    private static Map<Identifier, Identifier> mapSpriteLocations = new HashMap<>();
    private static Identifier LOCATION_ATLAS_PAINTINGS = new Identifier("textures/atlas/paintings.png");
    private static Throwable modelLoadingException;

    public static void update() {
        TextureAtlas mapBlocks = getTextureMapBlocks();
        if (mapBlocks != null) {
            String prefix = "minecraft:block/";
            iconGrassTop = getSpriteCheck(mapBlocks, prefix + "grass_block_top");
            iconGrassSide = getSpriteCheck(mapBlocks, prefix + "grass_block_side");
            iconGrassSideOverlay = getSpriteCheck(mapBlocks, prefix + "grass_block_side_overlay");
            iconSnow = getSpriteCheck(mapBlocks, prefix + "snow");
            iconGrassSideSnowed = getSpriteCheck(mapBlocks, prefix + "grass_block_snow");
            iconMyceliumSide = getSpriteCheck(mapBlocks, prefix + "mycelium_side");
            iconMyceliumTop = getSpriteCheck(mapBlocks, prefix + "mycelium_top");
            iconWaterStill = getSpriteCheck(mapBlocks, prefix + "water_still");
            iconWaterFlow = getSpriteCheck(mapBlocks, prefix + "water_flow");
            iconLavaStill = getSpriteCheck(mapBlocks, prefix + "lava_still");
            iconLavaFlow = getSpriteCheck(mapBlocks, prefix + "lava_flow");
            iconFireLayer0 = getSpriteCheck(mapBlocks, prefix + "fire_0");
            iconFireLayer1 = getSpriteCheck(mapBlocks, prefix + "fire_1");
            iconSoulFireLayer0 = getSpriteCheck(mapBlocks, prefix + "soul_fire_0");
            iconSoulFireLayer1 = getSpriteCheck(mapBlocks, prefix + "soul_fire_1");
            iconCampFire = getSpriteCheck(mapBlocks, prefix + "campfire_fire");
            iconCampFireLogLit = getSpriteCheck(mapBlocks, prefix + "campfire_log_lit");
            iconSoulCampFire = getSpriteCheck(mapBlocks, prefix + "soul_campfire_fire");
            iconSoulCampFireLogLit = getSpriteCheck(mapBlocks, prefix + "soul_campfire_log_lit");
            iconPortal = getSpriteCheck(mapBlocks, prefix + "nether_portal");
            iconGlass = getSpriteCheck(mapBlocks, prefix + "glass");
            iconGlassPaneTop = getSpriteCheck(mapBlocks, prefix + "glass_pane_top");
            String prefixItems = "minecraft:item/";
            mapSpriteLocations.clear();
        }
    }

    public static TextureAtlasSprite getSpriteCheck(TextureAtlas textureMap, String name) {
        TextureAtlasSprite sprite = textureMap.getUploadedSprite(name);
        if (sprite == null || MissingTextureAtlasSprite.isMisingSprite(sprite)) {
            Config.warn("Sprite not found: " + name);
        }

        return sprite;
    }

    public static BufferedImage fixTextureDimensions(String name, BufferedImage bi) {
        if (name.startsWith("/mob/zombie") || name.startsWith("/mob/pigzombie")) {
            int width = bi.getWidth();
            int height = bi.getHeight();
            if (width == height * 2) {
                BufferedImage scaledImage = new BufferedImage(width, height * 2, 2);
                Graphics2D gr = scaledImage.createGraphics();
                gr.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                gr.drawImage(bi, 0, 0, width, height, null);
                return scaledImage;
            }
        }

        return bi;
    }

    public static int ceilPowerOfTwo(int val) {
        int i = 1;

        while (i < val) {
            i *= 2;
        }

        return i;
    }

    public static int getPowerOfTwo(int val) {
        int i = 1;

        int po2;
        for (po2 = 0; i < val; po2++) {
            i *= 2;
        }

        return po2;
    }

    public static int twoToPower(int power) {
        int val = 1;

        for (int i = 0; i < power; i++) {
            val *= 2;
        }

        return val;
    }

    public static AbstractTexture getTexture(Identifier loc) {
        AbstractTexture tex = Config.getTextureManager().getTexture(loc);
        if (tex != null) {
            return tex;
        }

        if (!Config.hasResource(loc)) {
            return null;
        }

        tex = new SimpleTexture(loc);
        Config.getTextureManager().register(loc, tex);
        return tex;
    }

    public static void resourcesPreReload(ResourceManager rm) {
        modelLoadingException = null;
        Config.dbg("*** Reloading resources ***");
        TextureProperties.update();
        CustomItems.update();
    }

    public static Throwable getModelLoadingException() {
        return modelLoadingException;
    }

    public static void setModelLoadingException(Throwable exceptionIn) {
        modelLoadingException = exceptionIn;
    }

    public static void resourcesReloaded(ResourceManager rm) {
        if (getTextureMapBlocks() != null) {
            Config.dbg("*** Reloading custom textures ***");
            CustomSky.reset();
            TextureAnimations.reset();
            update();
            NaturalTextures.update();
            BetterGrass.update();
            BetterSnow.update();
            TextureAnimations.update();
            CustomColors.update();
            CustomSky.update();
            CustomItems.updateModels();
            CustomEntityModels.update();
            Shaders.resourcesReloaded();
            Lang.resourcesReloaded();
            Config.updateTexturePackClouds();
            SmartLeaves.updateLeavesModels();
            CustomPanorama.update();
            CustomGuis.update();
            MushroomCowMushroomLayer.update();
            CustomLoadingScreens.update();
            CustomBlockLayers.update();
            SmartAnimations.update();
            Config.getTextureManager().tick();
            Config.dbg("Disable Forge light pipeline");
            ReflectorForge.setForgeLightPipelineEnabled(false);
        }
    }

    public static TextureAtlas getTextureMapBlocks() {
        return Config.getTextureMapBlocks();
    }

    public static void registerResourceListener() {
        if (Config.getResourceManager() instanceof ReloadableResourceManager rrm) {
            SimplePreparableReloadListener rl = new SimplePreparableReloadListener() {
                @Override
                protected Object prepare(ResourceManager p_212854_1_, ProfilerFiller p_212854_2_) {
                    return null;
                }

                @Override
                protected void apply(Object p_212853_1_, ResourceManager p_212853_2_, ProfilerFiller p_212853_3_) {
                }
            };
            rrm.registerReloadListener(rl);
            ResourceManagerReloadListener rmrl = new ResourceManagerReloadListener() {
                @Override
                public void onResourceManagerReload(ResourceManager resourceManager) {
                    TextureUtils.resourcesReloaded(resourceManager);
                }
            };
            rrm.registerReloadListener(rmrl);
        }
    }

    public static void registerTickableTextures() {
        TickableAbstractTexture tt = new TickableAbstractTexture() {
            @Override
            public void tick() {
                TextureAnimations.updateAnimations();
            }

            @Override
            public int getGlTextureId() {
                return 0;
            }

            @Override
            public MultiTexID getMultiTexID() {
                return null;
            }
        };
        Identifier ttl = new Identifier("optifine/tickable_textures");
        Config.getTextureManager().register(ttl, tt);
    }

    public static void registerCustomModels(Map<Identifier, Resource> mapModels) {
        CustomItems.registerModels(mapModels);
    }

    public static void registerCustomSprites(TextureAtlas textureMap) {
        if (textureMap.isTerrain()) {
            Config.dbg("Register block sprites");
            ConnectedTextures.updateIcons(textureMap);
            BetterGrass.updateIcons(textureMap);
        }

        if (textureMap.isItems()) {
            Config.dbg("Register item sprites");
            CustomItems.registerIcons(textureMap);
        }

        textureMap.registerSprite(LOCATION_SPRITE_EMPTY);
    }

    public static void registerCustomSpriteLocations(Identifier atlasLocation, Set<Identifier> spriteLocations) {
        RandomEntities.registerSprites(atlasLocation, spriteLocations);
    }

    public static void refreshCustomSprites(TextureAtlas textureMap) {
        if (textureMap.isTerrain()) {
            Config.dbg("Update block sprites");
            ConnectedTextures.refreshIcons(textureMap);
            BetterGrass.refreshIcons(textureMap);
        }

        if (textureMap.isItems()) {
            Config.dbg("Update item sprites");
            CustomItems.updateIcons(textureMap);
        }

        EmissiveTextures.refreshIcons(textureMap);
    }

    public static Identifier fixResourceLocation(Identifier loc, String basePath) {
        if (!loc.getNamespace().equals("minecraft")) {
            return loc;
        }

        String path = loc.getPath();
        String pathFixed = fixResourcePath(path, basePath);
        if (pathFixed != path) {
            loc = new Identifier(loc.getNamespace(), pathFixed);
        }

        return loc;
    }

    public static String fixResourcePath(String path, String basePath) {
        String defaultNamespace = "minecraft:";
        if (path.startsWith(defaultNamespace)) {
            return StrUtils.removePrefix(path, defaultNamespace);
        }

        String strAssMc = "assets/minecraft/";
        if (path.startsWith(strAssMc)) {
            return path.substring(strAssMc.length());
        }

        if (path.startsWith("./")) {
            path = path.substring(2);
            if (!basePath.endsWith("/")) {
                basePath = basePath + "/";
            }

            return basePath + path;
        } else {
            if (path.startsWith("/~")) {
                path = path.substring(1);
            }

            String strOptifine = "optifine/";
            if (path.startsWith("~/")) {
                path = path.substring(2);
                return strOptifine + path;
            } else {
                return path.startsWith("/") ? strOptifine + path.substring(1) : path;
            }
        }
    }

    public static String getBasePath(String path) {
        int pos = path.lastIndexOf(47);
        return pos < 0 ? "" : path.substring(0, pos);
    }

    public static void applyAnisotropicLevel() {
        if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
            float maxLevel = GL11.glGetFloat(34047);
            float level = Config.getAnisotropicFilterLevel();
            level = Math.min(level, maxLevel);
            GL11.glTexParameterf(3553, 34046, level);
        }
    }

    public static void bindTexture(int glTexId) {
        GlStateManager._bindTexture(glTexId);
    }

    public static boolean isPowerOfTwo(int x) {
        int x2 = Mth.smallestEncompassingPowerOfTwo(x);
        return x2 == x;
    }

    public static NativeImage scaleImage(NativeImage ni, int w2) {
        BufferedImage bi = toBufferedImage(ni);
        BufferedImage bi2 = scaleImage(bi, w2);
        return toNativeImage(bi2);
    }

    public static BufferedImage toBufferedImage(NativeImage ni) {
        int width = ni.getWidth();
        int height = ni.getHeight();
        int[] data = new int[width * height];
        ni.getBufferRGBA().get(data);
        BufferedImage bi = new BufferedImage(width, height, 2);
        bi.setRGB(0, 0, width, height, data, 0, width);
        return bi;
    }

    private static NativeImage toNativeImage(BufferedImage bi) {
        int width = bi.getWidth();
        int height = bi.getHeight();
        int[] data = new int[width * height];
        bi.getRGB(0, 0, width, height, data, 0, width);
        NativeImage ni = new NativeImage(width, height, false);
        ni.getBufferRGBA().put(data);
        return ni;
    }

    public static BufferedImage scaleImage(BufferedImage bi, int w2) {
        int w = bi.getWidth();
        int h = bi.getHeight();
        int h2 = h * w2 / w;
        BufferedImage bi2 = new BufferedImage(w2, h2, 2);
        Graphics2D g2 = bi2.createGraphics();
        Object method = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
        if (w2 < w || w2 % w != 0) {
            method = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
        }

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, method);
        g2.drawImage(bi, 0, 0, w2, h2, null);
        return bi2;
    }

    public static int scaleToGrid(int size, int sizeGrid) {
        if (size == sizeGrid) {
            return size;
        }

        int sizeNew = size / sizeGrid * sizeGrid;

        while (sizeNew < size) {
            sizeNew += sizeGrid;
        }

        return sizeNew;
    }

    public static int scaleToMin(int size, int sizeMin) {
        if (size >= sizeMin) {
            return size;
        }

        int sizeNew = sizeMin / size * size;

        while (sizeNew < sizeMin) {
            sizeNew += size;
        }

        return sizeNew;
    }

    public static Dimension getImageSize(InputStream in, String suffix) {
        Iterator iter = ImageIO.getImageReadersBySuffix(suffix);

        while (iter.hasNext()) {
            ImageReader reader = (ImageReader)iter.next();

            try {
                ImageInputStream iis = ImageIO.createImageInputStream(in);
                reader.setInput(iis);
                int width = reader.getWidth(reader.getMinIndex());
                int height = reader.getHeight(reader.getMinIndex());
                return new Dimension(width, height);
            } catch (IOException e) {
            } finally {
                reader.dispose();
            }
        }

        return null;
    }

    public static void dbgMipmaps(TextureAtlasSprite textureatlassprite) {
        NativeImage[] mipmapImages = textureatlassprite.getMipmapImages();

        for (int l = 0; l < mipmapImages.length; l++) {
            NativeImage image = mipmapImages[l];
            if (image == null) {
                Config.dbg(l + ": " + image);
            } else {
                Config.dbg(l + ": " + image.getWidth() * image.getHeight());
            }
        }
    }

    public static void saveGlTexture(String name, int textureId, int mipmapLevels, int width, int height) {
        bindTexture(textureId);
        GL11.glPixelStorei(3333, 1);
        GL11.glPixelStorei(3317, 1);
        name = StrUtils.removeSuffix(name, ".png");
        File fileBase = new File(name);
        File dir = fileBase.getParentFile();
        if (dir != null) {
            dir.mkdirs();
        }

        for (int i = 0; i < 16; i++) {
            String namePng = name + "_" + i + ".png";
            File filePng = new File(namePng);
            filePng.delete();
        }

        for (int level = 0; level <= mipmapLevels; level++) {
            File filePng = new File(name + "_" + level + ".png");
            int widthLevel = width >> level;
            int heightLevel = height >> level;
            int sizeLevel = widthLevel * heightLevel;
            IntBuffer buf = BufferUtils.createIntBuffer(sizeLevel);
            int[] data = new int[sizeLevel];
            GL11.glGetTexImage(3553, level, 32993, 33639, buf);
            buf.get(data);
            BufferedImage image = new BufferedImage(widthLevel, heightLevel, 2);
            image.setRGB(0, 0, widthLevel, heightLevel, data, 0, widthLevel);

            try {
                ImageIO.write(image, "png", filePng);
                Config.dbg("Exported: " + filePng);
            } catch (Exception e) {
                Config.warn("Error writing: " + filePng);
                Config.warn(e.getClass().getName() + ": " + e.getMessage());
            }
        }
    }

    public static int getGLMaximumTextureSize() {
        if (glMaximumTextureSize < 0) {
            glMaximumTextureSize = detectGLMaximumTextureSize();
        }

        return glMaximumTextureSize;
    }

    private static int detectGLMaximumTextureSize() {
        for (int i = 65536; i > 0; i >>= 1) {
            GlStateManager._texImage2D(32868, 0, 6408, i, i, 0, 6408, 5121, (ByteBuffer)null);
            int err = GL11.glGetError();
            int width = GlStateManager._getTexLevelParameter(32868, 0, 4096);
            if (width != 0) {
                return i;
            }
        }

        return 0;
    }

    public static BufferedImage readBufferedImage(InputStream imageStream) throws IOException {
        if (imageStream == null) {
            return null;
        }

        try {
            return ImageIO.read(imageStream);
        } finally {
            IOUtils.closeQuietly(imageStream);
        }
    }

    public static int toAbgr(int argb) {
        int a = argb >> 24 & 0xFF;
        int r = argb >> 16 & 0xFF;
        int g = argb >> 8 & 0xFF;
        int b = argb >> 0 & 0xFF;
        return a << 24 | b << 16 | g << 8 | r;
    }

    public static void resetDataUnpacking() {
        GlStateManager._pixelStore(3314, 0);
        GlStateManager._pixelStore(3316, 0);
        GlStateManager._pixelStore(3315, 0);
        GlStateManager._pixelStore(3317, 4);
    }

    public static String getStackTrace(Throwable t) {
        CharArrayWriter caw = new CharArrayWriter();
        t.printStackTrace(new PrintWriter(caw));
        return caw.toString();
    }

    public static void debugTextureGenerated(int id) {
        mapTextureAllocations.put(id, getStackTrace(new Throwable("StackTrace")));
        Config.dbg("Textures: " + mapTextureAllocations.size());
    }

    public static void debugTextureDeleted(int id) {
        mapTextureAllocations.remove(id);
        Config.dbg("Textures: " + mapTextureAllocations.size());
    }

    public static TextureAtlasSprite getCustomSprite(TextureAtlasSprite sprite) {
        if (sprite == null) {
            return null;
        }

        if (Config.isRandomEntities()) {
            sprite = RandomEntities.getRandomSprite(sprite);
        }

        if (EmissiveTextures.isActive()) {
            sprite = EmissiveTextures.getEmissiveSprite(sprite);
        }

        return sprite;
    }

    public static Identifier getSpriteLocation(Identifier loc) {
        Identifier locSprite = mapSpriteLocations.get(loc);
        if (locSprite == null) {
            String pathSprite = loc.getPath();
            pathSprite = StrUtils.removePrefix(pathSprite, "textures/");
            pathSprite = StrUtils.removeSuffix(pathSprite, ".png");
            locSprite = new Identifier(loc.getNamespace(), pathSprite);
            mapSpriteLocations.put(loc, locSprite);
        }

        return locSprite;
    }

    public static int generateTextureId() {
        RenderSystem.assertOnRenderThread();
        return GlStateManager._genTexture();
    }

    public static void releaseTextureId(int textureId) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._deleteTexture(textureId);
    }

    public static void prepareImage(int textureId, int width, int height) {
        prepareImage(NativeImage.Format.RGBA, textureId, 0, width, height);
    }

    public static void prepareImage(NativeImage.Format pixelFormat, int textureId, int width, int height) {
        prepareImage(pixelFormat, textureId, 0, width, height);
    }

    public static void prepareImage(int textureId, int mipmapLevel, int width, int height) {
        prepareImage(NativeImage.Format.RGBA, textureId, mipmapLevel, width, height);
    }

    public static void prepareImage(NativeImage.Format pixelFormat, int textureId, int mipmapLevel, int width, int height) {
        RenderSystem.assertOnRenderThread();
        bindTexture(textureId);
        if (mipmapLevel >= 0) {
            GlStateManager.texParameter(3553, 33085, mipmapLevel);
            GlStateManager.texParameter(3553, 33082, 0.0F);
            GlStateManager.texParameter(3553, 33083, mipmapLevel);
            GlStateManager.texParameter(3553, 34049, 0.0F);
        }

        for (int i = 0; i <= mipmapLevel; i++) {
            GlStateManager._texImage2D(3553, i, GLConst.toGl(pixelFormat), width >> i, height >> i, 0, 6408, 5121, null);
        }
    }
}
