package net.optifine.shaders.vulkan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.optifine.shaders.config.ShaderType;
import net.optifine.shaders.SMCLog;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkDevice;

public class VulkanShaderProgram implements AutoCloseable {
    private final String name;
    private final Map<ShaderType, VulkanShaderModule> modules;
    private final List<Binding> bindings;
    private final Map<String, VulkanUniformInitializers.Value> initializers;
    private boolean closed;

    VulkanShaderProgram(String name, Map<ShaderType, VulkanShaderModule> modules) {
        this.name = name;
        this.modules = Collections.unmodifiableMap(new EnumMap<>(modules));
        Map<String, VulkanUniformInitializers.Value> initializers = new LinkedHashMap<>();
        for (VulkanShaderModule module : modules.values()) {
            module.getInitializers().forEach((uniform, value) -> {
                var previous = initializers.putIfAbsent(uniform, value);
                if (previous != null && !previous.equals(value)) {
                    throw new IllegalArgumentException("Conflicting uniform initializers in " + name + ": " + uniform);
                }
            });
        }
        this.initializers = Map.copyOf(initializers);
        this.bindings = this.linkResources();
        VulkanShaderModule vertex = modules.get(ShaderType.VERTEX);
        VulkanShaderModule geometry = modules.get(ShaderType.GEOMETRY);
        VulkanShaderModule fragment = modules.get(ShaderType.FRAGMENT);
        if (geometry != null) {
            linkVariables(vertex, geometry);
        }

        if (fragment != null) {
            for (VulkanShaderModule.Variable output : fragment.getOutputs()) {
                if (output.name().matches("outColor[0-9]+")) {
                    fragment.setLocation(output, Integer.parseInt(output.name().substring(8)));
                }
            }
            linkVariables(geometry != null ? geometry : vertex, fragment);
        }
    }

    public String getName() {
        return this.name;
    }

    public Map<ShaderType, VulkanShaderModule> getModules() {
        return this.modules;
    }

    public List<Binding> getBindings() {
        return this.bindings;
    }

    public Map<String, VulkanUniformInitializers.Value> getInitializers() {
        return this.initializers;
    }

    public VulkanDeviceProgram createDeviceProgram(VkDevice device) {
        if (this.closed) {
            throw new IllegalStateException("Vulkan shader program is closed: " + this.name);
        }

        return new VulkanDeviceProgram(device, this);
    }

    private List<Binding> linkResources() {
        Map<String, List<ResourceReference>> resources = new TreeMap<>();
        for (VulkanShaderModule module : this.modules.values()) {
            for (VulkanShaderModule.Resource resource : module.getResources()) {
                // Standalone uniforms have independent layouts in each stage. Never alias their buffers.
                String key = resource.descriptorType() + ":" + VulkanRectangleShaders.uniformName(resource.name());
                if (resource.descriptorType() == VK12.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER) {
                    key += ":" + module.getType();
                }

                resources.computeIfAbsent(key, x -> new ArrayList<>()).add(new ResourceReference(module.getType(), resource));
            }
        }

        List<Binding> bindings = new ArrayList<>();
        for (List<ResourceReference> references : resources.values()) {
            VulkanShaderModule.Resource first = references.getFirst().resource();
            int index = bindings.size();
            int stages = 0;
            for (ResourceReference reference : references) {
                VulkanShaderModule.Resource resource = reference.resource();
                if (resource.count() != first.count() || !resource.signature().equals(first.signature())
                    || !resource.name().equals(first.name())) {
                    throw new IllegalArgumentException("Conflicting shader resource declarations: " + first.name());
                }

                VulkanShaderModule module = this.modules.get(reference.stage());
                module.setBinding(resource, index);
                stages |= module.getStageFlags();
            }

            bindings.add(new Binding(index, first.name(), first.descriptorType(), first.count(), stages, List.copyOf(references)));
        }

        return List.copyOf(bindings);
    }

    private static void linkVariables(VulkanShaderModule producer, VulkanShaderModule consumer) {
        Map<String, VulkanShaderModule.Variable> outputs = new LinkedHashMap<>();
        Map<String, Integer> locations = new LinkedHashMap<>();
        int nextLocation = 0;
        for (VulkanShaderModule.Variable output : producer.getOutputs()) {
            outputs.put(output.name(), output);
            locations.put(output.name(), nextLocation);
            producer.setLocation(output, nextLocation);
            nextLocation += output.locationCount();
        }

        for (VulkanShaderModule.Variable input : consumer.getInputs()) {
            VulkanShaderModule.Variable output = outputs.get(input.name());
            if (output == null) {
                // Both APIs leave inputs without a preceding output undefined.
                // Reserve separate locations so they cannot alias an unrelated output.
                consumer.setLocation(input, nextLocation);
                nextLocation += input.locationCount();
                SMCLog.warning("Unwritten shader input " + consumer.getName() + ": " + input.name() + " (undefined value)");
                continue;
            }
            if (!output.signature().equals(input.signature())) {
                throw new IllegalArgumentException("Incompatible shader interface " + producer.getName() + " -> " + consumer.getName() + ": " + input.name());
            }

            consumer.setLocation(input, locations.get(input.name()));
        }
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.modules.values().forEach(VulkanShaderModule::close);
            this.closed = true;
        }
    }

    public record ResourceReference(ShaderType stage, VulkanShaderModule.Resource resource) {
    }

    public record Binding(int binding, String name, int descriptorType, int count, int stageFlags, List<ResourceReference> resources) {
    }
}
