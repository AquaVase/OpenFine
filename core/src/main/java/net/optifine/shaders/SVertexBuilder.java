package net.optifine.shaders;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.world.level.block.state.BlockState;
import net.optifine.Config;
import net.optifine.render.VertexPosition;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL20;

public class SVertexBuilder {
    private VertexFormat vertexFormat;
    private int vertexSize;
    private int offsetNormal;
    private int offsetUV;
    private boolean hasNormal;
    private boolean hasUV;
    private long[] entityData = new long[10];
    private int entityDataIndex = 0;

    public SVertexBuilder() {
        this.entityData[this.entityDataIndex] = 0L;
    }

    public void pushEntity(long data) {
        this.entityDataIndex++;
        this.entityData[this.entityDataIndex] = data;
    }

    public void popEntity() {
        this.entityData[this.entityDataIndex] = 0L;
        this.entityDataIndex--;
    }

    public static void pushEntity(BlockState blockState, VertexConsumer ivb) {
        if (ivb instanceof BufferBuilder wrr) {
            int blockId = BlockAliases.getAliasBlockId(blockState);
            int metadata = BlockAliases.getAliasMetadata(blockState);
            int renderType = BlockAliases.getRenderType(blockState);
            int dataLo = ((renderType & 65535) << 16) + (blockId & 65535);
            int dataHi = metadata & 65535;
            wrr.sVertexBuilder.pushEntity(((long)dataHi << 32) + dataLo);
        }
    }

    public static void popEntity(VertexConsumer ivb) {
        if (ivb instanceof BufferBuilder wrr) {
            wrr.sVertexBuilder.popEntity();
        }
    }

    public static boolean popEntity(boolean value, BufferBuilder wrr) {
        wrr.sVertexBuilder.popEntity();
        return value;
    }

    public static void endSetVertexFormat(BufferBuilder wrr) {
        SVertexBuilder svb = wrr.sVertexBuilder;
        VertexFormat vf = wrr.getVertexFormat();
        svb.vertexFormat = vf;
        svb.vertexSize = vf.getVertexSize() / 4;
        svb.hasNormal = vf.hasNormal();
        svb.hasUV = vf.hasUV(0);
        svb.offsetNormal = svb.hasNormal ? vf.getNormalOffset() / 4 : 0;
        svb.offsetUV = svb.hasUV ? vf.getUvOffsetById(0) / 4 : 0;
    }

    public VertexFormat getVertexFormat() {
        return this.vertexFormat;
    }

    public static void beginAddVertex(BufferBuilder wrr) {
        if (wrr.getVertexCount() == 0) {
            endSetVertexFormat(wrr);
        }
    }

    public static void endAddVertex(BufferBuilder wrr) {
        if (!Config.isMinecraftThread() || !ItemFeatureRenderer.isRenderItemGui() && !GuiRenderer.isRenderGui()) {
            SVertexBuilder svb = wrr.sVertexBuilder;
            if (svb.vertexSize == 18) {
                if (wrr.getDrawMode() == PrimitiveTopology.QUADS && wrr.getVertexCount() % 4 == 0) {
                    svb.calcNormal(wrr, wrr.getBufferIntSize() - 4 * svb.vertexSize);
                }

                long eData = svb.entityData[svb.entityDataIndex];
                int pos = wrr.getBufferIntSize() - 18 + 13;
                pos += wrr.getIntStartPosition();
                wrr.getIntBuffer().put(pos, (int)eData);
                wrr.getIntBuffer().put(pos + 1, (int)(eData >> 32));
            }
        }
    }

    public static void beginAddVertexData(BufferBuilder wrr, int[] data) {
        if (wrr.getVertexCount() == 0) {
            endSetVertexFormat(wrr);
        }

        SVertexBuilder svb = wrr.sVertexBuilder;
        if (svb.vertexSize == 18) {
            long eData = svb.entityData[svb.entityDataIndex];

            for (int pos = 13; pos + 1 < data.length; pos += 18) {
                data[pos] = (int)eData;
                data[pos + 1] = (int)(eData >> 32);
            }
        }
    }

    public static void beginAddVertexData(BufferBuilder wrr, ByteBuffer byteBuffer) {
        if (wrr.getVertexCount() == 0) {
            endSetVertexFormat(wrr);
        }

        SVertexBuilder svb = wrr.sVertexBuilder;
        if (svb.vertexSize == 18) {
            long eData = svb.entityData[svb.entityDataIndex];
            int dataLengthInt = byteBuffer.limit() / 4;

            for (int posInt = 13; posInt + 1 < dataLengthInt; posInt += 18) {
                int dataInt0 = (int)eData;
                int dataInt1 = (int)(eData >> 32);
                byteBuffer.putInt(posInt * 4, dataInt0);
                byteBuffer.putInt((posInt + 1) * 4, dataInt1);
            }
        }
    }

    public static void endAddVertexData(BufferBuilder wrr, int vertexCount) {
        SVertexBuilder svb = wrr.sVertexBuilder;
        if (svb.vertexSize == 18 && wrr.getDrawMode() == PrimitiveTopology.QUADS && wrr.getVertexCount() % 4 == 0) {
            endSetVertexFormat(wrr);
            int indexEnd = vertexCount * svb.vertexSize;

            for (int index = 0; index < indexEnd; index += svb.vertexSize * 4) {
                svb.calcNormal(wrr, index);
            }
        }
    }

    public void calcNormal(BufferBuilder wrr, int baseIndex) {
        baseIndex += wrr.getIntStartPosition();
        FloatBuffer floatBuffer = wrr.getFloatBuffer();
        IntBuffer intBuffer = wrr.getIntBuffer();
        float v0x = floatBuffer.get(baseIndex + 0 * this.vertexSize);
        float v0y = floatBuffer.get(baseIndex + 0 * this.vertexSize + 1);
        float v0z = floatBuffer.get(baseIndex + 0 * this.vertexSize + 2);
        float v0u = floatBuffer.get(baseIndex + 0 * this.vertexSize + this.offsetUV);
        float v0v = floatBuffer.get(baseIndex + 0 * this.vertexSize + this.offsetUV + 1);
        float v1x = floatBuffer.get(baseIndex + 1 * this.vertexSize);
        float v1y = floatBuffer.get(baseIndex + 1 * this.vertexSize + 1);
        float v1z = floatBuffer.get(baseIndex + 1 * this.vertexSize + 2);
        float v1u = floatBuffer.get(baseIndex + 1 * this.vertexSize + this.offsetUV);
        float v1v = floatBuffer.get(baseIndex + 1 * this.vertexSize + this.offsetUV + 1);
        float v2x = floatBuffer.get(baseIndex + 2 * this.vertexSize);
        float v2y = floatBuffer.get(baseIndex + 2 * this.vertexSize + 1);
        float v2z = floatBuffer.get(baseIndex + 2 * this.vertexSize + 2);
        float v2u = floatBuffer.get(baseIndex + 2 * this.vertexSize + this.offsetUV);
        float v2v = floatBuffer.get(baseIndex + 2 * this.vertexSize + this.offsetUV + 1);
        float v3x = floatBuffer.get(baseIndex + 3 * this.vertexSize);
        float v3y = floatBuffer.get(baseIndex + 3 * this.vertexSize + 1);
        float v3z = floatBuffer.get(baseIndex + 3 * this.vertexSize + 2);
        float v3u = floatBuffer.get(baseIndex + 3 * this.vertexSize + this.offsetUV);
        float v3v = floatBuffer.get(baseIndex + 3 * this.vertexSize + this.offsetUV + 1);
        float x1 = v2x - v0x;
        float y1 = v2y - v0y;
        float z1 = v2z - v0z;
        float x2 = v3x - v1x;
        float y2 = v3y - v1y;
        float z2 = v3z - v1z;
        float vnx = y1 * z2 - y2 * z1;
        float vny = z1 * x2 - z2 * x1;
        float vnz = x1 * y2 - x2 * y1;
        float lensq = vnx * vnx + vny * vny + vnz * vnz;
        float mult = lensq != 0.0 ? (float)(1.0 / Math.sqrt(lensq)) : 1.0F;
        vnx *= mult;
        vny *= mult;
        vnz *= mult;
        x1 = v1x - v0x;
        y1 = v1y - v0y;
        z1 = v1z - v0z;
        float u1 = v1u - v0u;
        float v1 = v1v - v0v;
        x2 = v2x - v0x;
        y2 = v2y - v0y;
        z2 = v2z - v0z;
        float u2 = v2u - v0u;
        float v2 = v2v - v0v;
        float d = u1 * v2 - u2 * v1;
        float r = d != 0.0F ? 1.0F / d : 1.0F;
        float tan1x = (v2 * x1 - v1 * x2) * r;
        float tan1y = (v2 * y1 - v1 * y2) * r;
        float tan1z = (v2 * z1 - v1 * z2) * r;
        float tan2x = (u1 * x2 - u2 * x1) * r;
        float tan2y = (u1 * y2 - u2 * y1) * r;
        float tan2z = (u1 * z2 - u2 * z1) * r;
        lensq = tan1x * tan1x + tan1y * tan1y + tan1z * tan1z;
        mult = lensq != 0.0 ? (float)(1.0 / Math.sqrt(lensq)) : 1.0F;
        tan1x *= mult;
        tan1y *= mult;
        tan1z *= mult;
        lensq = tan2x * tan2x + tan2y * tan2y + tan2z * tan2z;
        mult = lensq != 0.0 ? (float)(1.0 / Math.sqrt(lensq)) : 1.0F;
        tan2x *= mult;
        tan2y *= mult;
        tan2z *= mult;
        float tan3x = vnz * tan1y - vny * tan1z;
        float tan3y = vnx * tan1z - vnz * tan1x;
        float tan3z = vny * tan1x - vnx * tan1y;
        float tan1w = tan2x * tan3x + tan2y * tan3y + tan2z * tan3z < 0.0F ? -1.0F : 1.0F;
        int bnx = (int)(vnx * 127.0F) & 0xFF;
        int bny = (int)(vny * 127.0F) & 0xFF;
        int bnz = (int)(vnz * 127.0F) & 0xFF;
        int packedNormal = (bnz << 16) + (bny << 8) + bnx;
        intBuffer.put(baseIndex + 0 * this.vertexSize + this.offsetNormal, packedNormal);
        intBuffer.put(baseIndex + 1 * this.vertexSize + this.offsetNormal, packedNormal);
        intBuffer.put(baseIndex + 2 * this.vertexSize + this.offsetNormal, packedNormal);
        intBuffer.put(baseIndex + 3 * this.vertexSize + this.offsetNormal, packedNormal);
        int packedTan1xy = ((int)(tan1x * 32767.0F) & 65535) + (((int)(tan1y * 32767.0F) & 65535) << 16);
        int packedTan1zw = ((int)(tan1z * 32767.0F) & 65535) + (((int)(tan1w * 32767.0F) & 65535) << 16);
        intBuffer.put(baseIndex + 0 * this.vertexSize + 11, packedTan1xy);
        intBuffer.put(baseIndex + 0 * this.vertexSize + 11 + 1, packedTan1zw);
        intBuffer.put(baseIndex + 1 * this.vertexSize + 11, packedTan1xy);
        intBuffer.put(baseIndex + 1 * this.vertexSize + 11 + 1, packedTan1zw);
        intBuffer.put(baseIndex + 2 * this.vertexSize + 11, packedTan1xy);
        intBuffer.put(baseIndex + 2 * this.vertexSize + 11 + 1, packedTan1zw);
        intBuffer.put(baseIndex + 3 * this.vertexSize + 11, packedTan1xy);
        intBuffer.put(baseIndex + 3 * this.vertexSize + 11 + 1, packedTan1zw);
        float midU = (v0u + v1u + v2u + v3u) / 4.0F;
        float midV = (v0v + v1v + v2v + v3v) / 4.0F;
        floatBuffer.put(baseIndex + 0 * this.vertexSize + 9, midU);
        floatBuffer.put(baseIndex + 0 * this.vertexSize + 9 + 1, midV);
        floatBuffer.put(baseIndex + 1 * this.vertexSize + 9, midU);
        floatBuffer.put(baseIndex + 1 * this.vertexSize + 9 + 1, midV);
        floatBuffer.put(baseIndex + 2 * this.vertexSize + 9, midU);
        floatBuffer.put(baseIndex + 2 * this.vertexSize + 9 + 1, midV);
        floatBuffer.put(baseIndex + 3 * this.vertexSize + 9, midU);
        floatBuffer.put(baseIndex + 3 * this.vertexSize + 9 + 1, midV);
        if (Shaders.useVelocityAttrib) {
            VertexPosition[] vps = wrr.getQuadVertexPositions();
            int frameId = Config.getWorldRenderer().getFrameCount();
            this.setVelocity(floatBuffer, baseIndex, 0, vps, frameId, v0x, v0y, v0z);
            this.setVelocity(floatBuffer, baseIndex, 1, vps, frameId, v1x, v1y, v1z);
            this.setVelocity(floatBuffer, baseIndex, 2, vps, frameId, v2x, v2y, v2z);
            this.setVelocity(floatBuffer, baseIndex, 3, vps, frameId, v3x, v3y, v3z);
            wrr.setQuadVertexPositions(null);
        }

        if (wrr.getVertexFormat() == DefaultVertexFormat.BLOCK) {
            Vector3f mb = wrr.getMidBlock();
            float mbx = mb.x();
            float mby = mb.y();
            float mbz = mb.z();
            this.setMidBlock(intBuffer, baseIndex, 0, mbx - v0x, mby - v0y, mbz - v0z);
            this.setMidBlock(intBuffer, baseIndex, 1, mbx - v1x, mby - v1y, mbz - v1z);
            this.setMidBlock(intBuffer, baseIndex, 2, mbx - v2x, mby - v2y, mbz - v2z);
            this.setMidBlock(intBuffer, baseIndex, 3, mbx - v3x, mby - v3y, mbz - v3z);
        }
    }

    public void setMidBlock(IntBuffer intBuffer, int baseIndex, int vertex, float mbx, float mby, float mbz) {
        int imbx = (int)(mbx * 64.0F) & 0xFF;
        int imby = (int)(mby * 64.0F) & 0xFF;
        int imbz = (int)(mbz * 64.0F) & 0xFF;
        int packedMidBlock = (imbz << 16) + (imby << 8) + imbx;
        intBuffer.put(baseIndex + vertex * this.vertexSize + 8, packedMidBlock);
    }

    public void setVelocity(FloatBuffer floatBuffer, int baseIndex, int vertex, VertexPosition[] vps, int frameId, float x, float y, float z) {
        float vx = 0.0F;
        float vy = 0.0F;
        float vz = 0.0F;
        if (vps != null && vps.length == 4) {
            VertexPosition vp = vps[vertex];
            vp.setPosition(frameId, x, y, z);
            if (vp.isVelocityValid()) {
                vx = vp.getVelocityX();
                vy = vp.getVelocityY();
                vz = vp.getVelocityZ();
            }
        }

        int offset = baseIndex + vertex * this.vertexSize + 15;
        floatBuffer.put(offset + 0, vx);
        floatBuffer.put(offset + 1, vy);
        floatBuffer.put(offset + 2, vz);
    }

    public static void calcNormalChunkLayer(BufferBuilder wrr) {
        if (wrr.getVertexFormat().hasNormal() && wrr.getDrawMode() == PrimitiveTopology.QUADS && wrr.getVertexCount() % 4 == 0) {
            SVertexBuilder svb = wrr.sVertexBuilder;
            endSetVertexFormat(wrr);
            int indexEnd = wrr.getVertexCount() * svb.vertexSize;

            for (int index = 0; index < indexEnd; index += svb.vertexSize * 4) {
                svb.calcNormal(wrr, index);
            }
        }
    }

    public static boolean preDrawArrays(VertexFormat vf) {
        int vertexSizeByte = vf.getVertexSize();
        if (vertexSizeByte != 72) {
            return false;
        }

        GL20.glVertexAttribPointer(Shaders.midTexCoordAttrib, 2, 5126, false, vertexSizeByte, 36L);
        GL20.glVertexAttribPointer(Shaders.tangentAttrib, 4, 5122, false, vertexSizeByte, 44L);
        GL20.glVertexAttribPointer(Shaders.entityAttrib, 3, 5122, false, vertexSizeByte, 52L);
        GL20.glVertexAttribPointer(Shaders.velocityAttrib, 3, 5126, false, vertexSizeByte, 60L);
        GL20.glEnableVertexAttribArray(Shaders.midTexCoordAttrib);
        GL20.glEnableVertexAttribArray(Shaders.tangentAttrib);
        GL20.glEnableVertexAttribArray(Shaders.entityAttrib);
        GL20.glEnableVertexAttribArray(Shaders.velocityAttrib);
        return true;
    }

    public static void postDrawArrays() {
        GL20.glDisableVertexAttribArray(Shaders.midTexCoordAttrib);
        GL20.glDisableVertexAttribArray(Shaders.tangentAttrib);
        GL20.glDisableVertexAttribArray(Shaders.entityAttrib);
        GL20.glDisableVertexAttribArray(Shaders.velocityAttrib);
    }
}
