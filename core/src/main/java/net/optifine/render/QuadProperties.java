package net.optifine.render;

import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.optifine.model.QuadBounds;

public class QuadProperties {
    public long[] packedUvsSingle = null;
    public QuadBounds quadBounds;
    public boolean quadEmissiveChecked;
    public BakedQuad quadEmissive;
    public QuadVertexPositions quadVertexPositions;
}
