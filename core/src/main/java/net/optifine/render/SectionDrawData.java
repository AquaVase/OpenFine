package net.optifine.render;

import com.mojang.blaze3d.vertex.MeshData;
import java.nio.ByteBuffer;
import net.optifine.util.GlUtil;

public class SectionDrawData {
    public VboRegion vboRegion;
    public VboRange vboRange;
    public MeshData meshData;
    public ByteBuffer vertexBufferUpload;
    public MultiTextureData multiTextureData;
    public static ByteBuffer emptyBuffer = GlUtil.allocateMemory(0);
}
