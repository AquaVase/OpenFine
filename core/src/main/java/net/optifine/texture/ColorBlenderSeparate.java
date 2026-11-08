package net.optifine.texture;

public class ColorBlenderSeparate implements IColorBlender {
   private IBlender blenderR;
   private IBlender blenderG;
   private IBlender blenderB;
   private IBlender blenderA;

   public ColorBlenderSeparate(IBlender blenderR, IBlender blenderG, IBlender blenderB, IBlender blenderA) {
      this.blenderR = blenderR;
      this.blenderG = blenderG;
      this.blenderB = blenderB;
      this.blenderA = blenderA;
   }

   @Override
   public int blend(int c1, int c2, int c3, int c4) {
      int a1 = c1 >> 24 & 0xFF;
      int r1 = c1 >> 16 & 0xFF;
      int g1 = c1 >> 8 & 0xFF;
      int b1 = c1 & 0xFF;
      int a2 = c2 >> 24 & 0xFF;
      int r2 = c2 >> 16 & 0xFF;
      int g2 = c2 >> 8 & 0xFF;
      int b2 = c2 & 0xFF;
      int a3 = c3 >> 24 & 0xFF;
      int r3 = c3 >> 16 & 0xFF;
      int g3 = c3 >> 8 & 0xFF;
      int b3 = c3 & 0xFF;
      int a4 = c4 >> 24 & 0xFF;
      int r4 = c4 >> 16 & 0xFF;
      int g4 = c4 >> 8 & 0xFF;
      int b4 = c4 & 0xFF;
      int ax = this.blenderA.blend(a1, a2, a3, a4);
      int rx = this.blenderR.blend(r1, r2, r3, r4);
      int gx = this.blenderG.blend(g1, g2, g3, g4);
      int bx = this.blenderB.blend(b1, b2, b3, b4);
      return ax << 24 | rx << 16 | gx << 8 | bx;
   }
}
