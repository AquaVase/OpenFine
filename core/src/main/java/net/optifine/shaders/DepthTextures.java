package net.optifine.shaders;

import com.mojang.blaze3d.opengl.GlStateManager;

public class DepthTextures {
    private final String name;
    private final int[] textures;
    private final boolean separateTarget;
    private int targetTexture;

    public DepthTextures(String name, int capacity, boolean separateTarget) {
        this.name = name;
        this.textures = new int[capacity];
        this.separateTarget = separateTarget;
    }

    public void genTextures() {
        for (int i = 0; i < this.textures.length; i++) {
            this.textures[i] = GlStateManager._genTexture();
        }

        if (this.separateTarget) {
            this.targetTexture = GlStateManager._genTexture();
        }
    }

    public void deleteTextures() {
        for (int i = 0; i < this.textures.length; i++) {
            GlStateManager._deleteTexture(this.textures[i]);
        }

        if (this.separateTarget) {
            GlStateManager._deleteTexture(this.targetTexture);
        }
    }

    public int[] getTextures() {
        return this.textures;
    }

    public int get(int index) {
        return this.textures[index];
    }

    public boolean isSeparateTarget() {
        return this.separateTarget;
    }

    public int getTarget() {
        return this.separateTarget ? this.targetTexture : this.textures[0];
    }

    public void flipTarget() {
        if (this.separateTarget) {
            int tmp = this.targetTexture;
            this.targetTexture = this.textures[0];
            this.textures[0] = tmp;
        }
    }

    public void flipTextures(int index, int index2) {
        int tmp = this.textures[index];
        this.textures[index] = this.textures[index2];
        this.textures[index2] = tmp;
    }
}
