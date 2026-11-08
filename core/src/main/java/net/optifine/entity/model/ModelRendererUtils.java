package net.optifine.entity.model;

import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import net.minecraft.client.model.geom.ModelPart;

public class ModelRendererUtils {
    public static ModelPart getModelRenderer(Iterator<ModelPart> iterator, int index) {
        if (iterator == null) {
            return null;
        }

        if (index < 0) {
            return null;
        }

        for (int i = 0; i < index; i++) {
            if (!iterator.hasNext()) {
                return null;
            }

            ModelPart var3 = iterator.next();
        }

        return !iterator.hasNext() ? null : iterator.next();
    }

    public static ModelPart getModelRenderer(ImmutableList<ModelPart> models, int index) {
        if (models == null) {
            return null;
        } else if (index < 0) {
            return null;
        } else {
            return index >= models.size() ? null : models.get(index);
        }
    }
}
