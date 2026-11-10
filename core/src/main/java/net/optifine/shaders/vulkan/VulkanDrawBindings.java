package net.optifine.shaders.vulkan;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.vulkan.VulkanDevice;
import com.mojang.blaze3d.vulkan.VulkanGpuBuffer;
import com.mojang.blaze3d.vulkan.VulkanGpuTextureView;
import java.nio.LongBuffer;
import java.util.Map;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

// Uniform bytes and pooled descriptor sets remain alive until the GPU has consumed them.
public class VulkanDrawBindings implements AutoCloseable {
    private final VulkanDevice device;
    private final long pipelineLayout;
    private final VulkanDescriptorAllocator descriptors;
    private long set;
    private boolean closed;
    private final VulkanShaderRuntime.DeviceProgram program;
    private final VulkanShaderRuntime runtime;
    private final Map<Integer, GpuBufferSlice> explicitBuffers;
    private final boolean pushDescriptors;

    public VulkanDrawBindings(
        VulkanDevice device, VulkanShaderRuntime.DeviceProgram program, VulkanShaderRuntime runtime,
        Map<Integer, GpuBufferSlice> explicitBuffers
    ) {
        this.device = device;
        this.pipelineLayout = program.deviceProgram().getPipelineLayout();
        this.descriptors = program.deviceProgram().getDescriptors();
        this.program = program;
        this.runtime = runtime;
        this.explicitBuffers = explicitBuffers;
        this.pushDescriptors = program.deviceProgram().usesPushDescriptors();
    }

    public void bind(VkCommandBuffer commands, int bindPoint) {
        if (this.closed) {
            throw new IllegalStateException("Draw bindings are closed");
        }
        var program = this.program;
        var runtime = this.runtime;
        var explicitBuffers = this.explicitBuffers;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            if (!this.pushDescriptors) {
                if (this.set != 0L) {
                    throw new IllegalStateException("Draw bindings must be recorded once");
                }
                this.set = this.descriptors.acquire();
            }
            GpuBufferSlice upload = null;
            if (program.uniforms().getUniformSize() > 0) {
                try (var mapping = this.device.createCommandEncoder().transientMemory().allocateGpuMapped(program.uniforms().getUniformSize(),
                         this.device.getDeviceInfo().limits().minUniformOffsetAlignment(), GpuBuffer.USAGE_UNIFORM)) {
                    for (var resolved : program.uniforms().getDrawBindings()) {
                        if (resolved.uniform() != null) {
                            resolved.uniform().copyTo(mapping.data(), resolved.uniformOffset());
                        }
                    }
                    upload = mapping.slice();
                }
            }
            VkWriteDescriptorSet.Buffer writes = VkWriteDescriptorSet.calloc(program.source().getBindings().size(), stack);
            for (var resolved : program.uniforms().getDrawBindings()) {
                VulkanShaderProgram.Binding binding = resolved.source();
                VkWriteDescriptorSet write = writes.get().sType$Default().dstSet(this.set).dstBinding(binding.binding())
                    .descriptorType(binding.descriptorType()).descriptorCount(binding.count());
                VulkanUniformBuffer uniform = resolved.uniform();
                if (uniform != null) {
                    write.pBufferInfo(VkDescriptorBufferInfo.calloc(1, stack)
                        .buffer(((VulkanGpuBuffer)upload.buffer()).vkBuffer()).offset(upload.offset() + resolved.uniformOffset()).range(uniform.getSize()));
                } else if (binding.descriptorType() == VK12.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER
                    || binding.descriptorType() == VK12.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER) {
                    GpuBufferSlice buffer = explicitBuffers.get(binding.binding());
                    if (buffer != null) {
                        runtime.getBuffers().validate(binding.descriptorType(), buffer, binding.resources().getFirst().resource().size());
                        write.pBufferInfo(this.bufferInfo(stack, buffer, binding.count()));
                    } else {
                        VkDescriptorBufferInfo.Buffer buffers = VkDescriptorBufferInfo.calloc(binding.count(), stack);
                        for (int element = 0; element < binding.count(); element++) {
                            GpuBufferSlice slice = runtime.getBuffers().resolve(binding, element);
                            buffers.get(element).buffer(((VulkanGpuBuffer)slice.buffer()).vkBuffer()).offset(slice.offset()).range(slice.length());
                        }
                        write.pBufferInfo(buffers);
                    }
                } else if (binding.descriptorType() == VK12.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER
                    || binding.descriptorType() == VK12.VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE
                    || binding.descriptorType() == VK12.VK_DESCRIPTOR_TYPE_SAMPLER) {
                    VkDescriptorImageInfo.Buffer images = VkDescriptorImageInfo.calloc(binding.count(), stack);
                    for (int element = 0; element < binding.count(); element++) {
                        int unit = resolved.textureUnits()[element];
                        var resource = binding.resources().getFirst().resource();
                        VulkanShaderRuntime.TextureBinding texture = resource.imageDimension() < 0 ? runtime.getTexture(unit)
                            : runtime.getTexture(unit, resource.imageDimension(), resource.imageArrayed());
                        if (texture == null || texture.view().isClosed() || texture.view().texture().isClosed()) {
                            throw new IllegalStateException("Missing shader texture " + binding.name() + " at unit " + unit);
                        }
                        images.get(element).sampler(texture.sampler()).imageView(((VulkanGpuTextureView)texture.view()).vkImageView())
                            .imageLayout(VK12.VK_IMAGE_LAYOUT_GENERAL);
                    }
                    write.pImageInfo(images);
                } else if (binding.descriptorType() == VK12.VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER
                    || binding.descriptorType() == VK12.VK_DESCRIPTOR_TYPE_STORAGE_TEXEL_BUFFER) {
                    LongBuffer views = stack.mallocLong(binding.count());
                    boolean storage = binding.descriptorType() == VK12.VK_DESCRIPTOR_TYPE_STORAGE_TEXEL_BUFFER;
                    for (int element = 0; element < binding.count(); element++) {
                        int unit = resolved.textureUnits()[element];
                        var view = runtime.getTexelBuffer(unit, storage);
                        if (view == null) {
                            throw new IllegalStateException("Missing shader texel buffer " + binding.name() + " at unit " + unit);
                        }
                        views.put(view.getView());
                    }
                    write.pTexelBufferView(views.flip());
                } else if (binding.descriptorType() == VK12.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE) {
                    VkDescriptorImageInfo.Buffer images = VkDescriptorImageInfo.calloc(binding.count(), stack);
                    for (int element = 0; element < binding.count(); element++) {
                        int unit = resolved.textureUnits()[element];
                        var view = runtime.getImage(unit);
                        if (view == null || view.isClosed() || view.texture().isClosed()) {
                            throw new IllegalStateException("Missing shader storage image " + binding.name() + " at unit " + unit);
                        }
                        images.get(element).imageView(((VulkanGpuTextureView)view).vkImageView()).imageLayout(VK12.VK_IMAGE_LAYOUT_GENERAL);
                    }
                    write.pImageInfo(images);
                } else {
                    throw new UnsupportedOperationException("Shader descriptor type is not implemented: " + binding.descriptorType() + " (" + binding.name() + ")");
                }
            }
            writes.flip();
            if (this.pushDescriptors) {
                org.lwjgl.vulkan.KHRPushDescriptor.vkCmdPushDescriptorSetKHR(commands, bindPoint, this.pipelineLayout, 0, writes);
            } else {
                VK12.vkUpdateDescriptorSets(this.device.vkDevice(), writes, null);
                VK12.vkCmdBindDescriptorSets(commands, bindPoint, this.pipelineLayout, 0, stack.longs(this.set), null);
            }
        } catch (RuntimeException | Error e) {
            this.close();
            throw e;
        }
    }

    private VkDescriptorBufferInfo.Buffer bufferInfo(MemoryStack stack, GpuBufferSlice buffer, int count) {
        if (count != 1 || buffer.buffer().isClosed() || buffer.length() == 0) {
            throw new IllegalArgumentException("Expected one open, nonempty shader buffer");
        }
        return VkDescriptorBufferInfo.calloc(1, stack).buffer(((VulkanGpuBuffer)buffer.buffer()).vkBuffer())
            .offset(buffer.offset()).range(buffer.length());
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.closed = true;
            if (this.set != 0L) {
                long set = this.set;
                this.device.createCommandEncoder().queueForDestroy(() -> this.descriptors.release(set));
            }
        }
    }
}
