package net.optifine.render;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RenderState {
    private static EntityRenderState entityRenderState;
    private static BlockEntityRenderState blockEntityRenderState;
    private static boolean renderItemHead;
    private static boolean renderOverlayEyes;

    public static EntityRenderState setEntityRenderState(EntityRenderState entityRenderState) {
        EntityRenderState prev = RenderState.entityRenderState;
        RenderState.entityRenderState = entityRenderState;
        return prev;
    }

    public static EntityRenderState getEntityRenderState() {
        return entityRenderState;
    }

    public static Entity getEntity() {
        return entityRenderState == null ? null : entityRenderState.entity;
    }

    public static EntityRenderer getEntityRenderer() {
        return entityRenderState == null ? null : entityRenderState.entityRenderer;
    }

    public static BlockEntityRenderState setBlockEntityRenderState(BlockEntityRenderState blockEntityRenderState) {
        BlockEntityRenderState prev = RenderState.blockEntityRenderState;
        RenderState.blockEntityRenderState = blockEntityRenderState;
        return prev;
    }

    public static BlockEntityRenderState getBlockEntityRenderState() {
        return blockEntityRenderState;
    }

    public static BlockEntity getBlockEntity() {
        return blockEntityRenderState == null ? null : blockEntityRenderState.blockEntity;
    }

    public static BlockEntityRenderer getBlockEntityRenderer() {
        return blockEntityRenderState == null ? null : blockEntityRenderState.blockEntityRenderer;
    }

    public static void setRenderItemHead(boolean renderItemHead) {
        RenderState.renderItemHead = renderItemHead;
    }

    public static boolean isRenderItemHead() {
        return renderItemHead;
    }

    public static void setRenderOverlayEyes(boolean renderOverlayEyes) {
        RenderState.renderOverlayEyes = renderOverlayEyes;
    }

    public static boolean isRenderOverlayEyes() {
        return renderOverlayEyes;
    }

    public static Object getRenderState() {
        if (entityRenderState != null) {
            return entityRenderState;
        } else {
            return blockEntityRenderState != null ? blockEntityRenderState : null;
        }
    }

    public static void clear() {
        entityRenderState = null;
        blockEntityRenderState = null;
        renderItemHead = false;
        renderOverlayEyes = false;
    }
}
