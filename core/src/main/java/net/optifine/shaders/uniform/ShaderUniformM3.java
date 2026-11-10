package net.optifine.shaders.uniform;

import java.nio.FloatBuffer;
import net.optifine.util.BufferUtil;
import net.optifine.util.MathUtils;
import org.joml.Matrix3f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

public class ShaderUniformM3 extends ShaderUniformBase {
    private boolean transpose;
    private boolean uploaded;
    private FloatBuffer matrixBuffer = MemoryUtil.memAllocFloat(9);
    private FloatBuffer tempBuffer = MemoryUtil.memAllocFloat(9);

    public ShaderUniformM3(String name) {
        super(name);
    }

    public void setValue(Matrix3f matrixIn) {
        this.tempBuffer.clear();
        MathUtils.store(matrixIn, this.tempBuffer);
        this.setValue(false, this.tempBuffer);
    }

    public void setValue(boolean transpose, FloatBuffer matrix) {
        boolean unchanged = this.uploaded && this.transpose == transpose && matrix.remaining() == 9;
        for (int i = 0; unchanged && i < 9; i++) {
            unchanged = Float.floatToRawIntBits(matrix.get(matrix.position() + i)) == Float.floatToRawIntBits(this.matrixBuffer.get(i));
        }
        if (unchanged) {
            return;
        }
        this.transpose = transpose;
        matrix.mark();
        this.matrixBuffer.clear();
        this.matrixBuffer.put(matrix);
        this.matrixBuffer.rewind();
        matrix.reset();
        int location = this.getLocation();
        if (location >= 0) {
            flushRenderBuffers();
            if (!this.setVulkanMatrix(3, transpose, this.matrixBuffer)) {
                GL20.glUniformMatrix3fv(location, transpose, this.matrixBuffer);
            }
            this.checkGLError();
            this.uploaded = true;
        }
    }

    public float getValue(int row, int col) {
        int index = this.transpose ? col * 3 + row : row * 3 + col;
        return this.matrixBuffer.get(index);
    }

    @Override
    protected void onProgramSet(int program) {
        this.uploaded = false;
    }

    @Override
    protected void resetValue() {
        this.uploaded = false;
        BufferUtil.fill(this.matrixBuffer, 0.0F);
    }
}
