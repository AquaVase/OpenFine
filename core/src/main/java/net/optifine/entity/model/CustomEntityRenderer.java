package net.optifine.entity.model;

import net.minecraft.resources.Identifier;

public class CustomEntityRenderer {
    private String name = null;
    private String basePath = null;
    private Identifier textureLocation = null;
    private CustomModelRenderer[] customModelRenderers = null;
    private float shadowSize = 0.0F;

    public CustomEntityRenderer(String name, String basePath, Identifier textureLocation, CustomModelRenderer[] customModelRenderers, float shadowSize) {
        this.name = name;
        this.basePath = basePath;
        this.textureLocation = textureLocation;
        this.customModelRenderers = customModelRenderers;
        this.shadowSize = shadowSize;
    }

    public String getName() {
        return this.name;
    }

    public String getBasePath() {
        return this.basePath;
    }

    public Identifier getTextureLocation() {
        return this.textureLocation;
    }

    public CustomModelRenderer[] getCustomModelRenderers() {
        return this.customModelRenderers;
    }

    public float getShadowSize() {
        return this.shadowSize;
    }

    @Override
    public String toString() {
        return "name: "
            + this.name
            + ", path: "
            + this.basePath
            + ", texture: "
            + this.textureLocation
            + ", renderers: "
            + this.customModelRenderers.length
            + ", shadowSize: "
            + this.shadowSize;
    }
}
