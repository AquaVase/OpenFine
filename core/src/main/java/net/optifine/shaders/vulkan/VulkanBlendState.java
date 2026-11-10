package net.optifine.shaders.vulkan;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.vulkan.VulkanConst;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.optifine.render.GlBlendState;
import net.optifine.shaders.Program;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;

// Copy mutable pack properties before using them in a pipeline cache key.
public record VulkanBlendState(boolean defined, boolean enabled, int source, int destination, int sourceAlpha, int destinationAlpha) {
    private static final VulkanBlendState INHERIT = new VulkanBlendState(false, false, 1, 0, 1, 0);
    private static final ThreadLocal<List<VulkanBlendState>> capturedStates = ThreadLocal.withInitial(List::of);
    private static final Set<Integer> INTEGER_FORMATS = Arrays.stream(GpuFormat.values()).filter(format -> switch (format.componentType()) {
        case UINT_8, SINT_8, UINT_16, SINT_16, UINT_32, SINT_32 -> true;
        default -> false;
    }).map(VulkanConst::toVk).collect(Collectors.toUnmodifiableSet());

    public static void finish(VkPipelineColorBlendAttachmentState attachment, int format) {
        // OpenGL bypasses blending for integer color attachments.
        if (format == VK12.VK_FORMAT_UNDEFINED || INTEGER_FORMATS.contains(format)) {
            attachment.blendEnable(false);
        }
    }

    public static List<VulkanBlendState> capture(Program program, int count) {
        GlBlendState global = program == null ? null : program.getBlendState();
        GlBlendState[] indexed = program == null ? null : program.getBlendStatesIndexed();
        List<VulkanBlendState> previous = capturedStates.get();
        if (previous.size() == count) {
            boolean unchanged = true;
            for (int i = 0; i < count; i++) {
                GlBlendState state = indexed != null && i < indexed.length && indexed[i] != null ? indexed[i] : global;
                if (!previous.get(i).matches(state)) {
                    unchanged = false;
                    break;
                }
            }
            if (unchanged) {
                return previous;
            }
        }
        List<VulkanBlendState> states = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            GlBlendState state = global;
            if (indexed != null && i < indexed.length && indexed[i] != null) {
                state = indexed[i];
            }
            states.add(state == null ? INHERIT : new VulkanBlendState(true, state.isEnabled(), state.getSrcFactor(), state.getDstFactor(),
                state.getSrcFactorAlpha(), state.getDstFactorAlpha()));
        }
        List<VulkanBlendState> captured = List.copyOf(states);
        capturedStates.set(captured);
        return captured;
    }

    private boolean matches(GlBlendState state) {
        return state == null ? !this.defined : this.defined && this.enabled == state.isEnabled()
            && this.source == state.getSrcFactor() && this.destination == state.getDstFactor()
            && this.sourceAlpha == state.getSrcFactorAlpha() && this.destinationAlpha == state.getDstFactorAlpha();
    }

    public void apply(VkPipelineColorBlendAttachmentState attachment) {
        if (this.defined) {
            attachment.blendEnable(this.enabled)
                .srcColorBlendFactor(factor(this.source)).dstColorBlendFactor(factor(this.destination))
                .srcAlphaBlendFactor(factor(this.sourceAlpha)).dstAlphaBlendFactor(factor(this.destinationAlpha));
        }
    }

    private static int factor(int value) {
        return switch (value) {
            case 0 -> VK12.VK_BLEND_FACTOR_ZERO;
            case 1 -> VK12.VK_BLEND_FACTOR_ONE;
            case 768 -> VK12.VK_BLEND_FACTOR_SRC_COLOR;
            case 769 -> VK12.VK_BLEND_FACTOR_ONE_MINUS_SRC_COLOR;
            case 770 -> VK12.VK_BLEND_FACTOR_SRC_ALPHA;
            case 771 -> VK12.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA;
            case 772 -> VK12.VK_BLEND_FACTOR_DST_ALPHA;
            case 773 -> VK12.VK_BLEND_FACTOR_ONE_MINUS_DST_ALPHA;
            case 774 -> VK12.VK_BLEND_FACTOR_DST_COLOR;
            case 775 -> VK12.VK_BLEND_FACTOR_ONE_MINUS_DST_COLOR;
            case 776 -> VK12.VK_BLEND_FACTOR_SRC_ALPHA_SATURATE;
            case 32769 -> VK12.VK_BLEND_FACTOR_CONSTANT_COLOR;
            case 32770 -> VK12.VK_BLEND_FACTOR_ONE_MINUS_CONSTANT_COLOR;
            case 32771 -> VK12.VK_BLEND_FACTOR_CONSTANT_ALPHA;
            case 32772 -> VK12.VK_BLEND_FACTOR_ONE_MINUS_CONSTANT_ALPHA;
            default -> throw new IllegalArgumentException("Unsupported shader blend factor: " + value);
        };
    }
}
