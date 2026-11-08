package net.optifine.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.optifine.Config;
import net.optifine.DynamicLights;
import net.optifine.SmartAnimations;
import net.optifine.TextureAnimations;
import net.optifine.shaders.Shaders;

public class DebugEntryOF implements DebugScreenEntry {
    @Override
    public void display(DebugScreenDisplayer displayerIn, Level worldIn, LevelChunk chunkIn, LevelChunk serverChunkIn) {
        StringBuilder sb = new StringBuilder(32);
        sb.append("OptiFine_26.2_HD_U_K2_pre5");
        if (Config.isDynamicLights()) {
            sb.append(", DL: ");
            sb.append(String.valueOf(DynamicLights.getCount()));
        }

        sb.append(", A: ");
        TextureAtlas tm = Config.getTextureMapBlocks();
        if (SmartAnimations.isActive()) {
            sb.append(tm.getCountAnimationsActive() + TextureAnimations.getCountAnimationsActive());
            sb.append("/");
        }

        sb.append(tm.getCountAnimations() + TextureAnimations.getCountAnimations());
        displayerIn.addLine(sb.toString());
        sb.setLength(0);
        String shaderPack = Shaders.getShaderPackName();
        if (shaderPack != null) {
            sb.append("SH: ");
            sb.append(shaderPack);
            if (Config.isShadersShadows()) {
                LevelRenderer wr = Minecraft.getInstance().levelRenderer;
                int chunksShadow = wr.getRenderedChunksShadow();
                int entitiesShadow = wr.getCountEntitiesRenderedShadow();
                int tileEntitiesShadow = wr.getCountTileEntitiesRenderedShadow();
                sb.append(", SP C: ");
                sb.append(chunksShadow);
                sb.append(", E: ");
                sb.append(entitiesShadow);
                sb.append("+");
                sb.append(tileEntitiesShadow);
            }

            displayerIn.addLine(sb.toString());
        }
    }

    @Override
    public boolean isAllowed(boolean reducedDebugIn) {
        return true;
    }
}
