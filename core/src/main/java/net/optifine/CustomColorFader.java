package net.optifine;

import net.minecraft.world.phys.Vec3;

public class CustomColorFader {
    private Vec3 color = null;
    private long timeUpdate = System.currentTimeMillis();

    public Vec3 getColor(double x, double y, double z) {
        if (this.color == null) {
            this.color = new Vec3(x, y, z);
            return this.color;
        }

        long timeNow = System.currentTimeMillis();
        long timeDiff = timeNow - this.timeUpdate;
        if (timeDiff == 0L) {
            return this.color;
        }

        this.timeUpdate = timeNow;
        if (Math.abs(x - this.color.x) < 0.004 && Math.abs(y - this.color.y) < 0.004 && Math.abs(z - this.color.z) < 0.004) {
            return this.color;
        }

        double k = timeDiff * 0.001;
        k = Config.limit(k, 0.0, 1.0);
        double dx = x - this.color.x;
        double dy = y - this.color.y;
        double dz = z - this.color.z;
        double xn = this.color.x + dx * k;
        double yn = this.color.y + dy * k;
        double zn = this.color.z + dz * k;
        this.color = new Vec3(xn, yn, zn);
        return this.color;
    }
}
