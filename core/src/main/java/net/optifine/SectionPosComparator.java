package net.optifine;

import java.util.Comparator;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.SectionPos;

public class SectionPosComparator implements Comparator<SectionRenderDispatcher.RenderSection> {
    private int chunkPosX;
    private int chunkPosY;
    private int chunkPosZ;

    public SectionPosComparator(SectionPos sectionPos) {
        this.chunkPosX = sectionPos.x();
        this.chunkPosY = sectionPos.y();
        this.chunkPosZ = sectionPos.z();
    }

    public int compare(SectionRenderDispatcher.RenderSection rs1, SectionRenderDispatcher.RenderSection rs2) {
        SectionPos sp1 = rs1.getSectionPosition();
        SectionPos sp2 = rs2.getSectionPosition();
        int distSq1 = this.getDistSq(sp1);
        int distSq2 = this.getDistSq(sp2);
        return distSq1 - distSq2;
    }

    public int getDistSq(SectionPos sp) {
        int dx = sp.x() - this.chunkPosX;
        int dy = sp.y() - this.chunkPosY;
        int dz = sp.z() - this.chunkPosZ;
        return dx * dx + dy * dy + dz * dz;
    }
}
