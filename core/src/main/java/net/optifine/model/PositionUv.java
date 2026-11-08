package net.optifine.model;

import net.minecraft.client.model.geom.builders.UVPair;
import org.joml.Vector3f;

public class PositionUv {
    private Vector3f position;
    private long packedUv;

    public PositionUv(Vector3f position, long packedUv) {
        this.position = position;
        this.packedUv = packedUv;
    }

    public Vector3f position() {
        return this.position;
    }

    public long packedUv() {
        return this.packedUv;
    }

    @Override
    public String toString() {
        return "pos: " + this.position + "uv: " + UVPair.unpackU(this.packedUv) + ", " + UVPair.unpackV(this.packedUv);
    }
}
