package net.optifine.render;

import net.optifine.util.LinkedList;

public class VboRange {
    private int position = -1;
    private int size = 0;
    private LinkedList.Node<VboRange> node = new LinkedList.Node<>(this);

    public int getPosition() {
        return this.position;
    }

    public int getSize() {
        return this.size;
    }

    public int getPositionNext() {
        return this.position + this.size;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public LinkedList.Node<VboRange> getNode() {
        return this.node;
    }

    public VboRange getPrev() {
        LinkedList.Node<VboRange> nodePrev = this.node.getPrev();
        return nodePrev == null ? null : nodePrev.getItem();
    }

    public VboRange getNext() {
        LinkedList.Node<VboRange> nodeNext = this.node.getNext();
        return nodeNext == null ? null : nodeNext.getItem();
    }

    public boolean isAllocated() {
        return this.position >= 0;
    }

    @Override
    public String toString() {
        return this.position + "/" + this.size + "/" + (this.position + this.size);
    }
}
