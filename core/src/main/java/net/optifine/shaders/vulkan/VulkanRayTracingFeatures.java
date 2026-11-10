package net.optifine.shaders.vulkan;

import com.mojang.blaze3d.vulkan.VulkanBackend;
import com.mojang.blaze3d.vulkan.VulkanPhysicalDevice;
import com.mojang.blaze3d.vulkan.init.VulkanFeature;
import com.mojang.blaze3d.vulkan.init.VulkanPNextStruct;
import java.util.Set;
import net.optifine.Config;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceAccelerationStructureFeaturesKHR;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;
import org.lwjgl.vulkan.VkPhysicalDeviceRayQueryFeaturesKHR;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan12Features;

public class VulkanRayTracingFeatures {
    private static final Set<String> EXTENSIONS = Set.of(
        "VK_KHR_acceleration_structure", "VK_KHR_ray_query", "VK_KHR_deferred_host_operations"
    );

    public static void enable(VulkanPhysicalDevice physical, Set<String> extensions, Set<VulkanFeature> features) {
        if (Boolean.getBoolean("optifine.vulkan.disableRayQuery") || !EXTENSIONS.stream().allMatch(physical::hasDeviceExtension)) {
            Config.dbg("Vulkan hardware ray query unavailable or disabled");
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var query = VkPhysicalDeviceRayQueryFeaturesKHR.calloc(stack).sType$Default();
            var acceleration = VkPhysicalDeviceAccelerationStructureFeaturesKHR.calloc(stack).sType$Default().pNext(query.address());
            var vulkan12 = VkPhysicalDeviceVulkan12Features.calloc(stack).sType$Default().pNext(acceleration.address());
            var supported = VkPhysicalDeviceFeatures2.calloc(stack).sType$Default().pNext(vulkan12.address());
            VK11.vkGetPhysicalDeviceFeatures2(physical.vkPhysicalDevice(), supported);
            if (!query.rayQuery() || !acceleration.accelerationStructure() || !vulkan12.bufferDeviceAddress()) {
                Config.dbg("Vulkan hardware ray query features unavailable");
                return;
            }
            extensions.addAll(EXTENSIONS);
            features.add(new VulkanFeature(VulkanBackend.VK12_FEATURES_STRUCT, "bufferDeviceAddress",
                VkPhysicalDeviceVulkan12Features.BUFFERDEVICEADDRESS));
            features.add(new VulkanFeature(new VulkanPNextStruct(query.sType(), query.SIZEOF), "rayQuery",
                VkPhysicalDeviceRayQueryFeaturesKHR.RAYQUERY));
            features.add(new VulkanFeature(new VulkanPNextStruct(acceleration.sType(), acceleration.SIZEOF), "accelerationStructure",
                VkPhysicalDeviceAccelerationStructureFeaturesKHR.ACCELERATIONSTRUCTURE));
            Config.dbg("Vulkan hardware ray query enabled (KHR acceleration structure + buffer device address)");
        }
    }

    public static boolean isEnabled(VkDevice device) {
        return device.getCapabilities().VK_KHR_ray_query && device.getCapabilities().VK_KHR_acceleration_structure;
    }
}
