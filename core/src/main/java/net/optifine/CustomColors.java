package net.optifine;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import javax.imageio.ImageIO;
import net.minecraft.IdentifierException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import net.optifine.config.ConnectedParser;
import net.optifine.config.MatchBlock;
import net.optifine.render.RenderEnv;
import net.optifine.util.BiomeUtils;
import net.optifine.util.ColorUtils;
import net.optifine.util.EntityUtils;
import net.optifine.util.PotionUtils;
import net.optifine.util.PropertiesOrdered;
import net.optifine.util.ResUtils;
import net.optifine.util.StrUtils;
import net.optifine.util.TextureUtils;
import net.optifine.util.WorldUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class CustomColors {
    private static String paletteFormatDefault = "vanilla";
    private static CustomColormap waterColors = null;
    private static CustomColormap foliagePineColors = null;
    private static CustomColormap foliageBirchColors = null;
    private static CustomColormap swampFoliageColors = null;
    private static CustomColormap swampGrassColors = null;
    private static CustomColormap[] colorsBlockColormaps = null;
    private static CustomColormap[][] blockColormaps = null;
    private static CustomColormap skyColors = null;
    private static CustomColorFader skyColorFader = new CustomColorFader();
    private static CustomColormap fogColors = null;
    private static CustomColorFader fogColorFader = new CustomColorFader();
    private static CustomColormap underwaterColors = null;
    private static CustomColorFader underwaterColorFader = new CustomColorFader();
    private static CustomColormap underlavaColors = null;
    private static CustomColorFader underlavaColorFader = new CustomColorFader();
    private static LightMapPack[] lightMapPacks = null;
    private static int lightmapMinDimensionId = 0;
    private static CustomColormap redstoneColors = null;
    private static CustomColormap xpOrbColors = null;
    private static int xpOrbTime = -1;
    private static CustomColormap durabilityColors = null;
    private static CustomColormap stemColors = null;
    private static CustomColormap stemMelonColors = null;
    private static CustomColormap stemPumpkinColors = null;
    private static CustomColormap lavaDropColors = null;
    private static CustomColormap myceliumParticleColors = null;
    private static boolean useDefaultGrassFoliageColors = true;
    private static int particleWaterColor = -1;
    private static int particlePortalColor = -1;
    private static int lilyPadColor = -1;
    private static int expBarTextColor = -1;
    private static int bossTextColor = -1;
    private static int signTextColor = -1;
    private static Vec3 fogColorNether = null;
    private static Vec3 fogColorEnd = null;
    private static Vec3 skyColorEnd = null;
    private static Vec3 fogColorBuffer = new Vec3(0.0, 0.0, 0.0);
    private static Vec3 skyColorBuffer = new Vec3(0.0, 0.0, 0.0);
    private static int[] spawnEggPrimaryColors = null;
    private static int[] spawnEggSecondaryColors = null;
    private static int[] wolfCollarColors = null;
    private static int[] sheepColors = null;
    private static int[] textColors = null;
    private static int[] mapColorsOriginal = null;
    private static int[] dyeColorsOriginal = null;
    private static int[] potionColors = null;
    private static final BlockState BLOCK_STATE_DIRT = Blocks.DIRT.defaultBlockState();
    private static final BlockState BLOCK_STATE_WATER = Blocks.WATER.defaultBlockState();
    private static final BlockTintSource TINT_SOURCE_WATER = BlockTintSources.water();
    private static final BlockTintSource TINT_SOURCE_LILY_PAD = getBlockColors().getTintSource(Blocks.LILY_PAD.defaultBlockState(), 0);
    public static Random random = new Random();
    private static final CustomColors.IColorizer COLORIZER_GRASS = new CustomColors.IColorizer() {
        @Override
        public int getColor(BlockState blockState, BlockAndTintGetter blockAccess, BlockPos blockPos) {
            Biome biome = CustomColors.getColorBiome(blockAccess, blockPos);
            return CustomColors.swampGrassColors != null && biome == BiomeUtils.SWAMP
                ? CustomColors.swampGrassColors.getColor(biome, blockPos)
                : biome.getGrassColor(blockPos.getX(), blockPos.getZ());
        }

        @Override
        public boolean isColorConstant() {
            return false;
        }
    };
    private static final CustomColors.IColorizer COLORIZER_FOLIAGE = new CustomColors.IColorizer() {
        @Override
        public int getColor(BlockState blockState, BlockAndTintGetter blockAccess, BlockPos blockPos) {
            Biome biome = CustomColors.getColorBiome(blockAccess, blockPos);
            return CustomColors.swampFoliageColors != null && biome == BiomeUtils.SWAMP
                ? CustomColors.swampFoliageColors.getColor(biome, blockPos)
                : biome.getFoliageColor();
        }

        @Override
        public boolean isColorConstant() {
            return false;
        }
    };
    private static final CustomColors.IColorizer COLORIZER_FOLIAGE_PINE = new CustomColors.IColorizer() {
        @Override
        public int getColor(BlockState blockState, BlockAndTintGetter blockAccess, BlockPos blockPos) {
            return CustomColors.foliagePineColors != null ? CustomColors.foliagePineColors.getColor(blockAccess, blockPos) : -10380959;
        }

        @Override
        public boolean isColorConstant() {
            return CustomColors.foliagePineColors == null;
        }
    };
    private static final CustomColors.IColorizer COLORIZER_FOLIAGE_BIRCH = new CustomColors.IColorizer() {
        @Override
        public int getColor(BlockState blockState, BlockAndTintGetter blockAccess, BlockPos blockPos) {
            return CustomColors.foliageBirchColors != null ? CustomColors.foliageBirchColors.getColor(blockAccess, blockPos) : -8345771;
        }

        @Override
        public boolean isColorConstant() {
            return CustomColors.foliageBirchColors == null;
        }
    };
    private static final CustomColors.IColorizer COLORIZER_WATER = new CustomColors.IColorizer() {
        @Override
        public int getColor(BlockState blockState, BlockAndTintGetter blockAccess, BlockPos blockPos) {
            Biome biome = CustomColors.getColorBiome(blockAccess, blockPos);
            return CustomColors.waterColors != null ? CustomColors.waterColors.getColor(biome, blockPos) : biome.getWaterColor();
        }

        @Override
        public boolean isColorConstant() {
            return false;
        }
    };

    public static void update() {
        paletteFormatDefault = "vanilla";
        waterColors = null;
        foliageBirchColors = null;
        foliagePineColors = null;
        swampGrassColors = null;
        swampFoliageColors = null;
        skyColors = null;
        fogColors = null;
        underwaterColors = null;
        underlavaColors = null;
        redstoneColors = null;
        xpOrbColors = null;
        xpOrbTime = -1;
        durabilityColors = null;
        stemColors = null;
        lavaDropColors = null;
        myceliumParticleColors = null;
        lightMapPacks = null;
        particleWaterColor = -1;
        particlePortalColor = -1;
        lilyPadColor = -1;
        expBarTextColor = -1;
        bossTextColor = -1;
        signTextColor = -1;
        fogColorNether = null;
        fogColorEnd = null;
        skyColorEnd = null;
        colorsBlockColormaps = null;
        blockColormaps = null;
        useDefaultGrassFoliageColors = true;
        spawnEggPrimaryColors = null;
        spawnEggSecondaryColors = null;
        wolfCollarColors = null;
        sheepColors = null;
        textColors = null;
        setMapColors(mapColorsOriginal);
        setDyeColors(dyeColorsOriginal);
        potionColors = null;
        paletteFormatDefault = getValidProperty("optifine/color.properties", "palette.format", CustomColormap.FORMAT_STRINGS, "vanilla");
        String mcpColormap = "optifine/colormap/";
        String[] waterPaths = new String[]{"water.png", "watercolorx.png"};
        waterColors = getCustomColors(mcpColormap, waterPaths, 256, -1);
        updateUseDefaultGrassFoliageColors();
        if (Config.isCustomColors()) {
            String[] pinePaths = new String[]{"pine.png", "pinecolor.png"};
            foliagePineColors = getCustomColors(mcpColormap, pinePaths, 256, -1);
            String[] birchPaths = new String[]{"birch.png", "birchcolor.png"};
            foliageBirchColors = getCustomColors(mcpColormap, birchPaths, 256, -1);
            String[] swampGrassPaths = new String[]{"swampgrass.png", "swampgrasscolor.png"};
            swampGrassColors = getCustomColors(mcpColormap, swampGrassPaths, 256, -1);
            String[] swampFoliagePaths = new String[]{"swampfoliage.png", "swampfoliagecolor.png"};
            swampFoliageColors = getCustomColors(mcpColormap, swampFoliagePaths, 256, -1);
            String[] sky0Paths = new String[]{"sky0.png", "skycolor0.png"};
            skyColors = getCustomColors(mcpColormap, sky0Paths, 256, -1);
            String[] fog0Paths = new String[]{"fog0.png", "fogcolor0.png"};
            fogColors = getCustomColors(mcpColormap, fog0Paths, 256, -1);
            String[] underwaterPaths = new String[]{"underwater.png", "underwatercolor.png"};
            underwaterColors = getCustomColors(mcpColormap, underwaterPaths, 256, -1);
            String[] underlavaPaths = new String[]{"underlava.png", "underlavacolor.png"};
            underlavaColors = getCustomColors(mcpColormap, underlavaPaths, 256, -1);
            String[] redstonePaths = new String[]{"redstone.png", "redstonecolor.png"};
            redstoneColors = getCustomColors(mcpColormap, redstonePaths, 16, 1);
            xpOrbColors = getCustomColors(mcpColormap + "xporb.png", -1, -1);
            durabilityColors = getCustomColors(mcpColormap + "durability.png", -1, -1);
            String[] stemPaths = new String[]{"stem.png", "stemcolor.png"};
            stemColors = getCustomColors(mcpColormap, stemPaths, 8, 1);
            stemPumpkinColors = getCustomColors(mcpColormap + "pumpkinstem.png", 8, 1);
            stemMelonColors = getCustomColors(mcpColormap + "melonstem.png", 8, 1);
            lavaDropColors = getCustomColors(mcpColormap + "lavadrop.png", -1, 1);
            String[] myceliumPaths = new String[]{"myceliumparticle.png", "myceliumparticlecolor.png"};
            myceliumParticleColors = getCustomColors(mcpColormap, myceliumPaths, -1, -1);
            Pair<LightMapPack[], Integer> lightMaps = parseLightMapPacks();
            lightMapPacks = lightMaps.getLeft();
            lightmapMinDimensionId = lightMaps.getRight();
            readColorProperties("optifine/color.properties");
            blockColormaps = readBlockColormaps(new String[]{mcpColormap + "custom/", mcpColormap + "blocks/"}, colorsBlockColormaps, 256, -1);
            updateUseDefaultGrassFoliageColors();
        }
    }

    private static String getValidProperty(String fileName, String key, String[] validValues, String valDef) {
        try {
            Identifier loc = new Identifier(fileName);
            InputStream in = Config.getResourceStream(loc);
            if (in == null) {
                return valDef;
            } else {
                Properties props = new PropertiesOrdered();
                props.load(in);
                in.close();
                String val = props.getProperty(key);
                if (val == null) {
                    return valDef;
                } else {
                    List<String> listValidValues = Arrays.asList(validValues);
                    if (!listValidValues.contains(val)) {
                        warn("Invalid value: " + key + "=" + val);
                        warn("Expected values: " + Config.arrayToString(validValues));
                        return valDef;
                    } else {
                        dbg(key + "=" + val);
                        return val;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            return valDef;
        } catch (IOException e) {
            e.printStackTrace();
            return valDef;
        }
    }

    private static Pair<LightMapPack[], Integer> parseLightMapPacks() {
        String lightmapPrefix = "optifine/lightmap/world";
        String lightmapSuffix = ".png";
        String[] pathsLightmap = ResUtils.collectFiles(lightmapPrefix, lightmapSuffix);
        Map<Integer, String> mapLightmaps = new HashMap<>();

        for (int i = 0; i < pathsLightmap.length; i++) {
            String path = pathsLightmap[i];
            String dimIdStr = StrUtils.removePrefixSuffix(path, lightmapPrefix, lightmapSuffix);
            int dimId = Config.parseInt(dimIdStr, Integer.MIN_VALUE);
            if (dimId != Integer.MIN_VALUE) {
                mapLightmaps.put(dimId, path);
            }
        }

        Set<Integer> setDimIds = mapLightmaps.keySet();
        Integer[] dimIds = setDimIds.toArray(new Integer[setDimIds.size()]);
        Arrays.sort(dimIds);
        if (dimIds.length <= 0) {
            return new ImmutablePair<>(null, 0);
        }

        int minDimId = dimIds[0];
        int maxDimId = dimIds[dimIds.length - 1];
        int countDim = maxDimId - minDimId + 1;
        CustomColormap[] colormaps = new CustomColormap[countDim];

        for (int i = 0; i < dimIds.length; i++) {
            Integer dimId = dimIds[i];
            String path = mapLightmaps.get(dimId);
            CustomColormap colors = getCustomColors(path, -1, -1);
            if (colors != null) {
                if (colors.getWidth() < 16) {
                    warn("Invalid lightmap width: " + colors.getWidth() + ", path: " + path);
                } else {
                    int lightmapIndex = dimId - minDimId;
                    colormaps[lightmapIndex] = colors;
                }
            }
        }

        LightMapPack[] lmps = new LightMapPack[colormaps.length];

        for (int i = 0; i < colormaps.length; i++) {
            CustomColormap cm = colormaps[i];
            if (cm != null) {
                String name = cm.name;
                String basePath = cm.basePath;
                CustomColormap cmRain = getCustomColors(basePath + "/" + name + "_rain.png", -1, -1);
                CustomColormap cmThunder = getCustomColors(basePath + "/" + name + "_thunder.png", -1, -1);
                LightMap lm = new LightMap(cm);
                LightMap lmRain = cmRain != null ? new LightMap(cmRain) : null;
                LightMap lmThunder = cmThunder != null ? new LightMap(cmThunder) : null;
                LightMapPack lmp = new LightMapPack(lm, lmRain, lmThunder);
                lmps[i] = lmp;
            }
        }

        return new ImmutablePair<>(lmps, minDimId);
    }

    private static int getTextureHeight(String path, int defHeight) {
        try {
            InputStream in = Config.getResourceStream(new Identifier(path));
            if (in == null) {
                return defHeight;
            }

            BufferedImage bi = ImageIO.read(in);
            in.close();
            return bi == null ? defHeight : bi.getHeight();
        } catch (IOException e) {
            return defHeight;
        }
    }

    private static void readColorProperties(String fileName) {
        try {
            Identifier loc = new Identifier(fileName);
            InputStream in = Config.getResourceStream(loc);
            if (in == null) {
                return;
            }

            dbg("Loading " + fileName);
            Properties props = new PropertiesOrdered();
            props.load(in);
            in.close();
            particleWaterColor = readColor(props, new String[]{"particle.water", "drop.water"});
            particlePortalColor = readColor(props, "particle.portal");
            lilyPadColor = readColor(props, "lilypad");
            expBarTextColor = readColorAlpha(props, "text.xpbar");
            bossTextColor = readColorAlpha(props, "text.boss");
            signTextColor = readColorAlpha(props, "text.sign");
            fogColorNether = readColorVec3(props, "fog.nether");
            fogColorEnd = readColorVec3(props, "fog.end");
            skyColorEnd = readColorVec3(props, "sky.end");
            colorsBlockColormaps = readCustomColormaps(props, fileName);
            spawnEggPrimaryColors = readSpawnEggColors(props, fileName, "egg.shell.", "Spawn egg shell");
            spawnEggSecondaryColors = readSpawnEggColors(props, fileName, "egg.spots.", "Spawn egg spot");
            wolfCollarColors = readDyeColors(props, fileName, "collar.", "Wolf collar");
            sheepColors = readDyeColors(props, fileName, "sheep.", "Sheep");
            textColors = readTextColors(props, fileName, "text.code.", "Text");
            int[] mapColors = readMapColors(props, fileName, "map.", "Map");
            if (mapColors != null) {
                if (mapColorsOriginal == null) {
                    mapColorsOriginal = getMapColors();
                }

                setMapColors(mapColors);
            }

            int[] dyeColors = readDyeColors(props, fileName, "dye.", "Dye");
            if (dyeColors != null) {
                if (dyeColorsOriginal == null) {
                    dyeColorsOriginal = getDyeColors();
                }

                setDyeColors(dyeColors);
            }

            potionColors = readPotionColors(props, fileName, "potion.", "Potion");
            xpOrbTime = Config.parseInt(props.getProperty("xporb.time"), -1);
        } catch (FileNotFoundException e) {
            return;
        } catch (IOException e) {
            Config.warn("Error parsing: " + fileName);
            Config.warn(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private static CustomColormap[] readCustomColormaps(Properties props, String fileName) {
        List list = new ArrayList();
        String palettePrefix = "palette.block.";
        Map map = new HashMap();

        for (String key : (java.lang.Iterable<String>)(java.lang.Iterable<?>)(props.keySet())) {
            String value = props.getProperty(key);
            if (key.startsWith(palettePrefix)) {
                map.put(key, value);
            }
        }

        String[] propNames = (String[])map.keySet().toArray(new String[map.size()]);

        for (int i = 0; i < propNames.length; i++) {
            String name = propNames[i];
            String value = props.getProperty(name);
            dbg("Block palette: " + name + " = " + value);
            String path = name.substring(palettePrefix.length());
            String basePath = TextureUtils.getBasePath(fileName);
            path = TextureUtils.fixResourcePath(path, basePath);
            CustomColormap colors = getCustomColors(path, 256, -1);
            if (colors == null) {
                warn("Colormap not found: " + path);
            } else {
                ConnectedParser cp = new ConnectedParser("CustomColors");
                MatchBlock[] mbs = cp.parseMatchBlocks(value);
                if (mbs != null && mbs.length > 0) {
                    for (int m = 0; m < mbs.length; m++) {
                        MatchBlock mb = mbs[m];
                        colors.addMatchBlock(mb);
                    }

                    list.add(colors);
                } else {
                    warn("Invalid match blocks: " + value);
                }
            }
        }

        return list.size() <= 0 ? null : (CustomColormap[])list.toArray(new CustomColormap[list.size()]);
    }

    private static CustomColormap[][] readBlockColormaps(String[] basePaths, CustomColormap[] basePalettes, int width, int height) {
        String[] paths = ResUtils.collectFiles(basePaths, new String[]{".properties"});
        Arrays.sort(paths);
        List blockList = new ArrayList();

        for (int i = 0; i < paths.length; i++) {
            String path = paths[i];
            dbg("Block colormap: " + path);

            try {
                Identifier locFile = new Identifier("minecraft", path);
                InputStream in = Config.getResourceStream(locFile);
                if (in == null) {
                    warn("File not found: " + path);
                } else {
                    Properties props = new PropertiesOrdered();
                    props.load(in);
                    in.close();
                    CustomColormap cm = new CustomColormap(props, path, width, height, paletteFormatDefault);
                    if (cm.isValid(path) && cm.isValidMatchBlocks(path)) {
                        addToBlockList(cm, blockList);
                    }
                }
            } catch (FileNotFoundException e) {
                warn("File not found: " + path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (basePalettes != null) {
            for (int i = 0; i < basePalettes.length; i++) {
                CustomColormap cm = basePalettes[i];
                addToBlockList(cm, blockList);
            }
        }

        return blockList.size() <= 0 ? null : blockListToArray(blockList);
    }

    private static void addToBlockList(CustomColormap cm, List blockList) {
        int[] ids = cm.getMatchBlockIds();
        if (ids != null && ids.length > 0) {
            for (int i = 0; i < ids.length; i++) {
                int blockId = ids[i];
                if (blockId < 0) {
                    warn("Invalid block ID: " + blockId);
                } else {
                    addToList(cm, blockList, blockId);
                }
            }
        } else {
            warn("No match blocks: " + Config.arrayToString(ids));
        }
    }

    private static void addToList(CustomColormap cm, List list, int id) {
        while (id >= list.size()) {
            list.add(null);
        }

        List subList = (List)list.get(id);
        if (subList == null) {
            subList = new ArrayList();
            list.set(id, subList);
        }

        subList.add(cm);
    }

    private static CustomColormap[][] blockListToArray(List list) {
        CustomColormap[][] colArr = new CustomColormap[list.size()][];

        for (int i = 0; i < list.size(); i++) {
            List subList = (List)list.get(i);
            if (subList != null) {
                CustomColormap[] subArr = (CustomColormap[])subList.toArray(new CustomColormap[subList.size()]);
                colArr[i] = subArr;
            }
        }

        return colArr;
    }

    private static int readColor(Properties props, String[] names) {
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            int col = readColor(props, name);
            if (col >= 0) {
                return col;
            }
        }

        return -1;
    }

    private static int readColor(Properties props, String name) {
        String str = props.getProperty(name);
        if (str == null) {
            return -1;
        } else {
            str = str.trim();
            int color = parseColor(str);
            if (color < 0) {
                warn("Invalid color: " + name + " = " + str);
                return color;
            } else {
                dbg(name + " = " + str);
                return color;
            }
        }
    }

    private static int readColorAlpha(Properties props, String name) {
        String str = props.getProperty(name);
        if (str == null) {
            return -1;
        } else {
            str = str.trim();
            int color = parseColorAlpha(str);
            if (color == -1) {
                warn("Invalid color: " + name + " = " + str);
                return color;
            } else {
                dbg(name + " = " + str);
                return color;
            }
        }
    }

    private static int parseColor(String str) {
        if (str == null) {
            return -1;
        }

        str = str.trim();

        try {
            return Integer.parseInt(str, 16) & 16777215;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static int parseColorAlpha(String str) {
        if (str == null) {
            return -1;
        }

        str = str.trim();

        try {
            boolean hasAlpha = str.length() > 6;
            int val = Integer.parseInt(str, 16) & 16777215;
            if (!hasAlpha) {
                val |= -16777216;
            }

            return val;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static Vec3 readColorVec3(Properties props, String name) {
        int col = readColor(props, name);
        if (col < 0) {
            return null;
        }

        int red = col >> 16 & 0xFF;
        int green = col >> 8 & 0xFF;
        int blue = col & 0xFF;
        float redF = red / 255.0F;
        float greenF = green / 255.0F;
        float blueF = blue / 255.0F;
        return new Vec3(redF, greenF, blueF);
    }

    private static CustomColormap getCustomColors(String basePath, String[] paths, int width, int height) {
        for (int i = 0; i < paths.length; i++) {
            String path = paths[i];
            path = basePath + path;
            CustomColormap cols = getCustomColors(path, width, height);
            if (cols != null) {
                return cols;
            }
        }

        return null;
    }

    public static CustomColormap getCustomColors(String pathImage, int width, int height) {
        try {
            Identifier loc = new Identifier(pathImage);
            if (!Config.hasResource(loc)) {
                return null;
            }

            dbg("Colormap " + pathImage);
            Properties props = new PropertiesOrdered();
            String pathProps = StrUtils.replaceSuffix(pathImage, ".png", ".properties");
            Identifier locProps = new Identifier(pathProps);
            if (Config.hasResource(locProps)) {
                InputStream in = Config.getResourceStream(locProps);
                props.load(in);
                in.close();
                dbg("Colormap properties: " + pathProps);
            } else {
                props.put("format", paletteFormatDefault);
                props.put("source", pathImage);
                pathProps = pathImage;
            }

            CustomColormap cm = new CustomColormap(props, pathProps, width, height, paletteFormatDefault);
            return !cm.isValid(pathProps) ? null : cm;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void updateUseDefaultGrassFoliageColors() {
        useDefaultGrassFoliageColors = foliageBirchColors == null && foliagePineColors == null && swampGrassColors == null && swampFoliageColors == null;
    }

    public static int getColorMultiplier(BakedQuad quad, BlockState blockState, BlockAndTintGetter blockAccess, BlockPos blockPos, RenderEnv renderEnv) {
        return getColorMultiplier(quad.isTinted(), blockState, blockAccess, blockPos, renderEnv);
    }

    public static int getColorMultiplier(boolean quadHasTintIndex, BlockState blockState, BlockAndTintGetter blockAccess, BlockPos blockPos, RenderEnv renderEnv) {
        Block block = blockState.getBlock();
        BlockState blockStateColormap = blockState;
        if (blockColormaps != null) {
            if (!quadHasTintIndex) {
                if (block == Blocks.GRASS_BLOCK) {
                    blockStateColormap = BLOCK_STATE_DIRT;
                }

                if (block == Blocks.REDSTONE_WIRE) {
                    return -1;
                }
            }

            if (block instanceof DoublePlantBlock && blockState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
                blockPos = blockPos.below();
                blockStateColormap = blockAccess.getBlockState(blockPos);
            }

            CustomColormap cm = getBlockColormap(blockStateColormap);
            if (cm != null) {
                if (Config.isSmoothBiomes() && !cm.isColorConstant()) {
                    return getSmoothColorMultiplier(blockState, blockAccess, blockPos, cm, renderEnv.getColorizerBlockPosM());
                }

                return cm.getColor(blockAccess, blockPos);
            }
        }

        if (!quadHasTintIndex) {
            return -1;
        }

        if (block == Blocks.LILY_PAD) {
            return getLilypadColorMultiplier(blockAccess, blockPos);
        }

        if (block == Blocks.REDSTONE_WIRE) {
            return getRedstoneColor(renderEnv.getBlockState());
        }

        if (block instanceof StemBlock) {
            return getStemColorMultiplier(blockState, blockAccess, blockPos, renderEnv);
        }

        if (useDefaultGrassFoliageColors) {
            return -1;
        }

        CustomColors.IColorizer colorizer;
        if (block == Blocks.GRASS_BLOCK || block instanceof TallGrassBlock || block instanceof DoublePlantBlock || block == Blocks.SUGAR_CANE) {
            colorizer = COLORIZER_GRASS;
        } else if (block instanceof DoublePlantBlock) {
            colorizer = COLORIZER_GRASS;
            if (blockState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
                blockPos = blockPos.below();
            }
        } else if (block instanceof LeavesBlock) {
            if (block == Blocks.SPRUCE_LEAVES) {
                colorizer = COLORIZER_FOLIAGE_PINE;
            } else if (block == Blocks.BIRCH_LEAVES) {
                colorizer = COLORIZER_FOLIAGE_BIRCH;
            } else {
                if (block == Blocks.CHERRY_LEAVES) {
                    return -1;
                }

                if (!blockState.getBlockLocation().isDefaultNamespace()) {
                    return -1;
                }

                colorizer = COLORIZER_FOLIAGE;
            }
        } else {
            if (block != Blocks.VINE) {
                return -1;
            }

            colorizer = COLORIZER_FOLIAGE;
        }

        return Config.isSmoothBiomes() && !colorizer.isColorConstant()
            ? getSmoothColorMultiplier(blockState, blockAccess, blockPos, colorizer, renderEnv.getColorizerBlockPosM())
            : colorizer.getColor(blockStateColormap, blockAccess, blockPos);
    }

    protected static Biome getColorBiome(BlockAndTintGetter blockAccess, BlockPos blockPos) {
        Biome biome = BiomeUtils.getBiome(blockAccess, blockPos);
        return fixBiome(biome);
    }

    public static Biome fixBiome(Biome biome) {
        return (biome == BiomeUtils.SWAMP || biome == BiomeUtils.MANGROVE_SWAMP) && !Config.isSwampColors() ? BiomeUtils.PLAINS : biome;
    }

    private static CustomColormap getBlockColormap(BlockState blockState) {
        if (blockColormaps == null) {
            return null;
        } else if (!(blockState instanceof BlockState bs)) {
            return null;
        } else {
            int blockId = bs.getBlockId();
            if (blockId >= 0 && blockId < blockColormaps.length) {
                CustomColormap[] cms = blockColormaps[blockId];
                if (cms == null) {
                    return null;
                }

                for (int i = 0; i < cms.length; i++) {
                    CustomColormap cm = cms[i];
                    if (cm.matchesBlock(bs)) {
                        return cm;
                    }
                }

                return null;
            } else {
                return null;
            }
        }
    }

    private static int getSmoothColorMultiplier(
        BlockState blockState, BlockAndTintGetter blockAccess, BlockPos blockPos, CustomColors.IColorizer colorizer, BlockPosM blockPosM
    ) {
        int sumRed = 0;
        int sumGreen = 0;
        int sumBlue = 0;
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();
        BlockPosM posM = blockPosM;
        int radius = Config.getBiomeBlendRadius();
        int width = radius * 2 + 1;
        int count = width * width;

        for (int ix = x - radius; ix <= x + radius; ix++) {
            for (int iz = z - radius; iz <= z + radius; iz++) {
                posM.setXyz(ix, y, iz);
                int col = colorizer.getColor(blockState, blockAccess, posM);
                sumRed += col >> 16 & 0xFF;
                sumGreen += col >> 8 & 0xFF;
                sumBlue += col & 0xFF;
            }
        }

        int r = sumRed / count;
        int g = sumGreen / count;
        int b = sumBlue / count;
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    public static int getFluidColor(BlockTintSource tintSource, BlockAndTintGetter blockAccess, BlockState blockState, BlockPos blockPos, RenderEnv renderEnv) {
        Block block = blockState.getBlock();
        CustomColors.IColorizer colorizer = getBlockColormap(blockState);
        if (colorizer == null && blockState.getBlock() == Blocks.WATER) {
            colorizer = COLORIZER_WATER;
        }

        if (colorizer == null) {
            return tintSource != null ? tintSource.colorInWorld(blockState, blockAccess, blockPos) : -1;
        } else {
            return Config.isSmoothBiomes() && !colorizer.isColorConstant()
                ? getSmoothColorMultiplier(blockState, blockAccess, blockPos, colorizer, renderEnv.getColorizerBlockPosM())
                : colorizer.getColor(blockState, blockAccess, blockPos);
        }
    }

    public static BlockColors getBlockColors() {
        return Minecraft.getInstance().getBlockColors();
    }

    public static void updatePortalFX(Particle particle) {
        if (particlePortalColor >= 0) {
            if (particle instanceof SingleQuadParticle fx) {
                int col = particlePortalColor;
                int red = col >> 16 & 0xFF;
                int green = col >> 8 & 0xFF;
                int blue = col & 0xFF;
                float redF = red / 255.0F;
                float greenF = green / 255.0F;
                float blueF = blue / 255.0F;
                fx.setColor(redF, greenF, blueF);
            }
        }
    }

    public static void updateLavaFX(Particle particle) {
        if (lavaDropColors != null) {
            if (particle instanceof SingleQuadParticle fx) {
                int age = fx.getAge();
                int col = lavaDropColors.getColor(age);
                int red = col >> 16 & 0xFF;
                int green = col >> 8 & 0xFF;
                int blue = col & 0xFF;
                float redF = red / 255.0F;
                float greenF = green / 255.0F;
                float blueF = blue / 255.0F;
                fx.setColor(redF, greenF, blueF);
            }
        }
    }

    public static void updateMyceliumFX(Particle particle) {
        if (myceliumParticleColors != null) {
            if (particle instanceof SingleQuadParticle fx) {
                int col = myceliumParticleColors.getColorRandom();
                int red = col >> 16 & 0xFF;
                int green = col >> 8 & 0xFF;
                int blue = col & 0xFF;
                float redF = red / 255.0F;
                float greenF = green / 255.0F;
                float blueF = blue / 255.0F;
                fx.setColor(redF, greenF, blueF);
            }
        }
    }

    private static int getRedstoneColor(BlockState blockState) {
        if (redstoneColors == null) {
            return -1;
        }

        int level = getRedstoneLevel(blockState, 15);
        return redstoneColors.getColor(level);
    }

    public static void updateReddustFX(Particle particle, BlockAndTintGetter blockAccess, double x, double y, double z) {
        if (redstoneColors != null) {
            if (particle instanceof SingleQuadParticle fx) {
                BlockState state = blockAccess.getBlockState(BlockPos.containing(x, y, z));
                int level = getRedstoneLevel(state, 15);
                int col = redstoneColors.getColor(level);
                int red = col >> 16 & 0xFF;
                int green = col >> 8 & 0xFF;
                int blue = col & 0xFF;
                float redF = red / 255.0F;
                float greenF = green / 255.0F;
                float blueF = blue / 255.0F;
                fx.setColor(redF, greenF, blueF);
            }
        }
    }

    private static int getRedstoneLevel(BlockState state, int def) {
        Block block = state.getBlock();
        if (!(block instanceof RedStoneWireBlock)) {
            return def;
        } else {
            return !(state.getValue(RedStoneWireBlock.POWER) instanceof Integer valInt) ? def : valInt;
        }
    }

    public static float getXpOrbTimer(float timer) {
        if (xpOrbTime <= 0) {
            return timer;
        }

        float kt = 628.0F / xpOrbTime;
        return timer * kt;
    }

    public static int getXpOrbColor(float timer) {
        if (xpOrbColors == null) {
            return -1;
        }

        int index = (int)Math.round((Mth.sin(timer) + 1.0F) * (xpOrbColors.getLength() - 1) / 2.0);
        return xpOrbColors.getColor(index);
    }

    public static int getDurabilityColor(float dur, int color) {
        if (durabilityColors == null) {
            return color;
        }

        int index = (int)(dur * durabilityColors.getLength());
        return durabilityColors.getColor(index);
    }

    public static void updateWaterFX(Particle particle, BlockAndTintGetter blockAccess, double x, double y, double z, RenderEnv renderEnv) {
        if (waterColors != null || blockColormaps != null || particleWaterColor >= 0) {
            if (particle instanceof SingleQuadParticle fx) {
                BlockPos blockPos = BlockPos.containing(x, y, z);
                renderEnv.reset(BLOCK_STATE_WATER, blockPos);
                int col = getFluidColor(TINT_SOURCE_WATER, blockAccess, BLOCK_STATE_WATER, blockPos, renderEnv);
                int red = col >> 16 & 0xFF;
                int green = col >> 8 & 0xFF;
                int blue = col & 0xFF;
                float redF = red / 255.0F;
                float greenF = green / 255.0F;
                float blueF = blue / 255.0F;
                if (particleWaterColor >= 0) {
                    int redDrop = particleWaterColor >> 16 & 0xFF;
                    int greenDrop = particleWaterColor >> 8 & 0xFF;
                    int blueDrop = particleWaterColor & 0xFF;
                    redF = redDrop / 255.0F;
                    greenF = greenDrop / 255.0F;
                    blueF = blueDrop / 255.0F;
                    redF *= redDrop / 255.0F;
                    greenF *= greenDrop / 255.0F;
                    blueF *= blueDrop / 255.0F;
                }

                fx.setColor(redF, greenF, blueF);
            }
        }
    }

    private static int getLilypadColorMultiplier(BlockAndTintGetter blockAccess, BlockPos blockPos) {
        return lilyPadColor < 0 ? TINT_SOURCE_LILY_PAD.colorInWorld(Blocks.LILY_PAD.defaultBlockState(), blockAccess, blockPos) : lilyPadColor;
    }

    private static Vec3 getFogColorNether(Vec3 col) {
        return fogColorNether == null ? col : fogColorNether;
    }

    private static Vec3 getFogColorEnd(Vec3 col) {
        return fogColorEnd == null ? col : fogColorEnd;
    }

    private static Vec3 getSkyColorEnd(Vec3 col) {
        return skyColorEnd == null ? col : skyColorEnd;
    }

    public static int getSkyColor(int skyColor, BlockAndTintGetter blockAccess, double x, double y, double z) {
        Vec3 skyColorVec = ColorUtils.argbToVec3(skyColor);
        Vec3 newColVec = getSkyColor(skyColorVec, blockAccess, x, y, z);
        return ColorUtils.vec3ToArgb(newColVec);
    }

    public static Vec3 getSkyColor(Vec3 skyColor3d, BlockAndTintGetter blockAccess, double x, double y, double z) {
        if (skyColors == null) {
            return skyColor3d;
        }

        int col = skyColors.getColorSmooth(blockAccess, x, y, z, 3);
        int red = col >> 16 & 0xFF;
        int green = col >> 8 & 0xFF;
        int blue = col & 0xFF;
        float redF = red / 255.0F;
        float greenF = green / 255.0F;
        float blueF = blue / 255.0F;
        float cRed = (float)skyColor3d.x / 0.5F;
        float cGreen = (float)skyColor3d.y / 0.66275F;
        float cBlue = (float)skyColor3d.z;
        redF *= cRed;
        greenF *= cGreen;
        blueF *= cBlue;
        return skyColorFader.getColor(redF, greenF, blueF);
    }

    private static Vec3 getFogColor(Vec3 fogColor3d, BlockAndTintGetter blockAccess, double x, double y, double z) {
        if (fogColors == null) {
            return fogColor3d;
        }

        int col = fogColors.getColorSmooth(blockAccess, x, y, z, 3);
        int red = col >> 16 & 0xFF;
        int green = col >> 8 & 0xFF;
        int blue = col & 0xFF;
        float redF = red / 255.0F;
        float greenF = green / 255.0F;
        float blueF = blue / 255.0F;
        float cRed = (float)fogColor3d.x / 0.753F;
        float cGreen = (float)fogColor3d.y / 0.8471F;
        float cBlue = (float)fogColor3d.z;
        redF *= cRed;
        greenF *= cGreen;
        blueF *= cBlue;
        return fogColorFader.getColor(redF, greenF, blueF);
    }

    public static Vec3 getUnderwaterColor(BlockAndTintGetter blockAccess, double x, double y, double z) {
        return getUnderFluidColor(blockAccess, x, y, z, underwaterColors, underwaterColorFader);
    }

    public static Vec3 getUnderlavaColor(BlockAndTintGetter blockAccess, double x, double y, double z) {
        return getUnderFluidColor(blockAccess, x, y, z, underlavaColors, underlavaColorFader);
    }

    public static Vec3 getUnderFluidColor(
        BlockAndTintGetter blockAccess, double x, double y, double z, CustomColormap underFluidColors, CustomColorFader underFluidColorFader
    ) {
        if (underFluidColors == null) {
            return null;
        }

        int col = underFluidColors.getColorSmooth(blockAccess, x, y, z, 3);
        int red = col >> 16 & 0xFF;
        int green = col >> 8 & 0xFF;
        int blue = col & 0xFF;
        float redF = red / 255.0F;
        float greenF = green / 255.0F;
        float blueF = blue / 255.0F;
        return underFluidColorFader.getColor(redF, greenF, blueF);
    }

    private static int getStemColorMultiplier(BlockState blockState, BlockGetter blockAccess, BlockPos blockPos, RenderEnv renderEnv) {
        CustomColormap colors = stemColors;
        Block blockStem = blockState.getBlock();
        if (blockStem == Blocks.PUMPKIN_STEM && stemPumpkinColors != null) {
            colors = stemPumpkinColors;
        }

        if (blockStem == Blocks.MELON_STEM && stemMelonColors != null) {
            colors = stemMelonColors;
        }

        if (colors == null) {
            return -1;
        }

        if (!(blockStem instanceof StemBlock)) {
            return -1;
        }

        int level = blockState.getValue(StemBlock.AGE);
        return colors.getColor(level);
    }

    public static boolean updateLightmap(ClientLevel world, float torchFlickerX, NativeImage lmColors, boolean nightvision, float darkLight, float partialTicks) {
        if (world == null) {
            return false;
        } else if (lightMapPacks == null) {
            return false;
        } else {
            int dimensionId = WorldUtils.getDimensionId(world);
            int lightMapIndex = dimensionId - lightmapMinDimensionId;
            if (lightMapIndex >= 0 && lightMapIndex < lightMapPacks.length) {
                LightMapPack lightMapPack = lightMapPacks[lightMapIndex];
                return lightMapPack == null ? false : lightMapPack.updateLightmap(world, torchFlickerX, lmColors, nightvision, darkLight, partialTicks);
            } else {
                return false;
            }
        }
    }

    public static int getWorldFogColor(int color, Level world, Entity renderViewEntity, float partialTicks) {
        ColorUtils.argbToVec3(color, fogColorBuffer);
        Vec3 fogVec = getWorldFogColor(fogColorBuffer, world, renderViewEntity, partialTicks);
        return fogVec == fogColorBuffer ? color : ColorUtils.vec3ToArgb(fogVec);
    }

    public static Vec3 getWorldFogColor(Vec3 fogVec, Level world, Entity renderViewEntity, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (WorldUtils.isNether(world)) {
            return getFogColorNether(fogVec);
        } else if (WorldUtils.isOverworld(world)) {
            return getFogColor(fogVec, mc.level, renderViewEntity.getX(), renderViewEntity.getY() + 1.0, renderViewEntity.getZ());
        } else {
            return WorldUtils.isEnd(world) ? getFogColorEnd(fogVec) : fogVec;
        }
    }

    public static int getWorldSkyColor(int color, Level world, Entity renderViewEntity, float partialTicks) {
        ColorUtils.argbToVec3(color, skyColorBuffer);
        Vec3 skyVec = getWorldSkyColor(skyColorBuffer, world, renderViewEntity, partialTicks);
        return skyVec == skyColorBuffer ? color : ColorUtils.vec3ToArgb(skyVec);
    }

    public static Vec3 getWorldSkyColor(Vec3 skyVec, Level world, Entity renderViewEntity, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (WorldUtils.isOverworld(world) && renderViewEntity != null) {
            return getSkyColor(skyVec, mc.level, renderViewEntity.getX(), renderViewEntity.getY() + 1.0, renderViewEntity.getZ());
        } else {
            return WorldUtils.isEnd(world) ? getSkyColorEnd(skyVec) : skyVec;
        }
    }

    private static int[] readSpawnEggColors(Properties props, String fileName, String prefix, String logName) {
        List<Integer> list = new ArrayList<>();
        Set keys = props.keySet();
        int countColors = 0;

        for (String key : (java.lang.Iterable<String>)(java.lang.Iterable<?>)(keys)) {
            String value = props.getProperty(key);
            if (key.startsWith(prefix)) {
                String name = StrUtils.removePrefix(key, prefix);
                int id = EntityUtils.getEntityIdByName(name);

                try {
                    if (id < 0) {
                        id = EntityUtils.getEntityIdByLocation(new Identifier(name).toString());
                    }
                } catch (IdentifierException e) {
                    Config.warn("ResourceLocationException: " + e.getMessage());
                }

                if (id < 0) {
                    warn("Invalid spawn egg name: " + key);
                } else {
                    int color = parseColor(value);
                    if (color < 0) {
                        warn("Invalid spawn egg color: " + key + " = " + value);
                    } else {
                        while (list.size() <= id) {
                            list.add(-1);
                        }

                        list.set(id, color);
                        countColors++;
                    }
                }
            }
        }

        if (countColors <= 0) {
            return null;
        }

        dbg(logName + " colors: " + countColors);
        int[] colors = new int[list.size()];

        for (int i = 0; i < colors.length; i++) {
            colors[i] = list.get(i);
        }

        return colors;
    }

    private static int getSpawnEggColor(SpawnEggItem item, ItemStack itemStack, int layer, int color) {
        if (spawnEggPrimaryColors == null && spawnEggSecondaryColors == null) {
            return color;
        } else {
            EntityType entityType = SpawnEggItem.getType(itemStack);
            if (entityType == null) {
                return color;
            } else {
                int id = BuiltInRegistries.ENTITY_TYPE.getId(entityType);
                if (id < 0) {
                    return color;
                } else {
                    int[] eggColors = layer == 0 ? spawnEggPrimaryColors : spawnEggSecondaryColors;
                    if (eggColors == null) {
                        return color;
                    } else if (id >= 0 && id < eggColors.length) {
                        int eggColor = eggColors[id];
                        return eggColor < 0 ? color : eggColor;
                    } else {
                        return color;
                    }
                }
            }
        }
    }

    public static int getFullColorFromItemStack(ItemStack itemStack, int layer, int color) {
        int colNew = getColorFromItemStack(itemStack, layer, color);
        if (colNew != color) {
            colNew = ColorUtils.combineAlphaRbg(color, colNew);
        }

        return colNew;
    }

    private static int getColorFromItemStack(ItemStack itemStack, int layer, int color) {
        if (itemStack == null) {
            return color;
        } else {
            Item item = itemStack.getItem();
            if (item == null) {
                return color;
            } else if (item instanceof SpawnEggItem) {
                return getSpawnEggColor((SpawnEggItem)item, itemStack, layer, color);
            } else {
                return item == Items.LILY_PAD && lilyPadColor != -1 ? lilyPadColor : color;
            }
        }
    }

    private static int[] readDyeColors(Properties props, String fileName, String prefix, String logName) {
        DyeColor[] dyeValues = DyeColor.values();
        Map<String, DyeColor> mapDyes = new HashMap<>();

        for (int i = 0; i < dyeValues.length; i++) {
            DyeColor dye = dyeValues[i];
            mapDyes.put(dye.getSerializedName(), dye);
        }

        mapDyes.put("lightBlue", DyeColor.LIGHT_BLUE);
        mapDyes.put("silver", DyeColor.LIGHT_GRAY);
        int[] colors = new int[dyeValues.length];
        int countColors = 0;

        for (String key : (java.lang.Iterable<String>)(java.lang.Iterable<?>)(props.keySet())) {
            String value = props.getProperty(key);
            if (key.startsWith(prefix)) {
                String name = StrUtils.removePrefix(key, prefix);
                DyeColor dye = mapDyes.get(name);
                int color = parseColor(value);
                if (dye != null && color >= 0) {
                    int argb = ARGB.opaque(color);
                    colors[dye.ordinal()] = argb;
                    countColors++;
                } else {
                    warn("Invalid color: " + key + " = " + value);
                }
            }
        }

        if (countColors <= 0) {
            return null;
        }

        dbg(logName + " colors: " + countColors);
        return colors;
    }

    private static int getDyeColors(DyeColor dye, int[] dyeColors, int color) {
        if (dyeColors == null) {
            return color;
        }

        if (dye == null) {
            return color;
        }

        int customColor = dyeColors[dye.ordinal()];
        return customColor == 0 ? color : customColor;
    }

    public static int getWolfCollarColors(DyeColor dye, int color) {
        return getDyeColors(dye, wolfCollarColors, color);
    }

    public static int getSheepColors(DyeColor dye, int color) {
        return getDyeColors(dye, sheepColors, color);
    }

    private static int[] readTextColors(Properties props, String fileName, String prefix, String logName) {
        int[] colors = new int[32];
        Arrays.fill(colors, -1);
        int countColors = 0;

        for (String key : (java.lang.Iterable<String>)(java.lang.Iterable<?>)(props.keySet())) {
            String value = props.getProperty(key);
            if (key.startsWith(prefix)) {
                String name = StrUtils.removePrefix(key, prefix);
                int code = Config.parseInt(name, -1);
                int color = parseColorAlpha(value);
                if (code >= 0 && code < colors.length && color != -1) {
                    colors[code] = color;
                    countColors++;
                } else {
                    warn("Invalid color: " + key + " = " + value);
                }
            }
        }

        if (countColors <= 0) {
            return null;
        }

        dbg(logName + " colors: " + countColors);
        return colors;
    }

    public static int getTextColor(int index, int color) {
        if (textColors == null) {
            return color;
        } else if (index >= 0 && index < textColors.length) {
            int customColor = textColors[index];
            return customColor == -1 ? color : customColor;
        } else {
            return color;
        }
    }

    private static int[] readMapColors(Properties props, String fileName, String prefix, String logName) {
        int[] colors = new int[MapColor.MATERIAL_COLORS.length];
        Arrays.fill(colors, -1);
        int countColors = 0;

        for (String key : (java.lang.Iterable<String>)(java.lang.Iterable<?>)(props.keySet())) {
            String value = props.getProperty(key);
            if (key.startsWith(prefix)) {
                String name = StrUtils.removePrefix(key, prefix);
                int index = getMapColorIndex(name);
                int color = parseColor(value);
                if (index >= 0 && index < colors.length && color >= 0) {
                    colors[index] = color;
                    countColors++;
                } else {
                    warn("Invalid color: " + key + " = " + value);
                }
            }
        }

        if (countColors <= 0) {
            return null;
        }

        dbg(logName + " colors: " + countColors);
        return colors;
    }

    private static int[] readPotionColors(Properties props, String fileName, String prefix, String logName) {
        int[] colors = new int[getMaxPotionId() + 1];
        Arrays.fill(colors, -1);
        int countColors = 0;

        for (String key : (java.lang.Iterable<String>)(java.lang.Iterable<?>)(props.keySet())) {
            String value = props.getProperty(key);
            if (key.startsWith(prefix)) {
                String name = key;
                int index = getPotionId(name);
                int color = parseColor(value);
                if (index >= 0 && index < colors.length && color >= 0) {
                    colors[index] = color;
                    countColors++;
                } else {
                    warn("Invalid color: " + key + " = " + value);
                }
            }
        }

        if (countColors <= 0) {
            return null;
        }

        dbg(logName + " colors: " + countColors);
        return colors;
    }

    private static int getMaxPotionId() {
        int maxId = 0;

        for (Identifier rl : BuiltInRegistries.MOB_EFFECT.keySet()) {
            MobEffect potion = PotionUtils.getPotion(rl);
            int id = PotionUtils.getId(potion);
            if (id > maxId) {
                maxId = id;
            }
        }

        return maxId;
    }

    private static int getPotionId(String name) {
        if (name.equals("potion.water")) {
            return 0;
        }

        name = StrUtils.replacePrefix(name, "potion.", "effect.");
        String nameMc = StrUtils.replacePrefix(name, "effect.", "effect.minecraft.");

        for (Identifier rl : BuiltInRegistries.MOB_EFFECT.keySet()) {
            MobEffect potion = PotionUtils.getPotion(rl);
            String potionName = potion.getDescriptionId();
            if (potionName.equals(name) || potionName.equals(nameMc)) {
                return PotionUtils.getId(potion);
            }
        }

        return -1;
    }

    public static int getPotionColor(MobEffect potion, int color) {
        int potionId = 0;
        if (potion != null) {
            potionId = PotionUtils.getId(potion);
        }

        return getPotionColor(potionId, color);
    }

    public static int getPotionColor(int potionId, int color) {
        if (potionColors == null) {
            return color;
        } else if (potionId >= 0 && potionId < potionColors.length) {
            int potionColor = potionColors[potionId];
            return potionColor < 0 ? color : potionColor;
        } else {
            return color;
        }
    }

    private static int getMapColorIndex(String name) {
        if (name == null) {
            return -1;
        } else if (name.equals("air")) {
            return MapColor.NONE.id;
        } else if (name.equals("grass")) {
            return MapColor.GRASS.id;
        } else if (name.equals("sand")) {
            return MapColor.SAND.id;
        } else if (name.equals("cloth")) {
            return MapColor.WOOL.id;
        } else if (name.equals("tnt")) {
            return MapColor.FIRE.id;
        } else if (name.equals("ice")) {
            return MapColor.ICE.id;
        } else if (name.equals("iron")) {
            return MapColor.METAL.id;
        } else if (name.equals("foliage")) {
            return MapColor.PLANT.id;
        } else if (name.equals("clay")) {
            return MapColor.CLAY.id;
        } else if (name.equals("dirt")) {
            return MapColor.DIRT.id;
        } else if (name.equals("stone")) {
            return MapColor.STONE.id;
        } else if (name.equals("water")) {
            return MapColor.WATER.id;
        } else if (name.equals("wood")) {
            return MapColor.WOOD.id;
        } else if (name.equals("quartz")) {
            return MapColor.QUARTZ.id;
        } else if (name.equals("gold")) {
            return MapColor.GOLD.id;
        } else if (name.equals("diamond")) {
            return MapColor.DIAMOND.id;
        } else if (name.equals("lapis")) {
            return MapColor.LAPIS.id;
        } else if (name.equals("emerald")) {
            return MapColor.EMERALD.id;
        } else if (name.equals("podzol")) {
            return MapColor.PODZOL.id;
        } else if (name.equals("netherrack")) {
            return MapColor.NETHER.id;
        } else if (name.equals("snow") || name.equals("white")) {
            return MapColor.SNOW.id;
        } else if (name.equals("adobe") || name.equals("orange")) {
            return MapColor.COLOR_ORANGE.id;
        } else if (name.equals("magenta")) {
            return MapColor.COLOR_MAGENTA.id;
        } else if (name.equals("light_blue") || name.equals("lightBlue")) {
            return MapColor.COLOR_LIGHT_BLUE.id;
        } else if (name.equals("yellow")) {
            return MapColor.COLOR_YELLOW.id;
        } else if (name.equals("lime")) {
            return MapColor.COLOR_LIGHT_GREEN.id;
        } else if (name.equals("pink")) {
            return MapColor.COLOR_PINK.id;
        } else if (name.equals("gray")) {
            return MapColor.COLOR_GRAY.id;
        } else if (name.equals("silver") || name.equals("light_gray")) {
            return MapColor.COLOR_LIGHT_GRAY.id;
        } else if (name.equals("cyan")) {
            return MapColor.COLOR_CYAN.id;
        } else if (name.equals("purple")) {
            return MapColor.COLOR_PURPLE.id;
        } else if (name.equals("blue")) {
            return MapColor.COLOR_BLUE.id;
        } else if (name.equals("brown")) {
            return MapColor.COLOR_BROWN.id;
        } else if (name.equals("green")) {
            return MapColor.COLOR_GREEN.id;
        } else if (name.equals("red")) {
            return MapColor.COLOR_RED.id;
        } else if (name.equals("black")) {
            return MapColor.COLOR_BLACK.id;
        } else if (name.equals("white_terracotta")) {
            return MapColor.TERRACOTTA_WHITE.id;
        } else if (name.equals("orange_terracotta")) {
            return MapColor.TERRACOTTA_ORANGE.id;
        } else if (name.equals("magenta_terracotta")) {
            return MapColor.TERRACOTTA_MAGENTA.id;
        } else if (name.equals("light_blue_terracotta")) {
            return MapColor.TERRACOTTA_LIGHT_BLUE.id;
        } else if (name.equals("yellow_terracotta")) {
            return MapColor.TERRACOTTA_YELLOW.id;
        } else if (name.equals("lime_terracotta")) {
            return MapColor.TERRACOTTA_LIGHT_GREEN.id;
        } else if (name.equals("pink_terracotta")) {
            return MapColor.TERRACOTTA_PINK.id;
        } else if (name.equals("gray_terracotta")) {
            return MapColor.TERRACOTTA_GRAY.id;
        } else if (name.equals("light_gray_terracotta")) {
            return MapColor.TERRACOTTA_LIGHT_GRAY.id;
        } else if (name.equals("cyan_terracotta")) {
            return MapColor.TERRACOTTA_CYAN.id;
        } else if (name.equals("purple_terracotta")) {
            return MapColor.TERRACOTTA_PURPLE.id;
        } else if (name.equals("blue_terracotta")) {
            return MapColor.TERRACOTTA_BLUE.id;
        } else if (name.equals("brown_terracotta")) {
            return MapColor.TERRACOTTA_BROWN.id;
        } else if (name.equals("green_terracotta")) {
            return MapColor.TERRACOTTA_GREEN.id;
        } else if (name.equals("red_terracotta")) {
            return MapColor.TERRACOTTA_RED.id;
        } else if (name.equals("black_terracotta")) {
            return MapColor.TERRACOTTA_BLACK.id;
        } else if (name.equals("crimson_nylium")) {
            return MapColor.CRIMSON_NYLIUM.id;
        } else if (name.equals("crimson_stem")) {
            return MapColor.CRIMSON_STEM.id;
        } else if (name.equals("crimson_hyphae")) {
            return MapColor.CRIMSON_HYPHAE.id;
        } else if (name.equals("warped_nylium")) {
            return MapColor.WARPED_NYLIUM.id;
        } else if (name.equals("warped_stem")) {
            return MapColor.WARPED_STEM.id;
        } else if (name.equals("warped_hyphae")) {
            return MapColor.WARPED_HYPHAE.id;
        } else if (name.equals("warped_wart_block")) {
            return MapColor.WARPED_WART_BLOCK.id;
        } else if (name.equals("deepslate")) {
            return MapColor.DEEPSLATE.id;
        } else if (name.equals("raw_iron")) {
            return MapColor.RAW_IRON.id;
        } else {
            return name.equals("glow_lichen") ? MapColor.GLOW_LICHEN.id : -1;
        }
    }

    private static int[] getMapColors() {
        MapColor[] mapColors = MapColor.MATERIAL_COLORS;
        int[] colors = new int[mapColors.length];
        Arrays.fill(colors, -1);

        for (int i = 0; i < mapColors.length && i < colors.length; i++) {
            MapColor mapColor = mapColors[i];
            if (mapColor != null) {
                colors[i] = mapColor.col;
            }
        }

        return colors;
    }

    private static void setMapColors(int[] colors) {
        if (colors != null) {
            MapColor[] mapColors = MapColor.MATERIAL_COLORS;

            for (int i = 0; i < mapColors.length && i < colors.length; i++) {
                MapColor mapColor = mapColors[i];
                if (mapColor != null) {
                    int color = colors[i];
                    if (color >= 0 && mapColor.col != color) {
                        mapColor.col = color;
                    }
                }
            }
        }
    }

    private static int[] getDyeColors() {
        DyeColor[] dyeColors = DyeColor.values();
        int[] colors = new int[dyeColors.length];

        for (int i = 0; i < dyeColors.length && i < colors.length; i++) {
            DyeColor dyeColor = dyeColors[i];
            if (dyeColor != null) {
                colors[i] = dyeColor.getTextureDiffuseColor();
            }
        }

        return colors;
    }

    private static void setDyeColors(int[] colors) {
        if (colors != null) {
            DyeColor[] dyeColors = DyeColor.values();

            for (int i = 0; i < dyeColors.length && i < colors.length; i++) {
                DyeColor dyeColor = dyeColors[i];
                if (dyeColor != null) {
                    int color = colors[i];
                    if (color != 0 && dyeColor.getTextureDiffuseColor() != color) {
                        dyeColor.setTextureDiffuseColor(color);
                    }
                }
            }
        }
    }

    private static void dbg(String str) {
        Config.dbg("CustomColors: " + str);
    }

    private static void warn(String str) {
        Config.warn("CustomColors: " + str);
    }

    public static int getExpBarTextColor(int color) {
        return expBarTextColor == -1 ? color : expBarTextColor;
    }

    public static int getBossTextColor(int color) {
        return bossTextColor == -1 ? color : bossTextColor;
    }

    public static int getSignTextColor(int color) {
        if (color != 0) {
            return color;
        } else {
            return signTextColor == -1 ? color : signTextColor;
        }
    }

    public interface IColorizer {
        int getColor(BlockState var1, BlockAndTintGetter var2, BlockPos var3);

        boolean isColorConstant();
    }
}
