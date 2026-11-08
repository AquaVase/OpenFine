package net.optifine.model;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.optifine.BetterGrass;
import net.optifine.Config;
import net.optifine.ConnectedTextures;
import net.optifine.NaturalTextures;
import net.optifine.SmartLeaves;
import net.optifine.render.RenderEnv;

public class BlockModelCustomizer {
    private static final List<BakedQuad> NO_QUADS = ImmutableList.of();

    public static List<BlockStateModelPart> getRenderModel(List<BlockStateModelPart> modelIn, BlockState stateIn, RenderEnv renderEnv) {
        if (renderEnv.isSmartLeaves()) {
            modelIn = SmartLeaves.getLeavesModel(modelIn, stateIn);
        }

        return modelIn;
    }

    public static List<BakedQuad> getRenderQuads(
        List<BakedQuad> quads, BlockAndTintGetter worldIn, BlockState stateIn, BlockPos posIn, Direction enumfacing, RenderEnv renderEnv
    ) {
        if (enumfacing != null) {
            if (renderEnv.isSmartLeaves() && SmartLeaves.isSameLeaves(worldIn.getBlockState(posIn.relative(enumfacing)), stateIn)) {
                return NO_QUADS;
            }

            if (!renderEnv.isBreakingAnimation(quads) && Config.isBetterGrass()) {
                quads = BetterGrass.getFaceQuads(worldIn, stateIn, posIn, enumfacing, quads);
            }
        }

        List<BakedQuad> quadsNew = renderEnv.getListQuadsCustomizer();
        quadsNew.clear();

        for (int i = 0; i < quads.size(); i++) {
            BakedQuad quad = quads.get(i);
            BakedQuad[] quadArr = getRenderQuads(quad, worldIn, stateIn, posIn, enumfacing, renderEnv);
            if (i == 0 && quads.size() == 1 && quadArr.length == 1 && quadArr[0] == quad && quad.getQuadEmissive() == null) {
                return quads;
            }

            for (int q = 0; q < quadArr.length; q++) {
                BakedQuad quadSingle = quadArr[q];
                quadsNew.add(quadSingle);
                if (quadSingle.getQuadEmissive() != null) {
                    ChunkSectionLayer layer = quadSingle.materialInfo().layer();
                    renderEnv.getListQuadsOverlay(getEmissiveLayer(layer)).addQuad(quadSingle.getQuadEmissive(), stateIn);
                    renderEnv.setOverlaysRendered(true);
                }
            }
        }

        return quadsNew;
    }

    public static ChunkSectionLayer getEmissiveLayer(ChunkSectionLayer layer) {
        return layer != null && layer != ChunkSectionLayer.SOLID ? layer : ChunkSectionLayer.CUTOUT;
    }

    private static BakedQuad[] getRenderQuads(
        BakedQuad quad, BlockAndTintGetter worldIn, BlockState stateIn, BlockPos posIn, Direction facingIn, RenderEnv renderEnv
    ) {
        if (renderEnv.isBreakingAnimation(quad)) {
            return renderEnv.getArrayQuadsCtm(quad);
        }

        BakedQuad quadOriginal = quad;
        if (Config.isConnectedTextures()) {
            Direction facing = facingIn != null ? facingIn : quad.direction();
            BakedQuad[] quads = ConnectedTextures.getConnectedTexture(worldIn, stateIn, posIn, quad, facing, renderEnv);
            if (quads.length != 1 || quads[0] != quad) {
                return quads;
            }
        }

        if (Config.isNaturalTextures()) {
            quad = NaturalTextures.getNaturalTexture(stateIn, posIn, quad);
            if (quad != quadOriginal) {
                return renderEnv.getArrayQuadsCtm(quad);
            }
        }

        return renderEnv.getArrayQuadsCtm(quad);
    }
}
