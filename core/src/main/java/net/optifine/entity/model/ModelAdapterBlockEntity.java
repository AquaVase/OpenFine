package net.optifine.entity.model;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.optifine.entity.model.anim.IRenderResolver;
import net.optifine.entity.model.anim.RenderResolverTileEntity;

public abstract class ModelAdapterBlockEntity extends ModelAdapter {
   public ModelAdapterBlockEntity(BlockEntityType blockEntityType, String name) {
      super(blockEntityType, name);
   }

   @Override
   public IRenderResolver getRenderResolver() {
      return new RenderResolverTileEntity();
   }

   public Context getContext() {
      return this.getBlockEntityContext();
   }
}
