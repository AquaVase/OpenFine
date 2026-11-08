package net.optifine;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.util.Properties;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.optifine.render.BufferUploader;
import net.optifine.render.Tesselator;

public class CustomLoadingScreen {
    private Identifier locationTexture;
    private int scaleMode = 0;
    private int scale = 2;
    private boolean center;
    private static final int SCALE_DEFAULT = 2;
    private static final int SCALE_MODE_FIXED = 0;
    private static final int SCALE_MODE_FULL = 1;
    private static final int SCALE_MODE_STRETCH = 2;

    public CustomLoadingScreen(Identifier locationTexture, int scaleMode, int scale, boolean center) {
        this.locationTexture = locationTexture;
        this.scaleMode = scaleMode;
        this.scale = scale;
        this.center = center;
    }

    public static CustomLoadingScreen parseScreen(String path, int dimId, Properties props) {
        Identifier loc = new Identifier(path);
        int scaleMode = parseScaleMode(getProperty("scaleMode", dimId, props));
        int scaleDef = scaleMode == 0 ? 2 : 1;
        int scale = parseScale(getProperty("scale", dimId, props), scaleDef);
        boolean center = Config.parseBoolean(getProperty("center", dimId, props), false);
        return new CustomLoadingScreen(loc, scaleMode, scale, center);
    }

    private static String getProperty(String key, int dim, Properties props) {
        if (props == null) {
            return null;
        }

        String val = props.getProperty("dim" + dim + "." + key);
        return val != null ? val : props.getProperty(key);
    }

    private static int parseScaleMode(String str) {
        if (str == null) {
            return 0;
        }

        str = str.toLowerCase().trim();
        if (str.equals("fixed")) {
            return 0;
        }

        if (str.equals("full")) {
            return 1;
        }

        if (str.equals("stretch")) {
            return 2;
        }

        CustomLoadingScreens.warn("Invalid scale mode: " + str);
        return 0;
    }

    private static int parseScale(String str, int def) {
        if (str == null) {
            return def;
        } else {
            str = str.trim();
            int val = Config.parseInt(str, -1);
            if (val < 1) {
                CustomLoadingScreens.warn("Invalid scale: " + str);
                return def;
            } else {
                return val;
            }
        }
    }

    public void drawBackground(int width, int height) {
        Tesselator tessellator = Tesselator.getInstance();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        float div = 16 * this.scale;
        float uMax = width / div;
        float vMax = height / div;
        float du = 0.0F;
        float dv = 0.0F;
        if (this.center) {
            du = (div - width) / (div * 2.0F);
            dv = (div - height) / (div * 2.0F);
        }

        switch (this.scaleMode) {
            case 1:
                div = Math.max(width, height);
                uMax = this.scale * width / div;
                vMax = this.scale * height / div;
                if (this.center) {
                    du = this.scale * (div - width) / (div * 2.0F);
                    dv = this.scale * (div - height) / (div * 2.0F);
                }
                break;
            case 2:
                uMax = this.scale;
                vMax = this.scale;
                du = 0.0F;
                dv = 0.0F;
        }

        BufferBuilder bufferbuilder = tessellator.begin(PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.addVertex(0.0F, height, 0.0F).setUv(du, dv + vMax).setColor(255, 255, 255, 255);
        bufferbuilder.addVertex(width, height, 0.0F).setUv(du + uMax, dv + vMax).setColor(255, 255, 255, 255);
        bufferbuilder.addVertex(width, 0.0F, 0.0F).setUv(du + uMax, dv).setColor(255, 255, 255, 255);
        bufferbuilder.addVertex(0.0F, 0.0F, 0.0F).setUv(du, dv).setColor(255, 255, 255, 255);
        BufferUploader.draw(
            RenderPipelines.GUI_TEXTURED, bufferbuilder.buildOrThrow(), () -> "CustomLoadingScreen.background", this.locationTexture, FilterMode.NEAREST
        );
    }
}
