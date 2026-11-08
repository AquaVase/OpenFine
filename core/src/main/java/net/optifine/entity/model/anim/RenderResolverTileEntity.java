package net.optifine.entity.model.anim;

import net.optifine.expr.IExpression;

public class RenderResolverTileEntity implements IRenderResolver {
    @Override
    public IExpression getParameter(String name) {
        RenderEntityParameterBool parBool = RenderEntityParameterBool.parse(name);
        if (parBool != null && parBool.isBlockEntity()) {
            return parBool;
        }

        RenderEntityParameterFloat parFloat = RenderEntityParameterFloat.parse(name);
        return parFloat != null && parFloat.isBlockEntity() ? parFloat : null;
    }

    @Override
    public boolean isTileEntity() {
        return true;
    }
}
