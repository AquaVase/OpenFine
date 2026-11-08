package net.optifine.shaders;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.optifine.util.TextureUtils;

public class CustomTexture implements ICustomTexture {
    private int textureUnit = -1;
    private String path = null;
    private AbstractTexture texture = null;

    public CustomTexture(int textureUnit, String path, AbstractTexture texture) {
        this.textureUnit = textureUnit;
        this.path = path;
        this.texture = texture;
    }

    @Override
    public int getTextureUnit() {
        return this.textureUnit;
    }

    public String getPath() {
        return this.path;
    }

    public AbstractTexture getTexture() {
        return this.texture;
    }

    @Override
    public int getTextureId() {
        return this.texture.getGlTextureId();
    }

    @Override
    public void deleteTexture() {
        TextureUtils.releaseTextureId(this.texture.getGlTextureId());
    }

    @Override
    public String toString() {
        return "textureUnit: " + this.textureUnit + ", path: " + this.path + ", glTextureId: " + this.getTextureId();
    }
}
