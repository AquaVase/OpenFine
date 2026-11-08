package net.optifine.shaders.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.optifine.gui.GuiButtonOF;

public class GuiButtonDownloadShaders extends GuiButtonOF {
    public GuiButtonDownloadShaders(int buttonID, int xPos, int yPos) {
        super(buttonID, xPos, yPos, 22, 20, "");
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphicsIn, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            this.extractDefaultSprite(graphicsIn);
            Identifier locTexture = new Identifier("optifine/textures/icons.png");
            graphicsIn.blit(RenderPipelines.GUI_TEXTURED, locTexture, this.getX() + 3, this.getY() + 2, 0.0F, 0.0F, 16, 16, 256, 256);
        }
    }
}
