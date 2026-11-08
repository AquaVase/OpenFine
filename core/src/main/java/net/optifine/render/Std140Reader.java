package net.optifine.render;

import java.nio.ByteBuffer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.joml.Vector4i;

public class Std140Reader {
    private ByteBuffer buffer;
    private int start;

    public void setBuffer(ByteBuffer bufferIn) {
        this.buffer = bufferIn;
        this.start = bufferIn.position();
    }

    public Std140Reader align(int stepIn) {
        int pos = this.buffer.position();
        if ((pos - this.start) % stepIn == 0) {
            return this;
        }

        this.buffer.position(this.start + Mth.roundToward(pos - this.start, stepIn));
        return this;
    }

    public float getFloat() {
        this.align(4);
        return this.buffer.getFloat();
    }

    public int getInt() {
        this.align(4);
        return this.buffer.getInt();
    }

    public Vector2f getVec2(Vector2f vecIn) {
        this.align(8);
        vecIn.set(this.buffer);
        this.buffer.position(this.buffer.position() + 8);
        return vecIn;
    }

    public Vector2i getIVec2(Vector2i vecIn) {
        this.align(8);
        vecIn.set(this.buffer);
        this.buffer.position(this.buffer.position() + 8);
        return vecIn;
    }

    public Vector3f getVec3(Vector3f vecIn) {
        this.align(16);
        vecIn.set(this.buffer);
        this.buffer.position(this.buffer.position() + 16);
        return vecIn;
    }

    public Vector3i getIVec3(Vector3i vecIn) {
        this.align(16);
        vecIn.set(this.buffer);
        this.buffer.position(this.buffer.position() + 16);
        return vecIn;
    }

    public Vector4f getVec4(Vector4f vecIn) {
        this.align(16);
        vecIn.set(this.buffer);
        this.buffer.position(this.buffer.position() + 16);
        return vecIn;
    }

    public Vector4i putIVec4(Vector4i vecIn) {
        this.align(16);
        vecIn.set(this.buffer);
        this.buffer.position(this.buffer.position() + 16);
        return vecIn;
    }

    public Matrix4f getMat4f(Matrix4f matIn) {
        this.align(16);
        matIn.set(this.buffer);
        this.buffer.position(this.buffer.position() + 64);
        return matIn;
    }
}
