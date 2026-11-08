package net.optifine.entity.model;

import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.SkullBlock.Type;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class RendererUtils {
   public static Map<EntityType, EntityRenderer> getEntityRenderMap() {
      EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      return dispatcher.getEntityRenderMap();
   }

   public static Map<BlockEntityType, BlockEntityRenderer> getBlockEntityRenderMap() {
      BlockEntityRenderDispatcher dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
      return dispatcher.getBlockEntityRenderMap();
   }

   public static Map<Type, SkullModelBase> getSkullModelMap() {
      return SkullBlockRenderer.getGlobalModels();
   }
}
