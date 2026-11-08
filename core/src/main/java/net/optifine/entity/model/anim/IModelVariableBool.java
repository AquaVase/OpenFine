package net.optifine.entity.model.anim;

import net.optifine.expr.IExpression;
import net.optifine.expr.IExpressionBool;

public interface IModelVariableBool extends IExpressionBool, IModelVariable {
   boolean getValue();

   void setValue(boolean var1);

   @Override
   default void setValue(IExpression expr) {
      boolean val = ((IExpressionBool)expr).eval();
      this.setValue(val);
   }
}
