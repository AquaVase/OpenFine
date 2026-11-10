package net.optifine.shaders.vulkan;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.spvc.Spv;
import org.lwjgl.util.spvc.Spvc;

public class VulkanUniformInitializers {
    private static final Pattern UNIFORM = Pattern.compile("\\buniform\\b");
    private static final Pattern LAYOUT = Pattern.compile("\\blayout\\s*\\(");
    private static final Pattern DESCRIPTOR_SET = Pattern.compile("\\s*set\\s*=.*", Pattern.DOTALL);

    public static String metadataSource(String source) {
        // Descriptor sets have no OpenGL equivalent and do not affect constant values.
        // Keep line breaks and all other qualifiers for useful compiler diagnostics.
        StringBuilder result = new StringBuilder(source);
        var matcher = LAYOUT.matcher(source);
        while (matcher.find()) {
            int depth = 1;
            int start = matcher.end();
            List<Integer> starts = new ArrayList<>();
            List<Integer> ends = new ArrayList<>();
            starts.add(start);
            int end = start;
            for (; end < source.length(); end++) {
                char c = source.charAt(end);
                if (c == '(') {
                    depth++;
                } else if (c == ')') {
                    if (--depth == 0) {
                        ends.add(end);
                        break;
                    }
                } else if (c == ',' && depth == 1) {
                    ends.add(end);
                    starts.add(end + 1);
                }
            }
            if (depth != 0) {
                throw new IllegalArgumentException("Unclosed shader layout qualifier");
            }
            for (int i = 0; i < starts.size(); i++) {
                if (DESCRIPTOR_SET.matcher(source.substring(starts.get(i), ends.get(i))).matches()) {
                    int from = starts.size() == 1 ? matcher.start() : i == 0 ? starts.get(i) : starts.get(i) - 1;
                    int to = starts.size() == 1 ? end + 1 : i == 0 ? starts.get(i + 1) : ends.get(i);
                    for (int p = from; p < to; p++) {
                        if (source.charAt(p) != '\n' && source.charAt(p) != '\r') {
                            result.setCharAt(p, ' ');
                        }
                    }
                }
            }
        }
        return result.toString();
    }

    public static boolean hasInitializers(String source) {
        var matcher = UNIFORM.matcher(source);
        while (matcher.find()) {
            int braces = 0;
            int parentheses = 0;
            for (int i = matcher.end(); i < source.length(); i++) {
                char c = source.charAt(i);
                if (c == '{') {
                    braces++;
                } else if (c == '}') {
                    braces--;
                } else if (c == '(') {
                    parentheses++;
                } else if (c == ')') {
                    parentheses--;
                } else if (braces == 0 && parentheses == 0) {
                    if (c == '=') {
                        return true;
                    }
                    if (c == ';') {
                        break;
                    }
                }
            }
        }
        return false;
    }

    // OpenGL SPIR-V preserves glslang's constant-folded uniform initializers.
    // This module supplies metadata only; it is never submitted to a GPU.
    public static Map<String, Value> read(ByteBuffer spirv) {
        var words = spirv.asReadOnlyBuffer().order(ByteOrder.nativeOrder()).asIntBuffer();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pointer = stack.callocPointer(1);
            check(Spvc.spvc_context_create(pointer));
            long context = pointer.get(0);
            try {
                check(Spvc.spvc_context_parse_spirv(context, words, words.remaining(), pointer));
                long parsed = pointer.get(0);
                check(Spvc.spvc_context_create_compiler(context, Spvc.SPVC_BACKEND_NONE, parsed, Spvc.SPVC_CAPTURE_MODE_TAKE_OWNERSHIP, pointer));
                long compiler = pointer.get(0);
                Map<String, Value> values = new LinkedHashMap<>();
                for (int i = 5; i < words.limit();) {
                    int size = words.get(i) >>> 16;
                    int opcode = words.get(i) & 65535;
                    if (size == 0 || i + size > words.limit()) {
                        throw new IllegalArgumentException("Invalid uniform initializer SPIR-V instruction");
                    }
                    if (opcode == Spv.SpvOpVariable && size == 5 && words.get(i + 3) == Spv.SpvStorageClassUniformConstant) {
                        String name = Spvc.spvc_compiler_get_name(compiler, words.get(i + 2));
                        long constant = Spvc.spvc_compiler_get_constant_handle(compiler, words.get(i + 4));
                        readInitializer(compiler, constant, name, values, stack);
                    }
                    i += size;
                }
                return Map.copyOf(values);
            } finally {
                Spvc.spvc_context_destroy(context);
            }
        }
    }

    private static void readInitializer(long compiler, long constant, String name, Map<String, Value> values, MemoryStack stack) {
        long type = Spvc.spvc_compiler_get_type_handle(compiler, Spvc.spvc_constant_get_type(constant));
        if (Spvc.spvc_type_get_num_member_types(type) > 0 || Spvc.spvc_type_get_num_array_dimensions(type) > 1) {
            PointerBuffer constants = stack.callocPointer(1);
            PointerBuffer count = stack.callocPointer(1);
            Spvc.spvc_constant_get_subconstants(constant, constants, count);
            var ids = MemoryUtil.memIntBuffer(constants.get(0), Math.toIntExact(count.get(0)));
            boolean array = Spvc.spvc_type_get_num_array_dimensions(type) > 0;
            int index = 0;
            while (ids.hasRemaining()) {
                String memberName = array ? name + "[" + index + "]"
                    : name + "." + Spvc.spvc_compiler_get_member_name(compiler, Spvc.spvc_type_get_base_type_id(type), index);
                readInitializer(compiler, Spvc.spvc_compiler_get_constant_handle(compiler, ids.get()), memberName, values, stack);
                index++;
            }
        } else {
            List<Integer> components = new ArrayList<>();
            readConstant(compiler, constant, components, stack);
            values.put(name, new Value(Spvc.spvc_type_get_basetype(type), signature(type), List.copyOf(components)));
        }
    }

    private static void readConstant(long compiler, long constant, List<Integer> values, MemoryStack stack) {
        long type = Spvc.spvc_compiler_get_type_handle(compiler, Spvc.spvc_constant_get_type(constant));
        if (Spvc.spvc_type_get_num_member_types(type) != 0 || Spvc.spvc_type_get_bit_width(type) > 64) {
            throw new UnsupportedOperationException("Uniform initializers require supported scalar, vector, matrix or array layouts");
        }
        if (Spvc.spvc_type_get_num_array_dimensions(type) > 0) {
            PointerBuffer constants = stack.callocPointer(1);
            PointerBuffer count = stack.callocPointer(1);
            Spvc.spvc_constant_get_subconstants(constant, constants, count);
            var ids = MemoryUtil.memIntBuffer(constants.get(0), Math.toIntExact(count.get(0)));
            while (ids.hasRemaining()) {
                readConstant(compiler, Spvc.spvc_compiler_get_constant_handle(compiler, ids.get()), values, stack);
            }
        } else {
            for (int column = 0; column < Spvc.spvc_type_get_columns(type); column++) {
                for (int row = 0; row < Spvc.spvc_type_get_vector_size(type); row++) {
                    if (Spvc.spvc_type_get_bit_width(type) == 64) {
                        long value = Spvc.spvc_type_get_basetype(type) == Spvc.SPVC_BASETYPE_FP64
                            ? Double.doubleToRawLongBits(Spvc.spvc_constant_get_scalar_fp64(constant, column, row))
                            : Spvc.spvc_constant_get_scalar_u64(constant, column, row);
                        values.add((int)value);
                        values.add((int)(value >>> 32));
                        continue;
                    }
                    values.add(Spvc.spvc_type_get_basetype(type) == Spvc.SPVC_BASETYPE_FP32
                        ? Float.floatToRawIntBits(Spvc.spvc_constant_get_scalar_fp32(constant, column, row))
                        : Spvc.spvc_constant_get_scalar_u32(constant, column, row));
                }
            }
        }
    }

    private static String signature(long type) {
        StringBuilder result = new StringBuilder().append(Spvc.spvc_type_get_basetype(type)).append(':')
            .append(Spvc.spvc_type_get_bit_width(type)).append(':').append(Spvc.spvc_type_get_vector_size(type)).append(':')
            .append(Spvc.spvc_type_get_columns(type));
        for (int i = 0; i < Spvc.spvc_type_get_num_array_dimensions(type); i++) {
            result.append('[').append(Spvc.spvc_type_get_array_dimension(type, i)).append(']');
        }
        return result.toString();
    }

    private static void check(int result) {
        if (result != Spvc.SPVC_SUCCESS) {
            throw new IllegalArgumentException("Failed to reflect uniform initializers: " + result);
        }
    }

    public record Value(int baseType, String signature, List<Integer> components) {
        public void apply(VulkanProgramUniforms uniforms, String name) {
            if (this.baseType == Spvc.SPVC_BASETYPE_FP64 || this.baseType == Spvc.SPVC_BASETYPE_INT64 || this.baseType == Spvc.SPVC_BASETYPE_UINT64) {
                if ((this.components.size() & 1) != 0) {
                    throw new IllegalArgumentException("Incomplete 64-bit uniform initializer: " + name);
                }
                long[] bits = new long[this.components.size() / 2];
                for (int i = 0; i < bits.length; i++) {
                    bits[i] = Integer.toUnsignedLong(this.components.get(i * 2)) | (long)this.components.get(i * 2 + 1) << 32;
                }
                if (this.baseType == Spvc.SPVC_BASETYPE_FP64) {
                    double[] values = new double[bits.length];
                    for (int i = 0; i < bits.length; i++) {
                        values[i] = Double.longBitsToDouble(bits[i]);
                    }
                    uniforms.setDouble(name, values);
                } else {
                    uniforms.setLong(name, bits);
                }
            } else if (this.baseType == Spvc.SPVC_BASETYPE_FP32) {
                float[] values = new float[this.components.size()];
                for (int i = 0; i < values.length; i++) {
                    values[i] = Float.intBitsToFloat(this.components.get(i));
                }
                uniforms.setFloat(name, values);
            } else {
                uniforms.setInt(name, this.components.stream().mapToInt(Integer::intValue).toArray());
            }
        }
    }
}
