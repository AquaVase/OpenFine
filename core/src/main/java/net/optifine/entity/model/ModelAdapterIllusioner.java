package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.entity.IllusionerRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterIllusioner extends ModelAdapterIllager {
   public ModelAdapterIllusioner() {
      super(EntityTypes.ILLUSIONER, "illusioner", ModelLayers.ILLUSIONER);
      this.setAlias("illusion_illager");
   }

   @Override
   protected Model makeModel(ModelPart root) {
      IllagerModel model = new IllagerModel(root);
      model.getHat().visible = true;
      return model;
   }

   @Override
   protected LivingEntityRenderer makeLivingRenderer(Context context) {
      return new IllusionerRenderer(context);
   }
}
