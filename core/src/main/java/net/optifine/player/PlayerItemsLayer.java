package net.optifine.player;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.optifine.Config;

public class PlayerItemsLayer extends RenderLayer {
    private AvatarRenderer<AbstractClientPlayer> renderPlayer = null;

    public PlayerItemsLayer(AvatarRenderer<AbstractClientPlayer> renderPlayer) {
        super(renderPlayer);
        this.renderPlayer = renderPlayer;
    }

    @Override
    public void submit(PoseStack matrixStackIn, SubmitNodeCollector bufferSourceIn, int packedLightIn, EntityRenderState stateIn, float yRotIn, float xRotIn) {
        Entity entity = stateIn.entity;
        this.renderEquippedItems(entity, matrixStackIn, bufferSourceIn, packedLightIn, OverlayTexture.NO_OVERLAY);
    }

    protected void renderEquippedItems(Entity entityLiving, PoseStack matrixStackIn, SubmitNodeCollector bufferIn, int packedLightIn, int packedOverlayIn) {
        if (Config.isShowCapes()) {
            if (!entityLiving.isInvisible()) {
                if (entityLiving instanceof AbstractClientPlayer player) {
                    PlayerModel modelBipedMain = this.renderPlayer.getModel();
                    PlayerConfigurations.renderPlayerItems(modelBipedMain, player, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                }
            }
        }
    }
}
