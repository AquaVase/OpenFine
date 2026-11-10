package net.optifine;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.SectionMesh;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class DynamicLight {
    private Entity entity = null;
    private double offsetY = 0.0;
    private double lastPosX = -2.1474836E9F;
    private double lastPosY = -2.1474836E9F;
    private double lastPosZ = -2.1474836E9F;
    private int lastLightLevel = 0;
    private long timeCheckMs = 0L;
    private LongSet setLitChunkPos = new LongOpenHashSet(8);
    private LongSet setNewChunkPos = new LongOpenHashSet(8);

    public DynamicLight(Entity entity) {
        this.entity = entity;
        this.offsetY = entity.getEyeHeight();
    }

    public void update(LevelRenderer renderGlobal) {
        if (Config.isDynamicLightsFast()) {
            long timeNowMs = System.currentTimeMillis();
            if (timeNowMs < this.timeCheckMs + 500L) {
                return;
            }

            this.timeCheckMs = timeNowMs;
        }

        double posX = this.entity.getX() - 0.5;
        double posY = this.entity.getY() - 0.5 + this.offsetY;
        double posZ = this.entity.getZ() - 0.5;
        int lightLevel = DynamicLights.getLightLevel(this.entity);
        double dx = posX - this.lastPosX;
        double dy = posY - this.lastPosY;
        double dz = posZ - this.lastPosZ;
        double delta = 0.1;
        if (!(Math.abs(dx) <= delta) || !(Math.abs(dy) <= delta) || !(Math.abs(dz) <= delta) || this.lastLightLevel != lightLevel) {
            this.lastPosX = posX;
            this.lastPosY = posY;
            this.lastPosZ = posZ;
            this.lastLightLevel = lightLevel;
            LongSet setNewPos = this.setNewChunkPos;
            setNewPos.clear();
            if (lightLevel > 0) {
                Direction dirX = (Mth.floor(posX) & 15) >= 8 ? Direction.EAST : Direction.WEST;
                Direction dirY = (Mth.floor(posY) & 15) >= 8 ? Direction.UP : Direction.DOWN;
                Direction dirZ = (Mth.floor(posZ) & 15) >= 8 ? Direction.SOUTH : Direction.NORTH;
                long chunkPos = this.getChunkPos(posX, posY, posZ);
                SectionRenderDispatcher.RenderSection chunk = renderGlobal.getRenderChunk(chunkPos);
                long chunkPosX = getChunkPos(chunkPos, dirX);
                SectionRenderDispatcher.RenderSection chunkX = renderGlobal.getRenderChunk(chunkPosX);
                long chunkPosZ = getChunkPos(chunkPos, dirZ);
                SectionRenderDispatcher.RenderSection chunkZ = renderGlobal.getRenderChunk(chunkPosZ);
                long chunkPosXZ = getChunkPos(chunkPosX, dirZ);
                SectionRenderDispatcher.RenderSection chunkXZ = renderGlobal.getRenderChunk(chunkPosXZ);
                long chunkPosY = getChunkPos(chunkPos, dirY);
                SectionRenderDispatcher.RenderSection chunkY = renderGlobal.getRenderChunk(chunkPosY);
                long chunkPosYX = getChunkPos(chunkPosY, dirX);
                SectionRenderDispatcher.RenderSection chunkYX = renderGlobal.getRenderChunk(chunkPosYX);
                long chunkPosYZ = getChunkPos(chunkPosY, dirZ);
                SectionRenderDispatcher.RenderSection chunkYZ = renderGlobal.getRenderChunk(chunkPosYZ);
                long chunkPosYXZ = getChunkPos(chunkPosYX, dirZ);
                SectionRenderDispatcher.RenderSection chunkYXZ = renderGlobal.getRenderChunk(chunkPosYXZ);
                this.updateChunkLight(chunk, this.setLitChunkPos, setNewPos);
                this.updateChunkLight(chunkX, this.setLitChunkPos, setNewPos);
                this.updateChunkLight(chunkZ, this.setLitChunkPos, setNewPos);
                this.updateChunkLight(chunkXZ, this.setLitChunkPos, setNewPos);
                this.updateChunkLight(chunkY, this.setLitChunkPos, setNewPos);
                this.updateChunkLight(chunkYX, this.setLitChunkPos, setNewPos);
                this.updateChunkLight(chunkYZ, this.setLitChunkPos, setNewPos);
                this.updateChunkLight(chunkYXZ, this.setLitChunkPos, setNewPos);
            }

            this.updateLitChunks(renderGlobal);
            this.setNewChunkPos = this.setLitChunkPos;
            this.setLitChunkPos = setNewPos;
        }
    }

    private long getChunkPos(double x, double y, double z) {
        int cx = SectionPos.blockToSectionCoord(Mth.floor(x));
        int cy = SectionPos.blockToSectionCoord(Mth.floor(y));
        int cz = SectionPos.blockToSectionCoord(Mth.floor(z));
        return SectionPos.asLong(cx, cy, cz);
    }

    private static long getChunkPos(long pos, Direction facing) {
        int x = SectionPos.x(pos);
        int y = SectionPos.y(pos);
        int z = SectionPos.z(pos);
        x += facing.getStepX();
        y += facing.getStepY();
        z += facing.getStepZ();
        return SectionPos.asLong(x, y, z);
    }

    private void updateChunkLight(SectionRenderDispatcher.RenderSection renderChunk, LongSet setPrevPos, LongSet setNewPos) {
        if (renderChunk != null) {
            SectionMesh compiledChunk = renderChunk.getSectionMesh();
            if (compiledChunk != null && !compiledChunk.isEmpty()) {
                SectionPos sectionPos = renderChunk.getSectionPosition();
                Config.getLevelExtractor().setSectionDirty(sectionPos.getX(), sectionPos.getY(), sectionPos.getZ());
                renderChunk.setNeedsBackgroundPriorityUpdate(true);
            }

            long pos = renderChunk.getSectionPosition().asLong();
            if (setPrevPos != null) {
                setPrevPos.remove(pos);
            }

            if (setNewPos != null) {
                setNewPos.add(pos);
            }
        }
    }

    public void updateLitChunks(LevelRenderer renderGlobal) {
        for (LongIterator positions = this.setLitChunkPos.iterator(); positions.hasNext();) {
            SectionRenderDispatcher.RenderSection chunkOld = renderGlobal.getRenderChunk(positions.nextLong());
            this.updateChunkLight(chunkOld, null, null);
        }
    }

    public Entity getEntity() {
        return this.entity;
    }

    public double getLastPosX() {
        return this.lastPosX;
    }

    public double getLastPosY() {
        return this.lastPosY;
    }

    public double getLastPosZ() {
        return this.lastPosZ;
    }

    public int getLastLightLevel() {
        return this.lastLightLevel;
    }

    public double getOffsetY() {
        return this.offsetY;
    }

    @Override
    public String toString() {
        return "Entity: " + this.entity + ", offsetY: " + this.offsetY;
    }
}
