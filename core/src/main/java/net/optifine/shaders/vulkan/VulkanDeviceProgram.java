package net.optifine.shaders.vulkan;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.optifine.shaders.config.ShaderType;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkComputePipelineCreateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

public class VulkanDeviceProgram implements AutoCloseable {
    private final VkDevice device;
    private final Map<ShaderType, Long> modules = new EnumMap<>(ShaderType.class);
    private long descriptorSetLayout;
    private long pipelineLayout;
    private long computePipeline;
    private final List<Long> graphicsPipelines = new ArrayList<>();
    private boolean closed;
    private boolean pushDescriptors;
    private VulkanDescriptorAllocator descriptors;

    VulkanDeviceProgram(VkDevice device, VulkanShaderProgram program) {
        this.device = device;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pointer = stack.callocLong(1);
            for (VulkanShaderModule module : program.getModules().values()) {
                VkShaderModuleCreateInfo info = VkShaderModuleCreateInfo.calloc(stack).sType$Default().pCode(module.getSpirv());
                check(VK12.vkCreateShaderModule(device, info, null, pointer), "create shader module " + module.getName());
                this.modules.put(module.getType(), pointer.get(0));
            }

            VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.calloc(program.getBindings().size(), stack);
            for (VulkanShaderProgram.Binding binding : program.getBindings()) {
                bindings.get(binding.binding()).binding(binding.binding()).descriptorType(binding.descriptorType())
                    .descriptorCount(binding.count()).stageFlags(binding.stageFlags());
            }

            if (device.getCapabilities().VK_KHR_push_descriptor && !"false".equals(System.getProperty("optifine.vulkan.pushDescriptors"))) {
                var push = org.lwjgl.vulkan.VkPhysicalDevicePushDescriptorPropertiesKHR.calloc(stack).sType$Default();
                var properties = org.lwjgl.vulkan.VkPhysicalDeviceProperties2.calloc(stack).sType$Default().pNext(push.address());
                org.lwjgl.vulkan.VK11.vkGetPhysicalDeviceProperties2(device.getPhysicalDevice(), properties);
                this.pushDescriptors = program.getBindings().stream().mapToLong(VulkanShaderProgram.Binding::count).sum() <= push.maxPushDescriptors();
            }
            VkDescriptorSetLayoutCreateInfo setInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default().pBindings(bindings)
                .flags(this.pushDescriptors ? org.lwjgl.vulkan.KHRPushDescriptor.VK_DESCRIPTOR_SET_LAYOUT_CREATE_PUSH_DESCRIPTOR_BIT_KHR : 0);
            check(VK12.vkCreateDescriptorSetLayout(device, setInfo, null, pointer), "create descriptor layout " + program.getName());
            this.descriptorSetLayout = pointer.get(0);
            this.descriptors = new VulkanDescriptorAllocator(device, this.descriptorSetLayout, program.getBindings());
            VkPipelineLayoutCreateInfo layoutInfo = VkPipelineLayoutCreateInfo.calloc(stack).sType$Default().pSetLayouts(stack.longs(this.descriptorSetLayout));
            check(VK12.vkCreatePipelineLayout(device, layoutInfo, null, pointer), "create pipeline layout " + program.getName());
            this.pipelineLayout = pointer.get(0);
            if (this.modules.containsKey(ShaderType.COMPUTE)) {
                VkComputePipelineCreateInfo.Buffer info = VkComputePipelineCreateInfo.calloc(1, stack).sType$Default().layout(this.pipelineLayout);
                info.stage().sType$Default().stage(VK12.VK_SHADER_STAGE_COMPUTE_BIT).module(this.modules.get(ShaderType.COMPUTE)).pName(stack.UTF8("main"));
                pointer.put(0, 0L);
                int result = VK12.vkCreateComputePipelines(device, VK12.VK_NULL_HANDLE, info, null, pointer);
                // Vulkan may return a pipeline even when pipeline creation fails.
                this.computePipeline = pointer.get(0);
                check(result, "create compute pipeline " + program.getName());
            }
        } catch (RuntimeException | Error e) {
            this.close();
            throw e;
        }
    }

    public boolean usesPushDescriptors() {
        return this.pushDescriptors;
    }

    public long getDescriptorSetLayout() {
        this.checkOpen();
        return this.descriptorSetLayout;
    }

    public VulkanDescriptorAllocator getDescriptors() {
        this.checkOpen();
        return this.descriptors;
    }

    public long getPipelineLayout() {
        this.checkOpen();
        return this.pipelineLayout;
    }

    public long getComputePipeline() {
        this.checkOpen();
        if (this.computePipeline == 0L) {
            throw new IllegalStateException("Not a compute program");
        }

        return this.computePipeline;
    }

    public VkPipelineShaderStageCreateInfo.Buffer getShaderStages(MemoryStack stack) {
        this.checkOpen();
        VkPipelineShaderStageCreateInfo.Buffer stages = VkPipelineShaderStageCreateInfo.calloc(this.modules.size(), stack);
        ByteBuffer entryPoint = stack.UTF8("main");
        for (Map.Entry<ShaderType, Long> module : this.modules.entrySet()) {
            int stage = switch (module.getKey()) {
                case VERTEX -> VK12.VK_SHADER_STAGE_VERTEX_BIT;
                case FRAGMENT -> VK12.VK_SHADER_STAGE_FRAGMENT_BIT;
                case GEOMETRY -> VK12.VK_SHADER_STAGE_GEOMETRY_BIT;
                case COMPUTE -> VK12.VK_SHADER_STAGE_COMPUTE_BIT;
            };
            stages.get().sType$Default().stage(stage).module(module.getValue()).pName(entryPoint);
        }

        return stages.flip();
    }

    public long createGraphicsPipeline(VkGraphicsPipelineCreateInfo.Buffer info) {
        return this.createGraphicsPipeline(info, null);
    }

    public long createGraphicsPipeline(VkGraphicsPipelineCreateInfo.Buffer info, ByteBuffer vertexSpirv) {
        this.checkOpen();
        if (this.modules.containsKey(ShaderType.COMPUTE) || info.remaining() != 1) {
            throw new IllegalArgumentException("Expected one graphics pipeline create info");
        }

        // Keep the caller's create info and its native pointers unchanged.
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long vertexModule = 0L;
            try {
                if (vertexSpirv != null) {
                    LongBuffer modulePointer = stack.callocLong(1);
                    var moduleInfo = VkShaderModuleCreateInfo.calloc(stack).sType$Default().pCode(vertexSpirv);
                    check(VK12.vkCreateShaderModule(this.device, moduleInfo, null, modulePointer), "create converted vertex module");
                    vertexModule = modulePointer.get(0);
                }
                VkGraphicsPipelineCreateInfo.Buffer copy = VkGraphicsPipelineCreateInfo.calloc(1, stack);
                copy.get(0).set(info.get(info.position()));
                copy.pStages(this.getShaderStages(stack)).layout(this.pipelineLayout);
                if (vertexModule != 0L) {
                    for (var stage : copy.pStages()) {
                        if (stage.stage() == VK12.VK_SHADER_STAGE_VERTEX_BIT) {
                            stage.module(vertexModule);
                        }
                    }
                }
                LongBuffer pointer = stack.callocLong(1);
                int result = VK12.vkCreateGraphicsPipelines(this.device, VK12.VK_NULL_HANDLE, copy, null, pointer);
                if (result != VK12.VK_SUCCESS && pointer.get(0) != 0L) {
                    VK12.vkDestroyPipeline(this.device, pointer.get(0), null);
                }

                check(result, "create graphics pipeline");
                long pipeline = pointer.get(0);
                this.graphicsPipelines.add(pipeline);
                return pipeline;
            } finally {
                if (vertexModule != 0L) {
                    VK12.vkDestroyShaderModule(this.device, vertexModule, null);
                }
            }
        }
    }

    private void checkOpen() {
        if (this.closed) {
            throw new IllegalStateException("Vulkan device program is closed");
        }
    }

    static void check(int result, String operation) {
        if (result != VK12.VK_SUCCESS) {
            throw new IllegalStateException("Failed to " + operation + ": VkResult " + result);
        }
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.graphicsPipelines.forEach(pipeline -> VK12.vkDestroyPipeline(this.device, pipeline, null));
            this.graphicsPipelines.clear();
            if (this.descriptors != null) {
                this.descriptors.close();
            }
            if (this.computePipeline != 0L) {
                VK12.vkDestroyPipeline(this.device, this.computePipeline, null);
            }

            if (this.pipelineLayout != 0L) {
                VK12.vkDestroyPipelineLayout(this.device, this.pipelineLayout, null);
            }

            if (this.descriptorSetLayout != 0L) {
                VK12.vkDestroyDescriptorSetLayout(this.device, this.descriptorSetLayout, null);
            }

            this.modules.values().forEach(module -> VK12.vkDestroyShaderModule(this.device, module, null));
            this.modules.clear();
            this.closed = true;
        }
    }
}
