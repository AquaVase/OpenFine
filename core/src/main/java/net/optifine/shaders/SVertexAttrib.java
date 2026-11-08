package net.optifine.shaders;

import com.mojang.blaze3d.GpuFormat;

public class SVertexAttrib {
    public int index;
    public int count;
    public GpuFormat type;
    public int offset;

    public SVertexAttrib(int index, int count, GpuFormat type) {
        this.index = index;
        this.count = count;
        this.type = type;
    }
}
