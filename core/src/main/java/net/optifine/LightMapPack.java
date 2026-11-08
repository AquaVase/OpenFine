package net.optifine;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.multiplayer.ClientLevel;
import net.optifine.util.WorldUtils;

public class LightMapPack {
    private LightMap lightMap;
    private LightMap lightMapRain;
    private LightMap lightMapThunder;
    private int[] colorBuffer1 = new int[0];
    private int[] colorBuffer2 = new int[0];
    private int[] lmColorsBuffer = new int[0];

    public LightMapPack(LightMap lightMap, LightMap lightMapRain, LightMap lightMapThunder) {
        if (lightMapRain != null || lightMapThunder != null) {
            if (lightMapRain == null) {
                lightMapRain = lightMap;
            }

            if (lightMapThunder == null) {
                lightMapThunder = lightMapRain;
            }
        }

        this.lightMap = lightMap;
        this.lightMapRain = lightMapRain;
        this.lightMapThunder = lightMapThunder;
    }

    public boolean updateLightmap(ClientLevel world, float torchFlickerX, NativeImage lmColorsImage, boolean nightvision, float darkLight, float partialTicks) {
        int lmColorsLength = lmColorsImage.getWidth() * lmColorsImage.getHeight();
        if (this.lmColorsBuffer.length != lmColorsLength) {
            this.lmColorsBuffer = new int[lmColorsLength];
        }

        lmColorsImage.getBufferRGBA().get(this.lmColorsBuffer);
        boolean updated = this.updateLightmap(world, torchFlickerX, this.lmColorsBuffer, nightvision, darkLight, partialTicks);
        if (updated) {
            lmColorsImage.getBufferRGBA().put(this.lmColorsBuffer);
        }

        return updated;
    }

    public boolean updateLightmap(ClientLevel world, float torchFlickerX, int[] lmColors, boolean nightvision, float darkLight, float partialTicks) {
        if (this.lightMapRain == null && this.lightMapThunder == null) {
            return this.lightMap.updateLightmap(world, torchFlickerX, lmColors, nightvision, darkLight);
        }

        if (!WorldUtils.isEnd(world) && !WorldUtils.isNether(world)) {
            float rainStrength = world.getRainLevel(partialTicks);
            float thunderStrength = world.getThunderLevel(partialTicks);
            float delta = 1.0E-4F;
            boolean isRaining = rainStrength > delta;
            boolean isThundering = thunderStrength > delta;
            if (!isRaining && !isThundering) {
                return this.lightMap.updateLightmap(world, torchFlickerX, lmColors, nightvision, darkLight);
            }

            if (rainStrength > 0.0F) {
                thunderStrength /= rainStrength;
            }

            float clearBrightness = 1.0F - rainStrength;
            float rainBrightness = rainStrength - thunderStrength;
            float thunderBrightness = thunderStrength;
            if (this.colorBuffer1.length != lmColors.length) {
                this.colorBuffer1 = new int[lmColors.length];
                this.colorBuffer2 = new int[lmColors.length];
            }

            int count = 0;
            int[][] colors = new int[][]{lmColors, this.colorBuffer1, this.colorBuffer2};
            float[] brs = new float[3];
            if (clearBrightness > delta && this.lightMap.updateLightmap(world, torchFlickerX, colors[count], nightvision, darkLight)) {
                brs[count] = clearBrightness;
                count++;
            }

            if (rainBrightness > delta
                && this.lightMapRain != null
                && this.lightMapRain.updateLightmap(world, torchFlickerX, colors[count], nightvision, darkLight)) {
                brs[count] = rainBrightness;
                count++;
            }

            if (thunderBrightness > delta
                && this.lightMapThunder != null
                && this.lightMapThunder.updateLightmap(world, torchFlickerX, colors[count], nightvision, darkLight)) {
                brs[count] = thunderBrightness;
                count++;
            }

            if (count == 2) {
                return this.blend(colors[0], brs[0], colors[1], brs[1]);
            } else {
                return count == 3 ? this.blend(colors[0], brs[0], colors[1], brs[1], colors[2], brs[2]) : true;
            }
        } else {
            return this.lightMap.updateLightmap(world, torchFlickerX, lmColors, nightvision, darkLight);
        }
    }

    private boolean blend(int[] cols0, float br0, int[] cols1, float br1) {
        if (cols1.length != cols0.length) {
            return false;
        }

        for (int i = 0; i < cols0.length; i++) {
            int col0 = cols0[i];
            int red0 = col0 >> 16 & 0xFF;
            int green0 = col0 >> 8 & 0xFF;
            int blue0 = col0 & 0xFF;
            int col1 = cols1[i];
            int red1 = col1 >> 16 & 0xFF;
            int green1 = col1 >> 8 & 0xFF;
            int blue1 = col1 & 0xFF;
            int red = (int)(red0 * br0 + red1 * br1);
            int green = (int)(green0 * br0 + green1 * br1);
            int blue = (int)(blue0 * br0 + blue1 * br1);
            cols0[i] = 0xFF000000 | red << 16 | green << 8 | blue;
        }

        return true;
    }

    private boolean blend(int[] cols0, float br0, int[] cols1, float br1, int[] cols2, float br2) {
        if (cols1.length == cols0.length && cols2.length == cols0.length) {
            for (int i = 0; i < cols0.length; i++) {
                int col0 = cols0[i];
                int red0 = col0 >> 16 & 0xFF;
                int green0 = col0 >> 8 & 0xFF;
                int blue0 = col0 & 0xFF;
                int col1 = cols1[i];
                int red1 = col1 >> 16 & 0xFF;
                int green1 = col1 >> 8 & 0xFF;
                int blue1 = col1 & 0xFF;
                int col2 = cols2[i];
                int red2 = col2 >> 16 & 0xFF;
                int green2 = col2 >> 8 & 0xFF;
                int blue2 = col2 & 0xFF;
                int red = (int)(red0 * br0 + red1 * br1 + red2 * br2);
                int green = (int)(green0 * br0 + green1 * br1 + green2 * br2);
                int blue = (int)(blue0 * br0 + blue1 * br1 + blue2 * br2);
                cols0[i] = 0xFF000000 | red << 16 | green << 8 | blue;
            }

            return true;
        } else {
            return false;
        }
    }
}
