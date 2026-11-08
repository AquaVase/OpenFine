package net.optifine.render;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public class ChunkLayerMap<T> {
    private T[] values = (T[])(new Object[ChunkSectionLayer.VALUES.length]);
    private Supplier<T> defaultValue;

    public ChunkLayerMap(Function<ChunkSectionLayer, T> initialValue) {
        ChunkSectionLayer[] renderTypes = ChunkSectionLayer.VALUES;
        this.values = (T[])(new Object[renderTypes.length]);

        for (int i = 0; i < renderTypes.length; i++) {
            ChunkSectionLayer renderType = renderTypes[i];
            T t = initialValue.apply(renderType);
            this.values[renderType.ordinal()] = t;
        }

        for (int i = 0; i < this.values.length; i++) {
            if (this.values[i] == null) {
                throw new RuntimeException("Missing value at index: " + i);
            }
        }
    }

    public T get(ChunkSectionLayer layer) {
        return this.values[layer.ordinal()];
    }

    public Collection<T> values() {
        return Arrays.asList(this.values);
    }
}
