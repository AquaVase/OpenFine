package net.optifine.render;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.optifine.override.ChunkCacheOF;

public class LightCacheOF {
    public static final float getShadeBrightness(BlockState blockStateIn, BlockAndTintGetter worldIn, BlockPos blockPosIn) {
        float aoLight = getAoLightRaw(blockStateIn, worldIn, blockPosIn);
        return ModelBlockRenderer.fixAoLightValue(aoLight);
    }

    private static float getAoLightRaw(BlockState blockStateIn, BlockAndTintGetter worldIn, BlockPos blockPosIn) {
        if (blockStateIn.getBlock() == Blocks.MOVING_PISTON) {
            return 1.0F;
        } else {
            return blockStateIn.getBlock() == Blocks.SCAFFOLDING ? 1.0F : blockStateIn.getShadeBrightness(worldIn, blockPosIn);
        }
    }

    public static final int getLightCoords(BlockState blockStateIn, BlockAndTintGetter worldIn, BlockPos blockPosIn) {
        return worldIn instanceof ChunkCacheOF cc ? cc.getCombinedLight(blockStateIn, worldIn, blockPosIn) : getPackedLightRaw(worldIn, blockStateIn, blockPosIn);
    }

    public static int getPackedLightRaw(BlockAndTintGetter worldIn, BlockState blockStateIn, BlockPos blockPosIn) {
        return LightCoordsUtil.getLightCoords(LightCoordsUtil.BrightnessGetter.DEFAULT, worldIn, blockStateIn, blockPosIn);
    }
}
