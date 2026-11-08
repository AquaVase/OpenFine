package net.optifine.shaders;

import java.util.Iterator;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.optifine.util.WorldUtils;

public class ShadowUtils {
    public static Iterator<SectionRenderDispatcher.RenderSection> makeShadowChunkIterator(
        ClientLevel world, float partialTicks, Entity viewEntity, int renderDistanceChunks, ViewArea viewFrustum
    ) {
        float shadowRenderDistance = Shaders.getShadowRenderDistance();
        if (!(shadowRenderDistance <= 0.0F) && !(shadowRenderDistance >= (renderDistanceChunks - 1) * 16)) {
            int shadowDistanceChunks = Mth.ceil(shadowRenderDistance / 16.0F) + 1;
            float car = WorldUtils.getCelestialAngleRadians(partialTicks);
            float sunTiltRad = Shaders.sunPathRotation * Mth.deg2Rad;
            float sar = car > Mth.PId2 && car < 3.0F * Mth.PId2 ? car + (float) Math.PI : car;
            float dx = -Mth.sin(sar);
            float dy = Mth.cos(sar) * Mth.cos(sunTiltRad);
            float dz = -Mth.cos(sar) * Mth.sin(sunTiltRad);
            BlockPos posEntity = new BlockPos(Mth.floor(viewEntity.getX()) >> 4, Mth.floor(viewEntity.getY()) >> 4, Mth.floor(viewEntity.getZ()) >> 4);
            BlockPos posStart = posEntity.offset((int)(-dx * shadowDistanceChunks), (int)(-dy * shadowDistanceChunks), (int)(-dz * shadowDistanceChunks));
            BlockPos posEnd = posEntity.offset((int)(dx * renderDistanceChunks), (int)(dy * renderDistanceChunks), (int)(dz * renderDistanceChunks));
            return new IteratorRenderChunks(viewFrustum, posStart, posEnd, shadowDistanceChunks, shadowDistanceChunks);
        } else {
            return viewFrustum.getRenderSectionIterator();
        }
    }
}
