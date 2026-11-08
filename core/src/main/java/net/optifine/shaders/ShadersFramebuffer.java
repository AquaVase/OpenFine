package net.optifine.shaders;

import com.mojang.blaze3d.opengl.GlStateManager;
import java.awt.Dimension;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.optifine.util.CompoundIntKey;
import net.optifine.util.CompoundKey;
import net.optifine.util.DynamicDimension;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class ShadersFramebuffer implements IShadersFramebuffer {
    private String name;
    private int width;
    private int height;
    private int usedColorBuffers;
    private int usedDepthBuffers;
    private int maxDrawBuffers;
    private boolean[] depthFilterNearest;
    private boolean[] depthFilterHardware;
    private boolean[] colorFilterNearest;
    private DynamicDimension[] colorBufferSizes;
    private int[] buffersFormat;
    private int[] colorTextureUnits;
    private int[] depthTextureUnits;
    private int[] colorImageUnits;
    private boolean separateDepthTarget;
    private int glFramebuffer;
    private FlipTextures colorTexturesFlip;
    private DepthTextures depthTextures;
    private final DrawBuffers drawBuffers;
    private DrawBuffers activeDrawBuffers;
    private int[] drawColorTextures;
    private int[] drawColorTexturesMap;
    private boolean[] dirtyColorTextures;
    private Map<CompoundKey, FixedFramebuffer> fixedFramebuffers = new HashMap<>();

    public ShadersFramebuffer(
        String name,
        int width,
        int height,
        int usedColorBuffers,
        int usedDepthBuffers,
        int maxDrawBuffers,
        boolean[] depthFilterNearest,
        boolean[] depthFilterHardware,
        boolean[] colorFilterNearest,
        DynamicDimension[] colorBufferSizes,
        int[] buffersFormat,
        int[] colorTextureUnits,
        int[] depthTextureUnits,
        int[] colorImageUnits,
        boolean separateDepthTarget,
        DrawBuffers drawBuffers
    ) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.usedColorBuffers = usedColorBuffers;
        this.usedDepthBuffers = usedDepthBuffers;
        this.maxDrawBuffers = maxDrawBuffers;
        this.depthFilterNearest = depthFilterNearest;
        this.depthFilterHardware = depthFilterHardware;
        this.colorFilterNearest = colorFilterNearest;
        this.colorBufferSizes = colorBufferSizes;
        this.buffersFormat = buffersFormat;
        this.colorTextureUnits = colorTextureUnits;
        this.depthTextureUnits = depthTextureUnits;
        this.colorImageUnits = colorImageUnits;
        this.drawBuffers = drawBuffers;
        this.separateDepthTarget = separateDepthTarget;
    }

    public void setup() {
        if (this.exists()) {
            this.delete();
        }

        this.colorTexturesFlip = new FlipTextures(this.name + "ColorTexturesFlip", this.usedColorBuffers);
        this.depthTextures = new DepthTextures(this.name + "DepthTextures", this.usedDepthBuffers, this.separateDepthTarget);
        this.drawColorTextures = new int[this.usedColorBuffers];
        this.drawColorTexturesMap = new int[this.usedColorBuffers];
        this.dirtyColorTextures = new boolean[this.maxDrawBuffers];
        Arrays.fill(this.drawColorTextures, 0);
        Arrays.fill(this.drawColorTexturesMap, -1);
        Arrays.fill(this.dirtyColorTextures, false);

        for (int i = 0; i < this.drawBuffers.limit(); i++) {
            this.drawBuffers.put(i, 36064 + i);
        }

        this.glFramebuffer = GL30.glGenFramebuffers();
        this.bindFramebuffer();
        Shaders.debugLabels.applyLabelFramebuffer(this.glFramebuffer, this.name + "");
        GL30.glDrawBuffers(0);
        GL30.glReadBuffer(0);
        this.depthTextures.genTextures();
        this.colorTexturesFlip.clear().limit(this.usedColorBuffers).genTextures();
        this.colorTexturesFlip.position(0);

        for (int i = 0; i < this.usedDepthBuffers; i++) {
            GlStateManager._bindTexture(this.depthTextures.get(i));
            GL30.glTexParameteri(3553, 10242, 33071);
            GL30.glTexParameteri(3553, 10243, 33071);
            int filter = this.depthFilterNearest[i] ? 9728 : 9729;
            GL30.glTexParameteri(3553, 10241, filter);
            GL30.glTexParameteri(3553, 10240, filter);
            if (this.depthFilterHardware[i]) {
                GL30.glTexParameteri(3553, 34892, 34894);
            }

            GL30.glTexImage2D(3553, 0, 36012, this.width, this.height, 0, 6402, 5126, (FloatBuffer)null);
            GL30.glTexParameteriv(3553, 36422, new int[]{6403, 6403, 6403, 1});
            Shaders.debugLabels.applyLabelTexture(this.depthTextures.get(i), this.name + " depth" + i);
        }

        if (this.depthTextures.isSeparateTarget()) {
            int i = 0;
            GlStateManager._bindTexture(this.depthTextures.getTarget());
            GL30.glTexParameteri(3553, 10242, 33071);
            GL30.glTexParameteri(3553, 10243, 33071);
            int filter = this.depthFilterNearest[i] ? 9728 : 9729;
            GL30.glTexParameteri(3553, 10241, filter);
            GL30.glTexParameteri(3553, 10240, filter);
            if (this.depthFilterHardware[i]) {
                GL30.glTexParameteri(3553, 34892, 34894);
            }

            GL30.glTexImage2D(3553, 0, 36012, this.width, this.height, 0, 6402, 5126, (FloatBuffer)null);
            GL30.glTexParameteriv(3553, 36422, new int[]{6403, 6403, 6403, 1});
            Shaders.debugLabels.applyLabelTexture(this.depthTextures.getTarget(), this.name + " depthTarget" + i);
        }

        this.setFramebufferTexture2D(36160, 36096, 3553, this.depthTextures.getTarget(), 0);
        Shaders.checkGLError("FBS " + this.name + " depth");

        for (int i = 0; i < this.usedColorBuffers; i++) {
            GlStateManager._bindTexture(this.colorTexturesFlip.getA(i));
            GL30.glTexParameteri(3553, 10242, 33071);
            GL30.glTexParameteri(3553, 10243, 33071);
            int filter = this.colorFilterNearest[i] ? 9728 : 9729;
            GL30.glTexParameteri(3553, 10241, filter);
            GL30.glTexParameteri(3553, 10240, filter);
            Dimension dim = this.colorBufferSizes[i] != null
                ? this.colorBufferSizes[i].getDimension(this.width, this.height)
                : new Dimension(this.width, this.height);
            GL30.glTexImage2D(3553, 0, this.buffersFormat[i], dim.width, dim.height, 0, Shaders.getPixelFormat(this.buffersFormat[i]), 33639, (ByteBuffer)null);
            this.setFramebufferTexture2D(36160, 36064 + i, 3553, this.colorTexturesFlip.getA(i), 0);
            Shaders.checkGLError("FBS " + this.name + " colorA");
            Shaders.debugLabels.applyLabelTexture(this.colorTexturesFlip.getA(i), this.name + " color" + i + "/A");
        }

        for (int i = 0; i < this.usedColorBuffers; i++) {
            GlStateManager._bindTexture(this.colorTexturesFlip.getB(i));
            GL30.glTexParameteri(3553, 10242, 33071);
            GL30.glTexParameteri(3553, 10243, 33071);
            int filter = this.colorFilterNearest[i] ? 9728 : 9729;
            GL30.glTexParameteri(3553, 10241, filter);
            GL30.glTexParameteri(3553, 10240, filter);
            Dimension dim = this.colorBufferSizes[i] != null
                ? this.colorBufferSizes[i].getDimension(this.width, this.height)
                : new Dimension(this.width, this.height);
            GL30.glTexImage2D(3553, 0, this.buffersFormat[i], dim.width, dim.height, 0, Shaders.getPixelFormat(this.buffersFormat[i]), 33639, (ByteBuffer)null);
            Shaders.checkGLError("FBS " + this.name + " colorB");
            Shaders.debugLabels.applyLabelTexture(this.colorTexturesFlip.getB(i), this.name + " color" + i + "/B");
        }

        GlStateManager._bindTexture(0);
        if (this.usedColorBuffers > 0) {
            this.setDrawBuffers(this.drawBuffers);
            GL30.glReadBuffer(0);
        }

        int status = GL30.glCheckFramebufferStatus(36160);
        if (status != 36053) {
            Shaders.printChatAndLogError("[Shaders] Error creating framebuffer: " + this.name + ", status: " + status);
        } else {
            SMCLog.info("Framebuffer created: " + this.name);
        }
    }

    public void delete() {
        if (this.glFramebuffer != 0) {
            GL30.glDeleteFramebuffers(this.glFramebuffer);
            this.glFramebuffer = 0;
        }

        if (this.colorTexturesFlip != null) {
            this.colorTexturesFlip.deleteTextures();
            this.colorTexturesFlip = null;
        }

        if (this.depthTextures != null) {
            this.depthTextures.deleteTextures();
            this.depthTextures = null;
        }

        this.drawBuffers.position(0).fill(0);

        for (FixedFramebuffer ff : this.fixedFramebuffers.values()) {
            ff.delete();
        }

        this.fixedFramebuffers.clear();
    }

    public String getName() {
        return this.name;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    @Override
    public int getGlFramebuffer() {
        return this.glFramebuffer;
    }

    public boolean exists() {
        return this.glFramebuffer != 0;
    }

    public void bindFramebuffer() {
        GlState.bindFramebuffer(this);
    }

    public void setColorTextures(boolean main) {
        for (int i = 0; i < this.usedColorBuffers; i++) {
            this.setFramebufferTexture2D(36160, 36064 + i, 3553, this.colorTexturesFlip.get(main, i), 0);
        }
    }

    public void setDepthTexture() {
        this.setDepthTexture(this.depthTextures.getTarget());
    }

    public void unbindDepthTexture() {
        this.setDepthTexture(0);
    }

    public void setDepthTexture(int depthTex) {
        this.setFramebufferTexture2D(36160, 36096, 3553, depthTex, 0);
    }

    public void flipDepthTextures(int index, int index2) {
        this.depthTextures.flipTextures(index, index2);
    }

    public void flipDepthTarget() {
        this.depthTextures.flipTarget();
    }

    public boolean isSeparateDepthTarget() {
        return this.separateDepthTarget;
    }

    public void setColorBuffersFiltering(int minFilter, int magFilter) {
        GlStateManager._activeTexture(33984);

        for (int i = 0; i < this.usedColorBuffers; i++) {
            GlStateManager._bindTexture(this.colorTexturesFlip.getA(i));
            GL11.glTexParameteri(3553, 10241, minFilter);
            GL11.glTexParameteri(3553, 10240, magFilter);
            GlStateManager._bindTexture(this.colorTexturesFlip.getB(i));
            GL11.glTexParameteri(3553, 10241, minFilter);
            GL11.glTexParameteri(3553, 10240, magFilter);
        }

        GlStateManager._bindTexture(0);
    }

    public void setFramebufferTexture2D(int target, int attachment, int texTarget, int texture, int level) {
        int colorIndex = attachment - 36064;
        if (this.isColorBufferIndex(colorIndex)) {
            if (this.colorBufferSizes[colorIndex] != null) {
                if (this.isColorExtendedIndex(colorIndex)) {
                    return;
                }

                texture = 0;
            }

            this.drawColorTextures[colorIndex] = texture;
            if (colorIndex >= this.maxDrawBuffers) {
                int indexMapped = this.drawColorTexturesMap[colorIndex];
                if (!this.isDrawBufferIndex(indexMapped)) {
                    return;
                }

                attachment = 36064 + indexMapped;
            }
        }

        this.bindFramebuffer();
        GL30.glFramebufferTexture2D(target, attachment, texTarget, texture, level);
    }

    public boolean isColorBufferIndex(int index) {
        return index >= 0 && index < this.usedColorBuffers;
    }

    public boolean isColorExtendedIndex(int index) {
        return index >= this.maxDrawBuffers && index < this.usedColorBuffers;
    }

    public boolean isDrawBufferIndex(int index) {
        return index >= 0 && index < this.maxDrawBuffers;
    }

    private void setDrawColorTexturesMap(int[] newColorTexturesMap) {
        this.bindFramebuffer();

        for (int i = 0; i < this.maxDrawBuffers; i++) {
            if (this.dirtyColorTextures[i]) {
                int texture = this.drawColorTextures[i];
                GL30.glFramebufferTexture2D(36160, 36064 + i, 3553, texture, 0);
                this.dirtyColorTextures[i] = false;
            }
        }

        this.drawColorTexturesMap = newColorTexturesMap;

        for (int i = this.maxDrawBuffers; i < this.drawColorTexturesMap.length; i++) {
            int ai = this.drawColorTexturesMap[i];
            if (ai >= 0) {
                int texture = this.drawColorTextures[i];
                GL30.glFramebufferTexture2D(36160, 36064 + ai, 3553, texture, 0);
                this.dirtyColorTextures[ai] = true;
            }
        }
    }

    @Override
    public void setDrawBuffers(DrawBuffers drawBuffersIn) {
        if (drawBuffersIn == null) {
            drawBuffersIn = Shaders.drawBuffersNone;
        }

        this.setDrawColorTexturesMap(drawBuffersIn.getAttachmentMappings());
        this.activeDrawBuffers = drawBuffersIn;
        this.bindFramebuffer();
        GL30.glDrawBuffers(drawBuffersIn.getGlDrawBuffers());
        Shaders.checkGLError("setDrawBuffers");
    }

    public void setDrawBuffers() {
        this.setDrawBuffers(this.drawBuffers);
    }

    @Override
    public DrawBuffers getDrawBuffers() {
        return this.activeDrawBuffers;
    }

    public void bindDepthTextures(int[] depthTextureImageUnits) {
        for (int i = 0; i < this.usedDepthBuffers; i++) {
            GlStateManager._activeTexture(33984 + depthTextureImageUnits[i]);
            GlStateManager._bindTexture(this.depthTextures.get(i));
        }

        GlStateManager._activeTexture(33984);
    }

    public void bindColorTextures(int startColorBuffer) {
        for (int i = startColorBuffer; i < this.usedColorBuffers; i++) {
            GlStateManager._activeTexture(33984 + this.colorTextureUnits[i]);
            GlStateManager._bindTexture(this.colorTexturesFlip.getA(i));
            this.bindColorImage(i, true);
        }
    }

    public void bindColorImages(boolean main) {
        if (this.colorImageUnits != null) {
            for (int i = 0; i < this.usedColorBuffers; i++) {
                this.bindColorImage(i, main);
            }
        }
    }

    public void bindColorImage(int index, boolean main) {
        if (this.colorImageUnits != null) {
            if (index >= 0 && index < this.colorImageUnits.length) {
                int imageFormat = Shaders.getImageFormat(this.buffersFormat[index]);
                GlStateManager.bindImageTexture(this.colorImageUnits[index], this.colorTexturesFlip.get(main, index), 0, false, 0, 35002, imageFormat);
            }

            GlStateManager._activeTexture(33984);
        }
    }

    public void generateDepthMipmaps(boolean[] depthMipmapEnabled) {
        for (int i = 0; i < this.usedDepthBuffers; i++) {
            if (depthMipmapEnabled[i]) {
                GlStateManager._activeTexture(33984 + this.depthTextureUnits[i]);
                GlStateManager._bindTexture(this.depthTextures.get(i));
                GL30.glGenerateMipmap(3553);
                GL30.glTexParameteri(3553, 10241, this.depthFilterNearest[i] ? 9984 : 9987);
            }
        }

        GlStateManager._activeTexture(33984);
    }

    public void generateColorMipmaps(boolean main, boolean[] colorMipmapEnabled) {
        for (int i = 0; i < this.usedColorBuffers; i++) {
            if (colorMipmapEnabled[i]) {
                GlStateManager._activeTexture(33984 + this.colorTextureUnits[i]);
                GlStateManager._bindTexture(this.colorTexturesFlip.get(main, i));
                GL30.glGenerateMipmap(3553);
                GL30.glTexParameteri(3553, 10241, this.colorFilterNearest[i] ? 9984 : 9987);
            }
        }

        GlStateManager._activeTexture(33984);
    }

    public void genCompositeMipmap(int compositeMipmapSetting) {
        if (Shaders.hasGlGenMipmap) {
            for (int i = 0; i < this.usedColorBuffers; i++) {
                if ((compositeMipmapSetting & 1 << i) != 0) {
                    GlStateManager._activeTexture(33984 + this.colorTextureUnits[i]);
                    GL30.glTexParameteri(3553, 10241, 9987);
                    GL30.glGenerateMipmap(3553);
                }
            }

            GlStateManager._activeTexture(33984);
        }
    }

    public void flipColorTextures(boolean[] toggleColorTextures) {
        for (int i = 0; i < this.colorTexturesFlip.limit(); i++) {
            if (toggleColorTextures[i]) {
                this.flipColorTexture(i);
            }
        }
    }

    public void flipColorTexture(int index) {
        this.colorTexturesFlip.flip(index);
        GlStateManager._activeTexture(33984 + this.colorTextureUnits[index]);
        GlStateManager._bindTexture(this.colorTexturesFlip.getA(index));
        this.bindColorImage(index, true);
        this.setFramebufferTexture2D(36160, 36064 + index, 3553, this.colorTexturesFlip.getB(index), 0);
        GlStateManager._activeTexture(33984);
    }

    public void clearColorBuffers(boolean[] buffersClear, Vector4f[] clearColors) {
        for (int i = 0; i < this.usedColorBuffers; i++) {
            if (buffersClear[i]) {
                Vector4f col = clearColors[i];
                if (col != null) {
                    GL30.glClearColor(col.x(), col.y(), col.z(), col.w());
                }

                if (this.colorBufferSizes[i] != null) {
                    if (this.colorTexturesFlip.isChanged(i)) {
                        this.clearColorBufferFixedSize(i, false);
                    }

                    this.clearColorBufferFixedSize(i, true);
                } else {
                    if (this.colorTexturesFlip.isChanged(i)) {
                        this.setFramebufferTexture2D(36160, 36064 + i, 3553, this.colorTexturesFlip.getB(i), 0);
                        this.setDrawBuffers(Shaders.drawBuffersColorAtt[i]);
                        GL30.glClear(16384);
                        this.setFramebufferTexture2D(36160, 36064 + i, 3553, this.colorTexturesFlip.getA(i), 0);
                    }

                    this.setDrawBuffers(Shaders.drawBuffersColorAtt[i]);
                    GL30.glClear(16384);
                }
            }
        }
    }

    private void clearColorBufferFixedSize(int i, boolean main) {
        Dimension dim = this.colorBufferSizes[i].getDimension(this.width, this.height);
        if (dim != null) {
            FixedFramebuffer ff = this.getFixedFramebuffer(dim.width, dim.height, Shaders.drawBuffersColorAtt[i], main);
            ff.bindFramebuffer();
            GL30.glClear(16384);
        }
    }

    public void clearDepthBuffer(Vector4f col) {
        this.setFramebufferTexture2D(36160, 36096, 3553, this.depthTextures.getTarget(), 0);
        GL30.glClearColor(col.x(), col.y(), col.z(), col.w());
        GlStateManager._depthMask(true);
        GL30.glClear(256);
    }

    @Override
    public String toString() {
        return this.name
            + ", width: "
            + this.width
            + ", height: "
            + this.height
            + ", usedColorBuffers: "
            + this.usedColorBuffers
            + ", usedDepthBuffers: "
            + this.usedDepthBuffers
            + ", maxDrawBuffers: "
            + this.maxDrawBuffers
            + ", glFramebuffer: "
            + this.glFramebuffer;
    }

    public FixedFramebuffer getFixedFramebuffer(int width, int height, DrawBuffers dbs, boolean main) {
        IntBuffer glDbs = dbs.getGlDrawBuffers();
        int dbsLen = dbs.limit();
        int[] glTexs = new int[dbsLen];
        int[] glAtts = new int[dbsLen];

        for (int i = 0; i < glTexs.length; i++) {
            int att = dbs.get(i);
            int ix = att - 36064;
            if (this.isColorBufferIndex(ix)) {
                glTexs[i] = this.colorTexturesFlip.get(main, ix);
                glAtts[i] = glDbs.get(i);
            }
        }

        CompoundKey key = new CompoundKey(new CompoundIntKey(glTexs), new CompoundIntKey(glAtts));
        FixedFramebuffer ff = this.fixedFramebuffers.get(key);
        if (ff == null) {
            String fixedName = this.name + "/" + width + "x" + height;
            ff = new FixedFramebuffer(fixedName, width, height, glTexs, glAtts, this.depthFilterNearest[0], this.depthFilterHardware[0]);
            ff.setup();
            this.fixedFramebuffers.put(key, ff);
        }

        return ff;
    }
}
