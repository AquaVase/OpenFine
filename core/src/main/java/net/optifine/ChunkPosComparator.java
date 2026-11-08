package net.optifine;

import java.util.Comparator;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.util.Mth;

public class ChunkPosComparator implements Comparator<SectionRenderDispatcher.RenderSection> {
    private int chunkPosX;
    private int chunkPosZ;
    private double yawRad;
    private double pitchNorm;

    public ChunkPosComparator(int chunkPosX, int chunkPosZ, double yawRad, double pitchRad) {
        this.chunkPosX = chunkPosX;
        this.chunkPosZ = chunkPosZ;
        this.yawRad = yawRad;
        this.pitchNorm = 1.0 - Mth.clamp(Math.abs(pitchRad) / (Math.PI / 2), 0.0, 1.0);
    }

    public int compare(SectionRenderDispatcher.RenderSection rs1, SectionRenderDispatcher.RenderSection rs2) {
        int cp1x = rs1.getSectionPosition().x();
        int cp1z = rs1.getSectionPosition().z();
        int cp2x = rs2.getSectionPosition().x();
        int cp2z = rs2.getSectionPosition().z();
        int distSq1 = this.getDistSq(cp1x, cp1z);
        int distSq2 = this.getDistSq(cp2x, cp2z);
        return distSq1 - distSq2;
    }

    private int getDistSq(int cpx, int cpz) {
        int dx = cpx - this.chunkPosX;
        int dz = cpz - this.chunkPosZ;
        int distSq = dx * dx + dz * dz;
        double yaw = Mth.atan2(dz, dx);
        double dYaw = Math.abs(yaw - this.yawRad);
        if (dYaw > Math.PI) {
            dYaw = (Math.PI * 2) - dYaw;
        }

        return (int)(distSq * (0.2 + this.pitchNorm * dYaw));
    }
}
