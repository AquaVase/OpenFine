package net.optifine.shaders.config;

import java.util.Map;
import net.optifine.Config;
import net.optifine.expr.ConstantFloat;
import net.optifine.expr.FunctionBool;
import net.optifine.expr.FunctionType;
import net.optifine.expr.IExpression;
import net.optifine.expr.IExpressionResolver;

public class MacroExpressionResolver implements IExpressionResolver {
    private Map<String, String> mapMacroValues = null;

    public MacroExpressionResolver(Map<String, String> mapMacroValues) {
        this.mapMacroValues = mapMacroValues;
    }

    @Override
    public IExpression getExpression(String name) {
        String PREFIX_DEFINED = "defined_";
        if (name.startsWith(PREFIX_DEFINED)) {
            String macro = name.substring(PREFIX_DEFINED.length());
            return this.mapMacroValues.containsKey(macro) ? new FunctionBool(FunctionType.TRUE, null) : new FunctionBool(FunctionType.FALSE, null);
        }

        while (this.mapMacroValues.containsKey(name)) {
            String nameNext = this.mapMacroValues.get(name);
            if (nameNext == null || nameNext.equals(name)) {
                break;
            }

            name = nameNext;
        }

        int valInt = Config.parseInt(name, Integer.MIN_VALUE);
        if (valInt == Integer.MIN_VALUE) {
            Config.warn("Unknown macro value: " + name);
            return new ConstantFloat(0.0F);
        } else {
            return new ConstantFloat(valInt);
        }
    }
}
