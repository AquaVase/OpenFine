package net.optifine.model;

import java.util.List;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.optifine.Config;
import org.joml.Vector3fc;

public class ModelUtils {
   public static void dbgModel(BlockStateModelPart model) {
      if (model != null) {
         Config.dbg("Model: " + model + ", ao: " + model.useAmbientOcclusion() + ", particle: " + model.particleMaterial());
         Direction[] faces = Direction.VALUES;

         for (int i = 0; i < faces.length; i++) {
            Direction face = faces[i];
            List faceQuads = model.getQuads(face);
            dbgQuads(face.getSerializedName(), faceQuads, "  ");
         }

         List generalQuads = model.getQuads(null);
         dbgQuads("General", generalQuads, "  ");
      }
   }

   private static void dbgQuads(String name, List quads, String prefix) {
      for (BakedQuad quad : (java.lang.Iterable<BakedQuad>)(java.lang.Iterable<?>)(quads)) {
         dbgQuad(name, quad, prefix);
      }
   }

   public static void dbgQuad(String name, BakedQuad quad, String prefix) {
      Config.dbg(
         prefix
            + "Quad: "
            + quad.getClass().getName()
            + ", type: "
            + name
            + ", face: "
            + quad.direction()
            + ", tint: "
            + quad.tintIndex()
            + ", sprite: "
            + quad.sprite()
      );
      dbgVertexData(quad, "  " + prefix);
   }

   public static void dbgVertexData(BakedQuad quad, String prefix) {
      for (int i = 0; i < 4; i++) {
         Vector3fc pos = quad.position(i);
         float x = pos.x();
         float y = pos.y();
         float z = pos.z();
         float u = UVPair.unpackU(quad.packedUV(i));
         float v = UVPair.unpackV(quad.packedUV(i));
         Config.dbg(prefix + i + " xyz: " + x + "," + y + "," + z + " u,v: " + u + "," + v);
      }
   }
}
