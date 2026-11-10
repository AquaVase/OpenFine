package net.optifine.shaders;

import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vulkan.VulkanGpuSampler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.optifine.shaders.vulkan.VulkanShaderRuntime;

public class CustomTextureLocation implements ICustomTexture {
    private int textureUnit = -1;
    private Identifier location;
    private int variant = 0;
    private AbstractTexture texture;
    public static final int VARIANT_BASE = 0;
    public static final int VARIANT_NORMAL = 1;
    public static final int VARIANT_SPECULAR = 2;

    public CustomTextureLocation(int textureUnit, Identifier location, int variant) {
        this.textureUnit = textureUnit;
        this.location = location;
        this.variant = variant;
    }

    public AbstractTexture getTexture() {
        if (this.texture == null) {
            TextureManager textureManager = Minecraft.getInstance().getTextureManager();
            this.texture = textureManager.getTexture(this.location);
            if (this.texture == null) {
                this.texture = new SimpleTexture(this.location);
                textureManager.register(this.location, this.texture);
                this.texture = textureManager.getTexture(this.location);
            }
        }

        return this.texture;
    }

    public void reloadTexture() {
        this.texture = null;
    }

    @Override
    public void bindVulkan() {
        AbstractTexture texture = this.getTexture();
        VulkanShaderRuntime runtime = VulkanShaderRuntime.getInstance();
        GpuTextureView view = texture.getTextureView();
        if (this.variant == VARIANT_NORMAL || this.variant == VARIANT_SPECULAR) {
            MultiTexID multiTex = texture.getMultiTexID();
            view = runtime.getTextureView(this.variant == VARIANT_NORMAL ? multiTex.normTex : multiTex.specTex);
        }
        runtime.bindTexture(this.textureUnit, view, ((VulkanGpuSampler)texture.getSampler()).vkSampler());
    }

    @Override
    public int getTextureId() {
        AbstractTexture tex = this.getTexture();
        if (this.variant != 0 && tex instanceof AbstractTexture at) {
            MultiTexID mtid = at.getMultiTexID();
            if (mtid != null) {
                if (this.variant == 1) {
                    return mtid.norm;
                }

                if (this.variant == 2) {
                    return mtid.spec;
                }
            }
        }

        return tex.getGlTextureId();
    }

    @Override
    public int getTextureUnit() {
        return this.textureUnit;
    }

    @Override
    public void deleteTexture() {
    }

    @Override
    public String toString() {
        return "textureUnit: "
            + this.textureUnit
            + ", location: "
            + this.location
            + ", glTextureId: "
            + (this.texture != null ? this.texture.getGlTextureId() : "");
    }
}
