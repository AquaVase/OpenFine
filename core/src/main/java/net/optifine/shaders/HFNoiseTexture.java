package net.optifine.shaders;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vulkan.VulkanGpuSampler;
import java.nio.ByteBuffer;
import net.optifine.Config;
import net.optifine.shaders.vulkan.VulkanShaderRuntime;
import net.optifine.util.TextureUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class HFNoiseTexture implements ICustomTexture {
    private int texID;
    private int textureUnit = 15;
    private GpuTexture texture;
    private GpuTextureView textureView;

    public HFNoiseTexture(int width, int height) {
        byte[] image = this.genHFNoiseImage(width, height);
        if (Config.isVulkan()) {
            this.texture = RenderSystem.getDevice().createTexture("HFNoise", 5, GpuFormat.RGBA8_UNORM, width, height, 1, 1);
            try {
                this.textureView = RenderSystem.getDevice().createTextureView(this.texture);
                try (NativeImage pixels = new NativeImage(width, height, false)) {
                    int index = 0;
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            int red = image[index++] & 255;
                            int green = image[index++] & 255;
                            int blue = image[index++] & 255;
                            pixels.setPixelABGR(x, y, 0xFF000000 | blue << 16 | green << 8 | red);
                        }
                    }
                    RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.texture, pixels);
                }
            } catch (RuntimeException | Error e) {
                this.deleteTexture();
                throw e;
            }
            return;
        }
        this.texID = GL11.glGenTextures();
        ByteBuffer data = BufferUtils.createByteBuffer(image.length);
        data.put(image);
        data.flip();
        GlStateManager._bindTexture(this.texID);
        TextureUtils.resetDataUnpacking();
        GL11.glTexImage2D(3553, 0, 6407, width, height, 0, 6407, 5121, data);
        GL11.glTexParameteri(3553, 10242, 10497);
        GL11.glTexParameteri(3553, 10243, 10497);
        GL11.glTexParameteri(3553, 10240, 9729);
        GL11.glTexParameteri(3553, 10241, 9729);
        RenderSystem.getGlDevice().debugLabels().applyLabelTexture(this.texID, "HFNoise");
        GlStateManager._bindTexture(0);
    }

    public int getID() {
        return this.texID;
    }

    @Override
    public void deleteTexture() {
        if (this.textureView != null) {
            this.textureView.close();
            this.textureView = null;
        }
        if (this.texture != null) {
            this.texture.close();
            this.texture = null;
        }
        if (Config.isVulkan()) {
            return;
        }
        GlStateManager._deleteTexture(this.texID);
        this.texID = 0;
    }

    private int random(int seed) {
        seed ^= seed << 13;
        seed ^= seed >> 17;
        return seed ^ seed << 5;
    }

    @Override
    public void bindVulkan() {
        VulkanGpuSampler sampler = (VulkanGpuSampler)RenderSystem.getSamplerCache()
            .getSampler(AddressMode.REPEAT, AddressMode.REPEAT, FilterMode.LINEAR, FilterMode.LINEAR, false);
        VulkanShaderRuntime.getInstance().bindTexture(this.textureUnit, this.textureView, sampler.vkSampler());
    }

    private byte random(int x, int y, int z) {
        int seed = (this.random(x) + this.random(y * 19)) * this.random(z * 23) - z;
        return (byte)(this.random(seed) % 128);
    }

    private byte[] genHFNoiseImage(int width, int height) {
        byte[] image = new byte[width * height * 3];
        int index = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 1; z < 4; z++) {
                    image[index++] = this.random(x, y, z);
                }
            }
        }

        return image;
    }

    @Override
    public int getTextureId() {
        return this.texID;
    }

    @Override
    public int getTextureUnit() {
        return this.textureUnit;
    }
}
