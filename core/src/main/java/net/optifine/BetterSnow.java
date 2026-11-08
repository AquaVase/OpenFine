package net.optifine;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.optifine.util.DummyRandomSource;

public class BetterSnow {
    private static BlockStateModelPart modelSnowLayer = null;

    public static void update() {
        BlockStateModel bsm = Config.getMinecraft().getModelManager().getBlockStateModelSet().get(Blocks.SNOW.defaultBlockState());
        List<BlockStateModelPart> bmps = new ArrayList<>();
        bsm.collectParts(DummyRandomSource.INSTANCE, bmps);
        modelSnowLayer = bmps.get(0);
    }

    public static BlockStateModelPart getModelSnowLayer() {
        return modelSnowLayer;
    }

    public static BlockState getStateSnowLayer() {
        return Blocks.SNOW.defaultBlockState();
    }

    public static boolean shouldRender(BlockAndTintGetter lightReader, BlockState blockState, BlockPos blockPos) {
        if (!(lightReader instanceof BlockGetter blockAccess)) {
            return false;
        } else {
            return !checkBlock(blockAccess, blockState, blockPos) ? false : hasSnowNeighbours(blockAccess, blockPos);
        }
    }

    private static boolean hasSnowNeighbours(BlockGetter blockAccess, BlockPos pos) {
        Block blockSnow = Blocks.SNOW;
        if (blockAccess.getBlockState(pos.north()).getBlock() == blockSnow
            || blockAccess.getBlockState(pos.south()).getBlock() == blockSnow
            || blockAccess.getBlockState(pos.west()).getBlock() == blockSnow
            || blockAccess.getBlockState(pos.east()).getBlock() == blockSnow) {
            BlockState bsDown = blockAccess.getBlockState(pos.below());
            if (bsDown.isSolidRender()) {
                return true;
            }

            Block blockDown = bsDown.getBlock();
            if (blockDown instanceof StairBlock) {
                return bsDown.getValue(StairBlock.HALF) == Half.TOP;
            }

            if (blockDown instanceof SlabBlock) {
                return bsDown.getValue(SlabBlock.TYPE) == SlabType.TOP;
            }
        }

        return false;
    }

    private static boolean checkBlock(BlockGetter blockAccess, BlockState blockState, BlockPos blockPos) {
        if (blockState.isSolidRender()) {
            return false;
        }

        Block block = blockState.getBlock();
        if (block == Blocks.SNOW_BLOCK) {
            return false;
        }

        if (!(block instanceof VegetationBlock)
            || !(block instanceof DoublePlantBlock)
                && !(block instanceof FlowerBlock)
                && !(block instanceof MushroomBlock)
                && !(block instanceof SaplingBlock)
                && !(block instanceof TallGrassBlock)) {
            if (block instanceof FenceBlock
                || block instanceof FenceGateBlock
                || block instanceof FlowerPotBlock
                || block instanceof CrossCollisionBlock
                || block instanceof SugarCaneBlock
                || block instanceof WallBlock) {
                return true;
            } else if (block instanceof RedstoneTorchBlock) {
                return true;
            } else if (block instanceof StairBlock) {
                return blockState.getValue(StairBlock.HALF) == Half.TOP;
            } else if (block instanceof SlabBlock) {
                return blockState.getValue(SlabBlock.TYPE) == SlabType.TOP;
            } else if (block instanceof ButtonBlock) {
                return blockState.getValue(ButtonBlock.FACE) != AttachFace.FLOOR;
            } else if (block instanceof HopperBlock) {
                return true;
            } else if (block instanceof LadderBlock) {
                return true;
            } else if (block instanceof LeverBlock) {
                return true;
            } else {
                return block instanceof TurtleEggBlock ? true : block instanceof VineBlock;
            }
        } else {
            return true;
        }
    }
}
