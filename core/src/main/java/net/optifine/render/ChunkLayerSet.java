package net.optifine.render;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public class ChunkLayerSet implements Set<ChunkSectionLayer> {
    private boolean[] layers = new boolean[ChunkSectionLayer.VALUES.length];
    private boolean empty = true;

    public boolean add(ChunkSectionLayer layerIn) {
        this.layers[layerIn.ordinal()] = true;
        this.empty = false;
        return false;
    }

    public boolean contains(ChunkSectionLayer layerIn) {
        return this.layers[layerIn.ordinal()];
    }

    @Override
    public boolean contains(Object obj) {
        return obj instanceof ChunkSectionLayer ? this.contains((ChunkSectionLayer)obj) : false;
    }

    @Override
    public boolean isEmpty() {
        return this.empty;
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Iterator<ChunkSectionLayer> iterator() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean addAll(Collection<? extends ChunkSectionLayer> c) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported");
    }
}
