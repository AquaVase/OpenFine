package net.optifine.shaders.vulkan;

import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;
import net.optifine.shaders.config.ShaderType;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.shaderc.Shaderc;

public class VulkanShaderCompiler implements AutoCloseable {
    private static final Pattern TRINARY_HELPER = Pattern.compile(
        "\\b(?:float|double|int|uint|[diu]?vec[234])\\s+(min3|max3|mid3)\\s*\\(");
    private long compiler;
    private long options;
    private long initializerOptions;

    public VulkanShaderCompiler() {
        this.compiler = Shaderc.shaderc_compiler_initialize();
        this.options = Shaderc.shaderc_compile_options_initialize();
        this.initializerOptions = Shaderc.shaderc_compile_options_initialize();
        if (this.compiler == 0L || this.options == 0L || this.initializerOptions == 0L) {
            this.close();
            throw new IllegalStateException("Failed to initialize Vulkan shader compiler");
        }

        Shaderc.shaderc_compile_options_set_target_env(this.options, Shaderc.shaderc_target_env_vulkan, Shaderc.shaderc_env_version_vulkan_1_2);
        Shaderc.shaderc_compile_options_set_target_spirv(this.options, Shaderc.shaderc_spirv_version_1_5);
        Shaderc.shaderc_compile_options_set_forced_version_profile(this.options, 450, Shaderc.shaderc_profile_core);
        Shaderc.shaderc_compile_options_set_auto_bind_uniforms(this.options, true);
        Shaderc.shaderc_compile_options_set_auto_map_locations(this.options, true);
        // glslang puts legacy standalone uniforms into a reflected uniform block.
        Shaderc.shaderc_compile_options_set_vulkan_rules_relaxed(this.options, true);
        Shaderc.shaderc_compile_options_set_generate_debug_info(this.options);
        Shaderc.shaderc_compile_options_add_macro_definition(this.options, "gl_VertexID", "gl_VertexIndex");
        Shaderc.shaderc_compile_options_add_macro_definition(this.options, "gl_InstanceID", "gl_InstanceIndex");
        Shaderc.shaderc_compile_options_set_target_env(this.initializerOptions, Shaderc.shaderc_target_env_opengl, Shaderc.shaderc_env_version_opengl_4_5);
        Shaderc.shaderc_compile_options_set_forced_version_profile(this.initializerOptions, 450, Shaderc.shaderc_profile_core);
        Shaderc.shaderc_compile_options_set_auto_bind_uniforms(this.initializerOptions, true);
        Shaderc.shaderc_compile_options_set_auto_map_locations(this.initializerOptions, true);
        Shaderc.shaderc_compile_options_set_generate_debug_info(this.initializerOptions);
        Shaderc.shaderc_compile_options_add_macro_definition(this.initializerOptions, "gl_VertexIndex", "gl_VertexID");
        Shaderc.shaderc_compile_options_add_macro_definition(this.initializerOptions, "gl_InstanceIndex", "gl_InstanceID");
    }

    public synchronized VulkanShaderProgram compile(String name, Map<ShaderType, String> sources) {
        if (this.compiler == 0L) {
            throw new IllegalStateException("Vulkan shader compiler is closed");
        }

        boolean compute = sources.containsKey(ShaderType.COMPUTE);
        if (sources.isEmpty() || compute && sources.size() != 1 || !compute && !sources.containsKey(ShaderType.VERTEX)) {
            throw new IllegalArgumentException("Expected a compute program or a graphics program with a vertex shader: " + name);
        }

        Map<ShaderType, VulkanShaderModule> modules = new EnumMap<>(ShaderType.class);
        try {
            for (Map.Entry<ShaderType, String> entry : sources.entrySet()) {
                modules.put(entry.getKey(), this.compileModule(name, entry.getKey(), entry.getValue()));
            }

            return new VulkanShaderProgram(name, modules);
        } catch (RuntimeException | Error e) {
            modules.values().forEach(VulkanShaderModule::close);
            throw e;
        }
    }

    private VulkanShaderModule compileModule(String name, ShaderType type, String source) {
        source = VulkanTransparency.adapt(name, type, source);
        String filename = name + "." + type.name().toLowerCase(java.util.Locale.ROOT);
        // Expanded shader packs can exceed LWJGL's thread-local stack size.
        ByteBuffer sourceBuffer = MemoryUtil.memUTF8(source, false);
        ByteBuffer nameBuffer = null;
        ByteBuffer entryBuffer = null;
        long result = 0L;
        try {
            nameBuffer = MemoryUtil.memUTF8(filename);
            entryBuffer = MemoryUtil.memUTF8("main");
            long expanded = Shaderc.shaderc_compile_into_preprocessed_text(this.compiler, sourceBuffer, getShaderKind(type), nameBuffer, entryBuffer, this.options);
            try {
                checkInitializerResult(expanded);
                String original = MemoryUtil.memUTF8(Shaderc.shaderc_result_get_bytes(expanded));
                String adapted = VulkanRectangleShaders.adapt(renameLegacyIdentifiers(original));
                MemoryUtil.memFree(sourceBuffer);
                sourceBuffer = null;
                sourceBuffer = MemoryUtil.memUTF8(adapted, false);
            } finally {
                if (expanded != 0L) {
                    Shaderc.shaderc_result_release(expanded);
                }
            }
            result = Shaderc.shaderc_compile_into_spv(this.compiler, sourceBuffer, getShaderKind(type), nameBuffer, entryBuffer, this.options);
            if (result == 0L) {
                throw new IllegalStateException("Failed to compile Vulkan shader: " + filename);
            }

            String log = Shaderc.shaderc_result_get_error_message(result);
            if (Shaderc.shaderc_result_get_compilation_status(result) != Shaderc.shaderc_compilation_status_success) {
                throw new IllegalArgumentException("Error compiling Vulkan shader " + filename + ":\n" + log);
            }

            ByteBuffer bytes = Shaderc.shaderc_result_get_bytes(result);
            if (bytes == null) {
                throw new IllegalStateException("Vulkan shader compiler returned no SPIR-V: " + filename);
            }

            return new VulkanShaderModule(filename, type, bytes, log, this.readInitializers(type, sourceBuffer, nameBuffer, entryBuffer));
        } finally {
            if (result != 0L) {
                Shaderc.shaderc_result_release(result);
            }

            MemoryUtil.memFree(entryBuffer);
            MemoryUtil.memFree(nameBuffer);
            MemoryUtil.memFree(sourceBuffer);
        }
    }

    private Map<String, VulkanUniformInitializers.Value> readInitializers(ShaderType type, ByteBuffer source, ByteBuffer filename, ByteBuffer entry) {
        String expandedSource = MemoryUtil.memUTF8(source);
        if (!VulkanUniformInitializers.hasInitializers(expandedSource)) {
            return Map.of();
        }
        ByteBuffer metadata = MemoryUtil.memUTF8(VulkanUniformInitializers.metadataSource(expandedSource), false);
        long result = 0L;
        try {
            result = Shaderc.shaderc_compile_into_spv(this.compiler, metadata, getShaderKind(type), filename, entry, this.initializerOptions);
            checkInitializerResult(result);
            return VulkanUniformInitializers.read(Shaderc.shaderc_result_get_bytes(result));
        } finally {
            if (result != 0L) {
                Shaderc.shaderc_result_release(result);
            }
            MemoryUtil.memFree(metadata);
        }
    }

    private static String renameLegacyIdentifiers(String source) {
        // glslang exposes AMD trinary built-ins even with the extension disabled.
        // Keep the pack's own implementation instead of redeclaring those built-ins.
        var declarations = TRINARY_HELPER.matcher(source);
        java.util.Set<String> names = new java.util.LinkedHashSet<>();
        while (declarations.find()) {
            names.add(declarations.group(1));
        }
        // Some desktop GL drivers accept these reserved words as identifiers.
        for (String name : new String[]{"input", "output", "sampler"}) {
            String replacement = "_ofLegacy_" + name;
            while (Pattern.compile("\\b" + replacement + "\\b").matcher(source).find()) {
                replacement += "_";
            }
            source = source.replaceAll("\\b" + name + "\\b", replacement);
        }
        for (String name : names) {
            String replacement = "_ofLegacy_" + name;
            while (Pattern.compile("\\b" + replacement + "\\b").matcher(source).find()) {
                replacement += "_";
            }
            source = source.replaceAll("\\b" + name + "\\b(?=\\s*\\()", replacement);
        }
        return source;
    }

    private static void checkInitializerResult(long result) {
        if (result == 0L || Shaderc.shaderc_result_get_compilation_status(result) != Shaderc.shaderc_compilation_status_success) {
            throw new IllegalArgumentException("Failed to compile uniform initializer metadata: "
                + (result == 0L ? "No compiler result" : Shaderc.shaderc_result_get_error_message(result)));
        }
    }

    private static int getShaderKind(ShaderType type) {
        return switch (type) {
            case VERTEX -> Shaderc.shaderc_vertex_shader;
            case FRAGMENT -> Shaderc.shaderc_fragment_shader;
            case GEOMETRY -> Shaderc.shaderc_geometry_shader;
            case COMPUTE -> Shaderc.shaderc_compute_shader;
        };
    }

    @Override
    public synchronized void close() {
        if (this.initializerOptions != 0L) {
            Shaderc.shaderc_compile_options_release(this.initializerOptions);
            this.initializerOptions = 0L;
        }
        if (this.options != 0L) {
            Shaderc.shaderc_compile_options_release(this.options);
            this.options = 0L;
        }

        if (this.compiler != 0L) {
            Shaderc.shaderc_compiler_release(this.compiler);
            this.compiler = 0L;
        }
    }
}
