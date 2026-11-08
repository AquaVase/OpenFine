package net.optifine.entity.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;

public abstract class ModelAdapterAgeable extends ModelAdapterLiving implements IModelAdapterAgeable {
   private boolean baby;

   public ModelAdapterAgeable(EntityType entityType, String name, ModelLayerLocation modelLayer) {
      super(entityType, name, modelLayer);
   }

   @Override
   public ModelAdapter makeBaby() {
      return null;
   }

   @Override
   protected final LivingEntityRenderer makeLivingRenderer(Context context) {
      return this.makeAgeableRenderer(context);
   }

   protected abstract AgeableMobRenderer makeAgeableRenderer(Context var1);

   @Override
   protected final void modifyLivingRenderer(LivingEntityRenderer renderer, Model modelBase) {
      this.modifyAgeableRenderer((AgeableMobRenderer)renderer, modelBase);
   }

   protected void modifyAgeableRenderer(AgeableMobRenderer renderer, Model modelBase) {
      if (!(modelBase instanceof EntityModel)) {
         throw new IllegalArgumentException("Not an EntityModel: " + modelBase);
      }

      if (this.baby) {
         renderer.setBabyModel((EntityModel)modelBase);
      } else {
         renderer.setAdultModel((EntityModel)modelBase);
      }
   }

   public void setBaby(boolean baby) {
      this.baby = baby;
   }

   public boolean isBaby() {
      return this.baby;
   }
}
