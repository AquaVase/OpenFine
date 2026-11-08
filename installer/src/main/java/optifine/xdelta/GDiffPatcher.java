package optifine.xdelta;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class GDiffPatcher {
   public GDiffPatcher(File sourceFile, File patchFile, File outputFile) throws IOException, PatchException {
      RandomAccessFileSeekableSource source = new RandomAccessFileSeekableSource(new RandomAccessFile(sourceFile, "r"));
      InputStream patch = new FileInputStream(patchFile);
      OutputStream output = new FileOutputStream(outputFile);

      try {
         runPatch(source, patch, output);
      } catch (IOException e) {
         throw e;
      } catch (PatchException e) {
         throw e;
      } finally {
         source.close();
         patch.close();
         output.close();
      }
   }

   public GDiffPatcher(byte[] source, InputStream patch, OutputStream output) throws IOException, PatchException {
      this(new ByteArraySeekableSource(source), patch, output);
   }

   public GDiffPatcher(SeekableSource source, InputStream patch, OutputStream out) throws IOException, PatchException {
      runPatch(source, patch, out);
   }

   private static void runPatch(SeekableSource source, InputStream patch, OutputStream out) throws IOException, PatchException {
      DataOutputStream outOS = new DataOutputStream(out);
      DataInputStream patchIS = new DataInputStream(patch);

      try {
         byte[] buf = new byte[256];
         int i = 0;
         if (patchIS.readUnsignedByte() != 209
            || patchIS.readUnsignedByte() != 255
            || patchIS.readUnsignedByte() != 209
            || patchIS.readUnsignedByte() != 255
            || patchIS.readUnsignedByte() != 4) {
            System.err.println("magic string not found, aborting!");
            return;
         }

         i += 5;

         while (patchIS.available() > 0) {
            int command = patchIS.readUnsignedByte();
            int length = 0;
            int offset = 0;
            switch (command) {
               case 0:
                  break;
               case 1:
                  append(1, patchIS, outOS);
                  i += 2;
                  break;
               case 2:
                  append(2, patchIS, outOS);
                  i += 3;
                  break;
               case 246:
                  append(246, patchIS, outOS);
                  i += 247;
                  break;
               case 247:
                  length = patchIS.readUnsignedShort();
                  append(length, patchIS, outOS);
                  i += length + 3;
                  break;
               case 248:
                  length = patchIS.readInt();
                  append(length, patchIS, outOS);
                  i += length + 5;
                  break;
               case 249:
                  offset = patchIS.readUnsignedShort();
                  length = patchIS.readUnsignedByte();
                  copy(offset, length, source, outOS);
                  i += 4;
                  break;
               case 250:
                  offset = patchIS.readUnsignedShort();
                  length = patchIS.readUnsignedShort();
                  copy(offset, length, source, outOS);
                  i += 5;
                  break;
               case 251:
                  offset = patchIS.readUnsignedShort();
                  length = patchIS.readInt();
                  copy(offset, length, source, outOS);
                  i += 7;
                  break;
               case 252:
                  offset = patchIS.readInt();
                  length = patchIS.readUnsignedByte();
                  copy(offset, length, source, outOS);
                  i += 8;
                  break;
               case 253:
                  offset = patchIS.readInt();
                  length = patchIS.readUnsignedShort();
                  copy(offset, length, source, outOS);
                  i += 7;
                  break;
               case 254:
                  offset = patchIS.readInt();
                  length = patchIS.readInt();
                  copy(offset, length, source, outOS);
                  i += 9;
                  break;
               case 255:
                  long loffset = patchIS.readLong();
                  length = patchIS.readInt();
                  copy(loffset, length, source, outOS);
                  i += 13;
                  break;
               default:
                  append(command, patchIS, outOS);
                  i += command + 1;
            }
         }
      } catch (PatchException e) {
         throw e;
      } finally {
         outOS.flush();
      }
   }

   protected static void copy(long offset, int length, SeekableSource source, OutputStream output) throws IOException, PatchException {
      if (offset + length > source.length()) {
         throw new PatchException("truncated source file, aborting");
      }

      byte[] buf = new byte[256];
      source.seek(offset);

      while (length > 0) {
         int len = length > 256 ? 256 : length;
         int res = source.read(buf, 0, len);
         output.write(buf, 0, res);
         length -= res;
      }
   }

   protected static void append(int length, InputStream patch, OutputStream output) throws IOException {
      byte[] buf = new byte[256];

      while (length > 0) {
         int len = length > 256 ? 256 : length;
         int res = patch.read(buf, 0, len);
         output.write(buf, 0, res);
         length -= res;
      }
   }

   public static void main(String[] argv) {
      if (argv.length != 3) {
         System.err.println("usage GDiffPatch source patch output");
         System.err.println("aborting..");
      } else {
         try {
            File sourceFile = new File(argv[0]);
            File patchFile = new File(argv[1]);
            File outputFile = new File(argv[2]);
            if (sourceFile.length() > 2147483647L || patchFile.length() > 2147483647L) {
               System.err.println("source or patch is too large, max length is 2147483647");
               System.err.println("aborting..");
               return;
            }

            new GDiffPatcher(sourceFile, patchFile, outputFile);
            System.out.println("finished patching file");
         } catch (Exception ioe) {
            System.err.println("error while patching: " + ioe);
         }
      }
   }
}
