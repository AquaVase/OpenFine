package net.optifine;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.optifine.config.Matches;
import net.optifine.model.BakedQuadRetextured;
import net.optifine.model.BlockModelUtils;
import net.optifine.model.ListQuadsOverlay;
import net.optifine.render.RenderEnv;
import net.optifine.util.BiomeUtils;
import net.optifine.util.BlockUtils;
import net.optifine.util.PropertiesOrdered;
import net.optifine.util.ResUtils;
import net.optifine.util.StrUtils;
import net.optifine.util.TextureUtils;
import net.optifine.util.TileEntityUtils;

public class ConnectedTextures {
    private static Map[] spriteQuadMaps = null;
    private static Map[] spriteQuadFullMaps = null;
    private static Map[][] spriteQuadCompactMaps = null;
    private static ConnectedProperties[][] blockProperties = null;
    private static ConnectedProperties[][] tileProperties = null;
    private static boolean multipass = false;
    protected static final int UNKNOWN = -1;
    protected static final int Y_NEG_DOWN = 0;
    protected static final int Y_POS_UP = 1;
    protected static final int Z_NEG_NORTH = 2;
    protected static final int Z_POS_SOUTH = 3;
    protected static final int X_NEG_WEST = 4;
    protected static final int X_POS_EAST = 5;
    private static final int Y_AXIS = 0;
    private static final int Z_AXIS = 1;
    private static final int X_AXIS = 2;
    public static final BlockState AIR_DEFAULT_STATE = Blocks.AIR.defaultBlockState();
    private static TextureAtlasSprite emptySprite = null;
    public static Identifier LOCATION_SPRITE_EMPTY = TextureUtils.LOCATION_SPRITE_EMPTY;
    private static final BlockDir[] SIDES_Y_NEG_DOWN = new BlockDir[]{BlockDir.WEST, BlockDir.EAST, BlockDir.NORTH, BlockDir.SOUTH};
    private static final BlockDir[] SIDES_Y_POS_UP = new BlockDir[]{BlockDir.WEST, BlockDir.EAST, BlockDir.SOUTH, BlockDir.NORTH};
    private static final BlockDir[] SIDES_Z_NEG_NORTH = new BlockDir[]{BlockDir.EAST, BlockDir.WEST, BlockDir.DOWN, BlockDir.UP};
    private static final BlockDir[] SIDES_Z_POS_SOUTH = new BlockDir[]{BlockDir.WEST, BlockDir.EAST, BlockDir.DOWN, BlockDir.UP};
    private static final BlockDir[] SIDES_X_NEG_WEST = new BlockDir[]{BlockDir.NORTH, BlockDir.SOUTH, BlockDir.DOWN, BlockDir.UP};
    private static final BlockDir[] SIDES_X_POS_EAST = new BlockDir[]{BlockDir.SOUTH, BlockDir.NORTH, BlockDir.DOWN, BlockDir.UP};
    private static final BlockDir[] EDGES_Y_NEG_DOWN = new BlockDir[]{BlockDir.NORTH_EAST, BlockDir.NORTH_WEST, BlockDir.SOUTH_EAST, BlockDir.SOUTH_WEST};
    private static final BlockDir[] EDGES_Y_POS_UP = new BlockDir[]{BlockDir.SOUTH_EAST, BlockDir.SOUTH_WEST, BlockDir.NORTH_EAST, BlockDir.NORTH_WEST};
    private static final BlockDir[] EDGES_Z_NEG_NORTH = new BlockDir[]{BlockDir.DOWN_WEST, BlockDir.DOWN_EAST, BlockDir.UP_WEST, BlockDir.UP_EAST};
    private static final BlockDir[] EDGES_Z_POS_SOUTH = new BlockDir[]{BlockDir.DOWN_EAST, BlockDir.DOWN_WEST, BlockDir.UP_EAST, BlockDir.UP_WEST};
    private static final BlockDir[] EDGES_X_NEG_WEST = new BlockDir[]{BlockDir.DOWN_SOUTH, BlockDir.DOWN_NORTH, BlockDir.UP_SOUTH, BlockDir.UP_NORTH};
    private static final BlockDir[] EDGES_X_POS_EAST = new BlockDir[]{BlockDir.DOWN_NORTH, BlockDir.DOWN_SOUTH, BlockDir.UP_NORTH, BlockDir.UP_SOUTH};
    public static final TextureAtlasSprite SPRITE_DEFAULT = new TextureAtlasSprite(TextureAtlas.LOCATION_BLOCKS, new Identifier("default"));

    public static BakedQuad[] getConnectedTexture(
        BlockAndTintGetter blockAccess, BlockState blockState, BlockPos blockPos, BakedQuad quad, Direction facing, RenderEnv renderEnv
    ) {
        TextureAtlasSprite spriteIn = quad.sprite();
        if (spriteIn == null) {
            return renderEnv.getArrayQuadsCtm(quad);
        } else if (skipConnectedTexture(blockAccess, blockState, blockPos, quad, renderEnv)) {
            quad = getQuad(emptySprite, quad);
            return renderEnv.getArrayQuadsCtm(quad);
        } else {
            return getConnectedTextureMultiPass(blockAccess, blockState, blockPos, facing, quad, renderEnv);
        }
    }

    private static boolean skipConnectedTexture(BlockGetter blockAccess, BlockState blockState, BlockPos blockPos, BakedQuad quad, RenderEnv renderEnv) {
        Block block = blockState.getBlock();
        if (block instanceof IronBarsBlock) {
            Direction face = quad.direction();
            if (face != Direction.UP && face != Direction.DOWN) {
                return false;
            }

            if (!quad.isFaceQuad()) {
                return false;
            }

            BlockPos posNeighbour = blockPos.relative(quad.direction());
            BlockState stateNeighbour = blockAccess.getBlockState(posNeighbour);
            if (stateNeighbour.getBlock() != block) {
                return false;
            }

            Block blockNeighbour = stateNeighbour.getBlock();
            if (block instanceof StainedGlassPaneBlock && blockNeighbour instanceof StainedGlassPaneBlock) {
                DyeColor color = ((StainedGlassPaneBlock)block).getColor();
                DyeColor colorNeighbour = ((StainedGlassPaneBlock)blockNeighbour).getColor();
                if (color != colorNeighbour) {
                    return false;
                }
            }

            double midX = quad.getMidX();
            if (midX < 0.4) {
                if (stateNeighbour.getValue(CrossCollisionBlock.WEST)) {
                    return true;
                }
            } else if (midX > 0.6) {
                if (stateNeighbour.getValue(CrossCollisionBlock.EAST)) {
                    return true;
                }
            } else {
                double midZ = quad.getMidZ();
                if (midZ < 0.4) {
                    if (stateNeighbour.getValue(CrossCollisionBlock.NORTH)) {
                        return true;
                    }
                } else {
                    if (!(midZ > 0.6)) {
                        return true;
                    }

                    if (stateNeighbour.getValue(CrossCollisionBlock.SOUTH)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    protected static BakedQuad[] getQuads(TextureAtlasSprite sprite, BakedQuad quadIn, RenderEnv renderEnv) {
        if (sprite == null) {
            return null;
        }

        if (sprite == SPRITE_DEFAULT) {
            return renderEnv.getArrayQuadsCtm(quadIn);
        }

        BakedQuad quad = getQuad(sprite, quadIn);
        return renderEnv.getArrayQuadsCtm(quad);
    }

    private static synchronized BakedQuad getQuad(TextureAtlasSprite sprite, BakedQuad quadIn) {
        if (spriteQuadMaps == null) {
            return quadIn;
        }

        int spriteIndex = sprite.getIndexInMap();
        if (spriteIndex >= 0 && spriteIndex < spriteQuadMaps.length) {
            Map quadMap = spriteQuadMaps[spriteIndex];
            if (quadMap == null) {
                quadMap = new IdentityHashMap(1);
                spriteQuadMaps[spriteIndex] = quadMap;
            }

            BakedQuad quad = (BakedQuad)quadMap.get(quadIn);
            if (quad == null) {
                quad = makeSpriteQuad(quadIn, sprite);
                quadMap.put(quadIn, quad);
            }

            return quad;
        } else {
            return quadIn;
        }
    }

    private static synchronized BakedQuad getQuadFull(TextureAtlasSprite sprite, BakedQuad quadIn, int tintIndex) {
        if (spriteQuadFullMaps == null) {
            return null;
        }

        if (sprite == null) {
            return null;
        }

        int spriteIndex = sprite.getIndexInMap();
        if (spriteIndex >= 0 && spriteIndex < spriteQuadFullMaps.length) {
            Map quadMap = spriteQuadFullMaps[spriteIndex];
            if (quadMap == null) {
                quadMap = new EnumMap<>(Direction.class);
                spriteQuadFullMaps[spriteIndex] = quadMap;
            }

            Direction face = quadIn.direction();
            BakedQuad quad = (BakedQuad)quadMap.get(face);
            if (quad == null) {
                quad = BlockModelUtils.makeBakedQuad(face, sprite, tintIndex);
                quadMap.put(face, quad);
            }

            return quad;
        } else {
            return null;
        }
    }

    private static BakedQuad makeSpriteQuad(BakedQuad quad, TextureAtlasSprite sprite) {
        return BakedQuadRetextured.make(quad, sprite);
    }

    private static BakedQuad[] getConnectedTextureMultiPass(
        BlockAndTintGetter blockAccess, BlockState blockState, BlockPos blockPos, Direction side, BakedQuad quad, RenderEnv renderEnv
    ) {
        BakedQuad[] quads = getConnectedTextureSingle(blockAccess, blockState, blockPos, side, quad, true, 0, renderEnv);
        if (!multipass) {
            return quads;
        }

        if (quads.length == 1 && quads[0] == quad) {
            return quads;
        }

        List<BakedQuad> listQuads = renderEnv.getListQuadsCtmMultipass(quads);

        for (int q = 0; q < listQuads.size(); q++) {
            BakedQuad newQuad = listQuads.get(q);
            BakedQuad mpQuad = newQuad;

            for (int i = 0; i < 3; i++) {
                BakedQuad[] newMpQuads = getConnectedTextureSingle(blockAccess, blockState, blockPos, side, mpQuad, false, i + 1, renderEnv);
                if (newMpQuads.length != 1 || newMpQuads[0] == mpQuad) {
                    break;
                }

                mpQuad = newMpQuads[0];
            }

            listQuads.set(q, mpQuad);
        }

        for (int i = 0; i < quads.length; i++) {
            quads[i] = listQuads.get(i);
        }

        return quads;
    }

    public static BakedQuad[] getConnectedTextureSingle(
        BlockAndTintGetter blockAccess,
        BlockState blockState,
        BlockPos blockPos,
        Direction facing,
        BakedQuad quad,
        boolean checkBlocks,
        int pass,
        RenderEnv renderEnv
    ) {
        Block block = blockState.getBlock();
        TextureAtlasSprite icon = quad.sprite();
        if (tileProperties != null) {
            int iconId = icon.getIndexInMap();
            if (iconId >= 0 && iconId < tileProperties.length) {
                ConnectedProperties[] cps = tileProperties[iconId];
                if (cps != null) {
                    int side = getSide(facing);

                    for (int i = 0; i < cps.length; i++) {
                        ConnectedProperties cp = cps[i];
                        if (cp != null && cp.matchesBlockId(blockState.getBlockId())) {
                            BakedQuad[] newQuads = getConnectedTexture(cp, blockAccess, blockState, blockPos, side, quad, pass, renderEnv);
                            if (newQuads != null) {
                                return newQuads;
                            }
                        }
                    }
                }
            }
        }

        if (blockProperties != null && checkBlocks) {
            int blockId = renderEnv.getBlockId();
            if (blockId >= 0 && blockId < blockProperties.length) {
                ConnectedProperties[] cps = blockProperties[blockId];
                if (cps != null) {
                    int side = getSide(facing);

                    for (int i = 0; i < cps.length; i++) {
                        ConnectedProperties cp = cps[i];
                        if (cp != null && cp.matchesIcon(icon)) {
                            BakedQuad[] newQuads = getConnectedTexture(cp, blockAccess, blockState, blockPos, side, quad, pass, renderEnv);
                            if (newQuads != null) {
                                return newQuads;
                            }
                        }
                    }
                }
            }
        }

        return renderEnv.getArrayQuadsCtm(quad);
    }

    public static int getSide(Direction facing) {
        if (facing == null) {
            return -1;
        }

        switch (facing) {
            case DOWN:
                return 0;
            case UP:
                return 1;
            case EAST:
                return 5;
            case WEST:
                return 4;
            case NORTH:
                return 2;
            case SOUTH:
                return 3;
            default:
                return -1;
        }
    }

    private static Direction getFacing(int side) {
        switch (side) {
            case 0:
                return Direction.DOWN;
            case 1:
                return Direction.UP;
            case 2:
                return Direction.NORTH;
            case 3:
                return Direction.SOUTH;
            case 4:
                return Direction.WEST;
            case 5:
                return Direction.EAST;
            default:
                return Direction.UP;
        }
    }

    private static BakedQuad[] getConnectedTexture(
        ConnectedProperties cp, BlockAndTintGetter blockAccess, BlockState blockState, BlockPos blockPos, int side, BakedQuad quad, int pass, RenderEnv renderEnv
    ) {
        int vertAxis = 0;
        int metadata = blockState.getMetadata();
        Block block = blockState.getBlock();
        if (block instanceof RotatedPillarBlock) {
            vertAxis = getPillarAxis(blockState);
        }

        if (!cp.matchesBlock(blockState.getBlockId(), metadata)) {
            return null;
        }

        if (side >= 0 && cp.faces != 63) {
            int sideCheck = side;
            if (vertAxis != 0) {
                sideCheck = fixSideByAxis(side, vertAxis);
            }

            if ((1 << sideCheck & cp.faces) == 0) {
                return null;
            }
        }

        int y = blockPos.getY();
        if (cp.heights != null && !cp.heights.isInRange(y)) {
            return null;
        }

        if (cp.biomes != null) {
            Biome blockBiome = BiomeUtils.getBiome(blockAccess, blockPos);
            if (!cp.matchesBiome(blockBiome)) {
                return null;
            }
        }

        if (cp.nbtName != null) {
            String name = TileEntityUtils.getTileEntityName(blockAccess, blockPos);
            if (!cp.nbtName.matchesValue(name)) {
                return null;
            }
        }

        TextureAtlasSprite icon = quad.sprite();
        switch (cp.method) {
            case 1:
                return getQuads(getConnectedTextureCtm(cp, blockAccess, blockState, blockPos, vertAxis, side, icon, metadata, renderEnv), quad, renderEnv);
            case 2:
                return getQuads(getConnectedTextureHorizontal(cp, blockAccess, blockState, blockPos, vertAxis, side, icon, metadata), quad, renderEnv);
            case 3:
                return getQuads(getConnectedTextureTop(cp, blockAccess, blockState, blockPos, vertAxis, side, icon, metadata), quad, renderEnv);
            case 4:
                return getQuads(getConnectedTextureRandom(cp, blockAccess, blockState, blockPos, side), quad, renderEnv);
            case 5:
                return getQuads(getConnectedTextureRepeat(cp, blockPos, side), quad, renderEnv);
            case 6:
                return getQuads(getConnectedTextureVertical(cp, blockAccess, blockState, blockPos, vertAxis, side, icon, metadata), quad, renderEnv);
            case 7:
                return getQuads(getConnectedTextureFixed(cp), quad, renderEnv);
            case 8:
                return getQuads(getConnectedTextureHorizontalVertical(cp, blockAccess, blockState, blockPos, vertAxis, side, icon, metadata), quad, renderEnv);
            case 9:
                return getQuads(getConnectedTextureVerticalHorizontal(cp, blockAccess, blockState, blockPos, vertAxis, side, icon, metadata), quad, renderEnv);
            case 10:
                if (pass == 0) {
                    return getConnectedTextureCtmCompact(cp, blockAccess, blockState, blockPos, vertAxis, side, quad, metadata, renderEnv);
                }
            default:
                return null;
            case 11:
                return getConnectedTextureOverlay(cp, blockAccess, blockState, blockPos, vertAxis, side, quad, metadata, renderEnv);
            case 12:
                return getConnectedTextureOverlayFixed(cp, quad, renderEnv);
            case 13:
                return getConnectedTextureOverlayRandom(cp, blockAccess, blockState, blockPos, side, quad, renderEnv);
            case 14:
                return getConnectedTextureOverlayRepeat(cp, blockPos, side, quad, renderEnv);
            case 15:
                return getConnectedTextureOverlayCtm(cp, blockAccess, blockState, blockPos, vertAxis, side, quad, metadata, renderEnv);
        }
    }

    private static int fixSideByAxis(int side, int vertAxis) {
        switch (vertAxis) {
            case 0:
                return side;
            case 1:
                switch (side) {
                    case 0:
                        return 2;
                    case 1:
                        return 3;
                    case 2:
                        return 1;
                    case 3:
                        return 0;
                    default:
                        return side;
                }
            case 2:
                switch (side) {
                    case 0:
                        return 4;
                    case 1:
                        return 5;
                    case 2:
                    case 3:
                    default:
                        return side;
                    case 4:
                        return 1;
                    case 5:
                        return 0;
                }
            default:
                return side;
        }
    }

    private static int getPillarAxis(BlockState blockState) {
        Direction.Axis axis = blockState.getValue(RotatedPillarBlock.AXIS);
        switch (axis) {
            case X:
                return 2;
            case Z:
                return 1;
            default:
                return 0;
        }
    }

    private static TextureAtlasSprite getConnectedTextureRandom(
        ConnectedProperties cp, BlockGetter blockAccess, BlockState blockState, BlockPos blockPos, int side
    ) {
        if (cp.tileIcons.length == 1) {
            return cp.tileIcons[0];
        }

        int face = side / cp.symmetry * cp.symmetry;
        if (cp.linked) {
            BlockPos posDown = blockPos.below();

            for (BlockState bsDown = blockAccess.getBlockState(posDown); bsDown.getBlock() == blockState.getBlock(); bsDown = blockAccess.getBlockState(posDown)) {
                blockPos = posDown;
                posDown = blockPos.below();
                if (posDown.getY() < 0) {
                    break;
                }
            }
        }

        int rand = Config.getRandom(blockPos, face) & 2147483647;

        for (int i = 0; i < cp.randomLoops; i++) {
            rand = Config.intHash(rand);
        }

        int index = 0;
        if (cp.weights == null) {
            index = rand % cp.tileIcons.length;
        } else {
            int randWeight = rand % cp.sumAllWeights;
            int[] sumWeights = cp.sumWeights;

            for (int i = 0; i < sumWeights.length; i++) {
                if (randWeight < sumWeights[i]) {
                    index = i;
                    break;
                }
            }
        }

        return cp.tileIcons[index];
    }

    private static TextureAtlasSprite getConnectedTextureFixed(ConnectedProperties cp) {
        return cp.tileIcons[0];
    }

    private static TextureAtlasSprite getConnectedTextureRepeat(ConnectedProperties cp, BlockPos blockPos, int side) {
        if (cp.tileIcons.length == 1) {
            return cp.tileIcons[0];
        }

        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();
        int nx = 0;
        int ny = 0;
        switch (side) {
            case 0:
                nx = x;
                ny = -z - 1;
                break;
            case 1:
                nx = x;
                ny = z;
                break;
            case 2:
                nx = -x - 1;
                ny = -y;
                break;
            case 3:
                nx = x;
                ny = -y;
                break;
            case 4:
                nx = z;
                ny = -y;
                break;
            case 5:
                nx = -z - 1;
                ny = -y;
        }

        nx %= cp.width;
        ny %= cp.height;
        if (nx < 0) {
            nx += cp.width;
        }

        if (ny < 0) {
            ny += cp.height;
        }

        int index = ny * cp.width + nx;
        return cp.tileIcons[index];
    }

    private static TextureAtlasSprite getConnectedTextureCtm(
        ConnectedProperties cp,
        BlockGetter blockAccess,
        BlockState blockState,
        BlockPos blockPos,
        int vertAxis,
        int side,
        TextureAtlasSprite icon,
        int metadata,
        RenderEnv renderEnv
    ) {
        int index = getConnectedTextureCtmIndex(cp, blockAccess, blockState, blockPos, vertAxis, side, icon, metadata, renderEnv);
        return cp.tileIcons[index];
    }

    private static synchronized BakedQuad[] getConnectedTextureCtmCompact(
        ConnectedProperties cp,
        BlockGetter blockAccess,
        BlockState blockState,
        BlockPos blockPos,
        int vertAxis,
        int side,
        BakedQuad quad,
        int metadata,
        RenderEnv renderEnv
    ) {
        TextureAtlasSprite icon = quad.sprite();
        int index = getConnectedTextureCtmIndex(cp, blockAccess, blockState, blockPos, vertAxis, side, icon, metadata, renderEnv);
        return ConnectedTexturesCompact.getConnectedTextureCtmCompact(index, cp, side, quad, renderEnv);
    }

    private static BakedQuad[] getConnectedTextureOverlay(
        ConnectedProperties cp,
        BlockGetter blockAccess,
        BlockState blockState,
        BlockPos blockPos,
        int vertAxis,
        int side,
        BakedQuad quad,
        int metadata,
        RenderEnv renderEnv
    ) {
        if (!quad.isFullQuad()) {
            return null;
        }

        TextureAtlasSprite icon = quad.sprite();
        BlockDir[] dirSides = getSideDirections(side, vertAxis);
        boolean[] sides = renderEnv.getBorderFlags();

        for (int i = 0; i < 4; i++) {
            sides[i] = isNeighbourOverlay(cp, blockAccess, blockState, dirSides[i].offset(blockPos), side, icon, metadata);
        }

        ListQuadsOverlay listQuadsOverlay = renderEnv.getListQuadsOverlay(cp.layer);

        try {
            if (sides[0] && sides[1] && sides[2] && sides[3]) {
                listQuadsOverlay.addQuad(getQuadFull(cp.tileIcons[8], quad, cp.tintIndex), cp.tintBlockState);
                return null;
            }

            if (sides[0] && sides[1] && sides[2]) {
                listQuadsOverlay.addQuad(getQuadFull(cp.tileIcons[5], quad, cp.tintIndex), cp.tintBlockState);
                return null;
            }

            if (sides[0] && sides[2] && sides[3]) {
                listQuadsOverlay.addQuad(getQuadFull(cp.tileIcons[6], quad, cp.tintIndex), cp.tintBlockState);
                return null;
            }

            if (sides[1] && sides[2] && sides[3]) {
                listQuadsOverlay.addQuad(getQuadFull(cp.tileIcons[12], quad, cp.tintIndex), cp.tintBlockState);
                return null;
            }

            if (sides[0] && sides[1] && sides[3]) {
                listQuadsOverlay.addQuad(getQuadFull(cp.tileIcons[13], quad, cp.tintIndex), cp.tintBlockState);
                return null;
            }

            BlockDir[] dirEdges = getEdgeDirections(side, vertAxis);
            boolean[] edges = renderEnv.getBorderFlags2();

            for (int i = 0; i < 4; i++) {
                edges[i] = isNeighbourOverlay(cp, blockAccess, blockState, dirEdges[i].offset(blockPos), side, icon, metadata);
            }

            if (sides[1] && sides[2]) {
                listQuadsOverlay.addQuad(getQuadFull(cp.tileIcons[3], quad, cp.tintIndex), cp.tintBlockState);
                if (edges[3]) {
                    listQuadsOverlay.addQuad(getQuadFull(cp.tileIcons[16], quad, cp.tintIndex), cp.tintBlockState);
                }

                return null;
            } else if (sides[0] && sides[2]) {
                listQuadsOverlay.addQuad(getQuadFull(cp.tileIcons[4], quad, cp.tintIndex), cp.tintBlockState);
                if (edges[2]) {
                    listQuadsOverlay.addQuad(getQuadFull(cp.tileIcons[14], quad, cp.tintIndex), cp.tintBlockState);
                }

                return null;
            } else if (sides[1] && sides[3]) {
                listQuadsOverlay.addQuad(getQuadFull(cp.tileIcons[10], quad, cp.tintIndex), cp.tintBlockState);
                if (edges[1]) {
                    listQuadsOverlay.addQuad(getQuadFull(cp.tileIcons[2], quad, cp.tintIndex), cp.tintBlockState);
                }

                return null;
            } else if (sides[0] && sides[3]) {
                listQuadsOverlay.addQuad(getQuadFull(cp.tileIcons[11], quad, cp.tintIndex), cp.tintBlockState);
                if (edges[0]) {
                    listQuadsOverlay.addQuad(getQuadFull(cp.tileIcons[0], quad, cp.tintIndex), cp.tintBlockState);
                }

                return null;
            } else {
                boolean[] sidesMatch = renderEnv.getBorderFlags3();

                for (int i = 0; i < 4; i++) {
                    sidesMatch[i] = isNeighbourMatching(cp, blockAccess, blockState, dirSides[i].offset(blockPos), side, icon, metadata);
                }

                if (sides[0]) {
                    listQuadsOverlay.addQuad(getQuadFull(cp.tileIcons[9], quad, cp.tintIndex), cp.tintBlockState);
                }

                if (sides[1]) {
                    listQuadsOverlay.addQuad(getQuadFull(cp.tileIcons[7], quad, cp.tintIndex), cp.tintBlockState);
                }

                if (sides[2]) {
                    listQuadsOverlay.addQuad(getQuadFull(cp.tileIcons[1], quad, cp.tintIndex), cp.tintBlockState);
                }

                if (sides[3]) {
                    listQuadsOverlay.addQuad(getQuadFull(cp.tileIcons[15], quad, cp.tintIndex), cp.tintBlockState);
                }

                if (edges[0] && (sidesMatch[1] || sidesMatch[2]) && !sides[1] && !sides[2]) {
                    listQuadsOverlay.addQuad(getQuadFull(cp.tileIcons[0], quad, cp.tintIndex), cp.tintBlockState);
                }

                if (edges[1] && (sidesMatch[0] || sidesMatch[2]) && !sides[0] && !sides[2]) {
                    listQuadsOverlay.addQuad(getQuadFull(cp.tileIcons[2], quad, cp.tintIndex), cp.tintBlockState);
                }

                if (edges[2] && (sidesMatch[1] || sidesMatch[3]) && !sides[1] && !sides[3]) {
                    listQuadsOverlay.addQuad(getQuadFull(cp.tileIcons[14], quad, cp.tintIndex), cp.tintBlockState);
                }

                if (edges[3] && (sidesMatch[0] || sidesMatch[3]) && !sides[0] && !sides[3]) {
                    listQuadsOverlay.addQuad(getQuadFull(cp.tileIcons[16], quad, cp.tintIndex), cp.tintBlockState);
                }

                return null;
            }
        } finally {
            if (listQuadsOverlay.size() > 0) {
                renderEnv.setOverlaysRendered(true);
            }
        }
    }

    private static BakedQuad[] getConnectedTextureOverlayFixed(ConnectedProperties cp, BakedQuad quad, RenderEnv renderEnv) {
        if (!quad.isFullQuad()) {
            return null;
        }

        ListQuadsOverlay listQuadsOverlay = renderEnv.getListQuadsOverlay(cp.layer);

        try {
            TextureAtlasSprite sprite = getConnectedTextureFixed(cp);
            if (sprite != null) {
                listQuadsOverlay.addQuad(getQuadFull(sprite, quad, cp.tintIndex), cp.tintBlockState);
            }

            return null;
        } finally {
            if (listQuadsOverlay.size() > 0) {
                renderEnv.setOverlaysRendered(true);
            }
        }
    }

    private static BakedQuad[] getConnectedTextureOverlayRandom(
        ConnectedProperties cp, BlockGetter blockAccess, BlockState blockState, BlockPos blockPos, int side, BakedQuad quad, RenderEnv renderEnv
    ) {
        if (!quad.isFullQuad()) {
            return null;
        }

        ListQuadsOverlay listQuadsOverlay = renderEnv.getListQuadsOverlay(cp.layer);

        try {
            TextureAtlasSprite sprite = getConnectedTextureRandom(cp, blockAccess, blockState, blockPos, side);
            if (sprite != null) {
                listQuadsOverlay.addQuad(getQuadFull(sprite, quad, cp.tintIndex), cp.tintBlockState);
            }

            return null;
        } finally {
            if (listQuadsOverlay.size() > 0) {
                renderEnv.setOverlaysRendered(true);
            }
        }
    }

    private static BakedQuad[] getConnectedTextureOverlayRepeat(ConnectedProperties cp, BlockPos blockPos, int side, BakedQuad quad, RenderEnv renderEnv) {
        if (!quad.isFullQuad()) {
            return null;
        }

        ListQuadsOverlay listQuadsOverlay = renderEnv.getListQuadsOverlay(cp.layer);

        try {
            TextureAtlasSprite sprite = getConnectedTextureRepeat(cp, blockPos, side);
            if (sprite != null) {
                listQuadsOverlay.addQuad(getQuadFull(sprite, quad, cp.tintIndex), cp.tintBlockState);
            }

            return null;
        } finally {
            if (listQuadsOverlay.size() > 0) {
                renderEnv.setOverlaysRendered(true);
            }
        }
    }

    private static BakedQuad[] getConnectedTextureOverlayCtm(
        ConnectedProperties cp,
        BlockGetter blockAccess,
        BlockState blockState,
        BlockPos blockPos,
        int vertAxis,
        int side,
        BakedQuad quad,
        int metadata,
        RenderEnv renderEnv
    ) {
        if (!quad.isFullQuad()) {
            return null;
        }

        ListQuadsOverlay listQuadsOverlay = renderEnv.getListQuadsOverlay(cp.layer);

        try {
            TextureAtlasSprite sprite = getConnectedTextureCtm(cp, blockAccess, blockState, blockPos, vertAxis, side, quad.sprite(), metadata, renderEnv);
            if (sprite != null) {
                listQuadsOverlay.addQuad(getQuadFull(sprite, quad, cp.tintIndex), cp.tintBlockState);
            }

            return null;
        } finally {
            if (listQuadsOverlay.size() > 0) {
                renderEnv.setOverlaysRendered(true);
            }
        }
    }

    private static BlockDir[] getSideDirections(int side, int vertAxis) {
        switch (side) {
            case 0:
                return SIDES_Y_NEG_DOWN;
            case 1:
                return SIDES_Y_POS_UP;
            case 2:
                return SIDES_Z_NEG_NORTH;
            case 3:
                return SIDES_Z_POS_SOUTH;
            case 4:
                return SIDES_X_NEG_WEST;
            case 5:
                return SIDES_X_POS_EAST;
            default:
                throw new IllegalArgumentException("Unknown side: " + side);
        }
    }

    private static BlockDir[] getEdgeDirections(int side, int vertAxis) {
        switch (side) {
            case 0:
                return EDGES_Y_NEG_DOWN;
            case 1:
                return EDGES_Y_POS_UP;
            case 2:
                return EDGES_Z_NEG_NORTH;
            case 3:
                return EDGES_Z_POS_SOUTH;
            case 4:
                return EDGES_X_NEG_WEST;
            case 5:
                return EDGES_X_POS_EAST;
            default:
                throw new IllegalArgumentException("Unknown side: " + side);
        }
    }

    protected static Map[][] getSpriteQuadCompactMaps() {
        return spriteQuadCompactMaps;
    }

    private static int getConnectedTextureCtmIndex(
        ConnectedProperties cp,
        BlockGetter blockAccess,
        BlockState blockState,
        BlockPos blockPos,
        int vertAxis,
        int side,
        TextureAtlasSprite icon,
        int metadata,
        RenderEnv renderEnv
    ) {
        boolean[] borders = renderEnv.getBorderFlags();
        switch (side) {
            case 0:
                borders[0] = isNeighbour(cp, blockAccess, blockState, blockPos.west(), side, icon, metadata);
                borders[1] = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
                borders[2] = isNeighbour(cp, blockAccess, blockState, blockPos.north(), side, icon, metadata);
                borders[3] = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                if (cp.innerSeams) {
                    BlockPos posFront = blockPos.below();
                    borders[0] = borders[0] && !isNeighbour(cp, blockAccess, blockState, posFront.west(), side, icon, metadata);
                    borders[1] = borders[1] && !isNeighbour(cp, blockAccess, blockState, posFront.east(), side, icon, metadata);
                    borders[2] = borders[2] && !isNeighbour(cp, blockAccess, blockState, posFront.north(), side, icon, metadata);
                    borders[3] = borders[3] && !isNeighbour(cp, blockAccess, blockState, posFront.south(), side, icon, metadata);
                }
                break;
            case 1:
                borders[0] = isNeighbour(cp, blockAccess, blockState, blockPos.west(), side, icon, metadata);
                borders[1] = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
                borders[2] = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                borders[3] = isNeighbour(cp, blockAccess, blockState, blockPos.north(), side, icon, metadata);
                if (cp.innerSeams) {
                    BlockPos posFront = blockPos.above();
                    borders[0] = borders[0] && !isNeighbour(cp, blockAccess, blockState, posFront.west(), side, icon, metadata);
                    borders[1] = borders[1] && !isNeighbour(cp, blockAccess, blockState, posFront.east(), side, icon, metadata);
                    borders[2] = borders[2] && !isNeighbour(cp, blockAccess, blockState, posFront.south(), side, icon, metadata);
                    borders[3] = borders[3] && !isNeighbour(cp, blockAccess, blockState, posFront.north(), side, icon, metadata);
                }
                break;
            case 2:
                borders[0] = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
                borders[1] = isNeighbour(cp, blockAccess, blockState, blockPos.west(), side, icon, metadata);
                borders[2] = isNeighbour(cp, blockAccess, blockState, blockPos.below(), side, icon, metadata);
                borders[3] = isNeighbour(cp, blockAccess, blockState, blockPos.above(), side, icon, metadata);
                if (cp.innerSeams) {
                    BlockPos posFront = blockPos.north();
                    borders[0] = borders[0] && !isNeighbour(cp, blockAccess, blockState, posFront.east(), side, icon, metadata);
                    borders[1] = borders[1] && !isNeighbour(cp, blockAccess, blockState, posFront.west(), side, icon, metadata);
                    borders[2] = borders[2] && !isNeighbour(cp, blockAccess, blockState, posFront.below(), side, icon, metadata);
                    borders[3] = borders[3] && !isNeighbour(cp, blockAccess, blockState, posFront.above(), side, icon, metadata);
                }
                break;
            case 3:
                borders[0] = isNeighbour(cp, blockAccess, blockState, blockPos.west(), side, icon, metadata);
                borders[1] = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
                borders[2] = isNeighbour(cp, blockAccess, blockState, blockPos.below(), side, icon, metadata);
                borders[3] = isNeighbour(cp, blockAccess, blockState, blockPos.above(), side, icon, metadata);
                if (cp.innerSeams) {
                    BlockPos posFront = blockPos.south();
                    borders[0] = borders[0] && !isNeighbour(cp, blockAccess, blockState, posFront.west(), side, icon, metadata);
                    borders[1] = borders[1] && !isNeighbour(cp, blockAccess, blockState, posFront.east(), side, icon, metadata);
                    borders[2] = borders[2] && !isNeighbour(cp, blockAccess, blockState, posFront.below(), side, icon, metadata);
                    borders[3] = borders[3] && !isNeighbour(cp, blockAccess, blockState, posFront.above(), side, icon, metadata);
                }
                break;
            case 4:
                borders[0] = isNeighbour(cp, blockAccess, blockState, blockPos.north(), side, icon, metadata);
                borders[1] = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                borders[2] = isNeighbour(cp, blockAccess, blockState, blockPos.below(), side, icon, metadata);
                borders[3] = isNeighbour(cp, blockAccess, blockState, blockPos.above(), side, icon, metadata);
                if (cp.innerSeams) {
                    BlockPos posFront = blockPos.west();
                    borders[0] = borders[0] && !isNeighbour(cp, blockAccess, blockState, posFront.north(), side, icon, metadata);
                    borders[1] = borders[1] && !isNeighbour(cp, blockAccess, blockState, posFront.south(), side, icon, metadata);
                    borders[2] = borders[2] && !isNeighbour(cp, blockAccess, blockState, posFront.below(), side, icon, metadata);
                    borders[3] = borders[3] && !isNeighbour(cp, blockAccess, blockState, posFront.above(), side, icon, metadata);
                }
                break;
            case 5:
                borders[0] = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                borders[1] = isNeighbour(cp, blockAccess, blockState, blockPos.north(), side, icon, metadata);
                borders[2] = isNeighbour(cp, blockAccess, blockState, blockPos.below(), side, icon, metadata);
                borders[3] = isNeighbour(cp, blockAccess, blockState, blockPos.above(), side, icon, metadata);
                if (cp.innerSeams) {
                    BlockPos posFront = blockPos.east();
                    borders[0] = borders[0] && !isNeighbour(cp, blockAccess, blockState, posFront.south(), side, icon, metadata);
                    borders[1] = borders[1] && !isNeighbour(cp, blockAccess, blockState, posFront.north(), side, icon, metadata);
                    borders[2] = borders[2] && !isNeighbour(cp, blockAccess, blockState, posFront.below(), side, icon, metadata);
                    borders[3] = borders[3] && !isNeighbour(cp, blockAccess, blockState, posFront.above(), side, icon, metadata);
                }
        }

        int index = 0;
        if (borders[0] & !borders[1] & !borders[2] & !borders[3]) {
            index = 3;
        } else if (!borders[0] & borders[1] & !borders[2] & !borders[3]) {
            index = 1;
        } else if (!borders[0] & !borders[1] & borders[2] & !borders[3]) {
            index = 12;
        } else if (!borders[0] & !borders[1] & !borders[2] & borders[3]) {
            index = 36;
        } else if (borders[0] & borders[1] & !borders[2] & !borders[3]) {
            index = 2;
        } else if (!borders[0] & !borders[1] & borders[2] & borders[3]) {
            index = 24;
        } else if (borders[0] & !borders[1] & borders[2] & !borders[3]) {
            index = 15;
        } else if (borders[0] & !borders[1] & !borders[2] & borders[3]) {
            index = 39;
        } else if (!borders[0] & borders[1] & borders[2] & !borders[3]) {
            index = 13;
        } else if (!borders[0] & borders[1] & !borders[2] & borders[3]) {
            index = 37;
        } else if (!borders[0] & borders[1] & borders[2] & borders[3]) {
            index = 25;
        } else if (borders[0] & !borders[1] & borders[2] & borders[3]) {
            index = 27;
        } else if (borders[0] & borders[1] & !borders[2] & borders[3]) {
            index = 38;
        } else if (borders[0] & borders[1] & borders[2] & !borders[3]) {
            index = 14;
        } else if (borders[0] & borders[1] & borders[2] & borders[3]) {
            index = 26;
        }

        if (index == 0) {
            return index;
        }

        if (!Config.isConnectedTexturesFancy()) {
            return index;
        }

        boolean[] edges = borders;
        switch (side) {
            case 0:
                edges[0] = !isNeighbour(cp, blockAccess, blockState, blockPos.east().north(), side, icon, metadata);
                edges[1] = !isNeighbour(cp, blockAccess, blockState, blockPos.west().north(), side, icon, metadata);
                edges[2] = !isNeighbour(cp, blockAccess, blockState, blockPos.east().south(), side, icon, metadata);
                edges[3] = !isNeighbour(cp, blockAccess, blockState, blockPos.west().south(), side, icon, metadata);
                if (cp.innerSeams) {
                    BlockPos posFront = blockPos.below();
                    edges[0] = edges[0] || isNeighbour(cp, blockAccess, blockState, posFront.east().north(), side, icon, metadata);
                    edges[1] = edges[1] || isNeighbour(cp, blockAccess, blockState, posFront.west().north(), side, icon, metadata);
                    edges[2] = edges[2] || isNeighbour(cp, blockAccess, blockState, posFront.east().south(), side, icon, metadata);
                    edges[3] = edges[3] || isNeighbour(cp, blockAccess, blockState, posFront.west().south(), side, icon, metadata);
                }
                break;
            case 1:
                edges[0] = !isNeighbour(cp, blockAccess, blockState, blockPos.east().south(), side, icon, metadata);
                edges[1] = !isNeighbour(cp, blockAccess, blockState, blockPos.west().south(), side, icon, metadata);
                edges[2] = !isNeighbour(cp, blockAccess, blockState, blockPos.east().north(), side, icon, metadata);
                edges[3] = !isNeighbour(cp, blockAccess, blockState, blockPos.west().north(), side, icon, metadata);
                if (cp.innerSeams) {
                    BlockPos posFront = blockPos.above();
                    edges[0] = edges[0] || isNeighbour(cp, blockAccess, blockState, posFront.east().south(), side, icon, metadata);
                    edges[1] = edges[1] || isNeighbour(cp, blockAccess, blockState, posFront.west().south(), side, icon, metadata);
                    edges[2] = edges[2] || isNeighbour(cp, blockAccess, blockState, posFront.east().north(), side, icon, metadata);
                    edges[3] = edges[3] || isNeighbour(cp, blockAccess, blockState, posFront.west().north(), side, icon, metadata);
                }
                break;
            case 2:
                edges[0] = !isNeighbour(cp, blockAccess, blockState, blockPos.west().below(), side, icon, metadata);
                edges[1] = !isNeighbour(cp, blockAccess, blockState, blockPos.east().below(), side, icon, metadata);
                edges[2] = !isNeighbour(cp, blockAccess, blockState, blockPos.west().above(), side, icon, metadata);
                edges[3] = !isNeighbour(cp, blockAccess, blockState, blockPos.east().above(), side, icon, metadata);
                if (cp.innerSeams) {
                    BlockPos posFront = blockPos.north();
                    edges[0] = edges[0] || isNeighbour(cp, blockAccess, blockState, posFront.west().below(), side, icon, metadata);
                    edges[1] = edges[1] || isNeighbour(cp, blockAccess, blockState, posFront.east().below(), side, icon, metadata);
                    edges[2] = edges[2] || isNeighbour(cp, blockAccess, blockState, posFront.west().above(), side, icon, metadata);
                    edges[3] = edges[3] || isNeighbour(cp, blockAccess, blockState, posFront.east().above(), side, icon, metadata);
                }
                break;
            case 3:
                edges[0] = !isNeighbour(cp, blockAccess, blockState, blockPos.east().below(), side, icon, metadata);
                edges[1] = !isNeighbour(cp, blockAccess, blockState, blockPos.west().below(), side, icon, metadata);
                edges[2] = !isNeighbour(cp, blockAccess, blockState, blockPos.east().above(), side, icon, metadata);
                edges[3] = !isNeighbour(cp, blockAccess, blockState, blockPos.west().above(), side, icon, metadata);
                if (cp.innerSeams) {
                    BlockPos posFront = blockPos.south();
                    edges[0] = edges[0] || isNeighbour(cp, blockAccess, blockState, posFront.east().below(), side, icon, metadata);
                    edges[1] = edges[1] || isNeighbour(cp, blockAccess, blockState, posFront.west().below(), side, icon, metadata);
                    edges[2] = edges[2] || isNeighbour(cp, blockAccess, blockState, posFront.east().above(), side, icon, metadata);
                    edges[3] = edges[3] || isNeighbour(cp, blockAccess, blockState, posFront.west().above(), side, icon, metadata);
                }
                break;
            case 4:
                edges[0] = !isNeighbour(cp, blockAccess, blockState, blockPos.below().south(), side, icon, metadata);
                edges[1] = !isNeighbour(cp, blockAccess, blockState, blockPos.below().north(), side, icon, metadata);
                edges[2] = !isNeighbour(cp, blockAccess, blockState, blockPos.above().south(), side, icon, metadata);
                edges[3] = !isNeighbour(cp, blockAccess, blockState, blockPos.above().north(), side, icon, metadata);
                if (cp.innerSeams) {
                    BlockPos posFront = blockPos.west();
                    edges[0] = edges[0] || isNeighbour(cp, blockAccess, blockState, posFront.below().south(), side, icon, metadata);
                    edges[1] = edges[1] || isNeighbour(cp, blockAccess, blockState, posFront.below().north(), side, icon, metadata);
                    edges[2] = edges[2] || isNeighbour(cp, blockAccess, blockState, posFront.above().south(), side, icon, metadata);
                    edges[3] = edges[3] || isNeighbour(cp, blockAccess, blockState, posFront.above().north(), side, icon, metadata);
                }
                break;
            case 5:
                edges[0] = !isNeighbour(cp, blockAccess, blockState, blockPos.below().north(), side, icon, metadata);
                edges[1] = !isNeighbour(cp, blockAccess, blockState, blockPos.below().south(), side, icon, metadata);
                edges[2] = !isNeighbour(cp, blockAccess, blockState, blockPos.above().north(), side, icon, metadata);
                edges[3] = !isNeighbour(cp, blockAccess, blockState, blockPos.above().south(), side, icon, metadata);
                if (cp.innerSeams) {
                    BlockPos posFront = blockPos.east();
                    edges[0] = edges[0] || isNeighbour(cp, blockAccess, blockState, posFront.below().north(), side, icon, metadata);
                    edges[1] = edges[1] || isNeighbour(cp, blockAccess, blockState, posFront.below().south(), side, icon, metadata);
                    edges[2] = edges[2] || isNeighbour(cp, blockAccess, blockState, posFront.above().north(), side, icon, metadata);
                    edges[3] = edges[3] || isNeighbour(cp, blockAccess, blockState, posFront.above().south(), side, icon, metadata);
                }
        }

        if (index == 13 && edges[0]) {
            index = 4;
        } else if (index == 15 && edges[1]) {
            index = 5;
        } else if (index == 37 && edges[2]) {
            index = 16;
        } else if (index == 39 && edges[3]) {
            index = 17;
        } else if (index == 14 && edges[0] && edges[1]) {
            index = 7;
        } else if (index == 25 && edges[0] && edges[2]) {
            index = 6;
        } else if (index == 27 && edges[3] && edges[1]) {
            index = 19;
        } else if (index == 38 && edges[3] && edges[2]) {
            index = 18;
        } else if (index == 14 && !edges[0] && edges[1]) {
            index = 31;
        } else if (index == 25 && edges[0] && !edges[2]) {
            index = 30;
        } else if (index == 27 && !edges[3] && edges[1]) {
            index = 41;
        } else if (index == 38 && edges[3] && !edges[2]) {
            index = 40;
        } else if (index == 14 && edges[0] && !edges[1]) {
            index = 29;
        } else if (index == 25 && !edges[0] && edges[2]) {
            index = 28;
        } else if (index == 27 && edges[3] && !edges[1]) {
            index = 43;
        } else if (index == 38 && !edges[3] && edges[2]) {
            index = 42;
        } else if (index == 26 && edges[0] && edges[1] && edges[2] && edges[3]) {
            index = 46;
        } else if (index == 26 && !edges[0] && edges[1] && edges[2] && edges[3]) {
            index = 9;
        } else if (index == 26 && edges[0] && !edges[1] && edges[2] && edges[3]) {
            index = 21;
        } else if (index == 26 && edges[0] && edges[1] && !edges[2] && edges[3]) {
            index = 8;
        } else if (index == 26 && edges[0] && edges[1] && edges[2] && !edges[3]) {
            index = 20;
        } else if (index == 26 && edges[0] && edges[1] && !edges[2] && !edges[3]) {
            index = 11;
        } else if (index == 26 && !edges[0] && !edges[1] && edges[2] && edges[3]) {
            index = 22;
        } else if (index == 26 && !edges[0] && edges[1] && !edges[2] && edges[3]) {
            index = 23;
        } else if (index == 26 && edges[0] && !edges[1] && edges[2] && !edges[3]) {
            index = 10;
        } else if (index == 26 && edges[0] && !edges[1] && !edges[2] && edges[3]) {
            index = 34;
        } else if (index == 26 && !edges[0] && edges[1] && edges[2] && !edges[3]) {
            index = 35;
        } else if (index == 26 && edges[0] && !edges[1] && !edges[2] && !edges[3]) {
            index = 32;
        } else if (index == 26 && !edges[0] && edges[1] && !edges[2] && !edges[3]) {
            index = 33;
        } else if (index == 26 && !edges[0] && !edges[1] && edges[2] && !edges[3]) {
            index = 44;
        } else if (index == 26 && !edges[0] && !edges[1] && !edges[2] && edges[3]) {
            index = 45;
        }

        return index;
    }

    private static void switchValues(int ix1, int ix2, boolean[] arr) {
        boolean prev1 = arr[ix1];
        arr[ix1] = arr[ix2];
        arr[ix2] = prev1;
    }

    private static boolean isNeighbourOverlay(
        ConnectedProperties cp, BlockGetter worldReader, BlockState blockState, BlockPos blockPos, int side, TextureAtlasSprite icon, int metadata
    ) {
        BlockState neighbourState = worldReader.getBlockState(blockPos);
        if (!isFullCubeModel(neighbourState, worldReader, blockPos)) {
            return false;
        }

        if (cp.connectBlocks != null && !Matches.block(neighbourState.getBlockId(), neighbourState.getMetadata(), cp.connectBlocks)) {
            return false;
        }

        if (cp.connectTileIcons != null) {
            TextureAtlasSprite neighbourIcon = getNeighbourIcon(worldReader, blockState, blockPos, neighbourState, side);
            if (!Config.isSameOne(neighbourIcon, cp.connectTileIcons)) {
                return false;
            }
        }

        BlockPos posNeighbourStateAbove = blockPos.relative(getFacing(side));
        BlockState neighbourStateAbove = worldReader.getBlockState(posNeighbourStateAbove);
        if (neighbourStateAbove.isSolidRender()) {
            return false;
        } else {
            return side == 1 && neighbourStateAbove.getBlock() == Blocks.SNOW
                ? false
                : !isNeighbour(cp, worldReader, blockState, blockPos, neighbourState, side, icon, metadata);
        }
    }

    private static boolean isFullCubeModel(BlockState state, BlockGetter blockReader, BlockPos pos) {
        if (BlockUtils.isFullCube(state, blockReader, pos)) {
            return true;
        }

        Block block = state.getBlock();
        return block == Blocks.GLASS ? true : block instanceof StainedGlassBlock;
    }

    private static boolean isNeighbourMatching(
        ConnectedProperties cp, BlockGetter worldReader, BlockState blockState, BlockPos blockPos, int side, TextureAtlasSprite icon, int metadata
    ) {
        BlockState neighbourState = worldReader.getBlockState(blockPos);
        if (neighbourState == AIR_DEFAULT_STATE) {
            return false;
        }

        if (cp.matchBlocks != null && !cp.matchesBlock(neighbourState.getBlockId(), neighbourState.getMetadata())) {
            return false;
        }

        if (cp.matchTileIcons != null) {
            TextureAtlasSprite neighbourIcon = getNeighbourIcon(worldReader, blockState, blockPos, neighbourState, side);
            if (neighbourIcon != icon) {
                return false;
            }
        }

        BlockPos posNeighbourAbove = blockPos.relative(getFacing(side));
        BlockState neighbourStateAbove = worldReader.getBlockState(posNeighbourAbove);
        return neighbourStateAbove.isSolidRender() ? false : side != 1 || neighbourStateAbove.getBlock() != Blocks.SNOW;
    }

    private static boolean isNeighbour(
        ConnectedProperties cp, BlockGetter worldReader, BlockState blockState, BlockPos blockPos, int side, TextureAtlasSprite icon, int metadata
    ) {
        BlockState neighbourState = worldReader.getBlockState(blockPos);
        return isNeighbour(cp, worldReader, blockState, blockPos, neighbourState, side, icon, metadata);
    }

    private static boolean isNeighbour(
        ConnectedProperties cp,
        BlockGetter worldReader,
        BlockState blockState,
        BlockPos blockPos,
        BlockState neighbourState,
        int side,
        TextureAtlasSprite icon,
        int metadata
    ) {
        if (blockState == neighbourState) {
            return true;
        }

        if (cp.connect == 2) {
            if (neighbourState == null) {
                return false;
            }

            if (neighbourState == AIR_DEFAULT_STATE) {
                return false;
            }

            TextureAtlasSprite neighbourIcon = getNeighbourIcon(worldReader, blockState, blockPos, neighbourState, side);
            return neighbourIcon == icon;
        } else if (cp.connect == 1) {
            Block block = blockState.getBlock();
            Block neighbourBlock = neighbourState.getBlock();
            return neighbourBlock == block;
        } else {
            return false;
        }
    }

    private static TextureAtlasSprite getNeighbourIcon(BlockGetter worldReader, BlockState blockState, BlockPos blockPos, BlockState neighbourState, int side) {
        BlockStateModelPart model = BlockModelUtils.getModel(neighbourState);
        if (model == null) {
            return null;
        }

        Direction facing = getFacing(side);
        List quads = model.getQuads(facing);
        if (quads == null) {
            return null;
        }

        if (Config.isBetterGrass()) {
            quads = BetterGrass.getFaceQuads(worldReader, neighbourState, blockPos, facing, quads);
        }

        if (quads.size() > 0) {
            BakedQuad quad = (BakedQuad)quads.get(0);
            return quad.sprite();
        }

        List quadsGeneral = model.getQuads(null);
        if (quadsGeneral == null) {
            return null;
        }

        for (int i = 0; i < quadsGeneral.size(); i++) {
            BakedQuad quad = (BakedQuad)quadsGeneral.get(i);
            if (quad.direction() == facing) {
                return quad.sprite();
            }
        }

        return null;
    }

    private static TextureAtlasSprite getConnectedTextureHorizontal(
        ConnectedProperties cp, BlockGetter blockAccess, BlockState blockState, BlockPos blockPos, int vertAxis, int side, TextureAtlasSprite icon, int metadata
    ) {
        boolean left;
        boolean right;
        left = false;
        right = false;
        label46:
        switch (vertAxis) {
            case 0:
                switch (side) {
                    case 0:
                        left = isNeighbour(cp, blockAccess, blockState, blockPos.west(), side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
                        break label46;
                    case 1:
                        left = isNeighbour(cp, blockAccess, blockState, blockPos.west(), side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
                        break label46;
                    case 2:
                        left = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, blockState, blockPos.west(), side, icon, metadata);
                        break label46;
                    case 3:
                        left = isNeighbour(cp, blockAccess, blockState, blockPos.west(), side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
                        break label46;
                    case 4:
                        left = isNeighbour(cp, blockAccess, blockState, blockPos.north(), side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                        break label46;
                    case 5:
                        left = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, blockState, blockPos.north(), side, icon, metadata);
                    default:
                        break label46;
                }
            case 1:
                switch (side) {
                    case 0:
                        left = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, blockState, blockPos.west(), side, icon, metadata);
                        break label46;
                    case 1:
                        left = isNeighbour(cp, blockAccess, blockState, blockPos.west(), side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
                        break label46;
                    case 2:
                        left = isNeighbour(cp, blockAccess, blockState, blockPos.west(), side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
                        break label46;
                    case 3:
                        left = isNeighbour(cp, blockAccess, blockState, blockPos.west(), side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
                        break label46;
                    case 4:
                        left = isNeighbour(cp, blockAccess, blockState, blockPos.below(), side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, blockState, blockPos.above(), side, icon, metadata);
                        break label46;
                    case 5:
                        left = isNeighbour(cp, blockAccess, blockState, blockPos.above(), side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, blockState, blockPos.below(), side, icon, metadata);
                    default:
                        break label46;
                }
            case 2:
                switch (side) {
                    case 0:
                        left = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, blockState, blockPos.north(), side, icon, metadata);
                        break;
                    case 1:
                        left = isNeighbour(cp, blockAccess, blockState, blockPos.north(), side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                        break;
                    case 2:
                        left = isNeighbour(cp, blockAccess, blockState, blockPos.below(), side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, blockState, blockPos.above(), side, icon, metadata);
                        break;
                    case 3:
                        left = isNeighbour(cp, blockAccess, blockState, blockPos.above(), side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, blockState, blockPos.below(), side, icon, metadata);
                        break;
                    case 4:
                        left = isNeighbour(cp, blockAccess, blockState, blockPos.north(), side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                        break;
                    case 5:
                        left = isNeighbour(cp, blockAccess, blockState, blockPos.north(), side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                }
        }

        int index = 3;
        byte var11;
        if (left) {
            if (right) {
                var11 = 1;
            } else {
                var11 = 2;
            }
        } else if (right) {
            var11 = 0;
        } else {
            var11 = 3;
        }

        return cp.tileIcons[var11];
    }

    private static TextureAtlasSprite getConnectedTextureVertical(
        ConnectedProperties cp, BlockGetter blockAccess, BlockState blockState, BlockPos blockPos, int vertAxis, int side, TextureAtlasSprite icon, int metadata
    ) {
        boolean bottom = false;
        boolean top = false;
        switch (vertAxis) {
            case 0:
                if (side == 1) {
                    bottom = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                    top = isNeighbour(cp, blockAccess, blockState, blockPos.north(), side, icon, metadata);
                } else if (side == 0) {
                    bottom = isNeighbour(cp, blockAccess, blockState, blockPos.north(), side, icon, metadata);
                    top = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                } else {
                    bottom = isNeighbour(cp, blockAccess, blockState, blockPos.below(), side, icon, metadata);
                    top = isNeighbour(cp, blockAccess, blockState, blockPos.above(), side, icon, metadata);
                }
                break;
            case 1:
                if (side == 3) {
                    bottom = isNeighbour(cp, blockAccess, blockState, blockPos.below(), side, icon, metadata);
                    top = isNeighbour(cp, blockAccess, blockState, blockPos.above(), side, icon, metadata);
                } else if (side == 2) {
                    bottom = isNeighbour(cp, blockAccess, blockState, blockPos.above(), side, icon, metadata);
                    top = isNeighbour(cp, blockAccess, blockState, blockPos.below(), side, icon, metadata);
                } else {
                    bottom = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                    top = isNeighbour(cp, blockAccess, blockState, blockPos.north(), side, icon, metadata);
                }
                break;
            case 2:
                if (side == 5) {
                    bottom = isNeighbour(cp, blockAccess, blockState, blockPos.above(), side, icon, metadata);
                    top = isNeighbour(cp, blockAccess, blockState, blockPos.below(), side, icon, metadata);
                } else if (side == 4) {
                    bottom = isNeighbour(cp, blockAccess, blockState, blockPos.below(), side, icon, metadata);
                    top = isNeighbour(cp, blockAccess, blockState, blockPos.above(), side, icon, metadata);
                } else {
                    bottom = isNeighbour(cp, blockAccess, blockState, blockPos.west(), side, icon, metadata);
                    top = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
                }
        }

        int index = 3;
        byte var11;
        if (bottom) {
            if (top) {
                var11 = 1;
            } else {
                var11 = 2;
            }
        } else if (top) {
            var11 = 0;
        } else {
            var11 = 3;
        }

        return cp.tileIcons[var11];
    }

    private static TextureAtlasSprite getConnectedTextureHorizontalVertical(
        ConnectedProperties cp, BlockGetter blockAccess, BlockState blockState, BlockPos blockPos, int vertAxis, int side, TextureAtlasSprite icon, int metadata
    ) {
        TextureAtlasSprite[] tileIcons = cp.tileIcons;
        TextureAtlasSprite iconH = getConnectedTextureHorizontal(cp, blockAccess, blockState, blockPos, vertAxis, side, icon, metadata);
        if (iconH != null && iconH != icon && iconH != tileIcons[3]) {
            return iconH;
        } else {
            TextureAtlasSprite iconV = getConnectedTextureVertical(cp, blockAccess, blockState, blockPos, vertAxis, side, icon, metadata);
            if (iconV == tileIcons[0]) {
                return tileIcons[4];
            } else if (iconV == tileIcons[1]) {
                return tileIcons[5];
            } else {
                return iconV == tileIcons[2] ? tileIcons[6] : iconV;
            }
        }
    }

    private static TextureAtlasSprite getConnectedTextureVerticalHorizontal(
        ConnectedProperties cp, BlockGetter blockAccess, BlockState blockState, BlockPos blockPos, int vertAxis, int side, TextureAtlasSprite icon, int metadata
    ) {
        TextureAtlasSprite[] tileIcons = cp.tileIcons;
        TextureAtlasSprite iconV = getConnectedTextureVertical(cp, blockAccess, blockState, blockPos, vertAxis, side, icon, metadata);
        if (iconV != null && iconV != icon && iconV != tileIcons[3]) {
            return iconV;
        } else {
            TextureAtlasSprite iconH = getConnectedTextureHorizontal(cp, blockAccess, blockState, blockPos, vertAxis, side, icon, metadata);
            if (iconH == tileIcons[0]) {
                return tileIcons[4];
            } else if (iconH == tileIcons[1]) {
                return tileIcons[5];
            } else {
                return iconH == tileIcons[2] ? tileIcons[6] : iconH;
            }
        }
    }

    private static TextureAtlasSprite getConnectedTextureTop(
        ConnectedProperties cp, BlockGetter blockAccess, BlockState blockState, BlockPos blockPos, int vertAxis, int side, TextureAtlasSprite icon, int metadata
    ) {
        boolean top = false;
        switch (vertAxis) {
            case 0:
                if (side == 1 || side == 0) {
                    return null;
                }

                top = isNeighbour(cp, blockAccess, blockState, blockPos.above(), side, icon, metadata);
                break;
            case 1:
                if (side == 3 || side == 2) {
                    return null;
                }

                top = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                break;
            case 2:
                if (side == 5 || side == 4) {
                    return null;
                }

                top = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
        }

        return top ? cp.tileIcons[0] : null;
    }

    public static void updateIcons(TextureAtlas textureMap) {
        blockProperties = null;
        tileProperties = null;
        spriteQuadMaps = null;
        spriteQuadCompactMaps = null;
        if (Config.isConnectedTextures()) {
            PackResources[] rps = Config.getResourcePacks();

            for (int i = rps.length - 1; i >= 0; i--) {
                PackResources rp = rps[i];
                updateIcons(textureMap, rp);
            }

            updateIcons(textureMap, Config.getDefaultResourcePack());
            emptySprite = textureMap.registerSprite(LOCATION_SPRITE_EMPTY);
            spriteQuadMaps = new Map[textureMap.getCountRegisteredSprites() + 1];
            spriteQuadFullMaps = new Map[textureMap.getCountRegisteredSprites() + 1];
            spriteQuadCompactMaps = new Map[textureMap.getCountRegisteredSprites() + 1][];
            if (blockProperties.length <= 0) {
                blockProperties = null;
            }

            if (tileProperties.length <= 0) {
                tileProperties = null;
            }
        }
    }

    public static void updateIcons(TextureAtlas textureMap, PackResources rp) {
        String[] names = ResUtils.collectFiles(rp, "optifine/ctm/", ".properties", getDefaultCtmPaths());
        Arrays.sort(names);
        List tileList = makePropertyList(tileProperties);
        List blockList = makePropertyList(blockProperties);

        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            Config.dbg("ConnectedTextures: " + name);

            try {
                Identifier locFile = new Identifier(name);
                InputStream in = Config.getResourceStream(rp, PackType.CLIENT_RESOURCES, locFile);
                if (in == null) {
                    Config.warn("ConnectedTextures file not found: " + name);
                } else {
                    Properties props = new PropertiesOrdered();
                    props.load(in);
                    in.close();
                    ConnectedProperties cp = new ConnectedProperties(props, name);
                    if (cp.isValid(name)) {
                        cp.updateIcons(textureMap);
                        addToTileList(cp, tileList);
                        addToBlockList(cp, blockList);
                    }
                }
            } catch (FileNotFoundException e) {
                Config.warn("ConnectedTextures file not found: " + name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        blockProperties = propertyListToArray(blockList);
        tileProperties = propertyListToArray(tileList);
        multipass = detectMultipass();
        Config.dbg("Multipass connected textures: " + multipass);
    }

    public static void refreshIcons(TextureAtlas textureMap) {
        refreshIcons(blockProperties, textureMap);
        refreshIcons(tileProperties, textureMap);
        emptySprite = getSprite(textureMap, LOCATION_SPRITE_EMPTY);
    }

    private static TextureAtlasSprite getSprite(TextureAtlas textureMap, Identifier loc) {
        TextureAtlasSprite sprite = textureMap.getSprite(loc);
        if (sprite == null || MissingTextureAtlasSprite.isMisingSprite(sprite)) {
            Config.warn("Missing CTM sprite: " + loc);
        }

        return sprite;
    }

    private static void refreshIcons(ConnectedProperties[][] propertiesArray, TextureAtlas textureMap) {
        if (propertiesArray != null) {
            for (int i = 0; i < propertiesArray.length; i++) {
                ConnectedProperties[] properties = propertiesArray[i];
                if (properties != null) {
                    for (int c = 0; c < properties.length; c++) {
                        ConnectedProperties cp = properties[c];
                        if (cp != null) {
                            cp.refreshIcons(textureMap);
                        }
                    }
                }
            }
        }
    }

    private static List makePropertyList(ConnectedProperties[][] propsArr) {
        List list = new ArrayList();
        if (propsArr != null) {
            for (int i = 0; i < propsArr.length; i++) {
                ConnectedProperties[] props = propsArr[i];
                List propList = null;
                if (props != null) {
                    propList = new ArrayList<>(Arrays.asList(props));
                }

                list.add(propList);
            }
        }

        return list;
    }

    private static boolean detectMultipass() {
        List propList = new ArrayList();

        for (int i = 0; i < tileProperties.length; i++) {
            ConnectedProperties[] cps = tileProperties[i];
            if (cps != null) {
                propList.addAll(Arrays.asList(cps));
            }
        }

        for (int i = 0; i < blockProperties.length; i++) {
            ConnectedProperties[] cps = blockProperties[i];
            if (cps != null) {
                propList.addAll(Arrays.asList(cps));
            }
        }

        ConnectedProperties[] props = (ConnectedProperties[])propList.toArray(new ConnectedProperties[propList.size()]);
        Set matchIconSet = new HashSet();
        Set tileIconSet = new HashSet();

        for (int i = 0; i < props.length; i++) {
            ConnectedProperties cp = props[i];
            if (cp.matchTileIcons != null) {
                matchIconSet.addAll(Arrays.asList(cp.matchTileIcons));
            }

            if (cp.tileIcons != null) {
                tileIconSet.addAll(Arrays.asList(cp.tileIcons));
            }
        }

        matchIconSet.retainAll(tileIconSet);
        return !matchIconSet.isEmpty();
    }

    private static ConnectedProperties[][] propertyListToArray(List list) {
        ConnectedProperties[][] propArr = new ConnectedProperties[list.size()][];

        for (int i = 0; i < list.size(); i++) {
            List subList = (List)list.get(i);
            if (subList != null) {
                ConnectedProperties[] subArr = (ConnectedProperties[])subList.toArray(new ConnectedProperties[subList.size()]);
                propArr[i] = subArr;
            }
        }

        return propArr;
    }

    private static void addToTileList(ConnectedProperties cp, List tileList) {
        if (cp.matchTileIcons != null) {
            for (int i = 0; i < cp.matchTileIcons.length; i++) {
                TextureAtlasSprite icon = cp.matchTileIcons[i];
                if (!(icon instanceof TextureAtlasSprite ts)) {
                    Config.warn("TextureAtlasSprite is not TextureAtlasSprite: " + icon + ", name: " + icon.getName());
                } else {
                    int tileId = ts.getIndexInMap();
                    if (tileId < 0) {
                        Config.warn("Invalid tile ID: " + tileId + ", icon: " + ts.getName());
                    } else {
                        addToList(cp, tileList, tileId);
                    }
                }
            }
        }
    }

    private static void addToBlockList(ConnectedProperties cp, List blockList) {
        if (cp.matchBlocks != null) {
            for (int i = 0; i < cp.matchBlocks.length; i++) {
                int blockId = cp.matchBlocks[i].getBlockId();
                if (blockId < 0) {
                    Config.warn("Invalid block ID: " + blockId);
                } else {
                    addToList(cp, blockList, blockId);
                }
            }
        }
    }

    private static void addToList(ConnectedProperties cp, List list, int id) {
        while (id >= list.size()) {
            list.add(null);
        }

        List subList = (List)list.get(id);
        if (subList == null) {
            subList = new ArrayList();
            list.set(id, subList);
        }

        subList.add(cp);
    }

    private static String[] getDefaultCtmPaths() {
        List list = new ArrayList();
        addDefaultLocation(list, "textures/block/glass.png", "20_glass/glass.properties");
        addDefaultLocation(list, "textures/block/glass.png", "20_glass/glass_pane.properties");
        addDefaultLocation(list, "textures/block/tinted_glass.png", "21_tinted_glass/tinted_glass.properties");
        addDefaultLocation(list, "textures/block/bookshelf.png", "30_bookshelf/bookshelf.properties");
        addDefaultLocation(list, "textures/block/sandstone.png", "40_sandstone/sandstone.properties");
        addDefaultLocation(list, "textures/block/red_sandstone.png", "41_red_sandstone/red_sandstone.properties");
        String[] colors = new String[]{
            "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
        };

        for (int i = 0; i < colors.length; i++) {
            String color = colors[i];
            String prefix = StrUtils.fillLeft(i + "", 2, '0');
            addDefaultLocation(list, "textures/block/" + color + "_stained_glass.png", prefix + "_glass_" + color + "/glass_" + color + ".properties");
            addDefaultLocation(list, "textures/block/" + color + "_stained_glass.png", prefix + "_glass_" + color + "/glass_pane_" + color + ".properties");
        }

        return (String[])list.toArray(new String[list.size()]);
    }

    private static void addDefaultLocation(List list, String locBase, String pathSuffix) {
        String defPath = "optifine/ctm/default/";
        Identifier loc = new Identifier(locBase);
        PackResources rp = Config.getDefiningResourcePack(loc);
        if (rp != null) {
            if (rp.packId().equals("programmer_art")) {
                String defPathPa = defPath + "programmer_art/";
                list.add(defPathPa + pathSuffix);
            } else {
                if (rp == Config.getDefaultResourcePack()) {
                    list.add(defPath + pathSuffix);
                }
            }
        }
    }
}
