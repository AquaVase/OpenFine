package net.optifine;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirtPathBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.MyceliumBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.optifine.model.BlockModelUtils;
import net.optifine.util.PropertiesOrdered;

public class BetterGrass {
    private static boolean betterGrass = true;
    private static boolean betterDirtPath = true;
    private static boolean betterMycelium = true;
    private static boolean betterPodzol = true;
    private static boolean betterCrimsonNylium = true;
    private static boolean betterWarpedNylium = true;
    private static boolean betterGrassSnow = true;
    private static boolean betterMyceliumSnow = true;
    private static boolean betterPodzolSnow = true;
    private static boolean grassMultilayer = false;
    private static TextureAtlasSprite spriteGrass = null;
    private static TextureAtlasSprite spriteGrassSide = null;
    private static TextureAtlasSprite spriteDirtPath = null;
    private static TextureAtlasSprite spriteDirtPathSide = null;
    private static TextureAtlasSprite spriteMycelium = null;
    private static TextureAtlasSprite spritePodzol = null;
    private static TextureAtlasSprite spriteCrimsonNylium = null;
    private static TextureAtlasSprite spriteWarpedNylium = null;
    private static TextureAtlasSprite spriteSnow = null;
    private static boolean spritesLoaded = false;
    private static BlockStateModelPart modelCubeGrass = null;
    private static BlockStateModelPart modelDirtPath = null;
    private static BlockStateModelPart modelCubeDirtPath = null;
    private static BlockStateModelPart modelCubeMycelium = null;
    private static BlockStateModelPart modelCubePodzol = null;
    private static BlockStateModelPart modelCubeCrimsonNylium = null;
    private static BlockStateModelPart modelCubeWarpedNylium = null;
    private static BlockStateModelPart modelCubeSnow = null;
    private static boolean modelsLoaded = false;
    private static final String TEXTURE_GRASS_DEFAULT = "block/grass_block_top";
    private static final String TEXTURE_GRASS_SIDE_DEFAULT = "block/grass_block_side";
    private static final String TEXTURE_DIRT_PATH_DEFAULT = "block/dirt_path_top";
    private static final String TEXTURE_DIRT_PATH_SIDE_DEFAULT = "block/dirt_path_side";
    private static final String TEXTURE_MYCELIUM_DEFAULT = "block/mycelium_top";
    private static final String TEXTURE_PODZOL_DEFAULT = "block/podzol_top";
    private static final String TEXTURE_CRIMSON_NYLIUM = "block/crimson_nylium";
    private static final String TEXTURE_WARPED_NYLIUM = "block/warped_nylium";
    private static final String TEXTURE_SNOW_DEFAULT = "block/snow";

    public static void updateIcons(TextureAtlas textureMap) {
        spritesLoaded = false;
        modelsLoaded = false;
        loadProperties(textureMap);
    }

    public static void update() {
        if (spritesLoaded) {
            modelCubeGrass = BlockModelUtils.makeModelCube(spriteGrass, 0);
            if (grassMultilayer) {
                BlockStateModelPart modelCubeGrassSide = BlockModelUtils.makeModelCube(spriteGrassSide, -1);
                modelCubeGrass = BlockModelUtils.joinModelsCube(modelCubeGrassSide, modelCubeGrass);
            }

            modelDirtPath = BlockModelUtils.makeModel(Blocks.DIRT_PATH.defaultBlockState(), spriteDirtPathSide, spriteDirtPath);
            modelCubeDirtPath = BlockModelUtils.makeModelCube(spriteDirtPath, -1);
            modelCubeMycelium = BlockModelUtils.makeModelCube(spriteMycelium, -1);
            modelCubePodzol = BlockModelUtils.makeModelCube(spritePodzol, 0);
            modelCubeCrimsonNylium = BlockModelUtils.makeModelCube(spriteCrimsonNylium, -1);
            modelCubeWarpedNylium = BlockModelUtils.makeModelCube(spriteWarpedNylium, -1);
            modelCubeSnow = BlockModelUtils.makeModelCube(spriteSnow, -1);
            modelsLoaded = true;
        }
    }

    private static void loadProperties(TextureAtlas textureMap) {
        betterGrass = true;
        betterDirtPath = true;
        betterMycelium = true;
        betterPodzol = true;
        betterCrimsonNylium = true;
        betterWarpedNylium = true;
        betterGrassSnow = true;
        betterMyceliumSnow = true;
        betterPodzolSnow = true;
        spriteGrass = textureMap.registerSprite(new Identifier("block/grass_block_top"));
        spriteGrassSide = textureMap.registerSprite(new Identifier("block/grass_block_side"));
        spriteDirtPath = textureMap.registerSprite(new Identifier("block/dirt_path_top"));
        spriteDirtPathSide = textureMap.registerSprite(new Identifier("block/dirt_path_side"));
        spriteMycelium = textureMap.registerSprite(new Identifier("block/mycelium_top"));
        spritePodzol = textureMap.registerSprite(new Identifier("block/podzol_top"));
        spriteCrimsonNylium = textureMap.registerSprite(new Identifier("block/crimson_nylium"));
        spriteWarpedNylium = textureMap.registerSprite(new Identifier("block/warped_nylium"));
        spriteSnow = textureMap.registerSprite(new Identifier("block/snow"));
        spritesLoaded = true;
        String name = "optifine/bettergrass.properties";

        try {
            Identifier locFile = new Identifier(name);
            if (!Config.hasResource(locFile)) {
                return;
            }

            InputStream in = Config.getResourceStream(locFile);
            if (in == null) {
                return;
            }

            boolean defaultConfig = Config.isFromDefaultResourcePack(locFile);
            if (defaultConfig) {
                Config.dbg("BetterGrass: Parsing default configuration " + name);
            } else {
                Config.dbg("BetterGrass: Parsing configuration " + name);
            }

            Properties props = new PropertiesOrdered();
            props.load(in);
            in.close();
            betterGrass = getBoolean(props, "grass", true);
            betterDirtPath = getBoolean(props, "dirt_path", true);
            betterMycelium = getBoolean(props, "mycelium", true);
            betterPodzol = getBoolean(props, "podzol", true);
            betterCrimsonNylium = getBoolean(props, "crimson_nylium", true);
            betterWarpedNylium = getBoolean(props, "warped_nylium", true);
            betterGrassSnow = getBoolean(props, "grass.snow", true);
            betterMyceliumSnow = getBoolean(props, "mycelium.snow", true);
            betterPodzolSnow = getBoolean(props, "podzol.snow", true);
            grassMultilayer = getBoolean(props, "grass.multilayer", false);
            spriteGrass = registerSprite(props, "texture.grass", "block/grass_block_top", textureMap);
            spriteGrassSide = registerSprite(props, "texture.grass_side", "block/grass_block_side", textureMap);
            spriteDirtPath = registerSprite(props, "texture.dirt_path", "block/dirt_path_top", textureMap);
            spriteDirtPathSide = registerSprite(props, "texture.dirt_path_side", "block/dirt_path_side", textureMap);
            spriteMycelium = registerSprite(props, "texture.mycelium", "block/mycelium_top", textureMap);
            spritePodzol = registerSprite(props, "texture.podzol", "block/podzol_top", textureMap);
            spriteCrimsonNylium = registerSprite(props, "texture.crimson_nylium", "block/crimson_nylium", textureMap);
            spriteWarpedNylium = registerSprite(props, "texture.warped_nylium", "block/warped_nylium", textureMap);
            spriteSnow = registerSprite(props, "texture.snow", "block/snow", textureMap);
        } catch (IOException e) {
            Config.warn("Error reading: " + name + ", " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public static void refreshIcons(TextureAtlas textureMap) {
        spriteGrass = getSprite(textureMap, spriteGrass.getName());
        spriteGrassSide = getSprite(textureMap, spriteGrassSide.getName());
        spriteDirtPath = getSprite(textureMap, spriteDirtPath.getName());
        spriteDirtPathSide = getSprite(textureMap, spriteDirtPathSide.getName());
        spriteMycelium = getSprite(textureMap, spriteMycelium.getName());
        spritePodzol = getSprite(textureMap, spritePodzol.getName());
        spriteCrimsonNylium = getSprite(textureMap, spriteCrimsonNylium.getName());
        spriteWarpedNylium = getSprite(textureMap, spriteWarpedNylium.getName());
        spriteSnow = getSprite(textureMap, spriteSnow.getName());
    }

    private static TextureAtlasSprite getSprite(TextureAtlas textureMap, Identifier loc) {
        TextureAtlasSprite sprite = textureMap.getSprite(loc);
        if (sprite == null || MissingTextureAtlasSprite.isMisingSprite(sprite)) {
            Config.warn("Missing BetterGrass sprite: " + loc);
        }

        return sprite;
    }

    private static TextureAtlasSprite registerSprite(Properties props, String key, String textureDefault, TextureAtlas textureMap) {
        String texture = props.getProperty(key);
        if (texture == null) {
            texture = textureDefault;
        }

        Identifier locPng = new Identifier("textures/" + texture + ".png");
        if (!Config.hasResource(locPng)) {
            Config.warn("BetterGrass texture not found: " + locPng);
            texture = textureDefault;
        }

        Identifier locSprite = new Identifier(texture);
        return textureMap.registerSprite(locSprite);
    }

    public static List getFaceQuads(BlockGetter blockAccess, BlockState blockState, BlockPos blockPos, Direction facing, List quads) {
        if (facing == Direction.UP || facing == Direction.DOWN) {
            return quads;
        } else if (!modelsLoaded) {
            return quads;
        } else {
            Block block = blockState.getBlock();
            if (block instanceof MyceliumBlock) {
                return getFaceQuadsMycelium(blockAccess, blockState, blockPos, facing, quads);
            } else if (block instanceof DirtPathBlock) {
                return getFaceQuadsDirtPath(blockAccess, blockState, blockPos, facing, quads);
            } else if (block == Blocks.PODZOL) {
                return getFaceQuadsPodzol(blockAccess, blockState, blockPos, facing, quads);
            } else if (block == Blocks.CRIMSON_NYLIUM) {
                return getFaceQuadsCrimsonNylium(blockAccess, blockState, blockPos, facing, quads);
            } else if (block == Blocks.WARPED_NYLIUM) {
                return getFaceQuadsWarpedNylium(blockAccess, blockState, blockPos, facing, quads);
            } else if (block == Blocks.DIRT) {
                return getFaceQuadsDirt(blockAccess, blockState, blockPos, facing, quads);
            } else {
                return block instanceof GrassBlock ? getFaceQuadsGrass(blockAccess, blockState, blockPos, facing, quads) : quads;
            }
        }
    }

    private static List getFaceQuadsMycelium(BlockGetter blockAccess, BlockState blockState, BlockPos blockPos, Direction facing, List quads) {
        Block blockUp = blockAccess.getBlockState(blockPos.above()).getBlock();
        boolean snowy = blockUp == Blocks.SNOW_BLOCK || blockUp == Blocks.SNOW;
        if (Config.isBetterGrassFancy()) {
            if (snowy) {
                if (betterMyceliumSnow && getBlockAt(blockPos, facing, blockAccess) == Blocks.SNOW) {
                    return modelCubeSnow.getQuads(facing);
                }
            } else if (betterMycelium && getBlockAt(blockPos.below(), facing, blockAccess) == Blocks.MYCELIUM) {
                return modelCubeMycelium.getQuads(facing);
            }
        } else if (snowy) {
            if (betterMyceliumSnow) {
                return modelCubeSnow.getQuads(facing);
            }
        } else if (betterMycelium) {
            return modelCubeMycelium.getQuads(facing);
        }

        return quads;
    }

    private static List getFaceQuadsDirtPath(BlockGetter blockAccess, BlockState blockState, BlockPos blockPos, Direction facing, List quads) {
        if (!betterDirtPath) {
            return quads;
        } else if (Config.isBetterGrassFancy()) {
            return getBlockAt(blockPos.below(), facing, blockAccess) == Blocks.DIRT_PATH ? modelDirtPath.getQuads(facing) : quads;
        } else {
            return modelDirtPath.getQuads(facing);
        }
    }

    private static List getFaceQuadsPodzol(BlockGetter blockAccess, BlockState blockState, BlockPos blockPos, Direction facing, List quads) {
        Block blockTop = getBlockAt(blockPos, Direction.UP, blockAccess);
        boolean snowy = blockTop == Blocks.SNOW_BLOCK || blockTop == Blocks.SNOW;
        if (Config.isBetterGrassFancy()) {
            if (snowy) {
                if (betterPodzolSnow && getBlockAt(blockPos, facing, blockAccess) == Blocks.SNOW) {
                    return modelCubeSnow.getQuads(facing);
                }
            } else if (betterPodzol) {
                BlockPos posSide = blockPos.below().relative(facing);
                BlockState stateSide = blockAccess.getBlockState(posSide);
                if (stateSide.getBlock() == Blocks.PODZOL) {
                    return modelCubePodzol.getQuads(facing);
                }
            }
        } else if (snowy) {
            if (betterPodzolSnow) {
                return modelCubeSnow.getQuads(facing);
            }
        } else if (betterPodzol) {
            return modelCubePodzol.getQuads(facing);
        }

        return quads;
    }

    private static List getFaceQuadsCrimsonNylium(BlockGetter blockAccess, BlockState blockState, BlockPos blockPos, Direction facing, List quads) {
        if (!betterCrimsonNylium) {
            return quads;
        } else if (Config.isBetterGrassFancy()) {
            return getBlockAt(blockPos.below(), facing, blockAccess) == Blocks.CRIMSON_NYLIUM ? modelCubeCrimsonNylium.getQuads(facing) : quads;
        } else {
            return modelCubeCrimsonNylium.getQuads(facing);
        }
    }

    private static List getFaceQuadsWarpedNylium(BlockGetter blockAccess, BlockState blockState, BlockPos blockPos, Direction facing, List quads) {
        if (!betterWarpedNylium) {
            return quads;
        } else if (Config.isBetterGrassFancy()) {
            return getBlockAt(blockPos.below(), facing, blockAccess) == Blocks.WARPED_NYLIUM ? modelCubeWarpedNylium.getQuads(facing) : quads;
        } else {
            return modelCubeWarpedNylium.getQuads(facing);
        }
    }

    private static List getFaceQuadsDirt(BlockGetter blockAccess, BlockState blockState, BlockPos blockPos, Direction facing, List quads) {
        Block blockTop = getBlockAt(blockPos, Direction.UP, blockAccess);
        return blockTop == Blocks.DIRT_PATH && betterDirtPath && getBlockAt(blockPos, facing, blockAccess) == Blocks.DIRT_PATH
            ? modelCubeDirtPath.getQuads(facing)
            : quads;
    }

    private static List getFaceQuadsGrass(BlockGetter blockAccess, BlockState blockState, BlockPos blockPos, Direction facing, List quads) {
        Block blockUp = blockAccess.getBlockState(blockPos.above()).getBlock();
        boolean snowy = blockUp == Blocks.SNOW_BLOCK || blockUp == Blocks.SNOW;
        if (Config.isBetterGrassFancy()) {
            if (snowy) {
                if (betterGrassSnow && getBlockAt(blockPos, facing, blockAccess) == Blocks.SNOW) {
                    return modelCubeSnow.getQuads(facing);
                }
            } else if (betterGrass && getBlockAt(blockPos.below(), facing, blockAccess) == Blocks.GRASS_BLOCK) {
                return modelCubeGrass.getQuads(facing);
            }
        } else if (snowy) {
            if (betterGrassSnow) {
                return modelCubeSnow.getQuads(facing);
            }
        } else if (betterGrass) {
            return modelCubeGrass.getQuads(facing);
        }

        return quads;
    }

    private static Block getBlockAt(BlockPos blockPos, Direction facing, BlockGetter blockAccess) {
        BlockPos pos = blockPos.relative(facing);
        return blockAccess.getBlockState(pos).getBlock();
    }

    private static boolean getBoolean(Properties props, String key, boolean def) {
        String str = props.getProperty(key);
        return str == null ? def : Boolean.parseBoolean(str);
    }
}
