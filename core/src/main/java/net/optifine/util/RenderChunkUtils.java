package net.optifine.util;

import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class RenderChunkUtils {
    public static int getCountBlocks(SectionRenderDispatcher.RenderSection renderChunk) {
        LevelChunkSection[] ebss = renderChunk.getChunk().getSections();
        if (ebss == null) {
            return 0;
        }

        int indexEbs = renderChunk.getPosition().getY() - renderChunk.getWorld().getMinY() >> 4;
        LevelChunkSection ebs = ebss[indexEbs];
        return ebs == null ? 0 : ebs.getBlockRefCount();
    }

    public static double getRelativeBufferSize(SectionRenderDispatcher.RenderSection renderChunk) {
        int blockCount = getCountBlocks(renderChunk);
        return getRelativeBufferSize(blockCount);
    }

    public static double getRelativeBufferSize(int blockCount) {
        double countRel = blockCount / 4096.0;
        countRel *= 0.995;
        double weight = countRel * 2.0 - 1.0;
        weight = Mth.clamp(weight, -1.0, 1.0);
        return Math.sqrt(1.0 - weight * weight);
    }
}
