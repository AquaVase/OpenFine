package net.optifine.shaders.vulkan;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPassDescriptor;
import com.mojang.blaze3d.vulkan.VulkanCommandEncoder;
import com.mojang.blaze3d.vulkan.VulkanConst;
import com.mojang.blaze3d.vulkan.VulkanDevice;
import com.mojang.blaze3d.vulkan.VulkanGpuBuffer;
import com.mojang.blaze3d.vulkan.VulkanGpuTextureView;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.optifine.shaders.config.ShaderType;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.spvc.Spvc;
import org.lwjgl.vulkan.KHRDynamicRendering;
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
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderingAttachmentInfo;
import org.lwjgl.vulkan.VkRenderingInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkViewport;

public class VulkanCompositeRenderer implements AutoCloseable {
    private final VulkanDevice device;
    private final VulkanShaderRuntime.DeviceProgram program;
    private final VulkanShaderRuntime runtime;
    private final Map<TargetFormats, Long> pipelines = new LinkedHashMap<>();
    private final List<VertexInput> inputs = new ArrayList<>();
    private GpuBuffer vertices;
    private GpuBuffer indices;
    private boolean closed;

    public VulkanCompositeRenderer(VulkanDevice device, VulkanShaderRuntime.DeviceProgram program, VulkanShaderRuntime runtime) {
        this.device = device;
        this.program = program;
        this.runtime = runtime;
        VulkanShaderModule vertex = program.source().getModules().get(ShaderType.VERTEX);
        if (vertex == null) {
            throw new IllegalArgumentException("Composite draw requires a vertex program");
        }
        for (VulkanShaderModule.Variable input : vertex.getInputs()) {
            String[] signature = input.signature().split(":");
            if (signature.length != 4 || !signature[1].equals("32") || !signature[3].equals("1") || input.locationCount() != 1) {
                throw new IllegalArgumentException("Unsupported composite vertex input: " + input.name());
            }
            int type = Integer.parseInt(signature[0]);
            int format = switch (type) {
                case Spvc.SPVC_BASETYPE_FP32 -> VK12.VK_FORMAT_R32G32B32A32_SFLOAT;
                case Spvc.SPVC_BASETYPE_INT32 -> VK12.VK_FORMAT_R32G32B32A32_SINT;
                case Spvc.SPVC_BASETYPE_UINT32 -> VK12.VK_FORMAT_R32G32B32A32_UINT;
                default -> throw new IllegalArgumentException("Unsupported composite vertex type: " + input.name());
            };
            int location = vertex.getSpirv().asIntBuffer().get(input.locationOffset());
            this.inputs.add(new VertexInput(input.name(), location, format, type == Spvc.SPVC_BASETYPE_FP32));
        }
        try {
            if (!this.inputs.isEmpty()) {
                ByteBuffer data = ByteBuffer.allocateDirect(4 * this.inputs.size() * 16).order(ByteOrder.nativeOrder());
                float[][] corners = {{0, 0}, {1, 0}, {1, 1}, {0, 1}};
                for (float[] corner : corners) {
                    for (VertexInput input : this.inputs) {
                        float[] value = switch (input.name()) {
                            case "vaPosition", "Position" -> new float[]{corner[0], corner[1], 0, 1};
                            case "vaUV0", "UV0" -> new float[]{corner[0], corner[1], 0, 1};
                            case "vaColor", "Color" -> new float[]{1, 1, 1, 1};
                            case "vaNormal", "Normal" -> new float[]{0, 1, 0, 0};
                            case "vaUV2", "UV2" -> new float[]{240, 240, 0, 1};
                            default -> throw new IllegalArgumentException("Unknown composite vertex semantic: " + input.name());
                        };
                        for (float component : value) {
                            data.putInt(input.floating() ? Float.floatToRawIntBits(component) : (int)component);
                        }
                    }
                }
                data.flip();
                this.vertices = device.createBuffer(() -> "OptiFine composite vertices", GpuBuffer.USAGE_VERTEX, data);
            }
            ByteBuffer data = ByteBuffer.allocateDirect(12).order(ByteOrder.nativeOrder());
            data.asShortBuffer().put(new short[]{0, 1, 2, 0, 2, 3});
            this.indices = device.createBuffer(() -> "OptiFine composite indices", GpuBuffer.USAGE_INDEX, data);
        } catch (RuntimeException | Error e) {
            this.close();
            throw e;
        }
    }

    public void draw(RenderPassDescriptor target, boolean writeDepth, List<VulkanBlendState> blends) {
        if (this.closed) {
            throw new IllegalStateException("Composite renderer is closed");
        }
        var area = target.renderArea;
        if (area == null || area.width() <= 0 || area.height() <= 0 || writeDepth && target.depthAttachment() == null) {
            throw new IllegalArgumentException("Invalid composite render area or missing depth attachment");
        }
        List<Integer> colors = new ArrayList<>();
        for (var attachment : target.colorAttachments()) {
            colors.add(attachment == null ? VK12.VK_FORMAT_UNDEFINED : VulkanConst.toVk(attachment.textureView().texture().getFormat()));
        }
        int depth = target.depthAttachment() == null ? VK12.VK_FORMAT_UNDEFINED
            : VulkanConst.toVk(target.depthAttachment().textureView().texture().getFormat());
        TargetFormats formats = new TargetFormats(List.copyOf(colors), depth, writeDepth, blends);
        long pipeline = this.pipelines.computeIfAbsent(formats, this::createPipeline);
        try (VulkanDrawBindings bindings = new VulkanDrawBindings(this.device, this.program, this.runtime, Map.of());
             MemoryStack stack = MemoryStack.stackPush()) {
            VulkanCommandEncoder encoder = this.device.createCommandEncoder();
            VkCommandBuffer commands = encoder.allocateAndBeginTransientCommandBuffer();
            VulkanCommandEncoder.memoryBarrier(commands, stack);
            VkRenderingAttachmentInfo.Buffer attachments = VkRenderingAttachmentInfo.calloc(colors.size(), stack);
            for (int i = 0; i < colors.size(); i++) {
                var attachment = target.colorAttachments().get(i);
                attachments.get(i).sType$Default().imageLayout(VK12.VK_IMAGE_LAYOUT_GENERAL)
                    .loadOp(VK12.VK_ATTACHMENT_LOAD_OP_LOAD).storeOp(VK12.VK_ATTACHMENT_STORE_OP_STORE);
                if (attachment != null) {
                    attachments.get(i).imageView(((VulkanGpuTextureView)attachment.textureView()).vkImageView());
                    if (attachment.clearValue().isPresent()) {
                        var color = attachment.clearValue().get();
                        attachments.get(i).loadOp(VK12.VK_ATTACHMENT_LOAD_OP_CLEAR);
                        attachments.get(i).clearValue().color().float32(0, color.x()).float32(1, color.y()).float32(2, color.z()).float32(3, color.w());
                    }
                } else {
                    attachments.get(i).imageLayout(VK12.VK_IMAGE_LAYOUT_UNDEFINED)
                        .loadOp(VK12.VK_ATTACHMENT_LOAD_OP_DONT_CARE).storeOp(VK12.VK_ATTACHMENT_STORE_OP_DONT_CARE);
                }
            }
            VkRenderingInfo info = VkRenderingInfo.calloc(stack).sType$Default().layerCount(1).pColorAttachments(attachments);
            info.renderArea().offset().set(area.x(), area.y());
            info.renderArea().extent().set(area.width(), area.height());
            if (target.depthAttachment() != null) {
                VkRenderingAttachmentInfo attachment = VkRenderingAttachmentInfo.calloc(stack).sType$Default()
                    .imageView(((VulkanGpuTextureView)target.depthAttachment().textureView()).vkImageView()).imageLayout(VK12.VK_IMAGE_LAYOUT_GENERAL)
                    .loadOp(VK12.VK_ATTACHMENT_LOAD_OP_LOAD).storeOp(VK12.VK_ATTACHMENT_STORE_OP_STORE);
                if (target.depthAttachment().clearValue().isPresent()) {
                    attachment.loadOp(VK12.VK_ATTACHMENT_LOAD_OP_CLEAR);
                    attachment.clearValue().depthStencil().depth((float)target.depthAttachment().clearValue().getAsDouble());
                }
                info.pDepthAttachment(attachment);
            }
            KHRDynamicRendering.vkCmdBeginRenderingKHR(commands, info);
            VkViewport.Buffer viewport = VkViewport.calloc(1, stack).x(area.x()).y(area.y()).width(area.width()).height(area.height()).minDepth(0).maxDepth(1);
            VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);
            scissor.offset().set(area.x(), area.y());
            scissor.extent().set(area.width(), area.height());
            VK12.vkCmdSetViewport(commands, 0, viewport);
            VK12.vkCmdSetScissor(commands, 0, scissor);
            VK12.vkCmdBindPipeline(commands, VK12.VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline);
            bindings.bind(commands, VK12.VK_PIPELINE_BIND_POINT_GRAPHICS);
            if (this.vertices != null) {
                VK12.vkCmdBindVertexBuffers(commands, 0, stack.longs(((VulkanGpuBuffer)this.vertices).vkBuffer()), stack.longs(0));
            }
            VK12.vkCmdBindIndexBuffer(commands, ((VulkanGpuBuffer)this.indices).vkBuffer(), 0, VK12.VK_INDEX_TYPE_UINT16);
            VK12.vkCmdDrawIndexed(commands, 6, 1, 0, 0, 0);
            KHRDynamicRendering.vkCmdEndRenderingKHR(commands);
            VulkanCommandEncoder.memoryBarrier(commands, stack);
            VulkanDeviceProgram.check(VK12.vkEndCommandBuffer(commands), "end composite command buffer");
            encoder.execute(commands);
        }
    }

    private long createPipeline(TargetFormats formats) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPipelineVertexInputStateCreateInfo vertexInput = VkPipelineVertexInputStateCreateInfo.calloc(stack).sType$Default();
            if (!this.inputs.isEmpty()) {
                VkVertexInputBindingDescription.Buffer bindings = VkVertexInputBindingDescription.calloc(1, stack).binding(0)
                    .stride(this.inputs.size() * 16).inputRate(VK12.VK_VERTEX_INPUT_RATE_VERTEX);
                VkVertexInputAttributeDescription.Buffer attributes = VkVertexInputAttributeDescription.calloc(this.inputs.size(), stack);
                for (int i = 0; i < this.inputs.size(); i++) {
                    VertexInput input = this.inputs.get(i);
                    attributes.get(i).location(input.location()).binding(0).format(input.format()).offset(i * 16);
                }
                vertexInput.pVertexBindingDescriptions(bindings).pVertexAttributeDescriptions(attributes);
            }
            VkPipelineInputAssemblyStateCreateInfo assembly = VkPipelineInputAssemblyStateCreateInfo.calloc(stack).sType$Default()
                .topology(VK12.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
            VkPipelineViewportStateCreateInfo viewport = VkPipelineViewportStateCreateInfo.calloc(stack).sType$Default().viewportCount(1).scissorCount(1);
            VkPipelineRasterizationStateCreateInfo raster = VkPipelineRasterizationStateCreateInfo.calloc(stack).sType$Default()
                .polygonMode(VK12.VK_POLYGON_MODE_FILL).cullMode(VK12.VK_CULL_MODE_NONE).frontFace(VK12.VK_FRONT_FACE_COUNTER_CLOCKWISE).lineWidth(1);
            VkPipelineMultisampleStateCreateInfo multisample = VkPipelineMultisampleStateCreateInfo.calloc(stack).sType$Default().rasterizationSamples(VK12.VK_SAMPLE_COUNT_1_BIT);
            VkPipelineColorBlendAttachmentState.Buffer attachments = VkPipelineColorBlendAttachmentState.calloc(formats.colors().size(), stack);
            for (int i = 0; i < attachments.capacity(); i++) {
                var attachment = attachments.get(i).colorWriteMask(formats.colors().get(i) == VK12.VK_FORMAT_UNDEFINED ? 0 : 15);
                formats.blends().get(i).apply(attachment);
                VulkanBlendState.finish(attachment, formats.colors().get(i));
            }
            VkPipelineColorBlendStateCreateInfo blend = VkPipelineColorBlendStateCreateInfo.calloc(stack).sType$Default().pAttachments(attachments);
            VkPipelineDepthStencilStateCreateInfo depth = VkPipelineDepthStencilStateCreateInfo.calloc(stack).sType$Default()
                .depthTestEnable(formats.writeDepth()).depthWriteEnable(formats.writeDepth()).depthCompareOp(VK12.VK_COMPARE_OP_ALWAYS);
            VkPipelineDynamicStateCreateInfo dynamic = VkPipelineDynamicStateCreateInfo.calloc(stack).sType$Default()
                .pDynamicStates(stack.ints(VK12.VK_DYNAMIC_STATE_VIEWPORT, VK12.VK_DYNAMIC_STATE_SCISSOR));
            VkPipelineRenderingCreateInfo rendering = VkPipelineRenderingCreateInfo.calloc(stack).sType$Default()
                .pColorAttachmentFormats(stack.ints(formats.colors().stream().mapToInt(Integer::intValue).toArray())).depthAttachmentFormat(formats.depth());
            VkGraphicsPipelineCreateInfo.Buffer pipeline = VkGraphicsPipelineCreateInfo.calloc(1, stack).sType$Default().pNext(rendering.address())
                .pVertexInputState(vertexInput).pInputAssemblyState(assembly).pViewportState(viewport).pRasterizationState(raster)
                .pMultisampleState(multisample).pColorBlendState(blend).pDepthStencilState(depth).pDynamicState(dynamic);
            return this.program.deviceProgram().createGraphicsPipeline(pipeline);
        }
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.closed = true;
            if (this.vertices != null) {
                this.vertices.close();
            }
            if (this.indices != null) {
                this.indices.close();
            }
            this.pipelines.clear();
        }
    }

    private record VertexInput(String name, int location, int format, boolean floating) {
    }

    private record TargetFormats(List<Integer> colors, int depth, boolean writeDepth, List<VulkanBlendState> blends) {
    }
}
