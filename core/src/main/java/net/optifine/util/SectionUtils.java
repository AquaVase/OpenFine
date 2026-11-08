package net.optifine.util;

import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.optifine.ChunkPosComparator;
import net.optifine.SectionPosComparator;

public class SectionUtils {
    public static Collection<SectionRenderDispatcher.RenderSection> getSortedDist(List<SectionRenderDispatcher.RenderSection> listSections, LocalPlayer player) {
        if (listSections.size() <= 1) {
            return listSections;
        }

        SectionPos playerSectionPos = SectionPos.of(player.blockPosition());
        SectionPosComparator comp = new SectionPosComparator(playerSectionPos);
        PriorityQueue<SectionRenderDispatcher.RenderSection> queue = new PriorityQueue<>(listSections.size(), comp);

        for (SectionRenderDispatcher.RenderSection rs : listSections) {
            queue.add(rs);
        }

        return queue;
    }

    public static Collection<SectionRenderDispatcher.RenderSection> getSortedDistDir(
        List<SectionRenderDispatcher.RenderSection> listSections, LocalPlayer player
    ) {
        if (listSections.size() <= 1) {
            return listSections;
        }

        float playerYaw = player.getYRot() + 90.0F;

        while (playerYaw <= -180.0F) {
            playerYaw += 360.0F;
        }

        while (playerYaw > 180.0F) {
            playerYaw -= 360.0F;
        }

        double playerYawRad = playerYaw * (Math.PI / 180.0);
        double playerPitch = player.getXRot();
        double playerPitchRad = playerPitch * (Math.PI / 180.0);
        ChunkPos playerChunkPos = player.chunkPosition();
        ChunkPosComparator comp = new ChunkPosComparator(playerChunkPos.x(), playerChunkPos.z(), playerYawRad, playerPitchRad);
        PriorityQueue<SectionRenderDispatcher.RenderSection> queue = new PriorityQueue<>(listSections.size(), comp);

        for (SectionRenderDispatcher.RenderSection rs : listSections) {
            queue.add(rs);
        }

        return queue;
    }
}
