package net.optifine.shaders;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class SVertexFormat {
    public static final int vertexSizeBlock = 18;
    public static final int offsetMidBlock = 8;
    public static final int offsetMidTexCoord = 9;
    public static final int offsetTangent = 11;
    public static final int offsetEntity = 13;
    public static final int offsetVelocity = 15;
    public static final GpuFormat SHADERS_NORMAL_FORMAT = GpuFormat.RGB8_SNORM;
    public static final GpuFormat SHADERS_MIDBLOCK_3B = GpuFormat.RGB8_SNORM;
    public static final GpuFormat SHADERS_MIDTEXCOORD_2F = GpuFormat.RG32_FLOAT;
    public static final GpuFormat SHADERS_TANGENT_4S = GpuFormat.RGBA16_SINT;
    public static final GpuFormat SHADERS_MC_ENTITY_4S = GpuFormat.RGBA16_UINT;
    public static final GpuFormat SHADERS_VELOCITY_3F = GpuFormat.RGB32_FLOAT;
    public static final GpuFormat PADDING_1B = GpuFormat.R8_UINT;
    public static int POSITION_ATTRIBUTE_INDEX = VertexFormatElement.makeAttributeIndex("Position");
    public static int COLOR_ATTRIBUTE_INDEX = VertexFormatElement.makeAttributeIndex("Color");
    public static int UV0_ATTRIBUTE_INDEX = VertexFormatElement.makeAttributeIndex("UV0");
    public static int UV1_ATTRIBUTE_INDEX = VertexFormatElement.makeAttributeIndex("UV1");
    public static int UV2_ATTRIBUTE_INDEX = VertexFormatElement.makeAttributeIndex("UV2");
    public static int NORMAL_ATTRIBUTE_INDEX = VertexFormatElement.makeAttributeIndex("Normal");
    public static int LINE_WIDTH_ATTRIBUTE_INDEX = VertexFormatElement.makeAttributeIndex("LineWidth");

    public static VertexFormat makeExtendedFormatBlock(VertexFormat blockVanilla) {
        VertexFormat.Builder builder = new VertexFormat.Builder(blockVanilla.getStepRate());
        builder.addAll(blockVanilla);
        builder.addAttribute("Normal", SHADERS_NORMAL_FORMAT);
        builder.addAttribute("PaddingN", PADDING_1B);
        builder.addAttribute("MidOffset", SHADERS_MIDBLOCK_3B);
        builder.addAttribute("PaddingMO", PADDING_1B);
        builder.addAttribute("MidTexCoord", SHADERS_MIDTEXCOORD_2F);
        builder.addAttribute("Tangent", SHADERS_TANGENT_4S);
        builder.addAttribute("McEntity", SHADERS_MC_ENTITY_4S);
        builder.addAttribute("Velocity", SHADERS_VELOCITY_3F);
        VertexFormat vf = builder.build();
        vf.setExtended(true);
        return vf;
    }

    public static VertexFormat makeExtendedFormatEntity(VertexFormat entityVanilla) {
        VertexFormat.Builder builder = new VertexFormat.Builder(entityVanilla.getStepRate());
        builder.addAll(entityVanilla);
        builder.addAttribute("MidTexCoord", SHADERS_MIDTEXCOORD_2F);
        builder.addAttribute("Tangent", SHADERS_TANGENT_4S);
        builder.addAttribute("McEntity", SHADERS_MC_ENTITY_4S);
        builder.addAttribute("Velocity", SHADERS_VELOCITY_3F);
        VertexFormat vf = builder.build();
        vf.setExtended(true);
        return vf;
    }

    public static int removeExtendedElements(int maskElements) {
        int lastElemId = DefaultVertexFormat.ENTITY_VANILLA.getElements().size() - 1;
        int maskLastElem = 1 << lastElemId;
        int maskVanilla = (maskLastElem << 1) - 1;
        return maskElements & maskVanilla;
    }
}
