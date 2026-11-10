package net.optifine.shaders.vulkan;

import com.mojang.blaze3d.GpuFormat;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import net.optifine.texture.InternalFormat;
import net.optifine.texture.PixelFormat;
import net.optifine.texture.PixelType;
import org.lwjgl.system.MemoryUtil;

public class VulkanRawTextureData {
    // Match resetDataUnpacking(): native byte order, row alignment 4, no skips.
    // The returned native buffer belongs to the caller.
    public static ByteBuffer convert(GpuFormat target, PixelFormat format, PixelType type, ByteBuffer source, int width, int height, int depth) {
        return convert(target, null, format, type, source, width, height, depth);
    }

    public static ByteBuffer convert(
        GpuFormat target, InternalFormat storage, PixelFormat format, PixelType type, ByteBuffer source, int width, int height, int depth
    ) {
        if (width < 1 || height < 1 || depth < 1) {
            throw new IllegalArgumentException("Invalid raw texture extent");
        }
        boolean integer = format.name().endsWith("_INTEGER");
        int components = switch (format) {
            case RED, RED_INTEGER -> 1;
            case RG, RG_INTEGER -> 2;
            case RGB, BGR, RGB_INTEGER, BGR_INTEGER -> 3;
            default -> 4;
        };
        int[] packed = switch (type) {
            case UNSIGNED_BYTE_3_3_2, UNSIGNED_BYTE_2_3_3_REV -> new int[]{3, 3, 2};
            case UNSIGNED_SHORT_5_6_5, UNSIGNED_SHORT_5_6_5_REV -> new int[]{5, 6, 5};
            case UNSIGNED_SHORT_4_4_4_4, UNSIGNED_SHORT_4_4_4_4_REV -> new int[]{4, 4, 4, 4};
            case UNSIGNED_SHORT_5_5_5_1, UNSIGNED_SHORT_1_5_5_5_REV -> new int[]{5, 5, 5, 1};
            case UNSIGNED_INT_8_8_8_8, UNSIGNED_INT_8_8_8_8_REV -> new int[]{8, 8, 8, 8};
            case UNSIGNED_INT_10_10_10_2, UNSIGNED_INT_2_10_10_10_REV -> new int[]{10, 10, 10, 2};
            default -> null;
        };
        if (packed != null && packed.length != components) {
            throw new IllegalArgumentException("Packed pixel type does not match channel count: " + type + "/" + format);
        }
        if (packed != null && packed.length == 3 && format != PixelFormat.RGB && format != PixelFormat.RGB_INTEGER) {
            throw new IllegalArgumentException("Three-component packed pixels require RGB order: " + type + "/" + format);
        }
        int componentBytes = switch (type) {
            case BYTE, UNSIGNED_BYTE, UNSIGNED_BYTE_3_3_2, UNSIGNED_BYTE_2_3_3_REV -> 1;
            case SHORT, UNSIGNED_SHORT, HALF_FLOAT, UNSIGNED_SHORT_5_6_5, UNSIGNED_SHORT_5_6_5_REV,
                UNSIGNED_SHORT_4_4_4_4, UNSIGNED_SHORT_4_4_4_4_REV, UNSIGNED_SHORT_5_5_5_1, UNSIGNED_SHORT_1_5_5_5_REV -> 2;
            default -> 4;
        };
        boolean targetInteger = target == GpuFormat.RGB10A2_UINT || switch (target.componentType()) {
            case UINT_8, SINT_8, UINT_16, SINT_16, UINT_32, SINT_32 -> true;
            case UNORM_8, SNORM_8, UNORM_16, SNORM_16, FLOAT_16, FLOAT_32 -> false;
            default -> {
                if (target != GpuFormat.RGB10A2_UNORM && target != GpuFormat.RG11B10_FLOAT && target != GpuFormat.RGB10A2_UINT) {
                    throw new IllegalArgumentException("Unsupported raw texture storage: " + target);
                }
                yield false;
            }
        };
        if (integer != targetInteger || integer && (type == PixelType.HALF_FLOAT || type == PixelType.FLOAT)) {
            throw new IllegalArgumentException("Raw texture integer pixel format does not match storage: " + format + "/" + target);
        }
        int pixelBytes = packed == null ? Math.multiplyExact(components, componentBytes) : componentBytes;
        int rowBytes = Math.multiplyExact(width, pixelBytes);
        int stride = Math.addExact(rowBytes, 3) & ~3;
        int slice = Math.multiplyExact(stride, height);
        long required = (long)(depth - 1) * slice + (long)(height - 1) * stride + rowBytes;
        if (source.remaining() < required) {
            throw new IllegalArgumentException("Raw texture data is too short: expected " + required + ", got " + source.remaining());
        }
        int size = Math.multiplyExact(Math.multiplyExact(Math.multiplyExact(width, height), depth), target.blockSize());
        ByteBuffer output = MemoryUtil.memAlloc(size).order(ByteOrder.nativeOrder());
        try {
            ByteBuffer input = source.duplicate().order(ByteOrder.nativeOrder());
            boolean bgr = format == PixelFormat.BGR || format == PixelFormat.BGRA
                || format == PixelFormat.BGR_INTEGER || format == PixelFormat.BGRA_INTEGER;
            boolean reverse = type.name().endsWith("_REV");
            double[] pixel = new double[4];
            for (int z = 0; z < depth; z++) {
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        pixel[0] = pixel[1] = pixel[2] = 0;
                        pixel[3] = 1;
                        int offset = input.position() + z * slice + y * stride + x * pixelBytes;
                        if (packed == null) {
                            for (int c = 0; c < components; c++) {
                                pixel[bgr && c < 3 ? 2 - c : c] = read(input, offset + c * componentBytes, type, integer);
                            }
                        } else {
                            long word = switch (componentBytes) {
                                case 1 -> Byte.toUnsignedInt(input.get(offset));
                                case 2 -> Short.toUnsignedInt(input.getShort(offset));
                                default -> Integer.toUnsignedLong(input.getInt(offset));
                            };
                            int shift = reverse ? 0 : componentBytes * 8;
                            for (int c = 0; c < components; c++) {
                                if (!reverse) {
                                    shift -= packed[c];
                                }
                                int mask = (1 << packed[c]) - 1;
                                double value = word >>> shift & mask;
                                pixel[bgr && c < 3 ? 2 - c : c] = integer ? value : value / mask;
                                if (reverse) {
                                    shift += packed[c];
                                }
                            }
                        }
                        quantizeLegacyStorage(storage, pixel);
                        if (target == GpuFormat.RGB10A2_UNORM || target == GpuFormat.RGB10A2_UINT) {
                            boolean normalized = target == GpuFormat.RGB10A2_UNORM;
                            output.putInt(unormOrInteger(pixel[0], 1023, normalized) | unormOrInteger(pixel[1], 1023, normalized) << 10
                                | unormOrInteger(pixel[2], 1023, normalized) << 20 | unormOrInteger(pixel[3], 3, normalized) << 30);
                        } else if (target == GpuFormat.RG11B10_FLOAT) {
                            output.putInt(unsignedFloat(pixel[0], 6) | unsignedFloat(pixel[1], 6) << 11 | unsignedFloat(pixel[2], 5) << 22);
                        } else {
                            for (int c = 0; c < target.componentCount(); c++) {
                                write(output, target.componentType(), pixel[c]);
                            }
                        }
                    }
                }
            }
            return output.flip();
        } catch (RuntimeException | Error e) {
            MemoryUtil.memFree(output);
            throw e;
        }
    }

    private static int unormOrInteger(double value, int max, boolean normalized) {
        return normalized ? (int)Math.round(Math.clamp(value, 0, 1) * max) : (int)Math.clamp(value, 0, max);
    }

    private static int unsignedFloat(double value, int mantissaBits) {
        if (Double.isNaN(value)) {
            return 31 << mantissaBits | 1 << (mantissaBits - 1);
        }
        if (value <= 0) {
            return 0;
        }
        int exponent = Math.getExponent(value);
        if (exponent < -14) {
            return (int)Math.rint(Math.scalb(value, 14 + mantissaBits));
        }
        if (exponent > 15) {
            return 31 << mantissaBits;
        }
        int significand = (int)Math.rint(Math.scalb(value, mantissaBits - exponent));
        if (significand == 1 << (mantissaBits + 1)) {
            significand >>= 1;
            exponent++;
        }
        return exponent >= 16 ? 31 << mantissaBits : (exponent + 15) << mantissaBits | significand - (1 << mantissaBits);
    }

    private static void quantizeLegacyStorage(InternalFormat storage, double[] pixel) {
        if (storage == InternalFormat.R3_G3_B2 || storage == InternalFormat.RGB5_A1) {
            // OpenGL permits greater precision; use its RGB565 fallback for R3_G3_B2.
            int channels = storage == InternalFormat.R3_G3_B2 ? 3 : 4;
            for (int c = 0; c < channels; c++) {
                int max = c == 3 ? 1 : c == 1 && storage == InternalFormat.R3_G3_B2 ? 63 : 31;
                pixel[c] = unormOrInteger(pixel[c], max, true) / (double)max;
            }
            if (storage == InternalFormat.R3_G3_B2) {
                pixel[3] = 1;
            }
        } else if (storage == InternalFormat.RGB9_E5) {
            double max = 0;
            for (int c = 0; c < 3; c++) {
                pixel[c] = Double.isNaN(pixel[c]) ? 0 : Math.clamp(pixel[c], 0, 65408);
                max = Math.max(max, pixel[c]);
            }
            int exponent = Math.max(0, Math.getExponent(max) + 16);
            double step = Math.scalb(1.0, exponent - 24);
            if (Math.floor(max / step + 0.5) == 512) {
                step *= 2;
            }
            for (int c = 0; c < 3; c++) {
                pixel[c] = Math.floor(pixel[c] / step + 0.5) * step;
            }
            pixel[3] = 1;
        }
    }

    private static double read(ByteBuffer data, int offset, PixelType type, boolean integer) {
        double value = switch (type) {
            case BYTE -> data.get(offset);
            case UNSIGNED_BYTE -> Byte.toUnsignedInt(data.get(offset));
            case SHORT -> data.getShort(offset);
            case UNSIGNED_SHORT -> Short.toUnsignedInt(data.getShort(offset));
            case INT -> data.getInt(offset);
            case UNSIGNED_INT -> Integer.toUnsignedLong(data.getInt(offset));
            case HALF_FLOAT -> Float.float16ToFloat(data.getShort(offset));
            case FLOAT -> data.getFloat(offset);
            default -> throw new IllegalArgumentException("Unexpected raw pixel type: " + type);
        };
        if (integer) {
            return value;
        }
        return switch (type) {
            case BYTE -> Math.max(-1, value / 127.0);
            case UNSIGNED_BYTE -> value / 255.0;
            case SHORT -> Math.max(-1, value / 32767.0);
            case UNSIGNED_SHORT -> value / 65535.0;
            case INT -> Math.max(-1, value / 2147483647.0);
            case UNSIGNED_INT -> value / 4294967295.0;
            default -> value;
        };
    }

    private static void write(ByteBuffer output, GpuFormat.ComponentType type, double value) {
        switch (type) {
            case UNORM_8 -> output.put((byte)Math.round(Math.clamp(value, 0, 1) * 255));
            case SNORM_8 -> output.put((byte)Math.round(Math.clamp(value, -1, 1) * 127));
            case UNORM_16 -> output.putShort((short)Math.round(Math.clamp(value, 0, 1) * 65535));
            case SNORM_16 -> output.putShort((short)Math.round(Math.clamp(value, -1, 1) * 32767));
            case UINT_8 -> output.put((byte)Math.clamp(value, 0, 255));
            case SINT_8 -> output.put((byte)Math.clamp(value, -128, 127));
            case UINT_16 -> output.putShort((short)Math.clamp(value, 0, 65535));
            case SINT_16 -> output.putShort((short)Math.clamp(value, -32768, 32767));
            case UINT_32 -> output.putInt((int)(long)Math.clamp(value, 0, 4294967295.0));
            case SINT_32 -> output.putInt((int)Math.clamp(value, Integer.MIN_VALUE, Integer.MAX_VALUE));
            case FLOAT_16 -> output.putShort(Float.floatToFloat16((float)value));
            case FLOAT_32 -> output.putFloat((float)value);
            default -> throw new IllegalArgumentException("Unexpected raw texture component type: " + type);
        }
    }
}
