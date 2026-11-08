package net.optifine.player;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

public class PlayerItemRenderer {
    private int attachTo = 0;
    private ModelPart modelRenderer = null;

    public PlayerItemRenderer(int attachTo, ModelPart modelRenderer) {
        this.attachTo = attachTo;
        this.modelRenderer = modelRenderer;
    }

    public ModelPart getModelRenderer() {
        return this.modelRenderer;
    }

    public void render(
        HumanoidModel modelBiped, PoseStack matrixStackIn, SubmitNodeCollector bufferIn, RenderType renderTypeIn, int packedLightIn, int packedOverlayIn
    ) {
        ModelPart attachModel = PlayerItemModel.getAttachModel(modelBiped, this.attachTo);
        if (attachModel != null) {
            attachModel.translateAndRotate(matrixStackIn);
        }

        bufferIn.submitModelPart(this.modelRenderer, matrixStackIn, renderTypeIn, packedLightIn, packedOverlayIn, null);
    }
}
