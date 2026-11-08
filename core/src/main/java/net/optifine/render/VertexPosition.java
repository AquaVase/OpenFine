package net.optifine.render;

import net.optifine.shaders.Shaders;
import net.optifine.util.MathUtils;
import org.joml.Matrix4f;

public class VertexPosition {
    private int frameId;
    private float posX;
    private float posY;
    private float posZ;
    private float velocityX;
    private float velocityY;
    private float velocityZ;
    private boolean velocityValid;

    public void setPosition(int frameId, float x, float y, float z) {
        if (!Shaders.isShadowPass) {
            if (frameId != this.frameId) {
                Matrix4f mv = Shaders.getLastModelView();
                float tx = MathUtils.getTransformX(mv, x, y, z);
                float ty = MathUtils.getTransformY(mv, x, y, z);
                float tz = MathUtils.getTransformZ(mv, x, y, z);
                if (this.frameId != 0) {
                    this.velocityX = tx - this.posX;
                    this.velocityY = ty - this.posY;
                    this.velocityZ = tz - this.posZ;
                    this.velocityValid = frameId - this.frameId <= 3 && !Shaders.pointOfViewChanged;
                }

                this.frameId = frameId;
                this.posX = tx;
                this.posY = ty;
                this.posZ = tz;
            }
        }
    }

    public boolean isVelocityValid() {
        return this.velocityValid;
    }

    public float getVelocityX() {
        return this.velocityX;
    }

    public float getVelocityY() {
        return this.velocityY;
    }

    public float getVelocityZ() {
        return this.velocityZ;
    }

    public int getFrameId() {
        return this.frameId;
    }
}
