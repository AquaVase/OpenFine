package net.optifine.shaders.vulkan;

import com.mojang.blaze3d.vulkan.VulkanBackend;
import com.mojang.blaze3d.vulkan.VulkanDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vulkan.init.VulkanFeature;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;

public class VulkanShaderFeatures {
    public static String[] getLanguageExtensions() {
        // Packing functions are core in the GLSL 450 used by our Vulkan compiler.
        // Shader packs use this legacy macro to avoid redefining those built-ins.
        return new String[]{"GL_ARB_shading_language_packing"};
    }

    public static String[] getDeviceLanguageExtensions() {
        VulkanDevice device = (VulkanDevice)RenderSystem.getDevice().getBackend();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPhysicalDeviceFeatures features = VkPhysicalDeviceFeatures.calloc(stack);
            VK12.vkGetPhysicalDeviceFeatures(device.vkDevice().getPhysicalDevice(), features);
            return getLanguageExtensions(features);
        }
    }

    public static String[] getLanguageExtensions(VkPhysicalDeviceFeatures features) {
        List<String> extensions = new ArrayList<>(List.of(getLanguageExtensions()));
        if (features.shaderFloat64()) {
            extensions.add("GL_ARB_gpu_shader_fp64");
        }
        if (features.shaderInt64()) {
            extensions.add("GL_ARB_gpu_shader_int64");
        }
        return extensions.toArray(String[]::new);
    }

    private static final List<VulkanFeature> SHADER_FEATURES = List.of(
        feature("geometryShader", VkPhysicalDeviceFeatures.GEOMETRYSHADER),
        feature("shaderFloat64", VkPhysicalDeviceFeatures.SHADERFLOAT64),
        feature("shaderInt64", VkPhysicalDeviceFeatures.SHADERINT64),
        feature("independentBlend", VkPhysicalDeviceFeatures.INDEPENDENTBLEND),
        feature("shaderUniformBufferArrayDynamicIndexing", VkPhysicalDeviceFeatures.SHADERUNIFORMBUFFERARRAYDYNAMICINDEXING),
        feature("shaderSampledImageArrayDynamicIndexing", VkPhysicalDeviceFeatures.SHADERSAMPLEDIMAGEARRAYDYNAMICINDEXING),
        feature("shaderStorageBufferArrayDynamicIndexing", VkPhysicalDeviceFeatures.SHADERSTORAGEBUFFERARRAYDYNAMICINDEXING),
        feature("shaderStorageImageArrayDynamicIndexing", VkPhysicalDeviceFeatures.SHADERSTORAGEIMAGEARRAYDYNAMICINDEXING),
        feature("shaderImageGatherExtended", VkPhysicalDeviceFeatures.SHADERIMAGEGATHEREXTENDED),
        feature("shaderStorageImageExtendedFormats", VkPhysicalDeviceFeatures.SHADERSTORAGEIMAGEEXTENDEDFORMATS),
        feature("shaderStorageImageReadWithoutFormat", VkPhysicalDeviceFeatures.SHADERSTORAGEIMAGEREADWITHOUTFORMAT),
        feature("shaderStorageImageWriteWithoutFormat", VkPhysicalDeviceFeatures.SHADERSTORAGEIMAGEWRITEWITHOUTFORMAT),
        feature("vertexPipelineStoresAndAtomics", VkPhysicalDeviceFeatures.VERTEXPIPELINESTORESANDATOMICS),
        feature("fragmentStoresAndAtomics", VkPhysicalDeviceFeatures.FRAGMENTSTORESANDATOMICS),
        feature("shaderClipDistance", VkPhysicalDeviceFeatures.SHADERCLIPDISTANCE),
        feature("shaderCullDistance", VkPhysicalDeviceFeatures.SHADERCULLDISTANCE),
        feature("depthClamp", VkPhysicalDeviceFeatures.DEPTHCLAMP)
    );

    private static VulkanFeature feature(String name, int offset) {
        return new VulkanFeature(VulkanBackend.VK10_FEATURES_STRUCT, name, offset);
    }

    public static void enableSupported(VkPhysicalDevice physicalDevice, Set<VulkanFeature> enabledFeatures) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPhysicalDeviceFeatures2 features = VkPhysicalDeviceFeatures2.calloc(stack).sType$Default();
            VK12.vkGetPhysicalDeviceFeatures2(physicalDevice, features);
            for (VulkanFeature feature : SHADER_FEATURES) {
                if (feature.get(features)) {
                    enabledFeatures.add(feature);
                }
            }
        }
    }
}
