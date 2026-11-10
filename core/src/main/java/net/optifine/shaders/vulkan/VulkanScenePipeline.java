package net.optifine.shaders.vulkan;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vulkan.VulkanConst;
import com.mojang.blaze3d.vulkan.VulkanDevice;
import com.mojang.blaze3d.vulkan.VulkanGpuBuffer;
import java.nio.ByteOrder;
import java.util.List;
import net.optifine.shaders.config.ShaderType;
import net.optifine.shaders.Shaders;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRenderingCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;

public class VulkanScenePipeline {
    private final VulkanDevice device;
    private final VulkanVertexInputs vertexInputs;
    private final long pipeline;

    public VulkanScenePipeline(VulkanDevice device, VulkanShaderRuntime.DeviceProgram program, RenderPipeline state, List<Integer> colorFormats, int depthFormat, boolean shadow, boolean shadowCull, Boolean cull, Boolean depthWrite, List<VulkanBlendState> blends) {
        this.device = device;
        this.vertexInputs = new VulkanVertexInputs(program.source().getModules().get(ShaderType.VERTEX), state.getVertexFormatBindings());
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var assembly = VkPipelineInputAssemblyStateCreateInfo.calloc(stack).sType$Default().topology(VulkanConst.toVk(state.getPrimitiveTopology()));
            var raster = VkPipelineRasterizationStateCreateInfo.calloc(stack).sType$Default().polygonMode(VulkanConst.toVk(state.getPolygonMode()))
                .cullMode((shadow ? shadowCull : cull != null ? cull : state.isCull()) ? VK12.VK_CULL_MODE_BACK_BIT : VK12.VK_CULL_MODE_NONE).frontFace(VK12.VK_FRONT_FACE_CLOCKWISE).lineWidth(1);
            var depth = VkPipelineDepthStencilStateCreateInfo.calloc(stack).sType$Default();
            if (state.getDepthStencilState() != null && depthFormat != VK12.VK_FORMAT_UNDEFINED) {
                var depthState = state.getDepthStencilState();
                depth.depthTestEnable(true).depthWriteEnable(depthState.writeDepth()).depthCompareOp(VulkanConst.toVk(depthState.depthTest()));
                raster.depthBiasEnable(depthState.depthBiasConstant() != 0 || depthState.depthBiasScaleFactor() != 0)
                    .depthBiasConstantFactor(depthState.depthBiasConstant()).depthBiasSlopeFactor(depthState.depthBiasScaleFactor());
            }
            if (depthWrite != null && depthFormat != VK12.VK_FORMAT_UNDEFINED) {
                depth.depthWriteEnable(depthWrite);
            }
            if (shadow && depthFormat != VK12.VK_FORMAT_UNDEFINED) {
                depth.depthTestEnable(true).depthWriteEnable(true).depthCompareOp(VK12.VK_COMPARE_OP_LESS_OR_EQUAL);
            }
            var colorStates = state.getColorTargetStates();
            var attachments = VkPipelineColorBlendAttachmentState.calloc(colorFormats.size(), stack);
            for (int i = 0; i < colorFormats.size(); i++) {
                var colorState = colorStates.length == 0 ? null : colorStates[Math.min(i, colorStates.length - 1)];
                var attachment = attachments.get(i).colorWriteMask(colorFormats.get(i) == VK12.VK_FORMAT_UNDEFINED ? 0
                    : shadow || colorState == null ? 15 : VulkanConst.toVk(colorState));
                if (!shadow && colorState != null && colorState.blendFunction().isPresent()) {
                    var blend = colorState.blendFunction().get();
                    attachment.blendEnable(true).colorBlendOp(VulkanConst.toVk(blend.color().op())).alphaBlendOp(VulkanConst.toVk(blend.alpha().op()))
                        .srcColorBlendFactor(VulkanConst.toVk(blend.color().sourceFactor())).dstColorBlendFactor(VulkanConst.toVk(blend.color().destFactor()))
                        .srcAlphaBlendFactor(VulkanConst.toVk(blend.alpha().sourceFactor())).dstAlphaBlendFactor(VulkanConst.toVk(blend.alpha().destFactor()));
                }
                if (!shadow) {
                    blends.get(i).apply(attachment);
                }
                VulkanBlendState.finish(attachment, colorFormats.get(i));
            }
            var blend = VkPipelineColorBlendStateCreateInfo.calloc(stack).sType$Default().pAttachments(attachments);
            var viewport = VkPipelineViewportStateCreateInfo.calloc(stack).sType$Default().viewportCount(1).scissorCount(1);
            var multisample = VkPipelineMultisampleStateCreateInfo.calloc(stack).sType$Default().rasterizationSamples(VK12.VK_SAMPLE_COUNT_1_BIT);
            var dynamic = VkPipelineDynamicStateCreateInfo.calloc(stack).sType$Default().pDynamicStates(stack.ints(VK12.VK_DYNAMIC_STATE_VIEWPORT, VK12.VK_DYNAMIC_STATE_SCISSOR));
            var rendering = VkPipelineRenderingCreateInfo.calloc(stack).sType$Default().depthAttachmentFormat(depthFormat)
                .pColorAttachmentFormats(stack.ints(colorFormats.stream().mapToInt(Integer::intValue).toArray()));
            var pipeline = VkGraphicsPipelineCreateInfo.calloc(1, stack).sType$Default().pNext(rendering.address())
                .pVertexInputState(this.vertexInputs.createInfo(stack)).pInputAssemblyState(assembly).pRasterizationState(raster)
                .pDepthStencilState(depth).pColorBlendState(blend).pViewportState(viewport).pMultisampleState(multisample).pDynamicState(dynamic);
            this.pipeline = program.deviceProgram().createGraphicsPipeline(pipeline, this.vertexInputs.getConvertedSpirv());
        }
    }

    public void bind(VkCommandBuffer commands, VulkanShaderRuntime runtime) {
        VK12.vkCmdBindPipeline(commands, VK12.VK_PIPELINE_BIND_POINT_GRAPHICS, this.pipeline);
        List<VulkanVertexInputs.DefaultAttribute> defaults = this.vertexInputs.getDefaults();
        if (!defaults.isEmpty()) {
            try (var mapping = this.device.createCommandEncoder().transientMemory().allocateGpuMapped(defaults.size() * 16L, 4, GpuBuffer.USAGE_VERTEX);
                 MemoryStack stack = MemoryStack.stackPush()) {
                var bytes = mapping.data().order(ByteOrder.nativeOrder());
                for (var attribute : defaults) {
                    for (float value : runtime.getDefaultAttribute(attribute.semantic())) {
                        bytes.putInt(attribute.floating() ? Float.floatToRawIntBits(value) : (int)value);
                    }
                }
                VK12.vkCmdBindVertexBuffers(commands, this.vertexInputs.getDefaultBinding(),
                    stack.longs(((VulkanGpuBuffer)mapping.slice().buffer()).vkBuffer()), stack.longs(mapping.slice().offset()));
            }
        }
    }
}
