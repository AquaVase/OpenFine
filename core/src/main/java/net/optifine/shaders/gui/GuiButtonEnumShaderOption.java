package net.optifine.shaders.gui;

import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.resources.language.I18n;
import net.optifine.gui.GuiButtonOF;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.config.EnumShaderOption;

public class GuiButtonEnumShaderOption extends GuiButtonOF {
    private EnumShaderOption enumShaderOption = null;

    public GuiButtonEnumShaderOption(EnumShaderOption enumShaderOption, int x, int y, int widthIn, int heightIn) {
        super(enumShaderOption.ordinal(), x, y, widthIn, heightIn, getButtonText(enumShaderOption));
        this.enumShaderOption = enumShaderOption;
    }

    public EnumShaderOption getEnumShaderOption() {
        return this.enumShaderOption;
    }

    private static String getButtonText(EnumShaderOption eso) {
        String nameText = I18n.get(eso.getResourceKey()) + ": ";
        switch (eso) {
            case ANTIALIASING:
                return nameText + GuiShaders.toStringAa(Shaders.configAntialiasingLevel, eso.getDefaultInt());
            case NORMAL_MAP:
                return nameText + GuiShaders.toStringOnOff(Shaders.configNormalMap, eso.getDefaultBool());
            case SPECULAR_MAP:
                return nameText + GuiShaders.toStringOnOff(Shaders.configSpecularMap, eso.getDefaultBool());
            case RENDER_RES_MUL:
                return nameText + GuiShaders.toStringQuality(Shaders.configRenderResMul, eso.getDefaultFloat());
            case SHADOW_RES_MUL:
                return nameText + GuiShaders.toStringQuality(Shaders.configShadowResMul, eso.getDefaultFloat());
            case HAND_DEPTH_MUL:
                return nameText + GuiShaders.toStringHandDepth(Shaders.configHandDepthMul, eso.getDefaultFloat());
            case CLOUD_SHADOW:
                return nameText + GuiShaders.toStringOnOff(Shaders.configCloudShadow, eso.getDefaultBool());
            case OLD_HAND_LIGHT:
                return nameText + Shaders.configOldHandLight.getUserValue();
            case OLD_LIGHTING:
                return nameText + Shaders.configOldLighting.getUserValue();
            case SHADOW_CLIP_FRUSTRUM:
                return nameText + GuiShaders.toStringOnOff(Shaders.configShadowClipFrustrum, eso.getDefaultBool());
            case TWEAK_BLOCK_DAMAGE:
                return nameText + GuiShaders.toStringOnOff(Shaders.configTweakBlockDamage, eso.getDefaultBool());
            default:
                return nameText + Shaders.getEnumShaderOption(eso);
        }
    }

    public void updateButtonText() {
        this.setMessage(getButtonText(this.enumShaderOption));
    }

    @Override
    protected boolean isValidClickButton(MouseButtonInfo info) {
        return true;
    }
}
