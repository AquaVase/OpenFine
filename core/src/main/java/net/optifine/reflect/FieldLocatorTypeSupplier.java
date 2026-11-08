package net.optifine.reflect;

import java.lang.reflect.Field;
import java.util.function.Supplier;
import net.optifine.Log;

public class FieldLocatorTypeSupplier implements IFieldLocator {
    private ReflectorClass reflectorClass = null;
    private Supplier<Class> targetFieldTypeSupplier = null;
    private int targetFieldIndex;

    public FieldLocatorTypeSupplier(ReflectorClass reflectorClass, Supplier<Class> targetFieldTypeSupplier) {
        this(reflectorClass, targetFieldTypeSupplier, 0);
    }

    public FieldLocatorTypeSupplier(ReflectorClass reflectorClass, Supplier<Class> targetFieldTypeSupplier, int targetFieldIndex) {
        this.reflectorClass = reflectorClass;
        this.targetFieldTypeSupplier = targetFieldTypeSupplier;
        this.targetFieldIndex = targetFieldIndex;
    }

    @Override
    public Field getField() {
        Class targetFieldType = this.targetFieldTypeSupplier.get();
        Class cls = this.reflectorClass.getTargetClass();
        if (cls == null) {
            return null;
        }

        try {
            Field[] fileds = cls.getDeclaredFields();
            int fieldIndex = 0;

            for (int i = 0; i < fileds.length; i++) {
                Field field = fileds[i];
                if (field.getType() == targetFieldType) {
                    if (fieldIndex == this.targetFieldIndex) {
                        field.setAccessible(true);
                        return field;
                    }

                    fieldIndex++;
                }
            }

            Log.log("(Reflector) Field not present: " + cls.getName() + ".(type: " + targetFieldType + ", index: " + this.targetFieldIndex + ")");
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
