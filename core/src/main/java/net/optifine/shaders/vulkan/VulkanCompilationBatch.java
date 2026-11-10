package net.optifine.shaders.vulkan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import net.optifine.shaders.config.ShaderType;

public class VulkanCompilationBatch implements AutoCloseable {
    private final ExecutorService executor;
    private final BiFunction<String, Map<ShaderType, String>, VulkanShaderProgram> compiler;
    private final List<CompletableFuture<VulkanShaderProgram>> jobs = new ArrayList<>();
    private boolean closed;

    public VulkanCompilationBatch() {
        this(Math.clamp(Integer.getInteger("optifine.shaderCompileThreads", Math.max(1, Runtime.getRuntime().availableProcessors() / 2)), 1, 4),
            (name, sources) -> {
                try (var compiler = new VulkanShaderCompiler()) {
                    return compiler.compile(name, sources);
                }
            });
    }

    VulkanCompilationBatch(int threads, BiFunction<String, Map<ShaderType, String>, VulkanShaderProgram> compiler) {
        this.executor = Executors.newFixedThreadPool(threads, Thread.ofPlatform().daemon().name("OptiFine shader compiler-", 0).factory());
        this.compiler = compiler;
        net.optifine.shaders.SMCLog.info("Parallel Vulkan shader compilation enabled (%d workers)", threads);
    }

    public int submit(String name, Map<ShaderType, String> sources) {
        if (this.closed) {
            throw new IllegalStateException("Shader compilation batch is closed");
        }
        Map<ShaderType, String> snapshot = Map.copyOf(sources);
        this.jobs.add(CompletableFuture.supplyAsync(() -> this.compiler.apply(name, snapshot), this.executor));
        return this.jobs.size() - 1;
    }

    public VulkanShaderProgram take(int index) {
        var job = this.jobs.get(index);
        if (this.closed || job == null) {
            throw new IllegalStateException("Shader compilation result is no longer owned by this batch");
        }
        VulkanShaderProgram result = await(job);
        this.jobs.set(index, null);
        return result;
    }

    private static VulkanShaderProgram await(CompletableFuture<VulkanShaderProgram> job) {
        try {
            return job.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            if (e.getCause() instanceof Error cause) {
                throw cause;
            }
            throw e;
        }
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.closed = true;
            // Native compiler calls cannot be interrupted safely. Drain every job before releasing its result.
            this.executor.shutdown();
            for (var job : this.jobs) {
                if (job != null) {
                    try {
                        await(job).close();
                    } catch (RuntimeException | Error e) {
                        // The owner reports compilation failures when collecting results.
                    }
                }
            }
            this.executor.close();
            this.jobs.clear();
        }
    }
}
