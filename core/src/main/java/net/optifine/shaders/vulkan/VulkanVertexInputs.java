package net.optifine.shaders.vulkan;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.blaze3d.vulkan.VulkanConst;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.spvc.Spvc;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDivisorDescriptionEXT;
import org.lwjgl.vulkan.VkPipelineVertexInputDivisorStateCreateInfoEXT;

public class VulkanVertexInputs {
    private final List<Attribute> attributes = new ArrayList<>();
    private final List<Binding> bindings = new ArrayList<>();
    private final List<DefaultAttribute> defaults = new ArrayList<>();
    private final int defaultBinding;
    private final ByteBuffer convertedSpirv;

    public VulkanVertexInputs(VulkanShaderModule vertex, VertexFormat[] formats) {
        this.defaultBinding = formats.length;
        Map<Integer, Boolean> conversions = new HashMap<>();
        for (int i = 0; i < formats.length; i++) {
            if (formats[i] != null) {
                this.bindings.add(new Binding(i, formats[i].getVertexSize(), formats[i].getStepRate()));
            }
        }
        for (VulkanShaderModule.Variable input : vertex.getInputs()) {
            String semantic = semantic(input.name());
            String[] signature = input.signature().split(":");
            if (signature.length != 4 || !signature[1].equals("32") || !signature[3].equals("1") || input.locationCount() != 1) {
                throw new IllegalArgumentException("Unsupported scene vertex input: " + input.name());
            }
            int baseType = Integer.parseInt(signature[0]);
            int location = vertex.getSpirv().asIntBuffer().get(input.locationOffset());
            VertexFormatElement element = null;
            int slot = 0;
            for (; slot < formats.length; slot++) {
                if (formats[slot] != null && (element = formats[slot].getElement(semantic)) != null) {
                    break;
                }
            }
            if (element != null) {
                if (baseType == Spvc.SPVC_BASETYPE_FP32 && isInteger32(element.format())) {
                    conversions.put(location, element.format().componentType() == GpuFormat.ComponentType.SINT_32);
                }
                this.attributes.add(new Attribute(location, slot, format(element.format(), semantic, baseType), element.offset()));
            } else {
                int format = switch (baseType) {
                    case Spvc.SPVC_BASETYPE_FP32 -> VK12.VK_FORMAT_R32G32B32A32_SFLOAT;
                    case Spvc.SPVC_BASETYPE_INT32 -> VK12.VK_FORMAT_R32G32B32A32_SINT;
                    case Spvc.SPVC_BASETYPE_UINT32 -> VK12.VK_FORMAT_R32G32B32A32_UINT;
                    default -> throw new IllegalArgumentException("Unsupported scene vertex type: " + input.name());
                };
                this.attributes.add(new Attribute(location, this.defaultBinding, format, this.defaults.size() * 16));
                this.defaults.add(new DefaultAttribute(semantic, baseType == Spvc.SPVC_BASETYPE_FP32));
            }
        }
        if (!this.defaults.isEmpty()) {
            this.bindings.add(new Binding(this.defaultBinding, 0, 0));
        }
        this.convertedSpirv = VulkanVertexConversions.convert(vertex.getSpirv(), conversions);
    }

    public ByteBuffer getConvertedSpirv() {
        return this.convertedSpirv == null ? null : this.convertedSpirv.asReadOnlyBuffer().order(this.convertedSpirv.order());
    }

    private static boolean isInteger32(GpuFormat format) {
        return switch (format) {
            case R32_SINT, RG32_SINT, RGB32_SINT, RGBA32_SINT, R32_UINT, RG32_UINT, RGB32_UINT, RGBA32_UINT -> true;
            default -> false;
        };
    }

    public VkPipelineVertexInputStateCreateInfo createInfo(MemoryStack stack) {
        VkVertexInputBindingDescription.Buffer bindings = VkVertexInputBindingDescription.calloc(this.bindings.size(), stack);
        VkVertexInputBindingDivisorDescriptionEXT.Buffer divisors = VkVertexInputBindingDivisorDescriptionEXT.calloc(this.bindings.size(), stack);
        for (Binding binding : this.bindings) {
            bindings.get().binding(binding.slot()).stride(binding.stride()).inputRate(binding.stepRate() > 0 ? VK12.VK_VERTEX_INPUT_RATE_INSTANCE : VK12.VK_VERTEX_INPUT_RATE_VERTEX);
            if (binding.stepRate() > 0) {
                divisors.get().binding(binding.slot()).divisor(binding.stepRate());
            }
        }
        VkVertexInputAttributeDescription.Buffer attributes = VkVertexInputAttributeDescription.calloc(this.attributes.size(), stack);
        for (Attribute attribute : this.attributes) {
            attributes.get().location(attribute.location()).binding(attribute.slot()).format(attribute.format()).offset(attribute.offset());
        }
        VkPipelineVertexInputStateCreateInfo info = VkPipelineVertexInputStateCreateInfo.calloc(stack).sType$Default()
            .pVertexBindingDescriptions(bindings.flip()).pVertexAttributeDescriptions(attributes.flip());
        if (divisors.position() > 0) {
            info.pNext(VkPipelineVertexInputDivisorStateCreateInfoEXT.calloc(stack).sType$Default().pVertexBindingDivisors(divisors.flip()));
        }
        return info;
    }

    public List<DefaultAttribute> getDefaults() {
        return List.copyOf(this.defaults);
    }

    public int getDefaultBinding() {
        return this.defaultBinding;
    }

    private static String semantic(String name) {
        return switch (name) {
            case "vaPosition", "Position" -> "Position";
            case "vaColor", "Color" -> "Color";
            case "vaNormal", "Normal" -> "Normal";
            case "vaUV0", "UV0" -> "UV0";
            case "vaUV1", "UV1" -> "UV1";
            case "vaUV2", "UV2" -> "UV2";
            case "vaLineWidth", "LineWidth" -> "LineWidth";
            case "at_midBlock", "MidOffset" -> "MidOffset";
            case "mc_midTexCoord", "MidTexCoord" -> "MidTexCoord";
            case "at_tangent", "Tangent" -> "Tangent";
            case "mc_Entity", "McEntity" -> "McEntity";
            case "at_velocity", "Velocity" -> "Velocity";
            default -> throw new IllegalArgumentException("Unknown shader vertex semantic: " + name);
        };
    }

    private static int format(GpuFormat format, String semantic, int baseType) {
        if (baseType != Spvc.SPVC_BASETYPE_FP32) {
            return VulkanConst.toVk(format);
        }
        if (semantic.equals("Tangent") && format == GpuFormat.RGBA16_SINT) {
            return VK12.VK_FORMAT_R16G16B16A16_SNORM;
        }
        // Legacy floating inputs accept integer attributes through glVertexAttribPointer.
        return switch (format) {
            case R8_SINT -> VK12.VK_FORMAT_R8_SSCALED;
            case RG8_SINT -> VK12.VK_FORMAT_R8G8_SSCALED;
            case RGB8_SINT -> VK12.VK_FORMAT_R8G8B8_SSCALED;
            case RGBA8_SINT -> VK12.VK_FORMAT_R8G8B8A8_SSCALED;
            case R8_UINT -> VK12.VK_FORMAT_R8_USCALED;
            case RG8_UINT -> VK12.VK_FORMAT_R8G8_USCALED;
            case RGB8_UINT -> VK12.VK_FORMAT_R8G8B8_USCALED;
            case RGBA8_UINT -> VK12.VK_FORMAT_R8G8B8A8_USCALED;
            case R16_SINT -> VK12.VK_FORMAT_R16_SSCALED;
            case RG16_SINT -> VK12.VK_FORMAT_R16G16_SSCALED;
            case RGB16_SINT -> VK12.VK_FORMAT_R16G16B16_SSCALED;
            case RGBA16_SINT -> VK12.VK_FORMAT_R16G16B16A16_SSCALED;
            case R16_UINT -> VK12.VK_FORMAT_R16_USCALED;
            case RG16_UINT -> VK12.VK_FORMAT_R16G16_USCALED;
            case RGB16_UINT -> VK12.VK_FORMAT_R16G16B16_USCALED;
            case RGBA16_UINT -> VK12.VK_FORMAT_R16G16B16A16_USCALED;
            default -> VulkanConst.toVk(format);
        };
    }

    public record DefaultAttribute(String semantic, boolean floating) {
    }

    private record Attribute(int location, int slot, int format, int offset) {
    }

    private record Binding(int slot, int stride, int stepRate) {
    }
}

