package net.optifine.shaders.vulkan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.optifine.shaders.config.ShaderType;

public class VulkanCompilationBatch implements AutoCloseable {
    private final ExecutorService executor;
    private final Queue<VulkanShaderCompiler> compilerInstances = new ConcurrentLinkedQueue<>();
    private final ThreadLocal<VulkanShaderCompiler> compiler = ThreadLocal.withInitial(() -> {
        VulkanShaderCompiler compiler = new VulkanShaderCompiler();
        this.compilerInstances.add(compiler);
        return compiler;
    });
    private final List<CompletableFuture<VulkanShaderProgram>> jobs = new ArrayList<>();
    private boolean closed;

    public VulkanCompilationBatch() {
        this(Math.clamp(Integer.getInteger("optifine.shaderCompileThreads", Math.max(1, Runtime.getRuntime().availableProcessors() / 2)), 1, 4));
    }

    VulkanCompilationBatch(int threads) {
        this.executor = Executors.newFixedThreadPool(threads, Thread.ofPlatform().daemon().name("OptiFine shader compiler-", 0).factory());
        net.optifine.shaders.SMCLog.info("Parallel Vulkan shader compilation enabled (%d workers)", threads);
    }

    public int submit(String name, Map<ShaderType, String> sources) {
        if (this.closed) {
            throw new IllegalStateException("Shader compilation batch is closed");
        }
        Map<ShaderType, String> snapshot = Map.copyOf(sources);
        this.jobs.add(CompletableFuture.supplyAsync(() -> this.compiler.get().compile(name, snapshot), this.executor));
        return this.jobs.size() - 1;
    }

    public VulkanShaderProgram take(int index) {
        if (this.closed) {
            throw new IllegalStateException("Shader compilation batch is closed");
        }
        var job = this.jobs.get(index);
        if (job == null) {
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
            // Every worker has finished before its native compiler and options are released.
            this.compilerInstances.forEach(VulkanShaderCompiler::close);
            this.compilerInstances.clear();
            this.jobs.clear();
        }
    }
}
