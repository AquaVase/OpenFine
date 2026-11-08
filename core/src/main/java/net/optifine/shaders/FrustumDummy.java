package net.optifine.shaders;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public class FrustumDummy extends Frustum {
    public static final FrustumDummy INSTANCE = new FrustumDummy();

    private FrustumDummy() {
        super(new Matrix4f(), new Matrix4f());
        this.disabled = true;
    }

    @Override
    public Frustum offsetToFullyIncludeCameraCube(int stepIn) {
        return this;
    }

    @Override
    public boolean isVisible(AABB aabbIn) {
        return true;
    }

    @Override
    public int cubeInFrustum(BoundingBox boxIn) {
        return -2;
    }

    @Override
    public boolean isBoxInFrustumFully(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return true;
    }
}
