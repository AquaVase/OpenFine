package net.optifine.render;

import net.minecraft.client.gui.render.GuiRenderer;

public interface GuiRenderListener {
    void addToMeshBegin();

    void traverseStratumBegin(int var1);

    void meshToDraw(GuiRenderer.Draw var1);

    void traverseStratumEnd(int var1);

    void addToMeshEnd();
}
