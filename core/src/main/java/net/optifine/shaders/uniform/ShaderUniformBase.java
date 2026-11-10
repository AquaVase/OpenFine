package net.optifine.shaders.uniform;

import java.util.Arrays;
import java.nio.FloatBuffer;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.vulkan.VulkanProgramUniforms;
import org.lwjgl.opengl.GL20;

public abstract class ShaderUniformBase {
    private String name;
    private int program = 0;
    private int[] locations = new int[]{-1};
    private VulkanProgramUniforms vulkanUniforms;
    private static final int LOCATION_UNDEFINED = -1;
    private static final int LOCATION_UNKNOWN = Integer.MIN_VALUE;

    public ShaderUniformBase(String name) {
        this.name = name;
    }

    public void setProgram(int program) {
        this.setProgram(program, null);
    }

    public void setProgram(int program, VulkanProgramUniforms vulkanUniforms) {
        this.vulkanUniforms = vulkanUniforms;
        if (this.program != program) {
            this.program = program;
            this.expandLocations();
            this.onProgramSet(program);
        }
    }

    private void expandLocations() {
        if (this.program >= this.locations.length) {
            int[] locationsNew = new int[this.program * 2];
            Arrays.fill(locationsNew, Integer.MIN_VALUE);
            System.arraycopy(this.locations, 0, locationsNew, 0, this.locations.length);
            this.locations = locationsNew;
        }
    }

    protected abstract void onProgramSet(int var1);

    public String getName() {
        return this.name;
    }

    public int getProgram() {
        return this.program;
    }

    public int getLocation() {
        if (this.program <= 0) {
            return -1;
        }

        int location = this.locations[this.program];
        if (location == Integer.MIN_VALUE) {
            location = this.vulkanUniforms != null ? (this.vulkanUniforms.contains(this.name) ? 0 : -1)
                : GL20.glGetUniformLocation(this.program, this.name);
            this.locations[this.program] = location;
        }

        return location;
    }

    public boolean isDefined() {
        return this.getLocation() >= 0;
    }

    public void disable() {
        this.locations[this.program] = -1;
    }

    public void reset() {
        this.program = 0;
        this.locations = new int[]{-1};
        this.vulkanUniforms = null;
        this.resetValue();
    }

    protected abstract void resetValue();

    protected void checkGLError() {
        if (this.vulkanUniforms == null && Shaders.checkGLError(this.name) != 0) {
            this.disable();
        }
    }

    protected final void flushRenderBuffers() {
        if (this.vulkanUniforms != null) {
            this.vulkanUniforms.flush();
        } else {
            Shaders.flushRenderBuffers();
        }
    }

    protected boolean setVulkanFloat(float... values) {
        if (this.vulkanUniforms == null) {
            return false;
        }

        this.vulkanUniforms.setFloat(this.name, values);
        return true;
    }

    protected boolean setVulkanInt(int... values) {
        if (this.vulkanUniforms == null) {
            return false;
        }

        this.vulkanUniforms.setInt(this.name, values);
        return true;
    }

    protected boolean setVulkanMatrix(int size, boolean transpose, FloatBuffer matrix) {
        if (this.vulkanUniforms == null) {
            return false;
        }

        this.vulkanUniforms.setMatrix(this.name, size, transpose, matrix);
        return true;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
