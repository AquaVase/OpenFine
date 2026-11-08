package net.optifine.player;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Dimension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public class PlayerItemModel {
    private Dimension textureSize = null;
    private boolean usePlayerTexture = false;
    private PlayerItemRenderer[] modelRenderers = new PlayerItemRenderer[0];
    private Identifier textureLocation = null;
    private NativeImage textureImage = null;
    private DynamicTexture texture = null;
    private Identifier locationMissing = new Identifier("textures/block/red_wool.png");
    public static final int ATTACH_BODY = 0;
    public static final int ATTACH_HEAD = 1;
    public static final int ATTACH_LEFT_ARM = 2;
    public static final int ATTACH_RIGHT_ARM = 3;
    public static final int ATTACH_LEFT_LEG = 4;
    public static final int ATTACH_RIGHT_LEG = 5;
    public static final int ATTACH_CAPE = 6;

    public PlayerItemModel(Dimension textureSize, boolean usePlayerTexture, PlayerItemRenderer[] modelRenderers) {
        this.textureSize = textureSize;
        this.usePlayerTexture = usePlayerTexture;
        this.modelRenderers = modelRenderers;
    }

    public void render(
        HumanoidModel modelBiped, AbstractClientPlayer player, PoseStack matrixStackIn, SubmitNodeCollector bufferIn, int packedLightIn, int packedOverlayIn
    ) {
        Identifier locTex = this.locationMissing;
        if (this.usePlayerTexture) {
            locTex = player.getSkinTextureLocation();
        } else if (this.textureLocation != null) {
            if (this.texture == null && this.textureImage != null) {
                this.texture = new DynamicTexture(() -> this.textureLocation.toDebugFileName(), this.textureImage);
                Minecraft.getInstance().getTextureManager().register(this.textureLocation, this.texture);
            }

            locTex = this.textureLocation;
        } else {
            locTex = this.locationMissing;
        }

        for (int i = 0; i < this.modelRenderers.length; i++) {
            PlayerItemRenderer pir = this.modelRenderers[i];
            matrixStackIn.pushPose();
            RenderType renderType = RenderTypes.entityCutout(locTex);
            pir.render(modelBiped, matrixStackIn, bufferIn, renderType, packedLightIn, packedOverlayIn);
            matrixStackIn.popPose();
        }
    }

    public static ModelPart getAttachModel(HumanoidModel modelBiped, int attachTo) {
        switch (attachTo) {
            case 0:
                return modelBiped.body;
            case 1:
                return modelBiped.head;
            case 2:
                return modelBiped.leftArm;
            case 3:
                return modelBiped.rightArm;
            case 4:
                return modelBiped.leftLeg;
            case 5:
                return modelBiped.rightLeg;
            default:
                return null;
        }
    }

    public NativeImage getTextureImage() {
        return this.textureImage;
    }

    public void setTextureImage(NativeImage textureImage) {
        this.textureImage = textureImage;
    }

    public DynamicTexture getTexture() {
        return this.texture;
    }

    public Identifier getTextureLocation() {
        return this.textureLocation;
    }

    public void setTextureLocation(Identifier textureLocation) {
        this.textureLocation = textureLocation;
    }

    public boolean isUsePlayerTexture() {
        return this.usePlayerTexture;
    }
}
