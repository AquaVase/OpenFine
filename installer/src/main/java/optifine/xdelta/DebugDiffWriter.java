package optifine.xdelta;

import java.io.IOException;

public class DebugDiffWriter implements DiffWriter {
   byte[] buf = new byte[256];
   int buflen = 0;

   @Override
   public void addCopy(int offset, int length) throws IOException {
      if (this.buflen > 0) {
         this.writeBuf();
      }

      System.err.println("COPY off: " + offset + ", len: " + length);
   }

   @Override
   public void addData(byte b) throws IOException {
      if (this.buflen < 256) {
         this.buf[this.buflen++] = b;
      } else {
         this.writeBuf();
      }
   }

   private void writeBuf() {
      System.err.print("DATA: ");

      for (int ix = 0; ix < this.buflen; ix++) {
         if (this.buf[ix] == 10) {
            System.err.print("\\n");
         } else {
            System.err.print(String.valueOf((char)this.buf[ix]));
         }
      }

      System.err.println("");
      this.buflen = 0;
   }

   @Override
   public void flush() throws IOException {
   }

   @Override
   public void close() throws IOException {
   }
}
