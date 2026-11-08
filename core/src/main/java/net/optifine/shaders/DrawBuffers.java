package net.optifine.shaders;

import java.nio.IntBuffer;
import java.util.Arrays;
import net.optifine.util.ArrayUtils;
import net.optifine.util.BufferUtil;
import org.lwjgl.BufferUtils;

public class DrawBuffers {
    private String name;
    private final int maxColorBuffers;
    private final int maxDrawBuffers;
    private final IntBuffer drawBuffers;
    private int[] attachmentMappings;
    private IntBuffer glDrawBuffers;

    public DrawBuffers(String name, int maxColorBuffers, int maxDrawBuffers) {
        this.name = name;
        this.maxColorBuffers = maxColorBuffers;
        this.maxDrawBuffers = maxDrawBuffers;
        this.drawBuffers = IntBuffer.wrap(new int[maxDrawBuffers]);
    }

    public int get(int index) {
        return this.drawBuffers.get(index);
    }

    public DrawBuffers put(int attachment) {
        this.resetMappings();
        this.drawBuffers.put(attachment);
        return this;
    }

    public DrawBuffers put(int index, int attachment) {
        this.resetMappings();
        this.drawBuffers.put(index, attachment);
        return this;
    }

    public int position() {
        return this.drawBuffers.position();
    }

    public DrawBuffers position(int newPosition) {
        this.resetMappings();
        this.drawBuffers.position(newPosition);
        return this;
    }

    public int limit() {
        return this.drawBuffers.limit();
    }

    public DrawBuffers limit(int newLimit) {
        this.resetMappings();
        this.drawBuffers.limit(newLimit);
        return this;
    }

    public int capacity() {
        return this.drawBuffers.capacity();
    }

    public DrawBuffers fill(int val) {
        for (int i = 0; i < this.drawBuffers.limit(); i++) {
            this.drawBuffers.put(i, val);
        }

        this.resetMappings();
        return this;
    }

    private void resetMappings() {
        this.attachmentMappings = null;
        this.glDrawBuffers = null;
    }

    public int[] getAttachmentMappings() {
        if (this.attachmentMappings == null) {
            this.attachmentMappings = makeAttachmentMappings(this.drawBuffers, this.maxColorBuffers, this.maxDrawBuffers);
        }

        return this.attachmentMappings;
    }

    private static int[] makeAttachmentMappings(IntBuffer drawBuffers, int maxColorBuffers, int maxDrawBuffers) {
        int[] ams = new int[maxColorBuffers];
        Arrays.fill(ams, -1);

        for (int i = 0; i < drawBuffers.limit(); i++) {
            int att = drawBuffers.get(i);
            int ai = att - 36064;
            if (ai >= 0 && ai < maxDrawBuffers) {
                ams[ai] = ai;
            }
        }

        for (int i = 0; i < drawBuffers.limit(); i++) {
            int att = drawBuffers.get(i);
            int ai = att - 36064;
            if (ai >= maxDrawBuffers && ai < maxColorBuffers) {
                int mi = getMappingIndex(ai, maxDrawBuffers, ams);
                if (mi < 0) {
                    throw new RuntimeException("Too many draw buffers, mapping: " + ArrayUtils.arrayToString(ams));
                }

                ams[ai] = mi;
            }
        }

        return ams;
    }

    private static int getMappingIndex(int ai, int maxDrawBuffers, int[] attachmentMappings) {
        if (ai < maxDrawBuffers) {
            return ai;
        }

        if (attachmentMappings[ai] >= 0) {
            return attachmentMappings[ai];
        }

        for (int i = 0; i < maxDrawBuffers; i++) {
            if (!ArrayUtils.contains(attachmentMappings, i)) {
                return i;
            }
        }

        return -1;
    }

    public IntBuffer getGlDrawBuffers() {
        if (this.glDrawBuffers == null) {
            this.glDrawBuffers = makeGlDrawBuffers(this.drawBuffers, this.getAttachmentMappings());
        }

        return this.glDrawBuffers;
    }

    private static IntBuffer makeGlDrawBuffers(IntBuffer drawBuffers, int[] attachmentMappings) {
        IntBuffer glDrawBuffers = BufferUtils.createIntBuffer(drawBuffers.capacity());

        for (int i = 0; i < drawBuffers.limit(); i++) {
            int att = drawBuffers.get(i);
            int ai = att - 36064;
            int attFixed = 0;
            if (ai >= 0 && ai < attachmentMappings.length) {
                attFixed = 36064 + attachmentMappings[ai];
            }

            glDrawBuffers.put(i, attFixed);
        }

        glDrawBuffers.limit(drawBuffers.limit());
        glDrawBuffers.position(drawBuffers.position());
        return glDrawBuffers;
    }

    public String getInfo(boolean glBuffers) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < this.drawBuffers.limit(); i++) {
            int att = this.drawBuffers.get(i);
            int ai = att - 36064;
            if (glBuffers) {
                int[] ams = this.getAttachmentMappings();
                if (ai >= 0 && ai < ams.length) {
                    ai = ams[ai];
                }
            }

            String attName = this.getIndexName(ai);
            sb.append(attName);
        }

        return sb.toString();
    }

    private String getIndexName(int ai) {
        return ai >= 0 && ai < this.maxColorBuffers ? ai + "" : "N";
    }

    public int indexOf(int att) {
        for (int i = 0; i < this.limit(); i++) {
            if (this.get(i) == att) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public String toString() {
        return this.name
            + ": "
            + BufferUtil.getBufferString(this.drawBuffers)
            + ", mapping: "
            + ArrayUtils.arrayToString(this.attachmentMappings)
            + ", glDrawBuffers: "
            + BufferUtil.getBufferString(this.glDrawBuffers);
    }
}
