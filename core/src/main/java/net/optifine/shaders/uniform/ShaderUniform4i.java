package net.optifine.shaders.uniform;

import org.lwjgl.opengl.GL20;

public class ShaderUniform4i extends ShaderUniformBase {
    private int[][] programValues;
    private static final int VALUE_UNKNOWN = Integer.MIN_VALUE;

    public ShaderUniform4i(String name) {
        super(name);
        this.resetValue();
    }

    public void setValue(int v0, int v1, int v2, int v3) {
        int program = this.getProgramIndex();
        int[] valueOld = this.programValues[program];
        if (valueOld[0] != v0 || valueOld[1] != v1 || valueOld[2] != v2 || valueOld[3] != v3) {
            valueOld[0] = v0;
            valueOld[1] = v1;
            valueOld[2] = v2;
            valueOld[3] = v3;
            int location = this.getLocation();
            if (location >= 0) {
                flushRenderBuffers();
                if (!this.setVulkanInt(valueOld)) {
                    GL20.glUniform4i(location, v0, v1, v2, v3);
                }
                this.checkGLError();
            }
        }
    }

    public int[] getValue() {
        int program = this.getProgramIndex();
        return this.programValues[program];
    }

    @Override
    protected void onProgramSet(int program) {
        int programIndex = this.getProgramIndex();
        if (programIndex >= this.programValues.length) {
            int[][] valuesOld = this.programValues;
            int[][] valuesNew = new int[programIndex + 10][];
            System.arraycopy(valuesOld, 0, valuesNew, 0, valuesOld.length);
            this.programValues = valuesNew;
        }

        if (this.programValues[programIndex] == null) {
            this.programValues[programIndex] = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
        }
    }

    @Override
    protected void resetValue() {
        this.programValues = new int[][]{{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE}};
    }
}
