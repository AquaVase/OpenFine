package net.optifine.reflect;

import java.lang.reflect.Field;
import net.optifine.Log;

public class FieldLocatorType implements IFieldLocator {
   private ReflectorClass reflectorClass = null;
   private Class targetFieldType = null;
   private int targetFieldIndex;

   public FieldLocatorType(ReflectorClass reflectorClass, Class targetFieldType) {
      this(reflectorClass, targetFieldType, 0);
   }

   public FieldLocatorType(ReflectorClass reflectorClass, Class targetFieldType, int targetFieldIndex) {
      this.reflectorClass = reflectorClass;
      this.targetFieldType = targetFieldType;
      this.targetFieldIndex = targetFieldIndex;
   }

   @Override
   public Field getField() {
      Class cls = this.reflectorClass.getTargetClass();
      if (cls == null) {
         return null;
      }

      try {
         Field[] fileds = cls.getDeclaredFields();
         int fieldIndex = 0;

         for (int i = 0; i < fileds.length; i++) {
            Field field = fileds[i];
            if (field.getType() == this.targetFieldType) {
               if (fieldIndex == this.targetFieldIndex) {
                  field.setAccessible(true);
                  return field;
               }

               fieldIndex++;
            }
         }

         Log.log("(Reflector) Field not present: " + cls.getName() + ".(type: " + this.targetFieldType + ", index: " + this.targetFieldIndex + ")");
         return null;
      } catch (SecurityException e) {
         e.printStackTrace();
         return null;
      } catch (Throwable e) {
         e.printStackTrace();
         return null;
      }
   }
}
