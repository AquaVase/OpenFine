package net.optifine.shaders;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Collection;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ViewportEvent;
import net.optifine.Config;
import net.optifine.Lagometer;
import net.optifine.reflect.Reflector;
import net.optifine.render.GlBlendState;
import net.optifine.render.GlCullState;
import net.optifine.render.GlDepthState;
import net.optifine.render.GlScissorState;
import net.optifine.render.ICamera;
import net.optifine.render.RenderState;
import net.optifine.util.MathUtils;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class ShadersRender {
   private static final Identifier END_PORTAL_TEXTURE = new Identifier("textures/entity/end_portal.png");
   public static boolean frustumTerrainShadowChanged = false;
   public static boolean frustumEntitiesShadowChanged = false;
   public static int countEntitiesRenderedShadow;
   public static int countTileEntitiesRenderedShadow;
   private static AvatarRenderState playerRenderState = new AvatarRenderState();

   public static void setFrustrumPosition(ICamera frustum, double x, double y, double z) {
      frustum.setCameraPosition(x, y, z);
   }

   public static void beginTerrainSolid() {
      if (Shaders.isRenderingWorld) {
         Shaders.fogEnabled = true;
         Shaders.useProgram(Shaders.ProgramTerrain);
         Shaders.setRenderStage(RenderStage.TERRAIN_SOLID);
      }
   }

   public static void beginTerrainCutout() {
      if (Shaders.isRenderingWorld) {
         Shaders.useProgram(Shaders.ProgramTerrain);
         Shaders.setRenderStage(RenderStage.TERRAIN_CUTOUT);
      }
   }

   public static void endTerrain() {
      if (Shaders.isRenderingWorld) {
         Shaders.useProgram(Shaders.ProgramTexturedLit);
         Shaders.setRenderStage(RenderStage.NONE);
      }
   }

   public static void beginTranslucent() {
      if (Shaders.isRenderingWorld) {
         Shaders.useProgram(Shaders.ProgramWater);
         Shaders.setRenderStage(RenderStage.TERRAIN_TRANSLUCENT);
      }
   }

   public static void endTranslucent() {
      if (Shaders.isRenderingWorld) {
         Shaders.useProgram(Shaders.ProgramTexturedLit);
         Shaders.setRenderStage(RenderStage.NONE);
      }
   }

   public static void renderHandSolid(GameRenderer er, CameraRenderState cameraState, Matrix4fc viewIn, float partialTicks) {
      if (!Shaders.isShadowPass) {
         boolean blockTranslucentMain = Shaders.isItemToRenderMainTranslucent();
         boolean blockTranslucentOff = Shaders.isItemToRenderOffTranslucent();
         if (!blockTranslucentMain || !blockTranslucentOff) {
            Shaders.debugLabels.pushDebugGroup(() -> "Hand solid");
            Shaders.readCenterDepth();
            Shaders.beginHand(false);
            Shaders.setSkipRenderHands(blockTranslucentMain, blockTranslucentOff);
            SubmitNodeStorage storage = er.getHandAndScreenSubmitNodeStorage();
            FeatureRenderDispatcher renderDispatcher = er.getHandFeatureRenderDispatcher();
            er.renderItemInHand(cameraState, partialTicks, viewIn, false);
            renderDispatcher.renderAllFeatures(storage);
            Shaders.endHand();
            Shaders.setHandsRendered(!blockTranslucentMain, !blockTranslucentOff);
            Shaders.setSkipRenderHands(false, false);
            Shaders.debugLabels.popDebugGroup();
         }
      }
   }

   public static void renderHandTranslucent(GameRenderer er, CameraRenderState cameraState, Matrix4fc viewIn, float partialTicks) {
      if (!Shaders.isShadowPass && !Shaders.isBothHandsRendered()) {
         Shaders.debugLabels.pushDebugGroup(() -> "Hand translucent");
         Shaders.readCenterDepth();
         GlStateManager._enableBlend();
         Shaders.beginHand(true);
         Shaders.setSkipRenderHands(Shaders.isHandRenderedMain(), Shaders.isHandRenderedOff());
         SubmitNodeStorage storage = er.getHandAndScreenSubmitNodeStorage();
         FeatureRenderDispatcher renderDispatcher = er.getHandFeatureRenderDispatcher();
         er.renderItemInHand(cameraState, partialTicks, viewIn, true);
         renderDispatcher.renderAllFeatures(storage);
         Shaders.endHand();
         Shaders.setHandsRendered(true, true);
         Shaders.setSkipRenderHands(false, false);
         Shaders.debugLabels.popDebugGroup();
      }
   }

   public static void renderItemFP(
      ItemInHandRenderer itemRenderer,
      float partialTicks,
      PoseStack matrixStackIn,
      SubmitNodeCollector bufferIn,
      LocalPlayer playerEntityIn,
      int combinedLightIn,
      boolean renderTranslucent
   ) {
      GameRenderer gr = Config.getGameRenderer();
      SubmitNodeStorage storage = gr.getHandAndScreenSubmitNodeStorage();
      RenderState.setEntityRenderState(getPlayerRenderState(playerEntityIn));
      if (renderTranslucent) {
         matrixStackIn.pushPose();
         DrawBuffers drawBuffers = GlState.getDrawBuffers();
         GlState.setDrawBuffers(Shaders.drawBuffersNone);
         itemRenderer.submitHandsWithItems(partialTicks, matrixStackIn, bufferIn, playerEntityIn, combinedLightIn);
         gr.getHandFeatureRenderDispatcher().renderAllFeatures(storage);
         GlState.setDrawBuffers(drawBuffers);
         matrixStackIn.popPose();
      }

      itemRenderer.submitHandsWithItems(partialTicks, matrixStackIn, bufferIn, playerEntityIn, combinedLightIn);
      gr.getHandFeatureRenderDispatcher().renderAllFeatures(storage);
      RenderState.setEntityRenderState(null);
   }

   private static AvatarRenderState getPlayerRenderState(LocalPlayer playerEntityIn) {
      playerRenderState.entity = playerEntityIn;
      return playerRenderState;
   }

   public static void beginBlockDamage() {
      if (Shaders.isRenderingWorld) {
         Shaders.useProgram(Shaders.ProgramDamagedBlock);
         Shaders.setRenderStage(RenderStage.DESTROY);
         if (Shaders.ProgramDamagedBlock.getId() == Shaders.ProgramTerrain.getId()) {
            GlState.setDrawBuffers(Shaders.drawBuffersColorAtt[0]);
            GlStateManager._depthMask(false);
         }
      }
   }

   public static void endBlockDamage() {
      if (Shaders.isRenderingWorld) {
         GlStateManager._depthMask(true);
         Shaders.useProgram(Shaders.ProgramTexturedLit);
         Shaders.setRenderStage(RenderStage.NONE);
      }
   }

   public static void beginOutline() {
      if (Shaders.isRenderingWorld) {
         Shaders.useProgram(Shaders.ProgramBasic);
         Shaders.setRenderStage(RenderStage.OUTLINE);
      }
   }

   public static void endOutline() {
      if (Shaders.isRenderingWorld) {
         Shaders.useProgram(Shaders.ProgramTexturedLit);
         Shaders.setRenderStage(RenderStage.NONE);
      }
   }

   public static void beginDebug() {
      if (Shaders.isRenderingWorld) {
         Shaders.setRenderStage(RenderStage.DEBUG);
      }
   }

   public static void endDebug() {
      if (Shaders.isRenderingWorld) {
         Shaders.setRenderStage(RenderStage.NONE);
      }
   }

   public static void renderShadowMap(GameRenderer entityRenderer, Camera activeRenderInfo, DeltaTracker deltaTracker, float partialTicks) {
      if (Shaders.hasShadowMap) {
         Shaders.debugLabels.pushDebugGroup(() -> "Shadow pass");
         Minecraft mc = Minecraft.getInstance();
         ProfilerFiller profiler = Profiler.get();
         profiler.popPush("shadow pass");
         LevelRenderer renderGlobal = mc.levelRenderer;
         LevelExtractor levelExtractor = mc.levelExtractor;
         Shaders.isShadowPass = true;
         Shaders.checkGLError("pre shadow");
         GpuBufferSlice projectionPrev = RenderSystem.getProjectionMatrixBuffer();
         ProjectionType projectionTypePrev = RenderSystem.getProjectionType();
         RenderSystem.getModelViewStack().pushMatrix();
         profiler.popPush("shadow clear");
         Shaders.sfb.bindFramebuffer();
         Shaders.checkGLError("shadow bind sfb");
         profiler.popPush("shadow camera");
         updateActiveRenderInfo(activeRenderInfo, deltaTracker);
         mc.getBlockEntityRenderDispatcher().prepare(activeRenderInfo.position());
         mc.getEntityRenderDispatcher().prepare(activeRenderInfo, mc.crosshairPickEntity);
         PoseStack matrixStack = new PoseStack();
         Shaders.setCameraShadow(matrixStack, activeRenderInfo, partialTicks);
         Matrix4f viewMatrix = matrixStack.last().pose();
         GlStateManager.lockScissor(new GlScissorState(false));
         Shaders.checkGLError("shadow camera");
         Shaders.dispatchComputes(Shaders.dfb, Shaders.ProgramShadow.getComputePrograms());
         Shaders.useProgram(Shaders.ProgramShadow);
         Shaders.sfb.setDrawBuffers();
         Shaders.checkGLError("shadow drawbuffers");
         GL30.glReadBuffer(0);
         Shaders.checkGLError("shadow readbuffer");
         Shaders.sfb.setDepthTexture();
         Shaders.sfb.setColorTextures(true);
         Shaders.checkFramebufferStatus("shadow fb");
         GlStateManager._depthMask(true);
         GlStateManager.clearDepth(1.0);
         GlStateManager._clear(256);

         for (int i = 0; i < Shaders.usedShadowColorBuffers; i++) {
            if (Shaders.shadowBuffersClear[i]) {
               Vector4f col = Shaders.shadowBuffersClearColor[i];
               if (col != null) {
                  GlStateManager.clearColor(col.x(), col.y(), col.z(), col.w());
               } else {
                  GlStateManager.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
               }

               GlState.setDrawBuffers(Shaders.drawBuffersColorAtt[i]);
               GlStateManager._clear(16384);
            }
         }

         Shaders.sfb.setDrawBuffers();
         Shaders.checkGLError("shadow clear");
         profiler.popPush("shadow frustum");
         Frustum frustum = makeShadowFrustum(activeRenderInfo, partialTicks);
         profiler.popPush("shadow culling");
         Vec3 cameraPos = activeRenderInfo.position();
         frustum.prepare(cameraPos.x, cameraPos.y, cameraPos.z);
         GlStateManager.lockDepth(new GlDepthState(true, true, 515));
         GlStateManager._colorMask(15);
         GlStateManager.lockCull(new GlCullState(false));
         GlStateManager.lockBlend(new GlBlendState(false));
         profiler.popPush("shadow prepareterrain");
         mc.getTextureManager().bindTexture(TextureAtlas.LOCATION_BLOCKS);
         profiler.popPush("shadow setupterrain");
         renderGlobal.setShadowRenderInfos(true);
         Lagometer.timerVisibility.start();
         if (!activeRenderInfo.isDebugFrustum()) {
            applyFrustumShadow(renderGlobal, frustum);
         }

         Lagometer.timerVisibility.end();
         profiler.popPush("shadow updatechunks");
         profiler.popPush("shadow terrain");
         double x = cameraPos.x();
         double y = cameraPos.y();
         double z = cameraPos.z();
         Lagometer.timerTerrain.start();
         ChunkSectionsToRender chunksToRender = renderGlobal.prepareChunkRenders(viewMatrix);
         GpuSampler chunkSampler = renderGlobal.getChunkLayerSampler();
         if (Shaders.isRenderShadowTerrain()) {
            chunksToRender.renderGroup(ChunkSectionLayerGroup.OPAQUE, chunkSampler);
            Shaders.checkGLError("shadow terrain opaque");
         }

         FeatureRenderDispatcher featureRenderDispatcher = renderGlobal.getFeatureRenderDispatcher();
         SubmitNodeStorage submitNodeStorage = renderGlobal.getSubmitNodeStorage();
         LevelRenderState levelRenderState = new LevelRenderState();
         activeRenderInfo.extractRenderState(levelRenderState.cameraRenderState, partialTicks);
         profiler.popPush("shadow entities");
         countEntitiesRenderedShadow = 0;
         countTileEntitiesRenderedShadow = 0;
         TickRateManager tickRateManager = mc.level.tickRateManager();
         float frozenPartialTicks = tickRateManager.runsNormally() ? partialTicks : 1.0F;
         LevelRenderer wr = mc.levelRenderer;
         Shaders.beginEntities();
         profiler.push("extract shadow entities");
         if (Shaders.isRenderShadowEntities()) {
            levelExtractor.extractVisibleEntities(activeRenderInfo, frustum, deltaTracker, levelRenderState);
         }

         profiler.pop();
         renderGlobal.submitEntities(matrixStack, levelRenderState, submitNodeStorage);
         featureRenderDispatcher.renderAllFeatures(submitNodeStorage);
         countEntitiesRenderedShadow = levelRenderState.entityRenderStates.size();
         wr.checkPoseStack(matrixStack);
         Shaders.endEntities();
         Shaders.beginBlockEntities();
         AbstractSignRenderer.updateTextRenderDistance();
         boolean forgeRenderBoundingBox = Reflector.IForgeBlockEntity_getRenderBoundingBox.exists();
         float blockEntityPartialTicks = tickRateManager.isFrozen() ? frozenPartialTicks : partialTicks;
         profiler.popPush("extract shadow blockEntities");
         if (Shaders.isRenderShadowBlockEntities()) {
            levelExtractor.extractVisibleBlockEntities(activeRenderInfo, partialTicks, levelRenderState, frustum);
         }

         profiler.pop();
         renderGlobal.submitBlockEntities(matrixStack, levelRenderState, submitNodeStorage);
         featureRenderDispatcher.renderAllFeatures(submitNodeStorage);
         countTileEntitiesRenderedShadow = levelRenderState.blockEntityRenderStates.size();
         levelRenderState.reset();
         wr.checkPoseStack(matrixStack);
         Shaders.endBlockEntities();
         Lagometer.timerTerrain.end();
         Shaders.checkGLError("shadow entities");
         GlStateManager._depthMask(true);
         GlStateManager._disableBlend();
         GlStateManager.unlockCull();
         GlStateManager._enableCull();
         GlStateManager._blendFuncSeparate(770, 771, 1, 0);
         GlStateManager.alphaFunc(516, 0.1F);
         if (Shaders.usedShadowDepthBuffers >= 2) {
            Shaders.debugLabels.pushDebugGroup(() -> "Copy shadowtex1");
            ShadersTex.copyTexture(5, Shaders.shadowMapWidth, Shaders.shadowMapHeight);
            Shaders.debugLabels.popDebugGroup();
         }

         GlStateManager._disableBlend();
         GlStateManager._depthMask(true);
         mc.getTextureManager().bindTexture(TextureAtlas.LOCATION_BLOCKS);
         Shaders.checkGLError("shadow pre-translucent");
         Shaders.sfb.setDrawBuffers();
         Shaders.checkGLError("shadow drawbuffers pre-translucent");
         Shaders.checkFramebufferStatus("shadow pre-translucent");
         if (Shaders.isRenderShadowTranslucent()) {
            Lagometer.timerTerrain.start();
            profiler.popPush("shadow translucent");
            chunksToRender.renderGroup(ChunkSectionLayerGroup.TRANSLUCENT, chunkSampler);
            Shaders.checkGLError("shadow translucent");
            Lagometer.timerTerrain.end();
         }

         GlStateManager.unlockBlend();
         GlStateManager.unlockDepth();
         GlStateManager._depthMask(true);
         GlStateManager._enableCull();
         GlStateManager._disableBlend();
         GL30.glFlush();
         Shaders.checkGLError("shadow flush");
         Shaders.isShadowPass = false;
         renderGlobal.setShadowRenderInfos(false);
         profiler.popPush("shadow postprocess");
         if (Shaders.hasGlGenMipmap) {
            Shaders.sfb.generateDepthMipmaps(Shaders.shadowMipmapEnabled);
            Shaders.sfb.generateColorMipmaps(true, Shaders.shadowColorMipmapEnabled);
         }

         Shaders.checkGLError("shadow postprocess");
         if (Shaders.hasShadowcompPrograms) {
            Shaders.renderShadowComposites();
         }

         Shaders.dfb.bindFramebuffer();
         GlStateManager._viewport(0, 0, Shaders.renderWidth, Shaders.renderHeight);
         GlStateManager.unlockScissor();
         GlState.setDrawBuffers(null);
         mc.getTextureManager().bindTexture(TextureAtlas.LOCATION_BLOCKS);
         Shaders.useProgram(Shaders.ProgramTerrain);
         RenderSystem.getModelViewStack().popMatrix();
         RenderSystem.setProjectionMatrix(projectionPrev, projectionTypePrev);
         Shaders.checkGLError("shadow end");
         Shaders.debugLabels.popDebugGroup();
      }
   }

   public static void applyFrustumShadow(LevelRenderer renderGlobal, Frustum frustum) {
      Minecraft mc = Config.getMinecraft();
      LevelExtractor levelExtractor = mc.levelExtractor;
      ProfilerFiller profiler = Profiler.get();
      profiler.push("apply_shadow_frustum");
      int shadowRenderDistance = (int)Shaders.getShadowRenderDistance();
      int farPlaneDistance = Config.getGameRenderer().getRenderDistance();
      boolean checkDistance = shadowRenderDistance > 0 && shadowRenderDistance < farPlaneDistance;
      int maxChunkDist = checkDistance ? shadowRenderDistance : -1;
      if (frustumTerrainShadowChanged || renderGlobal.needsFrustumUpdate()) {
         levelExtractor.applyFrustum(frustum, false, maxChunkDist);
         frustumTerrainShadowChanged = false;
      }

      if (frustumEntitiesShadowChanged || mc.level.getSectionStorage().isUpdated()) {
         levelExtractor.applyFrustumEntities(frustum, maxChunkDist);
         frustumEntitiesShadowChanged = false;
      }

      profiler.pop();
   }

   public static Frustum makeShadowFrustum(Camera camera, float partialTicks) {
      if (!Shaders.isShadowCulling()) {
         return FrustumDummy.INSTANCE;
      }

      Minecraft mc = Config.getMinecraft();
      GameRenderer gameRenderer = Config.getGameRenderer();
      PoseStack matrixStackIn = new PoseStack();
      if (Reflector.ForgeEventFactoryClient_fireComputeCameraAngles.exists()) {
         ViewportEvent.ComputeCameraAngles cameraSetup = (ViewportEvent.ComputeCameraAngles)Reflector.ForgeEventFactoryClient_fireComputeCameraAngles
            .call(gameRenderer, camera, partialTicks);
         camera.setRotation(cameraSetup.getYaw(), cameraSetup.getPitch(), cameraSetup.getRoll());
         matrixStackIn.mulPose(Axis.ZP.rotationDegrees(cameraSetup.getRoll()));
      }

      matrixStackIn.mulPose(Axis.XP.rotationDegrees(camera.xRot()));
      matrixStackIn.mulPose(Axis.YP.rotationDegrees(camera.yRot() + 180.0F));
      Matrix4f matrixProjection = camera.createProjectionMatrixForCulling();
      Matrix4f matrix4f = matrixStackIn.last().pose();
      Vec3 pos = camera.position();
      double x = pos.x();
      double y = pos.y();
      double z = pos.z();
      Frustum frustum = new ShadowFrustum(matrix4f, matrixProjection);
      frustum.prepare(x, y, z);
      return frustum;
   }

   public static void updateActiveRenderInfo(Camera activeRenderInfo, DeltaTracker deltaTracker) {
      activeRenderInfo.update(deltaTracker);
   }

   public static void preRenderChunkLayer(ChunkSectionLayer blockLayerIn) {
      if (blockLayerIn == ChunkSectionLayer.SOLID) {
         beginTerrainSolid();
      }

      if (blockLayerIn == ChunkSectionLayer.CUTOUT) {
         beginTerrainCutout();
      }

      if (blockLayerIn == ChunkSectionLayer.TRANSLUCENT) {
         beginTranslucent();
      }

      if (Shaders.isRenderBackFace(blockLayerIn)) {
         GlStateManager._disableCull();
      }

      Shaders.setTextureMatrix(Shaders.MATRIX_IDENTITY);
   }

   public static void postRenderChunkLayer(ChunkSectionLayer blockLayerIn) {
      if (Shaders.isRenderBackFace(blockLayerIn)) {
         GlStateManager._enableCull();
      }
   }

   public static void preRender(RenderType renderType) {
      if (Shaders.isRenderingWorld) {
         if (!Shaders.isShadowPass) {
            if (renderType.isGlint()) {
               renderEnchantedGlintBegin();
            } else if (renderType.getName().equals("eyes")) {
               Shaders.beginSpiderEyes();
            } else if (renderType.getName().equals("crumbling")) {
               beginBlockDamage();
            } else if (renderType == RenderTypes.LINES || renderType == RenderTypes.LINES_TRANSLUCENT) {
               Shaders.beginLines();
            } else if (renderType == RenderTypes.leash()) {
               Shaders.beginLeash();
            } else if (renderType == RenderTypes.waterMask()) {
               Shaders.beginWaterMask();
            } else if (renderType.getName().equals("beacon_beam")) {
               Shaders.beginBeacon();
            }
         }
      }
   }

   public static void postRender(RenderType renderType) {
      if (Shaders.isRenderingWorld) {
         if (!Shaders.isShadowPass) {
            if (renderType.isGlint()) {
               renderEnchantedGlintEnd();
            } else if (renderType.getName().equals("eyes")) {
               Shaders.endSpiderEyes();
            } else if (renderType.getName().equals("crumbling")) {
               endBlockDamage();
            } else if (renderType == RenderTypes.LINES || renderType == RenderTypes.LINES_TRANSLUCENT) {
               Shaders.endLines();
            } else if (renderType == RenderTypes.leash()) {
               Shaders.endLeash();
            } else if (renderType == RenderTypes.waterMask()) {
               Shaders.endWaterMask();
            } else if (renderType.getName().equals("beacon_beam")) {
               Shaders.endBeacon();
            }
         }
      }
   }

   public static void enableArrayPointerVbo() {
      GL20.glEnableVertexAttribArray(Shaders.midBlockAttrib);
      GL20.glEnableVertexAttribArray(Shaders.midTexCoordAttrib);
      GL20.glEnableVertexAttribArray(Shaders.tangentAttrib);
      GL20.glEnableVertexAttribArray(Shaders.entityAttrib);
      GL20.glEnableVertexAttribArray(Shaders.velocityAttrib);
   }

   public static void setupArrayPointersVbo(boolean enableArrayIn) {
      int vertexSizeI = 18;
      if (enableArrayIn) {
         enableArrayPointerVbo();
      }

      GL20.glVertexAttribPointer(Shaders.midBlockAttrib, 3, 5120, false, 72, 32L);
      GL20.glVertexAttribPointer(Shaders.midTexCoordAttrib, 2, 5126, false, 72, 36L);
      GL20.glVertexAttribPointer(Shaders.tangentAttrib, 4, 5122, false, 72, 44L);
      GL20.glVertexAttribPointer(Shaders.entityAttrib, 3, 5122, false, 72, 52L);
      GL20.glVertexAttribPointer(Shaders.velocityAttrib, 3, 5126, false, 72, 60L);
   }

   public static void beaconBeamBegin() {
      Shaders.useProgram(Shaders.ProgramBeaconBeam);
   }

   public static void beaconBeamStartQuad1() {
   }

   public static void beaconBeamStartQuad2() {
   }

   public static void beaconBeamDraw1() {
   }

   public static void beaconBeamDraw2() {
      GlStateManager._disableBlend();
   }

   public static void renderEnchantedGlintBegin() {
      Shaders.useProgram(Shaders.ProgramArmorGlint);
   }

   public static void renderEnchantedGlintEnd() {
      if (Shaders.isRenderingWorld) {
         if (Shaders.isRenderingHand() && Shaders.isRenderBothHands()) {
            Shaders.useProgram(Shaders.ProgramHand);
         } else {
            Shaders.useProgram(Shaders.ProgramEntities);
         }
      } else {
         Shaders.useProgram(Shaders.ProgramNone);
      }
   }

   public static boolean renderEndPortal(
      Collection<Direction> directionsIn, float offsetUp, PoseStack.Pose matrixEntryIn, VertexConsumer bufferIn, int combinedLightIn, int combinedOverlayIn
   ) {
      Matrix4f matrix = matrixEntryIn.pose();
      Matrix3f matrixNormal = matrixEntryIn.normal();
      VertexConsumer bufferbuilder = bufferIn;
      float col = 0.5F;
      float r = col * 0.5F;
      float g = col * 1.0F;
      float b = col * 1.0F;
      float u0 = 0.0F;
      float u1 = 0.2F;
      float v0 = u0;
      float v1 = u1;
      float du = (float)(System.currentTimeMillis() % 100000L) / 100000.0F;
      float dv = du;
      float dy = offsetUp;
      int lm = combinedLightIn;
      int ov = combinedOverlayIn;
      float x = 0.0F;
      float y = 0.0F;
      float z = 0.0F;
      if (directionsIn.contains(Direction.SOUTH)) {
         Vec3i vec3i = Direction.SOUTH.getUnitVec3i();
         float xv = vec3i.getX();
         float yv = vec3i.getY();
         float zv = vec3i.getZ();
         float xn = MathUtils.getTransformX(matrixNormal, xv, yv, zv);
         float yn = MathUtils.getTransformY(matrixNormal, xv, yv, zv);
         float zn = MathUtils.getTransformZ(matrixNormal, xv, yv, zv);
         bufferbuilder.addVertex(matrix, x, y, z + 1.0F).setColor(r, g, b, 1.0F).setUv(u0 + du, v0 + dv).setOverlay(ov).setLight(lm).setNormal(xn, yn, zn);
         bufferbuilder.addVertex(matrix, x + 1.0F, y, z + 1.0F)
            .setColor(r, g, b, 1.0F)
            .setUv(u0 + du, v1 + dv)
            .setOverlay(ov)
            .setLight(lm)
            .setNormal(xn, yn, zn);
         bufferbuilder.addVertex(matrix, x + 1.0F, y + 1.0F, z + 1.0F)
            .setColor(r, g, b, 1.0F)
            .setUv(u1 + du, v1 + dv)
            .setOverlay(ov)
            .setLight(lm)
            .setNormal(xn, yn, zn);
         bufferbuilder.addVertex(matrix, x, y + 1.0F, z + 1.0F)
            .setColor(r, g, b, 1.0F)
            .setUv(u1 + du, v0 + dv)
            .setOverlay(ov)
            .setLight(lm)
            .setNormal(xn, yn, zn);
      }

      if (directionsIn.contains(Direction.NORTH)) {
         Vec3i vec3i = Direction.NORTH.getUnitVec3i();
         float xv = vec3i.getX();
         float yv = vec3i.getY();
         float zv = vec3i.getZ();
         float xn = MathUtils.getTransformX(matrixNormal, xv, yv, zv);
         float yn = MathUtils.getTransformY(matrixNormal, xv, yv, zv);
         float zn = MathUtils.getTransformZ(matrixNormal, xv, yv, zv);
         bufferbuilder.addVertex(matrix, x, y + 1.0F, z).setColor(r, g, b, 1.0F).setUv(u1 + du, v1 + dv).setOverlay(ov).setLight(lm).setNormal(xn, yn, zn);
         bufferbuilder.addVertex(matrix, x + 1.0F, y + 1.0F, z)
            .setColor(r, g, b, 1.0F)
            .setUv(u1 + du, v0 + dv)
            .setOverlay(ov)
            .setLight(lm)
            .setNormal(xn, yn, zn);
         bufferbuilder.addVertex(matrix, x + 1.0F, y, z).setColor(r, g, b, 1.0F).setUv(u0 + du, v0 + dv).setOverlay(ov).setLight(lm).setNormal(xn, yn, zn);
         bufferbuilder.addVertex(matrix, x, y, z).setColor(r, g, b, 1.0F).setUv(u0 + du, v1 + dv).setOverlay(ov).setLight(lm).setNormal(xn, yn, zn);
      }

      if (directionsIn.contains(Direction.EAST)) {
         Vec3i vec3i = Direction.EAST.getUnitVec3i();
         float xv = vec3i.getX();
         float yv = vec3i.getY();
         float zv = vec3i.getZ();
         float xn = MathUtils.getTransformX(matrixNormal, xv, yv, zv);
         float yn = MathUtils.getTransformY(matrixNormal, xv, yv, zv);
         float zn = MathUtils.getTransformZ(matrixNormal, xv, yv, zv);
         bufferbuilder.addVertex(matrix, x + 1.0F, y + 1.0F, z)
            .setColor(r, g, b, 1.0F)
            .setUv(u1 + du, v1 + dv)
            .setOverlay(ov)
            .setLight(lm)
            .setNormal(xn, yn, zn);
         bufferbuilder.addVertex(matrix, x + 1.0F, y + 1.0F, z + 1.0F)
            .setColor(r, g, b, 1.0F)
            .setUv(u1 + du, v0 + dv)
            .setOverlay(ov)
            .setLight(lm)
            .setNormal(xn, yn, zn);
         bufferbuilder.addVertex(matrix, x + 1.0F, y, z + 1.0F)
            .setColor(r, g, b, 1.0F)
            .setUv(u0 + du, v0 + dv)
            .setOverlay(ov)
            .setLight(lm)
            .setNormal(xn, yn, zn);
         bufferbuilder.addVertex(matrix, x + 1.0F, y, z).setColor(r, g, b, 1.0F).setUv(u0 + du, v1 + dv).setOverlay(ov).setLight(lm).setNormal(xn, yn, zn);
      }

      if (directionsIn.contains(Direction.WEST)) {
         Vec3i vec3i = Direction.WEST.getUnitVec3i();
         float xv = vec3i.getX();
         float yv = vec3i.getY();
         float zv = vec3i.getZ();
         float xn = MathUtils.getTransformX(matrixNormal, xv, yv, zv);
         float yn = MathUtils.getTransformY(matrixNormal, xv, yv, zv);
         float zn = MathUtils.getTransformZ(matrixNormal, xv, yv, zv);
         bufferbuilder.addVertex(matrix, x, y, z).setColor(r, g, b, 1.0F).setUv(u0 + du, v0 + dv).setOverlay(ov).setLight(lm).setNormal(xn, yn, zn);
         bufferbuilder.addVertex(matrix, x, y, z + 1.0F).setColor(r, g, b, 1.0F).setUv(u0 + du, v1 + dv).setOverlay(ov).setLight(lm).setNormal(xn, yn, zn);
         bufferbuilder.addVertex(matrix, x, y + 1.0F, z + 1.0F)
            .setColor(r, g, b, 1.0F)
            .setUv(u1 + du, v1 + dv)
            .setOverlay(ov)
            .setLight(lm)
            .setNormal(xn, yn, zn);
         bufferbuilder.addVertex(matrix, x, y + 1.0F, z).setColor(r, g, b, 1.0F).setUv(u1 + du, v0 + dv).setOverlay(ov).setLight(lm).setNormal(xn, yn, zn);
      }

      if (directionsIn.contains(Direction.DOWN)) {
         Vec3i vec3i = Direction.DOWN.getUnitVec3i();
         float xv = vec3i.getX();
         float yv = vec3i.getY();
         float zv = vec3i.getZ();
         float xn = MathUtils.getTransformX(matrixNormal, xv, yv, zv);
         float yn = MathUtils.getTransformY(matrixNormal, xv, yv, zv);
         float zn = MathUtils.getTransformZ(matrixNormal, xv, yv, zv);
         bufferbuilder.addVertex(matrix, x, y, z).setColor(r, g, b, 1.0F).setUv(u0 + du, v0 + dv).setOverlay(ov).setLight(lm).setNormal(xn, yn, zn);
         bufferbuilder.addVertex(matrix, x + 1.0F, y, z).setColor(r, g, b, 1.0F).setUv(u0 + du, v1 + dv).setOverlay(ov).setLight(lm).setNormal(xn, yn, zn);
         bufferbuilder.addVertex(matrix, x + 1.0F, y, z + 1.0F)
            .setColor(r, g, b, 1.0F)
            .setUv(u1 + du, v1 + dv)
            .setOverlay(ov)
            .setLight(lm)
            .setNormal(xn, yn, zn);
         bufferbuilder.addVertex(matrix, x, y, z + 1.0F).setColor(r, g, b, 1.0F).setUv(u1 + du, v0 + dv).setOverlay(ov).setLight(lm).setNormal(xn, yn, zn);
      }

      if (directionsIn.contains(Direction.UP)) {
         Vec3i vec3i = Direction.UP.getUnitVec3i();
         float xv = vec3i.getX();
         float yv = vec3i.getY();
         float zv = vec3i.getZ();
         float xn = MathUtils.getTransformX(matrixNormal, xv, yv, zv);
         float yn = MathUtils.getTransformY(matrixNormal, xv, yv, zv);
         float zn = MathUtils.getTransformZ(matrixNormal, xv, yv, zv);
         bufferbuilder.addVertex(matrix, x, y + dy, z + 1.0F).setColor(r, g, b, 1.0F).setUv(u0 + du, v0 + dv).setOverlay(ov).setLight(lm).setNormal(xn, yn, zn);
         bufferbuilder.addVertex(matrix, x + 1.0F, y + dy, z + 1.0F)
            .setColor(r, g, b, 1.0F)
            .setUv(u0 + du, v1 + dv)
            .setOverlay(ov)
            .setLight(lm)
            .setNormal(xn, yn, zn);
         bufferbuilder.addVertex(matrix, x + 1.0F, y + dy, z).setColor(r, g, b, 1.0F).setUv(u1 + du, v1 + dv).setOverlay(ov).setLight(lm).setNormal(xn, yn, zn);
         bufferbuilder.addVertex(matrix, x, y + dy, z).setColor(r, g, b, 1.0F).setUv(u1 + du, v0 + dv).setOverlay(ov).setLight(lm).setNormal(xn, yn, zn);
      }

      return true;
   }
}
