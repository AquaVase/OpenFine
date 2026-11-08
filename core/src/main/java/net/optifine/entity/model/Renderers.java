package net.optifine.entity.model;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.animal.parrot.ParrotModel;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.SkullBlock.Type;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class Renderers {
   private final Map<EntityType, EntityRenderer> entityRenderers = new HashMap<>();
   private final Map<BlockEntityType, BlockEntityRenderer> blockEntityRenderers = new HashMap<>();
   private final Map<Type, SkullModelBase> skullModels = new HashMap<>();
   private BookModel bookModel;
   private ParrotModel parrotModel;

   public static Renderers collectRenderers() {
      Renderers crs = new Renderers();
      crs.entityRenderers.putAll(getEntityRenderMap());
      crs.blockEntityRenderers.putAll(getBlockEntityRenderMap());
      crs.skullModels.putAll(getSkullModelMap());
      crs.bookModel = CustomStaticModels.getBookModel();
      crs.parrotModel = CustomStaticModels.getParrotModel();
      return crs;
   }

   public void restoreRenderers() {
      new Renderers();
      getEntityRenderMap().clear();
      getEntityRenderMap().putAll(this.entityRenderers);
      getBlockEntityRenderMap().clear();
      getBlockEntityRenderMap().putAll(this.blockEntityRenderers);
      getSkullModelMap().clear();
      getSkullModelMap().putAll(this.skullModels);
      CustomStaticModels.setBookModel(this.bookModel);
      CustomStaticModels.setParrotModel(this.parrotModel);
   }

   private static Map<EntityType, EntityRenderer> getEntityRenderMap() {
      return RendererUtils.getEntityRenderMap();
   }

   public static Map<BlockEntityType, BlockEntityRenderer> getBlockEntityRenderMap() {
      return RendererUtils.getBlockEntityRenderMap();
   }

   public static Map<Type, SkullModelBase> getSkullModelMap() {
      return RendererUtils.getSkullModelMap();
   }
}
