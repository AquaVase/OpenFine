package net.optifine.entity.model;

import java.util.function.Function;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public class CustomEntityModel extends Model {
    public CustomEntityModel(ModelPart root, Function<Identifier, RenderType> renderTypeIn) {
        super(root, renderTypeIn);
    }
}
