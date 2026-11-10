package net.optifine.shaders;

import java.util.ArrayList;
import java.util.List;
import org.lwjgl.opengl.ARBParallelShaderCompile;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.KHRParallelShaderCompile;

public class OpenGlCompilationBatch implements AutoCloseable {
    private final List<Runnable> completions = new ArrayList<>();
    private final boolean arb;
    private final boolean supported;
    private final int previousThreads;

    public OpenGlCompilationBatch() {
        this.arb = GL.getCapabilities().GL_ARB_parallel_shader_compile;
        this.supported = this.arb || GL.getCapabilities().GL_KHR_parallel_shader_compile;
        this.previousThreads = this.supported ? GL11.glGetInteger(37296) : 0;
        if (this.supported) {
            this.setThreads(Math.clamp(Integer.getInteger("optifine.shaderCompileThreads", Runtime.getRuntime().availableProcessors() / 2), 1, 4));
            SMCLog.info("Parallel OpenGL shader compilation enabled");
        }
    }

    public void defer(Runnable completion) {
        if (this.supported) {
            this.completions.add(completion);
        } else {
            completion.run();
        }
    }

    public void finish() {
        for (Runnable completion : this.completions) {
            completion.run();
        }
        this.completions.clear();
    }

    private void setThreads(int threads) {
        if (this.arb) {
            ARBParallelShaderCompile.glMaxShaderCompilerThreadsARB(threads);
        } else {
            KHRParallelShaderCompile.glMaxShaderCompilerThreadsKHR(threads);
        }
    }

    @Override
    public void close() {
        this.completions.clear();
        if (this.supported) {
            this.setThreads(this.previousThreads);
        }
    }
}
