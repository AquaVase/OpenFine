package net.optifine.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import net.optifine.Log;

public class ReflectorFields {
    private ReflectorClass reflectorClass;
    private Class fieldType;
    private int fieldCount;
    private ReflectorField[] reflectorFields;

    public ReflectorFields(ReflectorClass reflectorClassIn, Class fieldTypeIn, int fieldCountIn) {
        this.reflectorClass = reflectorClassIn;
        this.fieldType = fieldTypeIn;
        this.fieldCount = fieldCountIn;
        if (this.reflectorClass.exists()) {
            if (this.fieldType != null) {
                if (this.fieldCount < 0) {
                    this.fieldCount = detectFieldCount(this.reflectorClass, this.fieldType);
                }

                this.reflectorFields = new ReflectorField[this.fieldCount];

                for (int i = 0; i < this.reflectorFields.length; i++) {
                    this.reflectorFields[i] = new ReflectorField(this.reflectorClass, this.fieldType, i);
                }
            }
        }
    }

    private static int detectFieldCount(ReflectorClass refClass, Class fieldType) {
        Class cls = refClass.getTargetClass();
        if (cls == null) {
            return 0;
        }

        try {
            Field[] fileds = cls.getDeclaredFields();
            int fieldCount = 0;

            for (int i = 0; i < fileds.length; i++) {
                Field field = fileds[i];
                if (field.getType() == fieldType) {
                    fieldCount++;
                    field.setAccessible(true);
                }
            }

            if (fieldCount == 0) {
                Log.log("(Reflector) Fields not present: " + cls.getName() + ".(type: " + fieldType + ")");
            }

            return fieldCount;
        } catch (SecurityException e) {
            e.printStackTrace();
            return 0;
        } catch (Throwable e) {
            e.printStackTrace();
            return 0;
        }
    }

    public ReflectorClass getReflectorClass() {
        return this.reflectorClass;
    }

    public Class getFieldType() {
        return this.fieldType;
    }

    public int getFieldCount() {
        return this.fieldCount;
    }

    public ReflectorField getReflectorField(int index) {
        return index >= 0 && index < this.reflectorFields.length ? this.reflectorFields[index] : null;
    }

    public Object getValue(Object obj, int index) {
        return Reflector.getFieldValue(obj, this, index);
    }

    public void setValue(Object obj, int index, Object val) {
        Reflector.setFieldValue(obj, this, index, val);
    }

    public Object[] getFieldValues(Object obj) {
        if (!this.exists()) {
            return (Object[])Array.newInstance(this.fieldType, 0);
        }

        Object[] vals = (Object[])Array.newInstance(this.fieldType, this.fieldCount);

        for (int i = 0; i < vals.length; i++) {
            vals[i] = this.reflectorFields[i].getValue(obj);
        }

        return vals;
    }

    public boolean exists() {
        if (this.reflectorFields == null) {
            return false;
        }

        for (int i = 0; i < this.reflectorFields.length; i++) {
            ReflectorField reflectorField = this.reflectorFields[i];
            if (!reflectorField.exists()) {
                return false;
            }
        }

        return true;
    }
}
