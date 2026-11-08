package net.optifine.render;

import java.util.Locale;
import net.minecraft.client.renderer.fog.FogData;
import org.joml.Vector4f;

public class FogParameters {
    private Vector4f fogColor;
    private FogData fogData;

    public FogParameters(Vector4f fogColor, FogData fogData) {
        this.fogColor = fogColor;
        this.fogData = fogData;
    }

    public Vector4f getFogColor() {
        return this.fogColor;
    }

    public FogData getFogData() {
        return this.fogData;
    }

    public static String toString(FogData dataIn) {
        return dataIn.environmentalStart == Float.MAX_VALUE && dataIn.renderDistanceStart == Float.MAX_VALUE
            ? "MAX"
            : String.format(
                "envStart: %s, distStart: %s, envEnd: %s, distEnd: %s, skyEnd: %s, cloudEnd: %s",
                formatDist(dataIn.environmentalStart),
                formatDist(dataIn.renderDistanceStart),
                formatDist(dataIn.environmentalEnd),
                formatDist(dataIn.renderDistanceEnd),
                formatDist(dataIn.skyEnd),
                formatDist(dataIn.cloudEnd)
            );
    }

    public static String formatDist(float val) {
        return val == Float.MAX_VALUE ? "MAX" : String.format(Locale.ROOT, "%.0f", val);
    }

    @Override
    public String toString() {
        return "color: " + this.fogColor + ", data: " + toString(this.fogData);
    }
}
