package net.optifine.util;

import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.MemoryUtil.MemoryAllocator;

public class TestBuffers {
   private static MemoryAllocator ALLOCATOR = MemoryUtil.getAllocator(true);

   public static void main(String[] args) throws Exception {
      int count = 1000000;

      for (int i = 0; i < count; i++) {
         long ptr = allocate(1000L);
         testBuf(ptr, 1000);
         testBuf(ptr, 1000);
         testBuf(ptr, 1000);
         testBuf(ptr, 1000);
         testBuf(ptr, 1000);
         testBuf(ptr, 1000);
         testBuf(ptr, 1000);
         testBuf(ptr, 1000);
         dbg("Mem: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576L);
         free(ptr);
      }
   }

   private static void testBuf(long ptr, int size) {
      ByteBuffer bb = MemoryUtil.memByteBuffer(ptr, size);
   }

   private static long allocate(long capacityIn) {
      long ptr = ALLOCATOR.malloc(capacityIn);
      dbg("Alloc: " + ptr);
      return ptr;
   }

   private static long free(long ptr) {
      ALLOCATOR.free(ptr);
      dbg("Free: " + ptr);
      return ptr;
   }

   private static void dbg(String str) {
      System.out.println(str);
   }
}
