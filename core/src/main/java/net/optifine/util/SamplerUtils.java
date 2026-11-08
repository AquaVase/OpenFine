package net.optifine.util;

import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;

public class SamplerUtils {
    public static boolean isBlur(GpuSampler sampler) {
        return sampler.getMagFilter() == FilterMode.LINEAR;
    }

    public static boolean isClamp(GpuSampler sampler) {
        return sampler.getAddressModeU() == AddressMode.CLAMP_TO_EDGE;
    }
}
