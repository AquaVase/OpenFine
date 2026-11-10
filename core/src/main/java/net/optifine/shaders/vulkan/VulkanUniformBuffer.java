package net.optifine.shaders.vulkan;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import org.lwjgl.util.spvc.Spvc;
import org.lwjgl.vulkan.VK12;

public class VulkanUniformBuffer {
    private final ByteBuffer data;
    private final Map<String, Member> uniforms = new LinkedHashMap<>();

    public VulkanUniformBuffer(VulkanShaderModule.Resource resource) {
        if (resource.descriptorType() != VK12.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER) {
            throw new IllegalArgumentException("Not a uniform buffer: " + resource.name());
        }

        this.data = ByteBuffer.allocateDirect(Math.toIntExact(resource.size())).order(ByteOrder.nativeOrder());
        for (VulkanShaderModule.Uniform uniform : resource.uniforms()) {
            this.uniforms.put(uniform.name(), new Member(uniform));
        }
    }

    public boolean contains(String name) {
        return this.uniforms.containsKey(name);
    }

    public int getSize() {
        return this.data.capacity();
    }

    void copyTo(ByteBuffer target, int offset) {
        target.put(offset, this.data, 0, this.data.capacity());
    }

    public ByteBuffer getData() {
        return this.data.asReadOnlyBuffer().order(ByteOrder.nativeOrder());
    }

    public Member getMember(String name) {
        Member member = this.uniforms.get(name);
        if (member == null) {
            throw new IllegalArgumentException("Unknown uniform: " + name);
        }
        return member;
    }

    public void setFloat(String name, float... values) {
        this.getMember(name).setFloat(values);
    }

    public void setInt(String name, int... values) {
        this.getMember(name).setInt(values);
    }

    public void setDouble(String name, double... values) {
        this.getMember(name).setDouble(values);
    }

    public void setLong(String name, long... values) {
        this.getMember(name).setLong(values);
    }

    // Resolve reflection and component strides once, rather than for every uniform update.
    public class Member {
        private final VulkanShaderModule.Uniform uniform;
        private int[] offsets;

        private Member(VulkanShaderModule.Uniform uniform) {
            this.uniform = uniform;
        }

        private int[] offsets(int count, int bitWidth) {
            if (this.uniform.bitWidth() != bitWidth && !(bitWidth == 32 && this.uniform.baseType() == Spvc.SPVC_BASETYPE_BOOLEAN)) {
                throw new IllegalArgumentException("Uniform requires a different scalar width: " + this.uniform.name());
            }
            if (this.offsets == null) {
                if (this.uniform.arrayDimensions() > 1 || this.uniform.rows() < 1 || this.uniform.columns() < 1) {
                    throw new IllegalArgumentException("Unsupported uniform shape: " + this.uniform.name());
                }
                int size = Math.multiplyExact(Math.multiplyExact(this.uniform.rows(), this.uniform.columns()), this.uniform.arraySize());
                int[] offsets = new int[size];
                for (int i = 0; i < size; i++) {
                    offsets[i] = VulkanUniformBuffer.this.getOffset(this.uniform, i);
                }
                this.offsets = offsets;
            }
            if (count != this.offsets.length) {
                throw new IllegalArgumentException("Uniform component count does not match: " + this.uniform.name());
            }
            return this.offsets;
        }

        public void setFloat(float... values) {
            int[] offsets = this.offsets(values.length, 32);
            if (this.uniform.baseType() != Spvc.SPVC_BASETYPE_FP32) {
                throw new IllegalArgumentException("Not a float uniform: " + this.uniform.name());
            }
            for (int i = 0; i < offsets.length; i++) {
                VulkanUniformBuffer.this.put(offsets[i], Float.floatToRawIntBits(values[i]));
            }
        }

        public void setMatrix(int size, boolean transpose, FloatBuffer values) {
            int[] offsets = this.offsets(size * size, 32);
            if (this.uniform.baseType() != Spvc.SPVC_BASETYPE_FP32) {
                throw new IllegalArgumentException("Not a float uniform: " + this.uniform.name());
            }
            int start = values.position();
            for (int i = 0; i < offsets.length; i++) {
                int index = transpose ? i % size * size + i / size : i;
                VulkanUniformBuffer.this.put(offsets[i], Float.floatToRawIntBits(values.get(start + index)));
            }
        }

        public void setInt(int... values) {
            int[] offsets = this.offsets(values.length, 32);
            int type = this.uniform.baseType();
            if (type != Spvc.SPVC_BASETYPE_INT32 && type != Spvc.SPVC_BASETYPE_UINT32 && type != Spvc.SPVC_BASETYPE_BOOLEAN) {
                throw new IllegalArgumentException("Not an integer uniform: " + this.uniform.name());
            }
            for (int i = 0; i < offsets.length; i++) {
                VulkanUniformBuffer.this.put(offsets[i], type == Spvc.SPVC_BASETYPE_BOOLEAN ? (values[i] == 0 ? 0 : 1) : values[i]);
            }
        }

        public void setDouble(double... values) {
            int[] offsets = this.offsets(values.length, 64);
            if (this.uniform.baseType() != Spvc.SPVC_BASETYPE_FP64) {
                throw new IllegalArgumentException("Not a double uniform: " + this.uniform.name());
            }
            for (int i = 0; i < offsets.length; i++) {
                VulkanUniformBuffer.this.putLong(offsets[i], Double.doubleToRawLongBits(values[i]));
            }
        }

        public void setLong(long... values) {
            int[] offsets = this.offsets(values.length, 64);
            if (this.uniform.baseType() != Spvc.SPVC_BASETYPE_INT64 && this.uniform.baseType() != Spvc.SPVC_BASETYPE_UINT64) {
                throw new IllegalArgumentException("Not a 64-bit integer uniform: " + this.uniform.name());
            }
            for (int i = 0; i < offsets.length; i++) {
                VulkanUniformBuffer.this.putLong(offsets[i], values[i]);
            }
        }
    }

    private int getOffset(VulkanShaderModule.Uniform uniform, int component) {
        int matrixSize = uniform.rows() * uniform.columns();
        int element = component / matrixSize;
        int matrixComponent = component % matrixSize;
        int row = matrixComponent % uniform.rows();
        int column = matrixComponent / uniform.rows();
        int scalarBytes = uniform.baseType() == Spvc.SPVC_BASETYPE_BOOLEAN ? 4 : uniform.bitWidth() / 8;
        int offset = uniform.offset() + element * uniform.arrayStride();
        if (uniform.columns() > 1) {
            // Input has OpenGL's column-major order, regardless of the destination block layout.
            offset += uniform.rowMajor() ? row * uniform.matrixStride() + column * scalarBytes : column * uniform.matrixStride() + row * scalarBytes;
        } else {
            offset += row * scalarBytes;
        }

        if (offset < uniform.offset() || (long)offset + scalarBytes > uniform.offset() + uniform.size()) {
            throw new IllegalArgumentException("Uniform component exceeds reflected member size: " + uniform.name());
        }

        return offset;
    }

    private void put(int offset, int value) {
        this.data.putInt(offset, value);
    }

    private void putLong(int offset, long value) {
        this.data.putLong(offset, value);
    }
}
