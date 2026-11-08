package net.optifine.model;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.model.geom.ModelPart;

public class AttachmentPaths {
    private List<List<AttachmentPath>> pathsByType = new ArrayList<>();

    public void addPath(AttachmentPath ap) {
        AttachmentType type = ap.getAttachment().getType();

        while (this.pathsByType.size() <= type.ordinal()) {
            this.pathsByType.add(null);
        }

        List<AttachmentPath> paths = this.pathsByType.get(type.ordinal());
        if (paths == null) {
            paths = new ArrayList<>();
            this.pathsByType.set(type.ordinal(), paths);
        }

        paths.add(ap);
    }

    public void addPaths(List<ModelPart> parents, Attachment[] attachments) {
        ModelPart[] parentArr = parents.toArray(new ModelPart[parents.size()]);

        for (int i = 0; i < attachments.length; i++) {
            Attachment at = attachments[i];
            AttachmentPath path = new AttachmentPath(at, parentArr);
            this.addPath(path);
        }
    }

    public boolean isEmpty() {
        return this.pathsByType.isEmpty();
    }

    public AttachmentPath getVisiblePath(AttachmentType typeIn) {
        if (this.pathsByType.size() <= typeIn.ordinal()) {
            return null;
        }

        List<AttachmentPath> paths = this.pathsByType.get(typeIn.ordinal());
        if (paths == null) {
            return null;
        }

        for (AttachmentPath path : paths) {
            if (path.isVisible()) {
                return path;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        int count = 0;

        for (List paths : this.pathsByType) {
            if (paths != null) {
                count++;
            }
        }

        return "types: " + count;
    }
}
