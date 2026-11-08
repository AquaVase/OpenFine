package net.optifine;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.optifine.model.BlockModelUtils;

public class SmartLeaves {
    private static BlockStateModelPart modelLeavesCullAcacia = null;
    private static BlockStateModelPart modelLeavesCullBirch = null;
    private static BlockStateModelPart modelLeavesCullDarkOak = null;
    private static BlockStateModelPart modelLeavesCullJungle = null;
    private static BlockStateModelPart modelLeavesCullOak = null;
    private static BlockStateModelPart modelLeavesCullSpruce = null;
    private static BlockStateModelPart modelLeavesDoubleAcacia = null;
    private static BlockStateModelPart modelLeavesDoubleBirch = null;
    private static BlockStateModelPart modelLeavesDoubleDarkOak = null;
    private static BlockStateModelPart modelLeavesDoubleJungle = null;
    private static BlockStateModelPart modelLeavesDoubleOak = null;
    private static BlockStateModelPart modelLeavesDoubleSpruce = null;

    public static List<BlockStateModelPart> getLeavesModel(List<BlockStateModelPart> partsIn, BlockState stateIn) {
        if (partsIn.size() != 1) {
            return partsIn;
        }

        BlockStateModelPart part = partsIn.get(0);
        BlockStateModelPart part2 = getLeavesModel(part, stateIn);
        if (part2 == part) {
            return partsIn;
        }

        partsIn.set(0, part2);
        return partsIn;
    }

    public static BlockStateModelPart getLeavesModel(BlockStateModelPart model, BlockState stateIn) {
        if (!Config.isTreesSmart()) {
            return model;
        } else if (model == modelLeavesCullAcacia) {
            return modelLeavesDoubleAcacia;
        } else if (model == modelLeavesCullBirch) {
            return modelLeavesDoubleBirch;
        } else if (model == modelLeavesCullDarkOak) {
            return modelLeavesDoubleDarkOak;
        } else if (model == modelLeavesCullJungle) {
            return modelLeavesDoubleJungle;
        } else if (model == modelLeavesCullOak) {
            return modelLeavesDoubleOak;
        } else {
            return model == modelLeavesCullSpruce ? modelLeavesDoubleSpruce : model;
        }
    }

    public static boolean isSameLeaves(BlockState state1, BlockState state2) {
        if (state1 == state2) {
            return true;
        }

        Block block1 = state1.getBlock();
        Block block2 = state2.getBlock();
        return block1 == block2;
    }

    public static void updateLeavesModels() {
        List<String> updatedTypes = new ArrayList<>();
        modelLeavesCullAcacia = getModelCull(Blocks.ACACIA_LEAVES.defaultBlockState(), updatedTypes);
        modelLeavesCullBirch = getModelCull(Blocks.BIRCH_LEAVES.defaultBlockState(), updatedTypes);
        modelLeavesCullDarkOak = getModelCull(Blocks.DARK_OAK_LEAVES.defaultBlockState(), updatedTypes);
        modelLeavesCullJungle = getModelCull(Blocks.JUNGLE_LEAVES.defaultBlockState(), updatedTypes);
        modelLeavesCullOak = getModelCull(Blocks.OAK_LEAVES.defaultBlockState(), updatedTypes);
        modelLeavesCullSpruce = getModelCull(Blocks.SPRUCE_LEAVES.defaultBlockState(), updatedTypes);
        modelLeavesDoubleAcacia = getModelDoubleFace(modelLeavesCullAcacia);
        modelLeavesDoubleBirch = getModelDoubleFace(modelLeavesCullBirch);
        modelLeavesDoubleDarkOak = getModelDoubleFace(modelLeavesCullDarkOak);
        modelLeavesDoubleJungle = getModelDoubleFace(modelLeavesCullJungle);
        modelLeavesDoubleOak = getModelDoubleFace(modelLeavesCullOak);
        modelLeavesDoubleSpruce = getModelDoubleFace(modelLeavesCullSpruce);
        if (updatedTypes.size() > 0) {
            Config.dbg("Enable face culling: " + Config.arrayToString(updatedTypes.toArray()));
        }
    }

    static BlockStateModelPart getModelCull(BlockState blockState, List<String> updatedTypes) {
        String stateName = blockState.getBlockLocation().getPath();
        Identifier locState = new Identifier("blockstates/" + stateName + ".json");
        if (!Config.isFromDefaultResourcePack(locState)) {
            return null;
        }

        Identifier locModel = new Identifier("models/block/" + stateName + ".json");
        if (!Config.isFromDefaultResourcePack(locModel)) {
            return null;
        }

        BlockStateModel stateModel = BlockModelUtils.getBlockStateModel(blockState);
        if (BlockModelUtils.isMissingModel(stateModel)) {
            Config.warn("Model not found for block state: " + blockState);
            return BlockModelUtils.getMissingBlockModel();
        }

        BlockStateModelPart model = BlockModelUtils.getModel(blockState);

        for (Direction face : Direction.VALUES) {
            List<BakedQuad> quads = model.getQuads(face);
            if (quads.isEmpty()) {
                return null;
            }
        }

        return model;
    }

    private static BlockStateModelPart getModelDoubleFace(BlockStateModelPart model) {
        if (model == null) {
            return null;
        }

        if (model.getQuads(null).size() > 0) {
            Config.warn("SmartLeaves: Model is not cube, general quads: " + model.getQuads(null).size() + ", model: " + model);
            return model;
        }

        Direction[] faces = Direction.VALUES;

        for (int i = 0; i < faces.length; i++) {
            Direction face = faces[i];
            List<BakedQuad> quads = model.getQuads(face);
            if (quads.size() != 1) {
                Config.warn("SmartLeaves: Model is not cube, side: " + face + ", quads: " + quads.size() + ", model: " + model);
                return model;
            }
        }

        return BlockModelUtils.duplicateModel(model, SmartLeaves::addReverseQuads);
    }

    public static List<BakedQuad> addReverseQuads(List<BakedQuad> quads, Direction face) {
        if (face == null) {
            return quads;
        }

        List<BakedQuad> quads2 = new ArrayList<>(quads);
        BakedQuad quad = quads2.get(0);
        BakedQuad quad2 = quad.makeCopyReverse();
        quads2.add(quad2);
        return quads2;
    }
}
