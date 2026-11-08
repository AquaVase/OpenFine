package net.optifine.player;

import java.util.function.Function;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public class ModelPlayerItem extends Model {
    public ModelPlayerItem(Function<Identifier, RenderType> renderTypeIn) {
        super(ModelPart.makeRoot(), renderTypeIn);
    }
}
