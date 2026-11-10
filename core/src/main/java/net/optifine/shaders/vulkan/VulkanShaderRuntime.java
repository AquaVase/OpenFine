package net.optifine.shaders.vulkan;

import com.mojang.blaze3d.systems.GpuDeviceBackend;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.RenderPassDescriptor;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.vulkan.VulkanCommandEncoder;
import java.nio.ByteOrder;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.util.spvc.Spv;
import com.mojang.blaze3d.vulkan.VulkanDevice;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.AddressMode;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.optifine.Config;
import java.util.OptionalDouble;
import java.util.IdentityHashMap;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.GlState;
import net.optifine.shaders.ShadersFramebuffer;
import net.optifine.shaders.config.ShaderType;
import net.minecraft.client.Minecraft;

public class VulkanShaderRuntime {
    private static VulkanShaderRuntime instance;
    private final VulkanDevice device;
    private final VulkanShaderBuffers buffers;
    private final Map<Integer, StageSource> shaders = new LinkedHashMap<>();
    private final Map<Integer, DeviceProgram> programs = new LinkedHashMap<>();
    private final Map<Integer, VulkanCompositeRenderer> composites = new LinkedHashMap<>();
    private final Map<SceneKey, VulkanScenePipeline> scenePipelines = new LinkedHashMap<>();
    private final Map<String, float[]> defaultAttributes = new LinkedHashMap<>();
    private final Map<TextureTarget, TextureBinding> textures = new LinkedHashMap<>();
    private final TextureBinding[] textures2D = new TextureBinding[64];
    private GpuBuffer centerDepthReadback;
    private GpuFence centerDepthFence;
    private float lastCenterDepth = 0.5F;
    private final Map<Integer, GpuTextureView> images = new LinkedHashMap<>();
    private final Map<Integer, VulkanTexelBuffer> texelTextures = new LinkedHashMap<>();
    private final Map<Integer, VulkanTexelBuffer> texelImages = new LinkedHashMap<>();
    private final Map<GpuTexture, GpuTextureView> textureViews = new IdentityHashMap<>();
    private final Map<MaterialSamplerKey, GpuSampler> materialSamplers = new LinkedHashMap<>();
    private int depthMipmapProgram;
    private boolean shadowCull;
    private Boolean sceneCull;
    private Boolean sceneDepthWrite;
    private long textureVersion;
    private int nextShader = 1;
    private int nextProgram = 1;
    private VulkanCompilationBatch compilationBatch;
    private final Map<Integer, Integer> pendingPrograms = new LinkedHashMap<>();

    public void beginCompilation() {
        if (this.compilationBatch != null) {
            throw new IllegalStateException("Shader compilation is already active");
        }
        this.compilationBatch = new VulkanCompilationBatch();
    }

    public void finishCompilation() {
        try {
            for (var entry : this.pendingPrograms.entrySet()) {
                this.installProgram(entry.getKey(), this.compilationBatch.take(entry.getValue()));
            }
        } finally {
            this.cancelCompilation();
        }
    }

    public void cancelCompilation() {
        if (this.compilationBatch != null) {
            this.compilationBatch.close();
            this.compilationBatch = null;
        }
        this.pendingPrograms.clear();
    }

    private VulkanShaderRuntime(VulkanDevice device) {
        this.device = device;
        this.buffers = new VulkanShaderBuffers(device);
    }

    public VulkanShaderBuffers getBuffers() {
        return this.buffers;
    }

    public GpuSampler getMaterialSampler(GpuTexture texture, GpuSampler source, boolean blend, boolean auxiliary) {
        if (!(texture.getParentTexture() instanceof TextureAtlas)) {
            return source;
        }
        blend &= texture.isShaderTextureBlend();
        FilterMode minFilter = !blend ? FilterMode.NEAREST : auxiliary ? FilterMode.LINEAR : source.getMinFilter();
        FilterMode magFilter = !blend ? FilterMode.NEAREST : auxiliary ? FilterMode.LINEAR : source.getMagFilter();
        int anisotropy = blend ? Math.max(source.getMaxAnisotropy(), Config.getAnisotropicFilterLevel()) : 1;
        anisotropy = Math.max(1, Math.min(anisotropy, RenderSystem.getDevice().getDeviceInfo().limits().maxAnisotropy()));
        if (minFilter == source.getMinFilter() && magFilter == source.getMagFilter() && anisotropy == source.getMaxAnisotropy()) {
            return source;
        }
        MaterialSamplerKey key = new MaterialSamplerKey(source.getAddressModeU(), source.getAddressModeV(), minFilter, magFilter,
            anisotropy, source.getMaxLod());
        return this.materialSamplers.computeIfAbsent(key, value -> RenderSystem.getDevice().createSampler(value.u(), value.v(),
            value.minFilter(), value.magFilter(), value.anisotropy(), value.maxLod()));
    }

    public static VulkanShaderRuntime getInstance() {
        RenderSystem.assertOnRenderThread();
        if (!(RenderSystem.getDevice().getBackend() instanceof VulkanDevice device)) {
            throw new IllegalStateException("OptiFine Vulkan runtime requires a Vulkan device");
        }

        if (instance == null) {
            instance = new VulkanShaderRuntime(device);
        } else if (instance.device != device) {
            throw new IllegalStateException("Previous OptiFine Vulkan runtime was not released before replacing its device");
        }

        return instance;
    }

    public static boolean isSceneActive() {
        return Shaders.isRenderingWorld && Shaders.activeProgramID != 0;
    }

    public static RenderPassDescriptor redirectRenderPass(RenderPassDescriptor descriptor) {
        if (isSceneActive()) {
            for (var attachment : descriptor.colorAttachments()) {
                if (attachment != null) {
                    var target = redirectMainTarget(attachment.textureView().texture(), null);
                    if (target != null) {
                        return target;
                    }
                }
            }
            if (descriptor.depthAttachment() != null) {
                var target = redirectMainTarget(null, descriptor.depthAttachment().textureView().texture());
                if (target != null) {
                    return target;
                }
            }
        }
        return descriptor;
    }

    public static boolean isSceneActive(RenderPassDescriptor descriptor) {
        if (!isSceneActive() || !(GlState.getFramebuffer() instanceof ShadersFramebuffer)) {
            return false;
        }
        var renderer = Minecraft.getInstance().gameRenderer;
        if (renderer == null) {
            return false;
        }
        var target = renderer.mainRenderTarget();
        for (var attachment : descriptor.colorAttachments()) {
            if (attachment != null && attachment.textureView().texture() == target.getColorTexture()) {
                return true;
            }
        }
        return descriptor.depthAttachment() != null && descriptor.depthAttachment().textureView().texture() == target.getDepthTexture();
    }

    public static RenderPassDescriptor redirectMainTarget(GpuTexture color, GpuTexture depth) {
        if (Shaders.isRenderingWorld && GlState.getFramebuffer() instanceof ShadersFramebuffer framebuffer) {
            var renderer = Minecraft.getInstance().gameRenderer;
            if (renderer != null) {
                var target = renderer.mainRenderTarget();
                if (color != null && color == target.getColorTexture() || depth != null && depth == target.getDepthTexture()) {
                    return framebuffer.getVulkanFramebuffer().createRenderPassDescriptor(framebuffer.getDrawBuffers());
                }
            }
        }
        return null;
    }

    public static void releaseDevice(GpuDeviceBackend device) {
        if (instance != null && instance.device == device) {
            instance.clear();
            instance = null;
        }
    }

    public int createShader(ShaderType type, String path) {
        int id = this.nextShader++;
        this.shaders.put(id, new StageSource(type, path, null));
        return id;
    }

    public void setShaderSource(int id, String source) {
        StageSource stage = this.getShader(id);
        this.shaders.put(id, new StageSource(stage.type(), stage.path(), source));
    }

    public void deleteShader(int id) {
        this.shaders.remove(id);
    }

    public int linkProgram(String name, int... shaders) {
        Map<ShaderType, String> sources = new EnumMap<>(ShaderType.class);
        for (int id : shaders) {
            if (id == 0) {
                continue;
            }

            StageSource stage = this.getShader(id);
            if (stage.source() == null || sources.put(stage.type(), stage.source()) != null) {
                throw new IllegalArgumentException("Missing source or duplicate stage: " + stage.path());
            }
        }

        int id = this.nextProgram++;
        if (this.compilationBatch != null) {
            this.pendingPrograms.put(id, this.compilationBatch.submit(name, sources));
        } else {
            try (VulkanShaderCompiler compiler = new VulkanShaderCompiler()) {
                this.installProgram(id, compiler.compile(name, sources));
            }
        }
        return id;
    }

    private void installProgram(int id, VulkanShaderProgram source) {
        VulkanDeviceProgram program = null;
        try {
            program = source.createDeviceProgram(this.device.vkDevice());
            VulkanProgramUniforms uniforms = new VulkanProgramUniforms(source, Shaders::flushRenderBuffers, this.device.getDeviceInfo().limits().minUniformOffsetAlignment());
            this.programs.put(id, new DeviceProgram(source, program, uniforms));
        } catch (RuntimeException | Error e) {
            if (program != null) {
                program.close();
            }
            source.close();
            throw e;
        }
    }

    public DeviceProgram getProgram(int id) {
        DeviceProgram program = this.programs.get(id);
        if (program == null) {
            throw new IllegalArgumentException("Unknown OptiFine Vulkan program: " + id);
        }

        return program;
    }

    public void deleteProgram(int id) {
        this.scenePipelines.keySet().removeIf(key -> key.program() == id);
        VulkanCompositeRenderer composite = this.composites.remove(id);
        if (composite != null) {
            composite.close();
        }
        DeviceProgram program = this.programs.remove(id);
        if (program != null) {
            this.device.createCommandEncoder().queueForDestroy(() -> {
                program.deviceProgram().close();
                program.source().close();
            });
        }
    }

    public void drawComposite(int id, RenderPassDescriptor target, boolean writeDepth) {
        var blends = VulkanBlendState.capture(Shaders.activeProgramID == id ? Shaders.activeProgram : null, target.colorAttachments().size());
        this.composites.computeIfAbsent(id, key -> new VulkanCompositeRenderer(this.device, this.getProgram(key), this)).draw(target, writeDepth, blends);
    }

    public VulkanScenePipeline getScenePipeline(int id, RenderPipeline state, List<Integer> colors, int depth) {
        var blends = VulkanBlendState.capture(Shaders.activeProgram, colors.size());
        return this.scenePipelines.computeIfAbsent(new SceneKey(id, state, List.copyOf(colors), depth, Shaders.isShadowPass, this.shadowCull, this.sceneCull, this.sceneDepthWrite, blends),
            key -> new VulkanScenePipeline(this.device, this.getProgram(id), state, key.colors(), depth, key.shadow(), key.shadowCull(), key.cull(), key.depthWrite(), key.blends()));
    }

    public void setSceneCull(Boolean cull) {
        this.sceneCull = cull;
    }

    public void setSceneDepthWrite(Boolean write) {
        this.sceneDepthWrite = write;
    }

    public void setShadowCull(boolean cull) {
        this.shadowCull = cull;
    }

    public void setDefaultAttribute(String semantic, float x, float y, float z, float w) {
        this.defaultAttributes.put(semantic, new float[]{x, y, z, w});
    }

    public float[] getDefaultAttribute(String semantic) {
        float[] value = this.defaultAttributes.get(semantic);
        if (value != null) {
            return value.clone();
        }
        return switch (semantic) {
            case "Color" -> new float[]{1, 1, 1, 1};
            case "Normal" -> new float[]{0, 1, 0, 1};
            case "UV2" -> new float[]{240, 240, 0, 1};
            default -> new float[]{0, 0, 0, 1};
        };
    }

    public int getDepthMipmapProgram() {
        if (this.depthMipmapProgram == 0) {
            int vertex = this.createShader(ShaderType.VERTEX, "depth_mipmap.vsh");
            int fragment = this.createShader(ShaderType.FRAGMENT, "depth_mipmap.fsh");
            try {
                this.setShaderSource(vertex, """
                    #version 450
                    out vec2 uv;
                    void main() {
                        vec2 corners[4] = vec2[4](vec2(0, 0), vec2(1, 0), vec2(1, 1), vec2(0, 1));
                        uv = corners[gl_VertexID];
                        gl_Position = vec4(uv * 2.0 - 1.0, 0.0, 1.0);
                    }
                    """);
                this.setShaderSource(fragment, """
                    #version 450
                    uniform sampler2D sourceDepth;
                    in vec2 uv;
                    void main() { gl_FragDepth = textureLod(sourceDepth, uv, 0.0).r; }
                    """);
                this.depthMipmapProgram = this.linkProgram("depth_mipmap", vertex, fragment);
            } finally {
                this.deleteShader(vertex);
                this.deleteShader(fragment);
            }
        }
        return this.depthMipmapProgram;
    }

    public void clear() {
        if (this.centerDepthFence != null) {
            this.centerDepthFence.close();
            this.centerDepthFence = null;
        }
        if (this.centerDepthReadback != null) {
            this.centerDepthReadback.close();
            this.centerDepthReadback = null;
        }
        this.cancelCompilation();
        for (int id : this.programs.keySet().stream().toList()) {
            this.deleteProgram(id);
        }
        this.depthMipmapProgram = 0;
        this.shaders.clear();
        this.textures.clear();
        java.util.Arrays.fill(this.textures2D, null);
        this.images.clear();
        this.texelTextures.clear();
        this.texelImages.clear();
        this.buffers.clear();
        this.defaultAttributes.clear();
        this.sceneCull = null;
        this.sceneDepthWrite = null;
        this.textureViews.values().forEach(GpuTextureView::close);
        this.textureViews.clear();
        this.materialSamplers.values().forEach(GpuSampler::close);
        this.materialSamplers.clear();
        this.textureVersion++;
    }

    private record MaterialSamplerKey(AddressMode u, AddressMode v, FilterMode minFilter, FilterMode magFilter, int anisotropy,
        OptionalDouble maxLod) {
    }

    public GpuTextureView getTextureView(GpuTexture texture) {
        if (texture.isClosed()) {
            throw new IllegalStateException("Cannot bind a closed shader texture");
        }
        GpuTextureView existing = this.textureViews.get(texture);
        if (existing != null && !existing.isClosed()) {
            return existing;
        }
        this.textureViews.entrySet().removeIf(entry -> {
            if (entry.getKey().isClosed()) {
                entry.getValue().close();
                return true;
            }
            return false;
        });
        this.textureViews.remove(texture);
        return this.textureViews.computeIfAbsent(texture, RenderSystem.getDevice()::createTextureView);
    }

    public void bindTexture(int unit, GpuTextureView view, long sampler) {
        if (unit < 0 || view.isClosed() || view.texture().isClosed() || sampler == 0L) {
            throw new IllegalArgumentException("Invalid shader texture binding at unit " + unit);
        }
        var texture = (com.mojang.blaze3d.vulkan.VulkanGpuTexture)view.texture();
        int dimension = (texture.usage() & 16) != 0 ? Spv.SpvDimCube : switch (texture.imageType()) {
            case VK12.VK_IMAGE_TYPE_1D -> Spv.SpvDim1D;
            case VK12.VK_IMAGE_TYPE_3D -> Spv.SpvDim3D;
            default -> Spv.SpvDim2D;
        };
        // These views expose a single layer (or a single cube), even for layered textures.
        boolean arrayed = false;
        if (dimension == Spv.SpvDim2D && !arrayed && unit < this.textures2D.length) {
            TextureBinding previous = this.textures2D[unit];
            if (previous == null || previous.view() != view || previous.sampler() != sampler) {
                this.textures2D[unit] = new TextureBinding(view, sampler);
                this.textureVersion++;
            }
        } else {
            TextureTarget target = new TextureTarget(unit, dimension, arrayed);
            TextureBinding previous = this.textures.get(target);
            if (previous == null || previous.view() != view || previous.sampler() != sampler) {
                this.textures.put(target, new TextureBinding(view, sampler));
                this.textureVersion++;
            }
        }
    }

    public void unbindTexture(int unit) {
        this.unbindTexture(unit, Spv.SpvDim2D, false);
    }

    public void unbindTexture(int unit, int dimension, boolean arrayed) {
        if (dimension == Spv.SpvDim2D && !arrayed && unit >= 0 && unit < this.textures2D.length) {
            if (this.textures2D[unit] != null) {
                this.textures2D[unit] = null;
                this.textureVersion++;
            }
        } else if (this.textures.remove(new TextureTarget(unit, dimension, arrayed)) != null) {
            this.textureVersion++;
        }
    }

    public TextureBinding getTexture(int unit) {
        return this.getTexture(unit, Spv.SpvDim2D, false);
    }

    public TextureBinding getTexture(int unit, int dimension, boolean arrayed) {
        boolean direct = dimension == Spv.SpvDim2D && !arrayed && unit >= 0 && unit < this.textures2D.length;
        TextureBinding binding = direct ? this.textures2D[unit] : this.textures.get(new TextureTarget(unit, dimension, arrayed));
        if (binding != null && (binding.view().isClosed() || binding.view().texture().isClosed())) {
            this.unbindTexture(unit, dimension, arrayed);
            return null;
        }
        return binding;
    }

    public void unbindImage(int unit) {
        if (this.images.remove(unit) != null) {
            this.textureVersion++;
        }
    }

    public float readDepthAsync(GpuTexture texture, int x, int y) {
        if (this.centerDepthFence != null) {
            if (!this.centerDepthFence.awaitCompletion(0L)) {
                return this.lastCenterDepth;
            }
            try (var mapping = this.centerDepthReadback.map(true, false)) {
                this.lastCenterDepth = mapping.data().order(ByteOrder.nativeOrder()).getFloat(0);
            }
            this.centerDepthFence.close();
            this.centerDepthFence = null;
        }
        if (this.centerDepthReadback == null) {
            this.centerDepthReadback = this.device.createBuffer(() -> "OptiFine asynchronous center depth",
                GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_MAP_READ, Float.BYTES);
        }
        var encoder = this.device.createCommandEncoder();
        encoder.copyTextureToBuffer(texture, this.centerDepthReadback, 0, () -> {}, 0, x, y, 1, 1);
        this.centerDepthFence = encoder.createFence();
        return this.lastCenterDepth;
    }

    public float readDepth(GpuTexture texture, int x, int y) {
        try (GpuBuffer buffer = this.device.createBuffer(() -> "OptiFine center depth", GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_MAP_READ, 4)) {
            VulkanCommandEncoder encoder = this.device.createCommandEncoder();
            encoder.copyTextureToBuffer(texture, buffer, 0, () -> {}, 0, x, y, 1, 1);
            try (GpuFence fence = encoder.createFence()) {
                encoder.submit();
                if (!fence.awaitCompletion(5000000000L)) {
                    throw new IllegalStateException("Timed out reading shader center depth");
                }
            }
            try (var mapping = buffer.map(true, false)) {
                return mapping.data().order(ByteOrder.nativeOrder()).getFloat(0);
            }
        }
    }

    public void dispatchCompute(int id, int x, int y, int z) {
        if (x <= 0 || y <= 0 || z <= 0) {
            throw new IllegalArgumentException("Compute workgroup counts must be positive");
        }
        DeviceProgram program = this.getProgram(id);
        try (VulkanDrawBindings bindings = new VulkanDrawBindings(this.device, program, this, Map.of());
             MemoryStack stack = MemoryStack.stackPush()) {
            VulkanCommandEncoder encoder = this.device.createCommandEncoder();
            var commands = encoder.allocateAndBeginTransientCommandBuffer();
            VulkanCommandEncoder.memoryBarrier(commands, stack);
            VK12.vkCmdBindPipeline(commands, VK12.VK_PIPELINE_BIND_POINT_COMPUTE, program.deviceProgram().getComputePipeline());
            bindings.bind(commands, VK12.VK_PIPELINE_BIND_POINT_COMPUTE);
            VK12.vkCmdDispatch(commands, x, y, z);
            VulkanCommandEncoder.memoryBarrier(commands, stack);
            VulkanDeviceProgram.check(VK12.vkEndCommandBuffer(commands), "end shader compute command buffer");
            encoder.execute(commands);
        }
    }

    public void bindImage(int unit, GpuTextureView view) {
        if (unit < 0 || view.isClosed() || view.texture().isClosed() || (view.texture().usage() & GpuTexture.USAGE_STORAGE_BINDING) == 0) {
            throw new IllegalArgumentException("Invalid shader image binding at unit " + unit);
        }
        if (this.images.put(unit, view) != view) {
            this.textureVersion++;
        }
    }

    public GpuTextureView getImage(int unit) {
        GpuTextureView view = this.images.get(unit);
        if (view != null && (view.isClosed() || view.texture().isClosed())) {
            this.unbindImage(unit);
            return null;
        }
        return view;
    }

    public long getTextureVersion() {
        return this.textureVersion;
    }

    public void bindTexelBuffer(int unit, boolean storage, VulkanTexelBuffer view) {
        if (unit < 0 || view != null && (view.isClosed() || view.isStorage() != storage)) {
            throw new IllegalArgumentException("Invalid shader texel buffer binding at unit " + unit);
        }
        var bindings = storage ? this.texelImages : this.texelTextures;
        var previous = view == null ? bindings.remove(unit) : bindings.put(unit, view);
        if (previous != view) {
            this.textureVersion++;
        }
    }

    public VulkanTexelBuffer getTexelBuffer(int unit, boolean storage) {
        var bindings = storage ? this.texelImages : this.texelTextures;
        var view = bindings.get(unit);
        if (view != null && view.isClosed()) {
            this.bindTexelBuffer(unit, storage, null);
            return null;
        }
        return view;
    }

    private StageSource getShader(int id) {
        StageSource source = this.shaders.get(id);
        if (source == null) {
            throw new IllegalArgumentException("Unknown OptiFine Vulkan shader: " + id);
        }

        return source;
    }

    private record StageSource(ShaderType type, String path, String source) {
    }

    private record SceneKey(int program, RenderPipeline state, List<Integer> colors, int depth, boolean shadow, boolean shadowCull, Boolean cull, Boolean depthWrite, List<VulkanBlendState> blends) {
    }

    public record DeviceProgram(VulkanShaderProgram source, VulkanDeviceProgram deviceProgram, VulkanProgramUniforms uniforms) {
    }

    public record TextureBinding(GpuTextureView view, long sampler) {
    }

    private record TextureTarget(int unit, int dimension, boolean arrayed) {
    }
}
