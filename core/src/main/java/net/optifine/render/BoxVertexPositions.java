package net.optifine.render;

import net.optifine.util.IntExpiringCache;
import net.optifine.util.RandomUtils;

public class BoxVertexPositions extends IntExpiringCache<VertexPosition[][]> {
   public BoxVertexPositions() {
      super(60000 + RandomUtils.getRandomInt(10000));
   }

   protected VertexPosition[][] make() {
      VertexPosition[][] boxPositions = new VertexPosition[6][4];

      for (int i = 0; i < boxPositions.length; i++) {
         VertexPosition[] quadPositions = boxPositions[i];

         for (int p = 0; p < quadPositions.length; p++) {
            quadPositions[p] = new VertexPosition();
         }
      }

      return boxPositions;
   }
}
