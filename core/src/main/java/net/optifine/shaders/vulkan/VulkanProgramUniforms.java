package net.optifine.shaders.vulkan;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.lwjgl.vulkan.VK12;

public class VulkanProgramUniforms {
    private static final Pattern TEXTURE_ELEMENT = Pattern.compile("^(.*)\\[(\\d+)\\]$");
    private final Map<Integer, VulkanUniformBuffer> buffers = new LinkedHashMap<>();
    private final Map<String, List<VulkanUniformBuffer.Member>> members = new LinkedHashMap<>();
    private final Map<String, int[]> textureUnits = new LinkedHashMap<>();
    private final Map<String, List<int[]>> textureArrays = new LinkedHashMap<>();
    private final Runnable beforeUpdate;
    private long descriptorVersion;
    private final List<DrawBinding> drawBindings;
    private final int uniformSize;

    public VulkanProgramUniforms(VulkanShaderProgram program, Runnable beforeUpdate) {
        this(program, beforeUpdate, 1);
    }

    public VulkanProgramUniforms(VulkanShaderProgram program, Runnable beforeUpdate, long alignment) {
        if (alignment < 1) {
            throw new IllegalArgumentException("Invalid uniform alignment");
        }
        this.beforeUpdate = beforeUpdate;
        for (VulkanShaderProgram.Binding binding : program.getBindings()) {
            VulkanShaderModule.Resource resource = binding.resources().getFirst().resource();
            if (binding.descriptorType() == VK12.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER && resource.isDefaultUniformBlock()) {
                VulkanUniformBuffer buffer = new VulkanUniformBuffer(resource);
                this.buffers.put(binding.binding(), buffer);
                for (VulkanShaderModule.Uniform member : resource.uniforms()) {
                    this.members.computeIfAbsent(member.name(), name -> new ArrayList<>()).add(buffer.getMember(member.name()));
                }
            } else if (isTexture(binding.descriptorType())) {
                int[] units = new int[binding.count()];
                this.textureUnits.put(binding.name(), units);
                this.textureUnits.put(VulkanRectangleShaders.uniformName(binding.name()), units);
            }
        }
        Map<String, Map<Integer, int[]>> arrays = new LinkedHashMap<>();
        this.textureUnits.forEach((name, units) -> {
            var matcher = TEXTURE_ELEMENT.matcher(name);
            if (matcher.matches() && units.length == 1) {
                arrays.computeIfAbsent(matcher.group(1), key -> new TreeMap<>()).put(Integer.parseInt(matcher.group(2)), units);
            }
        });
        arrays.forEach((name, elements) -> {
            if (elements.keySet().stream().max(Integer::compareTo).orElse(-1) + 1 == elements.size()) {
                this.textureArrays.put(name, List.copyOf(elements.values()));
            }
        });
        List<DrawBinding> resolved = new ArrayList<>();
        int bytes = 0;
        for (var binding : program.getBindings()) {
            VulkanUniformBuffer uniform = this.buffers.get(binding.binding());
            if (uniform != null) {
                bytes = Math.toIntExact(Math.multiplyExact(Math.addExact(bytes, alignment - 1) / alignment, alignment));
            }
            resolved.add(new DrawBinding(binding, uniform, this.textureUnits.get(binding.name()), bytes));
            if (uniform != null) {
                bytes = Math.addExact(bytes, uniform.getSize());
            }
        }
        this.drawBindings = List.copyOf(resolved);
        this.uniformSize = bytes;
        program.getInitializers().forEach((name, value) -> {
            if (this.members.containsKey(name)) {
                value.apply(this, name);
            }
        });
    }

    record DrawBinding(VulkanShaderProgram.Binding source, VulkanUniformBuffer uniform, int[] textureUnits, int uniformOffset) {
    }

    int getUniformSize() {
        return this.uniformSize;
    }

    List<DrawBinding> getDrawBindings() {
        return this.drawBindings;
    }

    public boolean contains(String name) {
        return this.members.containsKey(name) || this.textureUnits.containsKey(name) || this.textureArrays.containsKey(name)
            || this.textureElement(name) != null;
    }

    public Map<Integer, VulkanUniformBuffer> getBuffers() {
        return Collections.unmodifiableMap(this.buffers);
    }

    public int getTextureUnit(String name, int element) {
        List<int[]> array = this.textureArrays.get(name);
        if (array != null) {
            return array.get(element)[0];
        }
        int[] units = this.textureUnits.get(name);
        if (units == null) {
            var selected = this.textureElement(name);
            if (selected != null && element == 0) {
                return selected.units()[selected.index()];
            }
            throw new IllegalArgumentException("Unknown texture uniform: " + name);
        }

        return units[element];
    }

    public long getDescriptorVersion() {
        return this.descriptorVersion;
    }

    public void flush() {
        this.beforeUpdate.run();
    }

    public void setFloat(String name, float... values) {
        for (VulkanUniformBuffer.Member member : this.getMembers(name)) {
            member.setFloat(values);
        }
    }

    public void setInt(String name, int... values) {
        var element = this.textureElement(name);
        if (element != null) {
            if (values.length != 1 || values[0] < 0) {
                throw new IllegalArgumentException("Expected one nonnegative texture unit: " + name);
            }
            if (element.units()[element.index()] != values[0]) {
                element.units()[element.index()] = values[0];
                this.descriptorVersion++;
            }
            return;
        }
        List<int[]> array = this.textureArrays.get(name);
        if (array != null) {
            if (array.size() != values.length) {
                throw new IllegalArgumentException("Texture uniform element count does not match: " + name);
            }
            for (int value : values) {
                if (value < 0) {
                    throw new IllegalArgumentException("Negative texture unit: " + name);
                }
            }
            for (int i = 0; i < values.length; i++) {
                if (array.get(i)[0] != values[i]) {
                    array.get(i)[0] = values[i];
                    this.descriptorVersion++;
                }
            }
            return;
        }
        int[] units = this.textureUnits.get(name);
        if (units != null) {
            if (units.length != values.length) {
                throw new IllegalArgumentException("Texture uniform element count does not match: " + name);
            }

            for (int value : values) {
                if (value < 0) {
                    throw new IllegalArgumentException("Negative texture unit: " + name);
                }
            }

            for (int i = 0; i < units.length; i++) {
                if (units[i] != values[i]) {
                    units[i] = values[i];
                    this.descriptorVersion++;
                }
            }
        } else {
            for (VulkanUniformBuffer.Member member : this.getMembers(name)) {
                member.setInt(values);
            }
        }
    }

    public void setDouble(String name, double... values) {
        for (VulkanUniformBuffer.Member member : this.getMembers(name)) {
            member.setDouble(values);
        }
    }

    public void setLong(String name, long... values) {
        for (VulkanUniformBuffer.Member member : this.getMembers(name)) {
            member.setLong(values);
        }
    }

    public void setMatrix(String name, int size, boolean transpose, FloatBuffer matrix) {
        if (size != 3 && size != 4 || matrix.remaining() != size * size) {
            throw new IllegalArgumentException("Invalid matrix size: " + name);
        }

        for (VulkanUniformBuffer.Member member : this.getMembers(name)) {
            member.setMatrix(size, transpose, matrix);
        }
    }

    private List<VulkanUniformBuffer.Member> getMembers(String name) {
        List<VulkanUniformBuffer.Member> members = this.members.get(name);
        if (members == null) {
            throw new IllegalArgumentException("Unknown scalar uniform: " + name);
        }
        return members;
    }

    private static boolean isTexture(int type) {
        return type == VK12.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER || type == VK12.VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE
            || type == VK12.VK_DESCRIPTOR_TYPE_SAMPLER || type == VK12.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE
            || type == VK12.VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER || type == VK12.VK_DESCRIPTOR_TYPE_STORAGE_TEXEL_BUFFER;
    }

    private TextureElement textureElement(String name) {
        if (this.textureUnits.containsKey(name)) {
            return null;
        }
        var matcher = TEXTURE_ELEMENT.matcher(name);
        if (matcher.matches() && matcher.group(2).length() <= 10) {
            int[] units = this.textureUnits.get(matcher.group(1));
            if (units != null) {
                long index = Long.parseLong(matcher.group(2));
                if (index < units.length) {
                    return new TextureElement(units, (int)index);
                }
            }
        }
        return null;
    }

    private record TextureElement(int[] units, int index) {
    }
}
