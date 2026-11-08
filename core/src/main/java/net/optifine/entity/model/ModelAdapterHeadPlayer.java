package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.level.block.SkullBlock.Types;

public class ModelAdapterHeadPlayer extends ModelAdapterHead {
   public ModelAdapterHeadPlayer() {
      super("head_player", ModelLayers.PLAYER_HEAD, Types.PLAYER);
   }
}
