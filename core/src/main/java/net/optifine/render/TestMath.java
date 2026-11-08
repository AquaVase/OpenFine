package net.optifine.render;

import java.util.Random;
import net.optifine.util.MathUtils;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class TestMath {
   static Random random = new Random(1L);

   public static void main(String[] args) {
      int count = 1;
      dbg("Test math: " + count);

      for (int i = 0; i < count; i++) {
         testMatrix4f_mulTranslate();
         testMatrix4f_mulScale();
         testMatrix4f_mulQuaternion();
         testMatrix3f_mulQuaternion();
         testVector4f_transform();
         testVector3f_transform();
      }

      dbg("Done");
   }

   private static void testMatrix4f_mulTranslate() {
      Matrix4f m = new Matrix4f();
      MathUtils.setRandom(m, random);
      Matrix4f m2 = MathUtils.copy(m);
      float x = random.nextFloat();
      float y = random.nextFloat();
      float z = random.nextFloat();
      m.mul(MathUtils.makeTranslate4f(x, y, z));
      MathUtils.mulTranslate(m2, x, y, z);
      if (!m2.equals(m)) {
         dbg("*** DIFFERENT ***");
         dbg(m.toString());
         dbg(m2.toString());
      }
   }

   private static void testMatrix4f_mulScale() {
      Matrix4f m = new Matrix4f();
      MathUtils.setRandom(m, random);
      Matrix4f m2 = MathUtils.copy(m);
      float x = random.nextFloat();
      float y = random.nextFloat();
      float z = random.nextFloat();
      m.mul(MathUtils.makeScale4f(x, y, z));
      MathUtils.mulScale(m2, x, y, z);
      if (!m2.equals(m)) {
         dbg("*** DIFFERENT ***");
         dbg(m.toString());
         dbg(m2.toString());
      }
   }

   private static void testMatrix4f_mulQuaternion() {
      Matrix4f m = new Matrix4f();
      MathUtils.setRandom(m, random);
      Matrix4f m2 = MathUtils.copy(m);
      Quaternionf q = new Quaternionf(random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat());
      m.mul(MathUtils.makeMatrix4f(q));
      MathUtils.mul(m2, q);
      if (!m2.equals(m)) {
         dbg("*** DIFFERENT ***");
         dbg(m.toString());
         dbg(m2.toString());
      }
   }

   private static void testMatrix3f_mulQuaternion() {
      Matrix3f m = new Matrix3f();
      MathUtils.setRandom(m, random);
      Matrix3f m2 = MathUtils.copy(m);
      Quaternionf q = new Quaternionf(random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat());
      m.mul(MathUtils.makeMatrix3f(q));
      MathUtils.mul(m2, q);
      if (!m2.equals(m)) {
         dbg("*** DIFFERENT ***");
         dbg(m.toString());
         dbg(m2.toString());
      }
   }

   private static void testVector3f_transform() {
      Vector3f v = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
      Vector3f v2 = MathUtils.copy(v);
      Matrix3f m = new Matrix3f();
      MathUtils.setRandom(m, random);
      MathUtils.transform(v, m);
      float x = MathUtils.getTransformX(m, v2.x(), v2.y(), v2.z());
      float y = MathUtils.getTransformY(m, v2.x(), v2.y(), v2.z());
      float z = MathUtils.getTransformZ(m, v2.x(), v2.y(), v2.z());
      v2 = new Vector3f(x, y, z);
      if (!v2.equals(v)) {
         dbg("*** DIFFERENT ***");
         dbg(v.toString());
         dbg(v2.toString());
      }
   }

   private static void testVector4f_transform() {
      Vector4f v = new Vector4f(random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat());
      Vector4f v2 = MathUtils.copy(v);
      Matrix4f m = new Matrix4f();
      MathUtils.setRandom(m, random);
      MathUtils.transform(v, m);
      float x = MathUtils.getTransformX(m, v2.x(), v2.y(), v2.z(), v2.w());
      float y = MathUtils.getTransformY(m, v2.x(), v2.y(), v2.z(), v2.w());
      float z = MathUtils.getTransformZ(m, v2.x(), v2.y(), v2.z(), v2.w());
      float w = MathUtils.getTransformW(m, v2.x(), v2.y(), v2.z(), v2.w());
      v2 = new Vector4f(x, y, z, w);
      if (!v2.equals(v)) {
         dbg("*** DIFFERENT ***");
         dbg(v.toString());
         dbg(v2.toString());
      }
   }

   private static void dbg(String str) {
      System.out.println(str);
   }
}
