package net.optifine.util;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.optifine.Config;

public class IntegratedServerUtils {
    public static ServerLevel getWorldServer() {
        Minecraft mc = Config.getMinecraft();
        Level world = mc.level;
        if (world == null) {
            return null;
        }

        if (!mc.isLocalServer()) {
            return null;
        }

        IntegratedServer is = mc.getSingleplayerServer();
        if (is == null) {
            return null;
        }

        ResourceKey<Level> wd = world.dimension();
        if (wd == null) {
            return null;
        }

        try {
            return is.getLevel(wd);
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static Entity getEntity(UUID uuid) {
        ServerLevel ws = getWorldServer();
        return ws == null ? null : ws.getEntity(uuid);
    }

    public static BlockEntity getTileEntity(BlockPos pos) {
        ServerLevel ws = getWorldServer();
        if (ws == null) {
            return null;
        }

        ChunkAccess chunk = ws.getChunkSource().getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);
        return chunk == null ? null : chunk.getBlockEntity(pos);
    }
}
