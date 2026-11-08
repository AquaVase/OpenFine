package net.optifine.util;

import com.google.gson.JsonObject;
import net.minecraft.client.renderer.texture.MipmapStrategy;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;

public class MetadataUtils {
    public static TextureMetadataSection parseTextureMetadataSection(JsonObject json) {
        boolean blur = Json.getBoolean(json, "blur", false);
        boolean clamp = Json.getBoolean(json, "clamp", false);
        String mipmapStr = Json.getString(json, "mipmap_strategy", "auto");
        MipmapStrategy mipmapStrat = parseMipmapStrategy(mipmapStr);
        float alphaCutoff = Json.getFloat(json, "alpha_cutoff_bias", 0.0F);
        return new TextureMetadataSection(blur, clamp, mipmapStrat, alphaCutoff);
    }

    private static MipmapStrategy parseMipmapStrategy(String str) {
        for (MipmapStrategy ms : MipmapStrategy.values()) {
            if (ms.getSerializedName().equals(str)) {
                return ms;
            }
        }

        return MipmapStrategy.AUTO;
    }
}
