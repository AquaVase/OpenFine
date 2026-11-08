package net.optifine.shaders;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MipmapStrategy;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.server.packs.resources.ResourceManager;
import net.optifine.util.MetadataUtils;
import org.apache.commons.io.IOUtils;

public class SimpleShaderTexture extends AbstractTexture {
    private String texturePath;
    private long size = 0L;

    public SimpleShaderTexture(String texturePath) {
        this.texturePath = texturePath;
    }

    public void loadTexture(ResourceManager resourceManager) throws IOException {
        this.deleteGlTexture();
        InputStream inputStream = Shaders.getShaderPackResourceStream(this.texturePath);
        if (inputStream == null) {
            throw new FileNotFoundException("Shader texture not found: " + this.texturePath);
        }

        try {
            NativeImage nativeImage = NativeImage.read(inputStream);
            this.size = nativeImage.getSize();
            TextureMetadataSection tms = loadTextureMetadataSection(this.texturePath, new TextureMetadataSection(false, false, MipmapStrategy.AUTO, 0.0F));
            this.texture = RenderSystem.getDevice()
                .createTexture(this.texturePath, 5, GpuFormat.RGBA8_UNORM, nativeImage.getWidth(), nativeImage.getHeight(), 1, 1);
            this.textureView = RenderSystem.getDevice().createTextureView(this.texture);
            nativeImage.uploadTextureSub(0, 0, 0, 0, 0, nativeImage.getWidth(), nativeImage.getHeight(), tms.blur(), tms.clamp(), false, true);
            this.texture.setParentTexture(this);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public static TextureMetadataSection loadTextureMetadataSection(String texturePath, TextureMetadataSection def) {
        String pathMeta = texturePath + ".mcmeta";
        String sectionName = "texture";
        InputStream inMeta = Shaders.getShaderPackResourceStream(pathMeta);
        if (inMeta != null) {
            BufferedReader brMeta = new BufferedReader(new InputStreamReader(inMeta));

            try {
                JsonObject jsonMeta = new JsonParser().parse(brMeta).getAsJsonObject();
                JsonObject jsonMetaTexture = jsonMeta.getAsJsonObject(sectionName);
                if (jsonMetaTexture != null) {
                    TextureMetadataSection meta = MetadataUtils.parseTextureMetadataSection(jsonMetaTexture);
                    if (meta != null) {
                        return meta;
                    }
                }
            } catch (RuntimeException re) {
                SMCLog.warning("Error reading metadata: " + pathMeta);
                SMCLog.warning(re.getClass().getName() + ": " + re.getMessage());
            } finally {
                IOUtils.closeQuietly(brMeta);
                IOUtils.closeQuietly(inMeta);
            }
        }

        return def;
    }

    public long getSize() {
        return this.size;
    }

    @Override
    public String toString() {
        return "Shader: " + this.texturePath + ", " + super.toString();
    }
}
