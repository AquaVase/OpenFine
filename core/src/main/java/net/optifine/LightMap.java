package net.optifine;

import net.minecraft.client.multiplayer.ClientLevel;
import net.optifine.util.WorldUtils;

public class LightMap {
    private CustomColormap lightMapRgb = null;
    private float[][] sunRgbs = new float[16][3];
    private float[][] torchRgbs = new float[16][3];

    public LightMap(CustomColormap lightMapRgb) {
        this.lightMapRgb = lightMapRgb;
    }

    public CustomColormap getColormap() {
        return this.lightMapRgb;
    }

    public boolean updateLightmap(ClientLevel world, float torchFlickerX, int[] lmColors, boolean nightvision, float darkLight) {
        if (this.lightMapRgb == null) {
            return false;
        }

        int height = this.lightMapRgb.getHeight();
        if (nightvision && height < 64) {
            return false;
        }

        int width = this.lightMapRgb.getWidth();
        if (width < 16) {
            warn("Invalid lightmap width: " + width);
            this.lightMapRgb = null;
            return false;
        }

        int startIndex = 0;
        if (nightvision) {
            startIndex = width * 16 * 2;
        }

        float sun = 1.1666666F * (WorldUtils.getSkyLightFactor(1.0F) - 0.2F);
        if (world.getSkyFlashTime() > 0) {
            sun = 1.0F;
        }

        sun = Config.limitTo1(sun);
        float sunX = sun * (width - 1);
        float torchX = Config.limitTo1(torchFlickerX + 0.5F) * (width - 1);
        float gamma = Config.limitTo1((float)Config.getGameSettings().gamma().get().doubleValue());
        boolean hasGamma = gamma > 1.0E-4F;
        float[][] colorsRgb = this.lightMapRgb.getColorsRgb();
        this.getLightMapColumn(colorsRgb, sunX, startIndex, width, this.sunRgbs);
        this.getLightMapColumn(colorsRgb, torchX, startIndex + 16 * width, width, this.torchRgbs);
        float[] rgb = new float[3];

        for (int is = 0; is < 16; is++) {
            for (int it = 0; it < 16; it++) {
                for (int ic = 0; ic < 3; ic++) {
                    float comp = Config.limitTo1(this.sunRgbs[is][ic] + this.torchRgbs[it][ic] - darkLight);
                    if (hasGamma) {
                        float cg = 1.0F - comp;
                        cg = 1.0F - cg * cg * cg * cg;
                        comp = gamma * cg + (1.0F - gamma) * comp;
                    }

                    rgb[ic] = comp;
                }

                int r = (int)(rgb[0] * 255.0F);
                int g = (int)(rgb[1] * 255.0F);
                int b = (int)(rgb[2] * 255.0F);
                lmColors[is * 16 + it] = 0xFF000000 | b << 16 | g << 8 | r;
            }
        }

        return true;
    }

    private void getLightMapColumn(float[][] origMap, float x, int offset, int width, float[][] colRgb) {
        if (offset + 16 * width > origMap.length) {
            offset = 0;
        }

        int xLow = (int)Math.floor(x);
        int xHigh = (int)Math.ceil(x);
        if (xLow == xHigh) {
            for (int y = 0; y < 16; y++) {
                float[] rgbLow = origMap[offset + y * width + xLow];
                float[] rgb = colRgb[y];

                for (int i = 0; i < 3; i++) {
                    rgb[i] = rgbLow[i];
                }
            }
        } else {
            float dLow = 1.0F - (x - xLow);
            float dHigh = 1.0F - (xHigh - x);

            for (int y = 0; y < 16; y++) {
                float[] rgbLow = origMap[offset + y * width + xLow];
                float[] rgbHigh = origMap[offset + y * width + xHigh];
                float[] rgb = colRgb[y];

                for (int i = 0; i < 3; i++) {
                    rgb[i] = rgbLow[i] * dLow + rgbHigh[i] * dHigh;
                }
            }
        }
    }

    private static void dbg(String str) {
        Config.dbg("CustomColors: " + str);
    }

    private static void warn(String str) {
        Config.warn("CustomColors: " + str);
    }
}
