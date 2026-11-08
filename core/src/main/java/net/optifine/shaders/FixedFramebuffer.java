package net.optifine.shaders;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import net.optifine.util.ArrayUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

public class FixedFramebuffer implements IShadersFramebuffer {
    private String name;
    private int width;
    private int height;
    private int[] colorTextures;
    private int[] colorAttachments;
    private boolean depthFilterNearest;
    private boolean depthFilterHardware;
    private int glFramebuffer;
    private int depthTexture;
    private IntBuffer glDrawBuffers;

    public FixedFramebuffer(
        String name, int width, int height, int[] colorTextures, int[] colorAttachments, boolean depthFilterNearest, boolean depthFilterHardware
    ) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.colorTextures = colorTextures;
        this.colorAttachments = colorAttachments;
        this.depthFilterNearest = depthFilterNearest;
        this.depthFilterHardware = depthFilterHardware;
    }

    public void setup() {
        if (this.exists()) {
            this.delete();
        }

        this.glFramebuffer = GL30.glGenFramebuffers();
        RenderSystem.getGlDevice().debugLabels().applyLabelFramebuffer(this.glFramebuffer, this.name + "");
        this.bindFramebuffer();
        GL30.glDrawBuffers(0);
        GL30.glReadBuffer(0);
        this.depthTexture = GL30.glGenTextures();
        GlStateManager._bindTexture(this.depthTexture);
        GL30.glTexParameteri(3553, 10242, 33071);
        GL30.glTexParameteri(3553, 10243, 33071);
        int filter = this.depthFilterNearest ? 9728 : 9729;
        GL30.glTexParameteri(3553, 10241, filter);
        GL30.glTexParameteri(3553, 10240, filter);
        if (this.depthFilterHardware) {
            GL30.glTexParameteri(3553, 34892, 34894);
        }

        GL30.glTexImage2D(3553, 0, 6402, this.width, this.height, 0, 6402, 5126, (FloatBuffer)null);
        GL30.glFramebufferTexture2D(36160, 36096, 3553, this.depthTexture, 0);
        Shaders.checkGLError("FBS " + this.name + " depth");
        Shaders.debugLabels.applyLabelTexture(this.depthTexture, this.name + " depth");

        for (int i = 0; i < this.colorTextures.length; i++) {
            GL30.glFramebufferTexture2D(36160, this.colorAttachments[i], 3553, this.colorTextures[i], 0);
            Shaders.checkGLError("FBS " + this.name + " color");
        }

        GlStateManager._bindTexture(0);
        if (this.colorTextures.length > 0) {
            this.glDrawBuffers = BufferUtils.createIntBuffer(this.colorAttachments.length);

            for (int i = 0; i < this.colorAttachments.length; i++) {
                int att = this.colorAttachments[i];
                this.glDrawBuffers.put(i, att);
            }

            GL30.glDrawBuffers(this.glDrawBuffers);
            GL30.glReadBuffer(0);
        }

        int status = GL30.glCheckFramebufferStatus(36160);
        if (status != 36053) {
            Shaders.printChatAndLogError("[Shaders] Error creating framebuffer: " + this.name + ", status: " + status);
        } else {
            SMCLog.info("Framebuffer created: " + this.name);
        }
    }

    public void bindFramebuffer() {
        GlState.bindFramebuffer(this);
    }

    public void delete() {
        if (this.glFramebuffer != 0) {
            GL30.glDeleteFramebuffers(this.glFramebuffer);
            this.glFramebuffer = 0;
        }

        if (this.depthTexture != 0) {
            GlStateManager._deleteTexture(this.depthTexture);
            this.depthTexture = 0;
        }

        this.glDrawBuffers = null;
    }

    public boolean exists() {
        return this.glFramebuffer != 0;
    }

    @Override
    public int getGlFramebuffer() {
        return this.glFramebuffer;
    }

    @Override
    public DrawBuffers getDrawBuffers() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setDrawBuffers(DrawBuffers drawBuffers) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public String toString() {
        return this.name + ", [" + ArrayUtils.arrayToString(this.colorTextures) + "], [" + ArrayUtils.arrayToString(this.colorAttachments) + "]";
    }
}
