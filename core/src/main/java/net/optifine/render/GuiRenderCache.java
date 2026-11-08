package net.optifine.render;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.optifine.util.HashMapMap;

public class GuiRenderCache implements GuiRenderListener {
    private GuiRenderer guiRenderer;
    private GuiRenderState renderState;
    private long cacheTimeMs;
    private long updateTimeMs;
    private int cacheStratum;
    private boolean renderCached;
    private boolean addToMesh;
    private int stratum;
    private List<GuiRenderer.Draw> meshes = new ArrayList<>();
    private static HashMapMap<PrimitiveTopology, VertexFormat, BufferBuilder> mapBufferBuilders = new HashMapMap<>();

    public GuiRenderCache(GuiRenderer guiRenderer, long cacheTimeMs) {
        this.guiRenderer = guiRenderer;
        this.renderState = guiRenderer.getRenderState();
        this.cacheTimeMs = cacheTimeMs;
        this.cacheStratum = -1;
    }

    public boolean drawCached(GuiGraphicsExtractor graphicsIn) {
        if (!this.checkGraphics(graphicsIn)) {
            return false;
        }

        this.cacheStratum = this.renderState.getStratum();
        this.renderState.addFrameListener(this);
        this.renderCached = System.currentTimeMillis() < this.updateTimeMs;
        return this.renderCached;
    }

    public void startRender(GuiGraphicsExtractor graphicsIn) {
        if (this.checkGraphics(graphicsIn)) {
            for (GuiRenderer.Draw bb : this.meshes) {
                ;
            }

            this.meshes.clear();

            for (BufferBuilder bb : mapBufferBuilders.values()) {
                bb.clear();
            }
        }
    }

    public void stopRender(GuiGraphicsExtractor graphicsIn) {
        if (this.checkGraphics(graphicsIn)) {
            this.updateTimeMs = System.currentTimeMillis() + this.cacheTimeMs;
        }
    }

    private boolean checkGraphics(GuiGraphicsExtractor graphicsIn) {
        return graphicsIn.getGuiRenderState() == this.renderState;
    }

    @Override
    public void addToMeshBegin() {
        this.addToMesh = true;
    }

    @Override
    public void traverseStratumBegin(int stratumIn) {
        if (this.addToMesh) {
            if (stratumIn == this.cacheStratum) {
                this.guiRenderer.flushMesh();
            }

            this.stratum = stratumIn;
            if (this.stratum == this.cacheStratum) {
                if (this.renderCached) {
                    this.guiRenderer.addMeshesToDraw(this.meshes);
                }
            }
        }
    }

    @Override
    public void meshToDraw(GuiRenderer.Draw mesh) {
        if (this.addToMesh) {
            if (this.stratum == this.cacheStratum) {
                if (!this.renderCached) {
                    ;
                }
            }
        }
    }

    @Override
    public void traverseStratumEnd(int stratumIn) {
        if (this.addToMesh) {
            if (stratumIn == this.cacheStratum) {
                this.guiRenderer.flushMesh();
            }

            this.stratum = -1;
        }
    }

    @Override
    public void addToMeshEnd() {
        this.addToMesh = false;
    }
}
