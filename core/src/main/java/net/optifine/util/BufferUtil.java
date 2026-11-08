package net.optifine.util;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class BufferUtil {
    public static String getBufferHex(BufferBuilder bb) {
        return getBufferHex(bb.getByteBuffer(), bb.getStartPosition(), bb.getDrawMode(), bb.getVertexFormat(), bb.getVertexCount());
    }

    public static String getBufferHex(MeshData md) {
        ByteBufferBuilder.Result res = md.getVertexResult();
        MeshData.DrawState ds = md.drawState();
        return getBufferHex(res.byteBuffer(), 0, ds.primitiveTopology(), ds.format(), ds.vertexCount());
    }

    public static String getBufferHex(ByteBuffer buf, int startPos, PrimitiveTopology drawMode, VertexFormat vf, int vertexCount) {
        String primitiveName = "";
        int vertexPerPrimitive = -1;
        byte var11;
        if (drawMode == PrimitiveTopology.QUADS) {
            primitiveName = "quad";
            var11 = 4;
        } else {
            if (drawMode != PrimitiveTopology.TRIANGLES) {
                return "Invalid draw mode: " + drawMode;
            }

            primitiveName = "triangle";
            var11 = 3;
        }

        StringBuffer sb = new StringBuffer();

        for (int v = 0; v < vertexCount; v++) {
            if (v % var11 == 0) {
                sb.append(primitiveName + " " + v / var11 + "\n");
            }

            String vs = getVertexHex(buf, startPos, vf, v);
            sb.append(vs);
            sb.append("\n");
        }

        return sb.toString();
    }

    private static String getVertexHex(ByteBuffer buf, int startPos, VertexFormat vf, int vertex) {
        StringBuffer sb = new StringBuffer();
        int pos = startPos + vertex * vf.getVertexSize();

        for (VertexFormatElement vfe : vf.getElements()) {
            GpuFormat gf = vfe.format();
            if (gf.componentCount() > 0) {
                sb.append("(");
            }

            for (int i = 0; i < gf.componentCount(); i++) {
                if (i > 0) {
                    sb.append(" ");
                }

                switch (gf.componentType()) {
                    case UNORM_8:
                    case UINT_8:
                        sb.append(buf.get(pos) & 255);
                        break;
                    case SNORM_8:
                    case SINT_8:
                        sb.append(buf.get(pos));
                        break;
                    case UNORM_16:
                    case UINT_16:
                        sb.append(buf.getShort(pos) & 65535);
                        break;
                    case SNORM_16:
                    case SINT_16:
                        sb.append(buf.getShort(pos));
                        break;
                    case UINT_32:
                        sb.append(buf.getInt(pos) & 4294967295L);
                        break;
                    case SINT_32:
                        sb.append(buf.getInt(pos));
                        break;
                    case FLOAT_16:
                        short f16bits = buf.getShort(pos);
                        float f16 = Float.float16ToFloat(f16bits);
                        sb.append(f16);
                        break;
                    case FLOAT_32:
                        sb.append(buf.getFloat(pos));
                        break;
                    default:
                        sb.append("???");
                }

                pos += gf.componentType().byteSize();
            }

            if (gf.componentCount() > 0) {
                sb.append(")");
            }
        }

        return sb.toString();
    }

    public static String getBufferString(IntBuffer buf) {
        if (buf == null) {
            return "null";
        }

        StringBuffer sb = new StringBuffer();
        sb.append("(pos=" + buf.position() + " lim=" + buf.limit() + " cap=" + buf.capacity() + ")");
        sb.append("[");
        int len = Math.min(buf.limit(), 1024);

        for (int i = 0; i < len; i++) {
            if (i > 0) {
                sb.append(", ");
            }

            sb.append(buf.get(i));
        }

        sb.append("]");
        return sb.toString();
    }

    public static int[] toArray(IntBuffer buf) {
        int[] arr = new int[buf.limit()];

        for (int i = 0; i < arr.length; i++) {
            arr[i] = buf.get(i);
        }

        return arr;
    }

    public static FloatBuffer createDirectFloatBuffer(int capacity) {
        return GlUtil.allocateMemory(capacity << 2).asFloatBuffer();
    }

    public static void fill(FloatBuffer buf, float val) {
        buf.clear();

        for (int i = 0; i < buf.capacity(); i++) {
            buf.put(i, val);
        }

        buf.clear();
    }

    public static void copyFull(FloatBuffer src, FloatBuffer dst) {
        src.position(0);
        dst.position(0);
        dst.put(src);
        src.position(0);
        dst.position(0);
    }
}
