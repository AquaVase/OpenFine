package net.optifine.shaders.vulkan;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import net.optifine.shaders.config.ShaderType;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.spvc.Spv;
import org.lwjgl.util.spvc.Spvc;
import org.lwjgl.util.spvc.SpvcReflectedResource;
import org.lwjgl.vulkan.VK12;

public class VulkanShaderModule implements AutoCloseable {
    private final String name;
    private final ShaderType type;
    private final String infoLog;
    private final Map<String, VulkanUniformInitializers.Value> initializers;
    private ByteBuffer spirv;
    private final Map<Integer, Integer> arrayElements = new HashMap<>();
    private final List<Resource> resources = new ArrayList<>();
    private final List<Variable> inputs = new ArrayList<>();
    private final List<Variable> outputs = new ArrayList<>();

    VulkanShaderModule(String name, ShaderType type, ByteBuffer bytes, String infoLog, Map<String, VulkanUniformInitializers.Value> initializers) {
        this.name = name;
        this.type = type;
        this.infoLog = infoLog;
        this.initializers = Map.copyOf(initializers);
        // A JVM-owned copy survives shaderc_result_release and cannot be double-freed.
        this.spirv = ByteBuffer.allocateDirect(bytes.remaining()).order(ByteOrder.nativeOrder());
        this.spirv.put(bytes).flip();
        IntBuffer words = this.spirv.asIntBuffer();
        for (int i = 5; i < words.limit();) {
            int count = words.get(i) >>> 16;
            if (count == 0 || i + count > words.limit()) {
                throw new IllegalArgumentException("Invalid shader SPIR-V instruction");
            }
            if ((words.get(i) & 65535) == Spv.SpvOpTypeArray) {
                this.arrayElements.put(words.get(i + 1), words.get(i + 2));
            }
            i += count;
        }
        this.reflect();
    }

    public String getName() {
        return this.name;
    }

    public ShaderType getType() {
        return this.type;
    }

    public String getInfoLog() {
        return this.infoLog;
    }

    public Map<String, VulkanUniformInitializers.Value> getInitializers() {
        return this.initializers;
    }

    public ByteBuffer getSpirv() {
        if (this.spirv == null) {
            throw new IllegalStateException("Vulkan shader module is closed: " + this.name);
        }

        return this.spirv.asReadOnlyBuffer().order(ByteOrder.nativeOrder());
    }

    public List<Resource> getResources() {
        return List.copyOf(this.resources);
    }

    public List<Variable> getInputs() {
        return List.copyOf(this.inputs);
    }

    public List<Variable> getOutputs() {
        return List.copyOf(this.outputs);
    }

    public int getStageFlags() {
        return switch (this.type) {
            case VERTEX -> VK12.VK_SHADER_STAGE_VERTEX_BIT;
            case FRAGMENT -> VK12.VK_SHADER_STAGE_FRAGMENT_BIT;
            case GEOMETRY -> VK12.VK_SHADER_STAGE_GEOMETRY_BIT;
            case COMPUTE -> VK12.VK_SHADER_STAGE_COMPUTE_BIT;
        };
    }

    void setBinding(Resource resource, int binding) {
        this.spirv.asIntBuffer().put(resource.bindingOffset(), binding).put(resource.setOffset(), 0);
    }

    void setLocation(Variable variable, int location) {
        this.spirv.asIntBuffer().put(variable.locationOffset(), location);
    }

    private void reflect() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pointer = stack.callocPointer(1);
            check(Spvc.spvc_context_create(pointer), "create reflection context");
            long context = pointer.get(0);
            try {
                check(Spvc.spvc_context_parse_spirv(context, this.spirv.asIntBuffer(), this.spirv.remaining() / 4, pointer), "parse SPIR-V");
                check(Spvc.spvc_context_create_compiler(context, Spvc.SPVC_BACKEND_NONE, pointer.get(0), Spvc.SPVC_CAPTURE_MODE_TAKE_OWNERSHIP, pointer),
                    "create reflection compiler");
                long compiler = pointer.get(0);
                check(Spvc.spvc_compiler_create_shader_resources(compiler, pointer), "reflect resources");
                long reflected = pointer.get(0);
                // Shared shader headers also declare textures that this entry point never uses.
                // After a reload those textures may have no binding, and must not become required descriptors.
                check(Spvc.spvc_compiler_get_active_interface_variables(compiler, pointer), "find active resources");
                long activeVariables = pointer.get(0);
                check(Spvc.spvc_compiler_create_shader_resources_for_active_variables(compiler, pointer, activeVariables), "reflect active resources");
                long activeResources = pointer.get(0);
                this.readResources(compiler, activeResources, Spvc.SPVC_RESOURCE_TYPE_UNIFORM_BUFFER, VK12.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, stack);
                this.readResources(compiler, activeResources, Spvc.SPVC_RESOURCE_TYPE_STORAGE_BUFFER, VK12.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, stack);
                this.readResources(compiler, activeResources, Spvc.SPVC_RESOURCE_TYPE_SAMPLED_IMAGE, VK12.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, stack);
                this.readResources(compiler, activeResources, Spvc.SPVC_RESOURCE_TYPE_STORAGE_IMAGE, VK12.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE, stack);
                this.readResources(compiler, activeResources, Spvc.SPVC_RESOURCE_TYPE_SEPARATE_IMAGE, VK12.VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE, stack);
                this.readResources(compiler, activeResources, Spvc.SPVC_RESOURCE_TYPE_SEPARATE_SAMPLERS, VK12.VK_DESCRIPTOR_TYPE_SAMPLER, stack);
                this.readVariables(compiler, reflected, Spvc.SPVC_RESOURCE_TYPE_STAGE_INPUT, this.inputs, stack);
                this.readVariables(compiler, reflected, Spvc.SPVC_RESOURCE_TYPE_STAGE_OUTPUT, this.outputs, stack);
            } finally {
                Spvc.spvc_context_destroy(context);
            }
        }
    }

    private void readResources(long compiler, long reflected, int resourceType, int descriptorType, MemoryStack stack) {
        for (SpvcReflectedResource resource : getResourceList(reflected, resourceType, stack)) {
            long typeHandle = Spvc.spvc_compiler_get_type_handle(compiler, resource.type_id());
            int count = 1;
            for (int i = 0; i < Spvc.spvc_type_get_num_array_dimensions(typeHandle); i++) {
                int size = Spvc.spvc_type_get_array_dimension(typeHandle, i);
                if (!Spvc.spvc_type_array_dimension_is_literal(typeHandle, i) || size == 0) {
                    throw new IllegalArgumentException("Descriptor arrays must have a fixed size: " + resource.nameString());
                }

                count = Math.multiplyExact(count, size);
            }

            int actualDescriptorType = descriptorType;
            if (descriptorType == VK12.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER || descriptorType == VK12.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE) {
                if (Spvc.spvc_type_get_image_dimension(typeHandle) == Spv.SpvDimBuffer) {
                    actualDescriptorType = descriptorType == VK12.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE
                        ? VK12.VK_DESCRIPTOR_TYPE_STORAGE_TEXEL_BUFFER : VK12.VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER;
                }
            }

            long size = 0L;
            if (resourceType == Spvc.SPVC_RESOURCE_TYPE_STORAGE_BUFFER) {
                PointerBuffer sizeBuffer = stack.callocPointer(1);
                long structType = Spvc.spvc_compiler_get_type_handle(compiler, resource.base_type_id());
                check(Spvc.spvc_compiler_get_declared_struct_size(compiler, structType, sizeBuffer), "get storage block size");
                size = sizeBuffer.get(0);
            }
            List<Uniform> uniforms = new ArrayList<>();
            if (resourceType == Spvc.SPVC_RESOURCE_TYPE_UNIFORM_BUFFER) {
                long structType = Spvc.spvc_compiler_get_type_handle(compiler, resource.base_type_id());
                PointerBuffer sizeBuffer = stack.callocPointer(1);
                check(Spvc.spvc_compiler_get_declared_struct_size(compiler, structType, sizeBuffer), "get uniform block size");
                size = sizeBuffer.get(0);
                this.readUniformMembers(compiler, resource.base_type_id(), "", 0, uniforms, stack);
            }

            String signature = getTypeSignature(compiler, typeHandle, false);
            if (resourceType == Spvc.SPVC_RESOURCE_TYPE_SAMPLED_IMAGE || resourceType == Spvc.SPVC_RESOURCE_TYPE_STORAGE_IMAGE
                || resourceType == Spvc.SPVC_RESOURCE_TYPE_SEPARATE_IMAGE) {
                signature += ":" + Spvc.spvc_type_get_image_dimension(typeHandle) + ":" + Spvc.spvc_type_get_image_arrayed(typeHandle)
                    + ":" + Spvc.spvc_type_get_image_multisampled(typeHandle) + ":" + Spvc.spvc_type_get_image_is_depth(typeHandle)
                    + ":" + Spvc.spvc_type_get_image_storage_format(typeHandle) + ":" + getTypeSignature(compiler,
                        Spvc.spvc_compiler_get_type_handle(compiler, Spvc.spvc_type_get_image_sampled_type(typeHandle)), false);
            }
            int imageDimension = resourceType == Spvc.SPVC_RESOURCE_TYPE_SAMPLED_IMAGE || resourceType == Spvc.SPVC_RESOURCE_TYPE_STORAGE_IMAGE
                || resourceType == Spvc.SPVC_RESOURCE_TYPE_SEPARATE_IMAGE
                ? Spvc.spvc_type_get_image_dimension(typeHandle) : -1;
            boolean imageArrayed = imageDimension >= 0 && Spvc.spvc_type_get_image_arrayed(typeHandle);
            this.resources.add(new Resource(resource.nameString(), actualDescriptorType, count, size, List.copyOf(uniforms), signature, imageDimension, imageArrayed,
                Spvc.spvc_compiler_get_decoration(compiler, resource.id(), Spv.SpvDecorationBinding),
                Spvc.spvc_compiler_get_decoration(compiler, resource.id(), Spv.SpvDecorationDescriptorSet),
                decorationOffset(compiler, resource.id(), Spv.SpvDecorationBinding, stack),
                decorationOffset(compiler, resource.id(), Spv.SpvDecorationDescriptorSet, stack)));
        }
    }

    private void readUniformMembers(long compiler, int typeId, String prefix, int baseOffset, List<Uniform> uniforms, MemoryStack stack) {
        long structType = Spvc.spvc_compiler_get_type_handle(compiler, typeId);
        IntBuffer offset = stack.callocInt(1);
        IntBuffer stride = stack.callocInt(1);
        PointerBuffer size = stack.callocPointer(1);
        for (int i = 0; i < Spvc.spvc_type_get_num_member_types(structType); i++) {
            check(Spvc.spvc_compiler_type_struct_member_offset(compiler, structType, i, offset), "get uniform offset");
            check(Spvc.spvc_compiler_get_declared_struct_member_size(compiler, structType, i, size), "get uniform size");
            int memberId = Spvc.spvc_type_get_member_type(structType, i);
            long memberType = Spvc.spvc_compiler_get_type_handle(compiler, memberId);
            String name = prefix + Spvc.spvc_compiler_get_member_name(compiler, typeId, i);
            int memberOffset = Math.addExact(baseOffset, offset.get(0));
            int matrixStride = 0;
            if (Spvc.spvc_type_get_columns(memberType) > 1) {
                check(Spvc.spvc_compiler_type_struct_member_matrix_stride(compiler, structType, i, stride), "get uniform matrix stride");
                matrixStride = stride.get(0);
            }
            this.readUniformValue(compiler, memberId, name, memberOffset, size.get(0), matrixStride,
                Spvc.spvc_compiler_has_member_decoration(compiler, typeId, i, Spv.SpvDecorationRowMajor), uniforms, stack);
        }
    }

    private void readUniformValue(long compiler, int typeId, String name, int offset, long size, int matrixStride,
        boolean rowMajor, List<Uniform> uniforms, MemoryStack stack) {
        long type = Spvc.spvc_compiler_get_type_handle(compiler, typeId);
        int dimensions = Spvc.spvc_type_get_num_array_dimensions(type);
        int arraySize = 1;
        int arrayStride = 0;
        if (dimensions > 0) {
            int outerDimension = dimensions - 1;
            if (!Spvc.spvc_type_array_dimension_is_literal(type, outerDimension)) {
                throw new IllegalArgumentException("Uniform array size is not a literal: " + name);
            }
            arraySize = Spvc.spvc_type_get_array_dimension(type, outerDimension);
            arrayStride = Spvc.spvc_compiler_get_decoration(compiler, typeId, Spv.SpvDecorationArrayStride);
            if (arraySize <= 0 || arrayStride <= 0 || !this.arrayElements.containsKey(typeId)) {
                throw new IllegalArgumentException("Invalid uniform array layout: " + name);
            }
        }
        boolean structure = Spvc.spvc_type_get_basetype(type) == Spvc.SPVC_BASETYPE_STRUCT;
        if (dimensions <= 1 && !structure) {
            uniforms.add(new Uniform(name, offset, size, Spvc.spvc_type_get_basetype(type),
                Spvc.spvc_type_get_bit_width(type), Spvc.spvc_type_get_vector_size(type), Spvc.spvc_type_get_columns(type),
                arraySize, dimensions, arrayStride, matrixStride, rowMajor));
        }
        if (dimensions > 0) {
            for (int element = 0; element < arraySize; element++) {
                this.readUniformValue(compiler, this.arrayElements.get(typeId), name + "[" + element + "]",
                    Math.addExact(offset, Math.multiplyExact(element, arrayStride)), arrayStride, matrixStride, rowMajor, uniforms, stack);
            }
        } else if (structure) {
            this.readUniformMembers(compiler, Spvc.spvc_type_get_base_type_id(type), name + ".", offset, uniforms, stack);
        }
    }

    private void readVariables(long compiler, long reflected, int resourceType, List<Variable> variables, MemoryStack stack) {
        for (SpvcReflectedResource resource : getResourceList(reflected, resourceType, stack)) {
            if (Spvc.spvc_compiler_has_decoration(compiler, resource.id(), Spv.SpvDecorationBuiltIn)) {
                continue;
            }

            long typeHandle = Spvc.spvc_compiler_get_type_handle(compiler, resource.type_id());
            String variableName = resource.nameString();
            if (Spvc.spvc_type_get_num_member_types(typeHandle) > 0) {
                // GLSL matches interface blocks by block type, not by instance name.
                variableName = Spvc.spvc_compiler_get_name(compiler, resource.base_type_id());
            }

            boolean perVertexArray = this.type == ShaderType.GEOMETRY && resourceType == Spvc.SPVC_RESOURCE_TYPE_STAGE_INPUT;
            variables.add(new Variable(variableName, getLocationCount(compiler, typeHandle, perVertexArray),
                getTypeSignature(compiler, typeHandle, perVertexArray),
                decorationOffset(compiler, resource.id(), Spv.SpvDecorationLocation, stack)));
        }
    }

    private static String getTypeSignature(long compiler, long type, boolean perVertexArray) {
        StringBuilder signature = new StringBuilder();
        int members = Spvc.spvc_type_get_num_member_types(type);
        if (members > 0) {
            signature.append('{');
            for (int i = 0; i < members; i++) {
                signature.append(getTypeSignature(compiler, Spvc.spvc_compiler_get_type_handle(compiler,
                    Spvc.spvc_type_get_member_type(type, i)), false)).append(';');
            }
            signature.append('}');
        } else {
            signature.append(Spvc.spvc_type_get_basetype(type)).append(':').append(Spvc.spvc_type_get_bit_width(type))
                .append(':').append(Spvc.spvc_type_get_vector_size(type)).append(':').append(Spvc.spvc_type_get_columns(type));
        }

        int dimensions = Spvc.spvc_type_get_num_array_dimensions(type) - (perVertexArray ? 1 : 0);
        for (int i = 0; i < dimensions; i++) {
            signature.append('[').append(Spvc.spvc_type_get_array_dimension(type, i)).append(']');
        }
        return signature.toString();
    }

    private static int getLocationCount(long compiler, long type, boolean perVertexArray) {
        int count = 0;
        int members = Spvc.spvc_type_get_num_member_types(type);
        if (members > 0) {
            for (int i = 0; i < members; i++) {
                count += getLocationCount(compiler, Spvc.spvc_compiler_get_type_handle(compiler, Spvc.spvc_type_get_member_type(type, i)), false);
            }
        } else {
            int width = Spvc.spvc_type_get_bit_width(type) * Spvc.spvc_type_get_vector_size(type);
            count = Math.max(1, (width + 127) / 128) * Math.max(1, Spvc.spvc_type_get_columns(type));
        }

        int dimensions = Spvc.spvc_type_get_num_array_dimensions(type) - (perVertexArray ? 1 : 0);
        for (int i = 0; i < dimensions; i++) {
            if (!Spvc.spvc_type_array_dimension_is_literal(type, i)) {
                throw new IllegalArgumentException("Stage interface arrays must have a fixed size");
            }

            count = Math.multiplyExact(count, Spvc.spvc_type_get_array_dimension(type, i));
        }

        return count;
    }

    private static SpvcReflectedResource.Buffer getResourceList(long reflected, int type, MemoryStack stack) {
        PointerBuffer pointer = stack.callocPointer(1);
        PointerBuffer count = stack.callocPointer(1);
        check(Spvc.spvc_resources_get_resource_list_for_type(reflected, type, pointer, count), "get resource list");
        return SpvcReflectedResource.create(pointer.get(0), Math.toIntExact(count.get(0)));
    }

    private static int decorationOffset(long compiler, int id, int decoration, MemoryStack stack) {
        IntBuffer offset = stack.callocInt(1);
        if (!Spvc.spvc_compiler_get_binary_offset_for_decoration(compiler, id, decoration, offset)) {
            throw new IllegalArgumentException("Missing SPIR-V decoration " + decoration + " for resource " + id);
        }

        return offset.get(0);
    }

    private static void check(int result, String operation) {
        if (result != Spvc.SPVC_SUCCESS) {
            throw new IllegalArgumentException("Failed to " + operation + ": " + result);
        }
    }

    @Override
    public void close() {
        this.spirv = null;
    }

    public record Uniform(
        String name, int offset, long size, int baseType, int bitWidth, int rows, int columns,
        int arraySize, int arrayDimensions, int arrayStride, int matrixStride, boolean rowMajor
    ) {
    }

    public record Resource(
        String name, int descriptorType, int count, long size, List<Uniform> uniforms, String signature, int imageDimension, boolean imageArrayed,
        int originalBinding, int originalSet, int bindingOffset, int setOffset
    ) {
        public boolean isDefaultUniformBlock() {
            return this.name.equals("gl_DefaultUniformBlock") || this.name.equals("$Global");
        }
    }

    public record Variable(String name, int locationCount, String signature, int locationOffset) {
    }
}
