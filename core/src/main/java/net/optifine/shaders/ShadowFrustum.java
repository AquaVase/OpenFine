package net.optifine.shaders;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.Mth;
import net.optifine.Config;
import net.optifine.util.MathUtils;
import net.optifine.util.WorldUtils;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ShadowFrustum extends Frustum {
    public ShadowFrustum(Matrix4f matrixIn, Matrix4f projectionIn) {
        super(matrixIn, projectionIn, true);
        this.extendForShadows(matrixIn, projectionIn);
    }

    private void extendForShadows(Matrix4f matrixIn, Matrix4f projectionIn) {
        ClientLevel world = Config.getMinecraft().level;
        if (world != null) {
            Matrix4f matrixFull = MathUtils.copy(projectionIn);
            matrixFull.mul(matrixIn);
            matrixFull.transpose();
            Vector4f viewUp = new Vector4f(0.0F, 1.0F, 0.0F, 0.0F);
            MathUtils.transform(viewUp, matrixFull);
            viewUp.normalize();
            Vector4f viewRight = new Vector4f(-1.0F, 0.0F, 0.0F, 0.0F);
            MathUtils.transform(viewRight, matrixFull);
            viewRight.normalize();
            float partialTicks = 0.0F;
            float car = WorldUtils.getCelestialAngleRadians(partialTicks);
            float sunTiltRad = Shaders.sunPathRotation * Mth.deg2Rad;
            float sar = car > Mth.PId2 && car < 3.0F * Mth.PId2 ? car + (float) Math.PI : car;
            float sx = -Mth.sin(sar);
            float sy = Mth.cos(sar) * Mth.cos(sunTiltRad);
            float sz = -Mth.cos(sar) * Mth.sin(sunTiltRad);
            Vector4f vecSun = new Vector4f(sx, sy, sz, 0.0F);
            vecSun.normalize();
            Vector3f viewUpDot = MathUtils.makeVector3f(viewUp);
            viewUpDot.mul(viewUp.dot(vecSun));
            Vector3f vecSunH3 = MathUtils.makeVector3f(vecSun);
            vecSunH3.sub(viewUpDot);
            vecSunH3.normalize();
            Vector4f vecSunH = new Vector4f(vecSunH3.x(), vecSunH3.y(), vecSunH3.z(), 0.0F);
            Vector3f viewRightDot = MathUtils.makeVector3f(viewRight);
            viewRightDot.mul(viewRight.dot(vecSun));
            Vector3f vecSunV3 = MathUtils.makeVector3f(vecSun);
            vecSunV3.sub(viewRightDot);
            vecSunV3.normalize();
            Vector4f vecSunV = new Vector4f(vecSunV3.x(), vecSunV3.y(), vecSunV3.z(), 0.0F);
            Vector4f vecRight = this.planes[0];
            Vector4f vecLeft = this.planes[1];
            Vector4f vecTop = this.planes[2];
            Vector4f vecBottom = this.planes[3];
            Vector4f vecFar = this.planes[4];
            Vector4f vecNear = this.planes[5];
            vecRight.normalize();
            vecLeft.normalize();
            vecTop.normalize();
            vecBottom.normalize();
            vecFar.normalize();
            vecNear.normalize();
            float dotRight = vecRight.dot(vecSunH);
            float dotLeft = vecLeft.dot(vecSunH);
            float dotTop = vecTop.dot(vecSunV);
            float dotBottom = vecBottom.dot(vecSunV);
            float farPlaneDistance = Config.getGameRenderer().getRenderDistance();
            float mulFarDist = Config.isFogOff() ? 1.414F : 1.0F;
            float rotRight = 0.0F;
            float rotLeft = 0.0F;
            if (dotRight < 0.0F || dotLeft < 0.0F) {
                vecNear.add(0.0F, 0.0F, 0.0F, farPlaneDistance);
                if (dotRight < 0.0F && dotLeft < 0.0F) {
                    rotRight = this.rotateDotPlus(vecRight, vecSunH, -1, viewUp);
                    rotLeft = this.rotateDotPlus(vecLeft, vecSunH, 1, viewUp);
                    vecRight.set(-vecRight.x(), -vecRight.y(), -vecRight.z(), -vecRight.w());
                    vecLeft.set(-vecLeft.x(), -vecLeft.y(), -vecLeft.z(), -vecLeft.w());
                    float distRight = -dotRight * farPlaneDistance * mulFarDist;
                    float distLeft = -dotLeft * farPlaneDistance * mulFarDist;
                    vecRight.add(0.0F, 0.0F, 0.0F, distRight);
                    vecLeft.add(0.0F, 0.0F, 0.0F, distLeft);
                } else if (dotRight < 0.0F) {
                    rotRight = this.rotateDotPlus(vecRight, vecSunH, -1, viewUp);
                } else {
                    rotLeft = this.rotateDotPlus(vecLeft, vecSunH, 1, viewUp);
                }
            }

            int minWorldHeight = world.getMinY();
            int maxWorldHeight = world.getMaxY();
            float eyeHeight = (int)Config.getMinecraft().player.getEyeY();
            float maxDistBottom = Config.limit(eyeHeight - minWorldHeight, 0.0F, farPlaneDistance);
            float maxDistTop = Config.limit(maxWorldHeight - eyeHeight, 0.0F, farPlaneDistance);
            float rotTop = 0.0F;
            float rotBottom = 0.0F;
            if (dotTop < 0.0F || dotBottom < 0.0F) {
                vecNear.add(0.0F, 0.0F, 0.0F, farPlaneDistance);
                if (dotTop < 0.0F && dotBottom < 0.0F) {
                    rotTop = this.rotateDotPlus(vecTop, vecSunV, -1, viewRight);
                    rotBottom = this.rotateDotPlus(vecBottom, vecSunV, 1, viewRight);
                    vecTop.set(-vecTop.x(), -vecTop.y(), -vecTop.z(), -vecTop.w());
                    vecBottom.set(-vecBottom.x(), -vecBottom.y(), -vecBottom.z(), -vecBottom.w());
                    float distTop = -dotTop * maxDistTop;
                    float distBottom = -dotBottom * maxDistBottom;
                    vecTop.add(0.0F, 0.0F, 0.0F, distTop);
                    vecBottom.add(0.0F, 0.0F, 0.0F, distBottom);
                } else if (dotTop < 0.0F) {
                    rotTop = this.rotateDotPlus(vecTop, vecSunV, -1, viewRight);
                } else {
                    rotBottom = this.rotateDotPlus(vecBottom, vecSunV, 1, viewRight);
                }
            }
        }
    }

    private float rotateDotPlus(Vector4f vecFrustum, Vector4f vecSun, int angleDeg, Vector4f vecRot) {
        Vector3f vecRot3 = MathUtils.makeVector3f(vecRot);
        Quaternionf rot = MathUtils.rotationDegrees(vecRot3, angleDeg);
        float angleDegSum = 0.0F;

        while (true) {
            float dot = vecFrustum.dot(vecSun);
            if (dot >= 0.0F) {
                return angleDegSum;
            }

            MathUtils.transform(vecFrustum, rot);
            vecFrustum.normalize();
            angleDegSum += angleDeg;
        }
    }
}
