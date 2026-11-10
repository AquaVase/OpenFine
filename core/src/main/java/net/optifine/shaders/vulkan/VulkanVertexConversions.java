package net.optifine.shaders.vulkan;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.lwjgl.util.spvc.Spv;

public class VulkanVertexConversions {
    public static ByteBuffer convert(ByteBuffer spirv, Map<Integer, Boolean> locations) {
        if (locations.isEmpty()) {
            return null;
        }

        IntBuffer words = spirv.asIntBuffer();
        Map<Integer, Integer> variableLocations = new HashMap<>();
        Map<Integer, Integer> pointerTypes = new HashMap<>();
        Map<Integer, Integer> vectorSizes = new HashMap<>();
        Map<List<Integer>, Integer> types = new HashMap<>();
        int entryPoint = 0;
        for (int i = 5; i < words.limit(); i += words.get(i) >>> 16) {
            int op = words.get(i) & 65535;
            if (op == Spv.SpvOpDecorate && words.get(i + 2) == Spv.SpvDecorationLocation) {
                variableLocations.put(words.get(i + 1), words.get(i + 3));
            } else if (op == Spv.SpvOpTypePointer && words.get(i + 2) == Spv.SpvStorageClassInput) {
                pointerTypes.put(words.get(i + 1), words.get(i + 3));
            } else if (op == Spv.SpvOpTypeVector) {
                vectorSizes.put(words.get(i + 1), words.get(i + 3));
            } else if (op == Spv.SpvOpEntryPoint && words.get(i + 1) == Spv.SpvExecutionModelVertex) {
                entryPoint = words.get(i + 2);
            }
            if (op == Spv.SpvOpTypeInt || op == Spv.SpvOpTypeVector || op == Spv.SpvOpTypePointer) {
                List<Integer> key = new ArrayList<>();
                key.add(op);
                for (int j = 2; j < words.get(i) >>> 16; j++) {
                    key.add(words.get(i + j));
                }
                types.put(key, words.get(i + 1));
            }
        }

        int[] bound = {words.get(3)};
        List<Integer> declarations = new ArrayList<>();
        Map<Integer, Integer> privatePointers = new HashMap<>();
        Map<Integer, Conversion> conversions = new LinkedHashMap<>();
        for (int i = 5; i < words.limit(); i += words.get(i) >>> 16) {
            if ((words.get(i) & 65535) == Spv.SpvOpVariable && words.get(i + 3) == Spv.SpvStorageClassInput) {
                int variable = words.get(i + 2);
                Integer location = variableLocations.get(variable);
                if (location != null && locations.containsKey(location)) {
                    int floatType = pointerTypes.get(words.get(i + 1));
                    int components = vectorSizes.getOrDefault(floatType, 1);
                    boolean signed = locations.get(location);
                    int integer = type(types, declarations, bound, Spv.SpvOpTypeInt, 32, signed ? 1 : 0);
                    int inputType = components == 1 ? integer : type(types, declarations, bound, Spv.SpvOpTypeVector, integer, components);
                    int inputPointer = type(types, declarations, bound, Spv.SpvOpTypePointer, Spv.SpvStorageClassInput, inputType);
                    int privatePointer = type(types, declarations, bound, Spv.SpvOpTypePointer, Spv.SpvStorageClassPrivate, floatType);
                    conversions.put(variable, new Conversion(floatType, signed, inputType, inputPointer, privatePointer,
                        bound[0]++, bound[0]++, bound[0]++));
                }
            }
        }
        if (conversions.size() != locations.size() || entryPoint == 0) {
            throw new IllegalArgumentException("Unable to locate converted vertex inputs");
        }
        // Access chains retain floating point pointees, but now point into Private storage.
        for (Map.Entry<Integer, Integer> entry : pointerTypes.entrySet()) {
            privatePointers.put(entry.getKey(), type(types, declarations, bound, Spv.SpvOpTypePointer,
                Spv.SpvStorageClassPrivate, entry.getValue()));
        }

        List<Integer> output = new ArrayList<>(words.limit() + declarations.size() + conversions.size() * 32);
        for (int i = 0; i < 5; i++) {
            output.add(i == 3 ? bound[0] : words.get(i));
        }
        Set<Integer> privateValues = new HashSet<>(conversions.keySet());
        boolean declared = false;
        boolean entryFunction = false;
        boolean initialize = false;
        for (int i = 5; i < words.limit();) {
            int count = words.get(i) >>> 16;
            int op = words.get(i) & 65535;
            if (op == Spv.SpvOpFunction) {
                if (!declared) {
                    output.addAll(declarations);
                    for (Map.Entry<Integer, Conversion> entry : conversions.entrySet()) {
                        Conversion conversion = entry.getValue();
                        emit(output, Spv.SpvOpVariable, conversion.inputPointer(), conversion.input(), Spv.SpvStorageClassInput);
                        emit(output, Spv.SpvOpVariable, conversion.privatePointer(), entry.getKey(), Spv.SpvStorageClassPrivate);
                    }
                    declared = true;
                }
                entryFunction = words.get(i + 2) == entryPoint;
            } else if (op == Spv.SpvOpLabel && entryFunction) {
                initialize = true;
                entryFunction = false;
            } else if (initialize && op != Spv.SpvOpVariable && op != Spv.SpvOpLine && op != Spv.SpvOpNoLine) {
                // Function-local variables must remain the first instructions in the entry block.
                for (Map.Entry<Integer, Conversion> entry : conversions.entrySet()) {
                    Conversion conversion = entry.getValue();
                    emit(output, Spv.SpvOpLoad, conversion.inputType(), conversion.loaded(), conversion.input());
                    emit(output, conversion.signed() ? Spv.SpvOpConvertSToF : Spv.SpvOpConvertUToF,
                        conversion.floatType(), conversion.converted(), conversion.loaded());
                    emit(output, Spv.SpvOpStore, entry.getKey(), conversion.converted());
                }
                initialize = false;
            }

            if (op == Spv.SpvOpEntryPoint && words.get(i + 2) == entryPoint) {
                List<Integer> instruction = new ArrayList<>();
                for (int j = 0; j < count; j++) {
                    instruction.add(words.get(i + j));
                }
                boolean allGlobals = words.get(1) >= 0x00010400;
                int interfaces = 3;
                while (!hasTerminator(words.get(i + interfaces++))) {
                }
                for (int j = interfaces; j < count; j++) {
                    Conversion conversion = conversions.get(words.get(i + j));
                    if (conversion != null) {
                        instruction.set(j, conversion.input());
                        if (allGlobals) {
                            instruction.add(words.get(i + j));
                        }
                    }
                }
                instruction.set(0, instruction.size() << 16 | op);
                output.addAll(instruction);
            } else if ((op == Spv.SpvOpDecorate || op == Spv.SpvOpName) && conversions.containsKey(words.get(i + 1))) {
                output.add(words.get(i));
                output.add(conversions.get(words.get(i + 1)).input());
                for (int j = 2; j < count; j++) {
                    output.add(words.get(i + j));
                }
            } else if (op == Spv.SpvOpVariable && conversions.containsKey(words.get(i + 2))) {
                // Recreate these globals after the original type declarations.
            } else if ((op == Spv.SpvOpAccessChain || op == Spv.SpvOpInBoundsAccessChain || op == Spv.SpvOpCopyObject)
                       && privateValues.contains(words.get(i + 3))) {
                privateValues.add(words.get(i + 2));
                output.add(words.get(i));
                output.add(privatePointers.get(words.get(i + 1)));
                for (int j = 2; j < count; j++) {
                    output.add(words.get(i + j));
                }
            } else {
                for (int j = 0; j < count; j++) {
                    output.add(words.get(i + j));
                }
            }
            i += count;
        }
        ByteBuffer result = ByteBuffer.allocateDirect(output.size() * 4).order(ByteOrder.nativeOrder());
        for (int word : output) {
            result.putInt(word);
        }
        return result.flip();
    }

    private static int type(Map<List<Integer>, Integer> types, List<Integer> declarations, int[] bound, int op, int... operands) {
        List<Integer> key = new ArrayList<>();
        key.add(op);
        for (int operand : operands) {
            key.add(operand);
        }
        return types.computeIfAbsent(key, ignored -> {
            int id = bound[0]++;
            declarations.add((operands.length + 2) << 16 | op);
            declarations.add(id);
            for (int operand : operands) {
                declarations.add(operand);
            }
            return id;
        });
    }

    private static boolean hasTerminator(int word) {
        return (word & 255) == 0 || (word & 65280) == 0 || (word & 16711680) == 0 || (word & -16777216) == 0;
    }

    private static void emit(List<Integer> output, int op, int... operands) {
        output.add((operands.length + 1) << 16 | op);
        for (int operand : operands) {
            output.add(operand);
        }
    }

    private record Conversion(int floatType, boolean signed, int inputType, int inputPointer, int privatePointer,
                              int input, int loaded, int converted) {
    }
}
