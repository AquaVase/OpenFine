package net.optifine.reflect;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.fish.SalmonModel;
import net.minecraft.client.model.animal.fish.TropicalFishLargeModel;
import net.minecraft.client.model.animal.fish.TropicalFishSmallModel;
import net.minecraft.client.model.animal.ghast.HappyGhastModel;
import net.minecraft.client.model.animal.llama.LlamaModel;
import net.minecraft.client.model.animal.llama.LlamaSpitModel;
import net.minecraft.client.model.animal.wolf.WolfModel;
import net.minecraft.client.model.effects.EvokerFangsModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.dragon.EnderDragonModel;
import net.minecraft.client.model.monster.zombie.DrownedModel;
import net.minecraft.client.model.object.armorstand.ArmorStandArmorModel;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.client.model.object.banner.BannerModel;
import net.minecraft.client.model.object.bell.BellModel;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.model.object.cart.MinecartModel;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.model.object.crystal.EndCrystalModel;
import net.minecraft.client.model.object.leash.LeashKnotModel;
import net.minecraft.client.model.object.projectile.ArrowModel;
import net.minecraft.client.model.object.projectile.ShulkerBulletModel;
import net.minecraft.client.model.object.projectile.TridentModel;
import net.minecraft.client.model.object.projectile.WindChargeModel;
import net.minecraft.client.model.object.skull.SkullModel;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.ConduitRenderer;
import net.minecraft.client.renderer.blockentity.CopperGolemStatueBlockRenderer;
import net.minecraft.client.renderer.blockentity.DecoratedPotRenderer;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.blockentity.LecternRenderer;
import net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EvokerFangsRenderer;
import net.minecraft.client.renderer.entity.LeashKnotRenderer;
import net.minecraft.client.renderer.entity.LlamaSpitRenderer;
import net.minecraft.client.renderer.entity.PufferfishRenderer;
import net.minecraft.client.renderer.entity.RaftRenderer;
import net.minecraft.client.renderer.entity.SalmonRenderer;
import net.minecraft.client.renderer.entity.ShulkerBulletRenderer;
import net.minecraft.client.renderer.entity.ThrownTridentRenderer;
import net.minecraft.client.renderer.entity.TropicalFishRenderer;
import net.minecraft.client.renderer.entity.WindChargeRenderer;
import net.minecraft.client.renderer.entity.WitherSkullRenderer;
import net.minecraft.client.renderer.entity.ZombieNautilusRenderer;
import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import net.minecraft.client.renderer.entity.layers.LivingEntityEmissiveLayer;
import net.minecraft.client.renderer.entity.layers.LlamaDecorLayer;
import net.minecraft.client.renderer.entity.layers.RopesLayer;
import net.minecraft.client.renderer.entity.layers.SheepWoolUndercoatLayer;
import net.minecraft.client.renderer.entity.layers.TropicalFishPatternLayer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.client.renderer.entity.layers.WolfArmorLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraftforge.client.model.ForgeFaceData;
import net.optifine.Log;
import net.optifine.util.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Reflector {
    private static final Logger LOGGER = LogManager.getLogger();
    private static boolean logForge = registerResolvable("*** Reflector Forge ***");
    public static ReflectorClass BrandingControl = new ReflectorClass("net.minecraftforge.internal.BrandingControl");
    public static ReflectorMethod BrandingControl_getBrandings = new ReflectorMethod(BrandingControl, "getBrandings");
    public static ReflectorMethod BrandingControl_getBranding = new ReflectorMethod(BrandingControl, "getBranding");
    public static ReflectorMethod BrandingControl_forEachLine = new ReflectorMethod(BrandingControl, "forEachLine");
    public static ReflectorMethod BrandingControl_forEachAboveCopyrightLine = new ReflectorMethod(BrandingControl, "forEachAboveCopyrightLine");
    public static ReflectorClass CapabilityProvider = new ReflectorClass("net.minecraftforge.common.capabilities.CapabilityProvider");
    public static ReflectorMethod CapabilityProvider_gatherCapabilities = new ReflectorMethod(CapabilityProvider, "gatherCapabilities", new Class[0]);
    public static ReflectorClass ClientModLoader = new ReflectorClass("net.minecraftforge.client.loading.ClientModLoader");
    public static ReflectorMethod ClientModLoader_isLoading = new ReflectorMethod(ClientModLoader, "isLoading");
    public static ReflectorClass ChunkEvent_Load = new ReflectorClass("net.minecraftforge.event.level.ChunkEvent$Load");
    public static ReflectorField ChunkEvent_Load_BUS = ChunkEvent_Load.makeField("BUS");
    public static ReflectorConstructor ChunkEvent_Load_Constructor = new ReflectorConstructor(ChunkEvent_Load, new Class[]{ChunkAccess.class, boolean.class});
    public static ReflectorClass ChunkEvent_Unload = new ReflectorClass("net.minecraftforge.event.level.ChunkEvent$Unload");
    public static ReflectorField ChunkEvent_Unload_BUS = ChunkEvent_Unload.makeField("BUS");
    public static ReflectorConstructor ChunkEvent_Unload_Constructor = new ReflectorConstructor(ChunkEvent_Unload, new Class[]{ChunkAccess.class});
    public static ReflectorClass ColorResolverManager = new ReflectorClass("net.minecraftforge.client.ColorResolverManager");
    public static ReflectorMethod ColorResolverManager_registerBlockTintCaches = ColorResolverManager.makeMethod("registerBlockTintCaches");
    public static ReflectorClass CrashReportAnalyser = new ReflectorClass("net.minecraftforge.logging.CrashReportAnalyser");
    public static ReflectorMethod CrashReportAnalyser_appendSuspectedMods = new ReflectorMethod(CrashReportAnalyser, "appendSuspectedMods");
    public static ReflectorClass CrashReportExtender = new ReflectorClass("net.minecraftforge.logging.CrashReportExtender");
    public static ReflectorMethod CrashReportExtender_extendSystemReport = new ReflectorMethod(CrashReportExtender, "extendSystemReport");
    public static ReflectorMethod CrashReportExtender_generateEnhancedStackTraceT = new ReflectorMethod(
        CrashReportExtender, "generateEnhancedStackTrace", new Class[]{Throwable.class}
    );
    public static ReflectorMethod CrashReportExtender_generateEnhancedStackTraceSTE = new ReflectorMethod(
        CrashReportExtender, "generateEnhancedStackTrace", new Class[]{StackTraceElement[].class}
    );
    public static ReflectorClass EntitySpectatorShaderManager = new ReflectorClass("net.minecraftforge.client.EntitySpectatorShaderManager");
    public static ReflectorMethod EntitySpectatorShaderManager_get = EntitySpectatorShaderManager.makeMethod("get");
    public static ReflectorClass EventBus = new ReflectorClass("net.minecraftforge.eventbus.api.bus.EventBus");
    public static ReflectorMethod EventBus_post = new ReflectorMethod(EventBus, "post");
    public static ReflectorClass ForgeEntity = new ReflectorClass(Entity.class);
    public static ReflectorMethod ForgeEntity_isInWaterOrSwimmable = ForgeEntity.makeMethod("isInWaterOrSwimmable");
    public static ReflectorClass ForgeEnderDragon = new ReflectorClass(EnderDragon.class);
    public static ReflectorMethod ForgeEnderDragon_getParts = ForgeEnderDragon.makeMethod("getParts");
    public static ReflectorClass IForgeBlockState = new ReflectorClass("net.minecraftforge.common.extensions.IForgeBlockState");
    public static ReflectorMethod IForgeBlockState_getSoundType3 = new ReflectorMethod(
        IForgeBlockState, "getSoundType", new Class[]{LevelReader.class, BlockPos.class, Entity.class}
    );
    public static ReflectorMethod IForgeBlockState_getStateAtViewpoint = new ReflectorMethod(IForgeBlockState, "getStateAtViewpoint");
    public static ReflectorMethod IForgeBlockState_shouldDisplayFluidOverlay = new ReflectorMethod(IForgeBlockState, "shouldDisplayFluidOverlay");
    public static ReflectorClass IForgeEntity = new ReflectorClass("net.minecraftforge.common.extensions.IForgeEntity");
    public static ReflectorMethod IForgeEntity_canUpdate = new ReflectorMethod(IForgeEntity, "canUpdate", new Class[0]);
    public static ReflectorMethod IForgeEntity_getEyeInFluidType = new ReflectorMethod(IForgeEntity, "getEyeInFluidType");
    public static ReflectorMethod IForgeEntity_getParts = new ReflectorMethod(IForgeEntity, "getParts");
    public static ReflectorMethod IForgeEntity_hasCustomOutlineRendering = new ReflectorMethod(IForgeEntity, "hasCustomOutlineRendering");
    public static ReflectorMethod IForgeEntity_isMultipartEntity = new ReflectorMethod(IForgeEntity, "isMultipartEntity");
    public static ReflectorMethod IForgeEntity_onAddedToWorld = new ReflectorMethod(IForgeEntity, "onAddedToWorld");
    public static ReflectorMethod IForgeEntity_onRemovedFromWorld = new ReflectorMethod(IForgeEntity, "onRemovedFromWorld");
    public static ReflectorMethod IForgeEntity_shouldRiderSit = new ReflectorMethod(IForgeEntity, "shouldRiderSit");
    public static ReflectorClass ForgeEventFactory = new ReflectorClass("net.minecraftforge.event.ForgeEventFactory");
    public static ReflectorMethod ForgeEventFactory_canEntityDespawn = new ReflectorMethod(ForgeEventFactory, "canEntityDespawn");
    public static ReflectorMethod ForgeEventFactory_fireChunkTicketLevelUpdated = new ReflectorMethod(ForgeEventFactory, "fireChunkTicketLevelUpdated");
    public static ReflectorMethod ForgeEventFactory_getMobGriefingEvent = new ReflectorMethod(ForgeEventFactory, "getMobGriefingEvent");
    public static ReflectorMethod ForgeEventFactory_onChunkDataSave = new ReflectorMethod(ForgeEventFactory, "onChunkDataSave");
    public static ReflectorMethod ForgeEventFactory_onChunkLoad = new ReflectorMethod(ForgeEventFactory, "onChunkLoad");
    public static ReflectorMethod ForgeEventFactory_onChunkUnload = new ReflectorMethod(ForgeEventFactory, "onChunkUnload");
    public static ReflectorMethod ForgeEventFactory_onDifficultyChange = new ReflectorMethod(ForgeEventFactory, "onDifficultyChange");
    public static ReflectorMethod ForgeEventFactory_onEntityJoinLevel = new ReflectorMethod(
        ForgeEventFactory, "onEntityJoinLevel", new Class[]{Entity.class, Level.class}
    );
    public static ReflectorMethod ForgeEventFactory_onEntityLeaveLevel = new ReflectorMethod(ForgeEventFactory, "onEntityLeaveLevel");
    public static ReflectorMethod ForgeEventFactory_onLevelLoad = new ReflectorMethod(ForgeEventFactory, "onLevelLoad");
    public static ReflectorMethod ForgeEventFactory_onLivingChangeTargetMob = new ReflectorMethod(ForgeEventFactory, "onLivingChangeTargetMob");
    public static ReflectorMethod ForgeEventFactory_onPlaySoundAtEntity = new ReflectorMethod(ForgeEventFactory, "onPlaySoundAtEntity");
    public static ReflectorMethod ForgeEventFactory_onPlaySoundAtPosition = new ReflectorMethod(ForgeEventFactory, "onPlaySoundAtPosition");
    public static ReflectorClass ForgeEventFactoryClient = new ReflectorClass("net.minecraftforge.client.event.ForgeEventFactoryClient");
    public static ReflectorMethod ForgeEventFactoryClient_fireComputeCameraAngles = new ReflectorMethod(ForgeEventFactoryClient, "fireComputeCameraAngles");
    public static ReflectorMethod ForgeEventFactoryClient_fireComputeFov = new ReflectorMethod(ForgeEventFactoryClient, "fireComputeFov");
    public static ReflectorMethod ForgeEventFactoryClient_fireFovModifierEvent = new ReflectorMethod(ForgeEventFactoryClient, "fireFovModifierEvent");
    public static ReflectorMethod ForgeEventFactoryClient_fireRenderNameTagEvent = new ReflectorMethod(ForgeEventFactoryClient, "fireRenderNameTagEvent");
    public static ReflectorMethod ForgeEventFactoryClient_onCreateSkullModels = new ReflectorMethod(ForgeEventFactoryClient, "onCreateSkullModels");
    public static ReflectorMethod ForgeEventFactoryClient_onGatherLayers = new ReflectorMethod(ForgeEventFactoryClient, "onGatherLayers");
    public static ReflectorMethod ForgeEventFactoryClient_onInitLevelRenderer = new ReflectorMethod(ForgeEventFactoryClient, "onInitLevelRenderer");
    public static ReflectorMethod ForgeEventFactoryClient_onRenderItemInFrame = new ReflectorMethod(ForgeEventFactoryClient, "onRenderItemInFrame");
    public static ReflectorMethod ForgeEventFactoryClient_onRenderLivingPre = new ReflectorMethod(ForgeEventFactoryClient, "onRenderLivingPre");
    public static ReflectorMethod ForgeEventFactoryClient_onRenderLivingPost = new ReflectorMethod(ForgeEventFactoryClient, "onRenderLivingPost");
    public static ReflectorClass ForgeFaceData = new ReflectorClass(ForgeFaceData.class);
    public static ReflectorMethod ForgeFaceData_calculateNormals = ForgeFaceData.makeMethod("calculateNormals");
    public static ReflectorClass ForgeHooks = new ReflectorClass("net.minecraftforge.common.ForgeHooks");
    public static ReflectorMethod ForgeHooks_getDyeColorFromItemStack = new ReflectorMethod(ForgeHooks, "getDyeColorFromItemStack");
    public static ReflectorClass ForgeHooksClient = new ReflectorClass("net.minecraftforge.client.ForgeHooksClient");
    public static ReflectorMethod ForgeHooksClient_onCustomizeBossEventProgress = new ReflectorMethod(ForgeHooksClient, "onCustomizeBossEventProgress");
    public static ReflectorMethod ForgeHooksClient_drawScreen = new ReflectorMethod(ForgeHooksClient, "drawScreen");
    public static ReflectorMethod ForgeHooksClient_gatherTooltipComponents6 = new ReflectorMethod(
        ForgeHooksClient, "gatherTooltipComponents", new Class[]{ItemStack.class, List.class, int.class, int.class, int.class, Font.class}
    );
    public static ReflectorMethod ForgeHooksClient_gatherTooltipComponents7 = new ReflectorMethod(
        ForgeHooksClient, "gatherTooltipComponents", new Class[]{ItemStack.class, List.class, Optional.class, int.class, int.class, int.class, Font.class}
    );
    public static ReflectorMethod ForgeHooksClient_gatherTooltipComponentsFromElements = new ReflectorMethod(
        ForgeHooksClient, "gatherTooltipComponentsFromElements"
    );
    public static ReflectorMethod ForgeHooksClient_getFogColor = new ReflectorMethod(ForgeHooksClient, "getFogColor");
    public static ReflectorMethod ForgeHooksClient_getArmorModel = new ReflectorMethod(ForgeHooksClient, "getArmorModel");
    public static ReflectorMethod ForgeHooksClient_getAtlastMetadataSections = new ReflectorMethod(ForgeHooksClient, "getAtlastMetadataSections");
    public static ReflectorMethod ForgeHooksClient_getFluidSprites = new ReflectorMethod(ForgeHooksClient, "getFluidSprites");
    public static ReflectorMethod ForgeHooksClient_getGuiFarPlane = new ReflectorMethod(ForgeHooksClient, "getGuiFarPlane");
    public static ReflectorMethod ForgeHooksClient_getShaderImportLocation = new ReflectorMethod(ForgeHooksClient, "getShaderImportLocation");
    public static ReflectorMethod ForgeHooksClient_isNameplateInRenderDistance = new ReflectorMethod(ForgeHooksClient, "isNameplateInRenderDistance");
    public static ReflectorMethod ForgeHooksClient_loadTextureAtlasSprite = new ReflectorMethod(ForgeHooksClient, "loadTextureAtlasSprite");
    public static ReflectorMethod ForgeHooksClient_loadSpriteContents = new ReflectorMethod(ForgeHooksClient, "loadSpriteContents");
    public static ReflectorMethod ForgeHooksClient_onCustomizeChatEvent = new ReflectorMethod(ForgeHooksClient, "onCustomizeChatEvent");
    public static ReflectorMethod ForgeHooksClient_onCustomizeDebugEvent = new ReflectorMethod(ForgeHooksClient, "onCustomizeDebugEvent");
    public static ReflectorMethod ForgeHooksClient_onExtractBlockOutline = new ReflectorMethod(ForgeHooksClient, "onExtractBlockOutline");
    public static ReflectorMethod ForgeHooksClient_onKeyInput = new ReflectorMethod(ForgeHooksClient, "onKeyInput");
    public static ReflectorMethod ForgeHooksClient_onModelBake = new ReflectorMethod(ForgeHooksClient, "onModelBake");
    public static ReflectorMethod ForgeHooksClient_onModifyBakingResult = new ReflectorMethod(ForgeHooksClient, "onModifyBakingResult");
    public static ReflectorMethod ForgeHooksClient_onRegisterPictureInPictureRenderers = new ReflectorMethod(
        ForgeHooksClient, "onRegisterPictureInPictureRenderers"
    );
    public static ReflectorMethod ForgeHooksClient_onRenderTooltipPre = new ReflectorMethod(ForgeHooksClient, "onRenderTooltipPre");
    public static ReflectorMethod ForgeHooksClient_onScreenCharTyped = new ReflectorMethod(ForgeHooksClient, "onScreenCharTyped");
    public static ReflectorMethod ForgeHooksClient_onScreenKeyPressed = new ReflectorMethod(ForgeHooksClient, "onScreenKeyPressed");
    public static ReflectorMethod ForgeHooksClient_onScreenKeyReleased = new ReflectorMethod(ForgeHooksClient, "onScreenKeyReleased");
    public static ReflectorMethod ForgeHooksClient_onTextureStitchedPost = new ReflectorMethod(ForgeHooksClient, "onTextureStitchedPost");
    public static ReflectorMethod ForgeHooksClient_renderBlockOverlay = new ReflectorMethod(ForgeHooksClient, "renderBlockOverlay");
    public static ReflectorMethod ForgeHooksClient_renderFireOverlay = new ReflectorMethod(ForgeHooksClient, "renderFireOverlay");
    public static ReflectorMethod ForgeHooksClient_renderWaterOverlay = new ReflectorMethod(ForgeHooksClient, "renderWaterOverlay");
    public static ReflectorMethod ForgeHooksClient_renderSpecificFirstPersonHand = new ReflectorMethod(ForgeHooksClient, "renderSpecificFirstPersonHand");
    public static ReflectorMethod ForgeHooksClient_setupFog = new ReflectorMethod(ForgeHooksClient, "setupFog");
    public static ReflectorMethod ForgeHooksClient_shouldCauseReequipAnimation = new ReflectorMethod(ForgeHooksClient, "shouldCauseReequipAnimation");
    public static ReflectorClass ForgeConfig = new ReflectorClass("net.minecraftforge.common.ForgeConfig");
    public static ReflectorField ForgeConfig_CLIENT = new ReflectorField(ForgeConfig, "CLIENT");
    public static ReflectorClass ForgeConfigSpec = new ReflectorClass("net.minecraftforge.common.ForgeConfigSpec");
    public static ReflectorField ForgeConfigSpec_childConfig = new ReflectorField(ForgeConfigSpec, "childConfig");
    public static ReflectorClass ForgeConfigSpec_ConfigValue = new ReflectorClass("net.minecraftforge.common.ForgeConfigSpec$ConfigValue");
    public static ReflectorField ForgeConfigSpec_ConfigValue_defaultSupplier = new ReflectorField(ForgeConfigSpec_ConfigValue, "defaultSupplier");
    public static ReflectorField ForgeConfigSpec_ConfigValue_spec = new ReflectorField(ForgeConfigSpec_ConfigValue, "spec");
    public static ReflectorMethod ForgeConfigSpec_ConfigValue_get = new ReflectorMethod(ForgeConfigSpec_ConfigValue, "get");
    public static ReflectorClass ChunkAccess = new ReflectorClass(ChunkAccess.class);
    public static ReflectorMethod ChunkAccess_getWorldForge = ChunkAccess.makeMethod("getWorldForge");
    public static ReflectorClass IForgeItemStack = new ReflectorClass("net.minecraftforge.common.extensions.IForgeItemStack");
    public static ReflectorMethod IForgeItemStack_getHighlightTip = new ReflectorMethod(IForgeItemStack, "getHighlightTip");
    public static ReflectorClass ForgeItemTags = new ReflectorClass(ItemTags.class);
    public static ReflectorMethod ForgeItemTags_create = ForgeItemTags.makeMethod("create", new Class[]{String.class, String.class});
    public static ReflectorClass ForgeKeyBinding = new ReflectorClass(KeyMapping.class);
    public static ReflectorMethod ForgeKeyBinding_setKeyConflictContext = new ReflectorMethod(ForgeKeyBinding, "setKeyConflictContext");
    public static ReflectorMethod ForgeKeyBinding_setKeyModifierAndCode = new ReflectorMethod(ForgeKeyBinding, "setKeyModifierAndCode");
    public static ReflectorMethod ForgeKeyBinding_getKeyModifier = new ReflectorMethod(ForgeKeyBinding, "getKeyModifier");
    public static ReflectorClass ForgeLayeredDraw = new ReflectorClass("net.minecraftforge.client.gui.overlay.ForgeLayeredDraw");
    public static ReflectorMethod ForgeLayeredDraw_init = ForgeLayeredDraw.makeMethod("init");
    public static ReflectorMethod ForgeLayeredDraw_beginRender = ForgeLayeredDraw.makeMethod("beginRender");
    public static ReflectorClass ForgeParticleResources = new ReflectorClass(ParticleResources.class);
    public static ReflectorMethod ForgeParticleResources_getProvider = ForgeParticleResources.makeMethod("getProvider");
    public static ReflectorClass IForgeBlockEntity = new ReflectorClass("net.minecraftforge.common.extensions.IForgeBlockEntity");
    public static ReflectorMethod IForgeBlockEntity_getRenderBoundingBox = new ReflectorMethod(IForgeBlockEntity, "getRenderBoundingBox");
    public static ReflectorMethod IForgeBlockEntity_hasCustomOutlineRendering = new ReflectorMethod(IForgeBlockEntity, "hasCustomOutlineRendering");
    public static ReflectorClass ForgeVersion = new ReflectorClass("net.minecraftforge.versions.forge.ForgeVersion");
    public static ReflectorMethod ForgeVersion_getVersion = ForgeVersion.makeMethod("getVersion");
    public static ReflectorClass FramePassManager = new ReflectorClass("net.minecraftforge.client.FramePassManager");
    public static ReflectorMethod FramePassManager_insertForgePasses = FramePassManager.makeMethod("insertForgePasses");
    public static ReflectorClass GeometryLoaderManager = new ReflectorClass("net.minecraftforge.client.model.geometry.GeometryLoaderManager");
    public static ReflectorMethod GeometryLoaderManager_init = GeometryLoaderManager.makeMethod("init");
    public static ReflectorClass ImmediateWindowHandler = new ReflectorClass("net.minecraftforge.fml.loading.ImmediateWindowHandler");
    public static ReflectorMethod ImmediateWindowHandler_positionWindow = ImmediateWindowHandler.makeMethod("positionWindow");
    public static ReflectorMethod ImmediateWindowHandler_setupMinecraftWindow = ImmediateWindowHandler.makeMethod("setupMinecraftWindow");
    public static ReflectorMethod ImmediateWindowHandler_updateFBSize = ImmediateWindowHandler.makeMethod("updateFBSize");
    public static ReflectorClass ItemDecoratorHandler = new ReflectorClass("net.minecraftforge.client.ItemDecoratorHandler");
    public static ReflectorMethod ItemDecoratorHandler_of = ItemDecoratorHandler.makeMethod("of", new Class[]{ItemStack.class});
    public static ReflectorMethod ItemDecoratorHandler_render = ItemDecoratorHandler.makeMethod("render");
    public static ReflectorClass KeyConflictContext = new ReflectorClass("net.minecraftforge.client.settings.KeyConflictContext");
    public static ReflectorField KeyConflictContext_IN_GAME = new ReflectorField(KeyConflictContext, "IN_GAME");
    public static ReflectorClass KeyModifier = new ReflectorClass("net.minecraftforge.client.settings.KeyModifier");
    public static ReflectorMethod KeyModifier_valueFromString = new ReflectorMethod(KeyModifier, "valueFromString");
    public static ReflectorField KeyModifier_NONE = new ReflectorField(KeyModifier, "NONE");
    public static ReflectorClass Launch = new ReflectorClass("net.minecraft.launchwrapper.Launch");
    public static ReflectorField Launch_blackboard = new ReflectorField(Launch, "blackboard");
    public static ReflectorClass MinecraftForge = new ReflectorClass("net.minecraftforge.common.MinecraftForge");
    public static ReflectorClass ModContainer = new ReflectorClass("net.minecraftforge.fml.ModContainer");
    public static ReflectorMethod ModContainer_getModId = new ReflectorMethod(ModContainer, "getModId");
    public static ReflectorClass ModList = new ReflectorClass("net.minecraftforge.fml.ModList");
    public static ReflectorField ModList_mods = ModList.makeField("mods");
    public static ReflectorMethod ModList_get = ModList.makeMethod("get");
    public static ReflectorClass ModListScreen = new ReflectorClass("net.minecraftforge.client.gui.ModListScreen");
    public static ReflectorConstructor ModListScreen_Constructor = new ReflectorConstructor(ModListScreen, new Class[]{Screen.class});
    public static ReflectorClass ModLoader = new ReflectorClass("net.minecraftforge.fml.ModLoader");
    public static ReflectorMethod ModLoader_get = ModLoader.makeMethod("get");
    public static ReflectorMethod ModLoader_postEvent = ModLoader.makeMethod("postEvent");
    public static ReflectorClass TitleScreenModUpdateIndicator = new ReflectorClass("net.minecraftforge.client.gui.TitleScreenModUpdateIndicator");
    public static ReflectorMethod TitleScreenModUpdateIndicator_init = TitleScreenModUpdateIndicator.makeMethod(
        "init", new Class[]{TitleScreen.class, Button.class}
    );
    public static ReflectorClass PartEntity = new ReflectorClass("net.minecraftforge.entity.PartEntity");
    public static ReflectorClass PlayLevelSoundEvent = new ReflectorClass("net.minecraftforge.event.PlayLevelSoundEvent");
    public static ReflectorMethod PlayLevelSoundEvent_getSound = new ReflectorMethod(PlayLevelSoundEvent, "getSound");
    public static ReflectorMethod PlayLevelSoundEvent_getSource = new ReflectorMethod(PlayLevelSoundEvent, "getSource");
    public static ReflectorMethod PlayLevelSoundEvent_getNewVolume = new ReflectorMethod(PlayLevelSoundEvent, "getNewVolume");
    public static ReflectorMethod PlayLevelSoundEvent_getNewPitch = new ReflectorMethod(PlayLevelSoundEvent, "getNewPitch");
    public static ReflectorClass RenderBlockScreenEffectEvent_OverlayType = new ReflectorClass(
        "net.minecraftforge.client.event.RenderBlockScreenEffectEvent$OverlayType"
    );
    public static ReflectorField RenderBlockScreenEffectEvent_OverlayType_BLOCK = new ReflectorField(RenderBlockScreenEffectEvent_OverlayType, "BLOCK");
    public static ReflectorClass CustomizeGuiOverlayEvent_BossEventProgress = new ReflectorClass(
        "net.minecraftforge.client.event.CustomizeGuiOverlayEvent$BossEventProgress"
    );
    public static ReflectorMethod CustomizeGuiOverlayEvent_BossEventProgress_getIncrement = CustomizeGuiOverlayEvent_BossEventProgress.makeMethod("getIncrement");
    public static ReflectorClass RenderNameTagEvent = new ReflectorClass("net.minecraftforge.client.event.RenderNameTagEvent");
    public static ReflectorMethod RenderNameTagEvent_getContent = new ReflectorMethod(RenderNameTagEvent, "getContent");
    public static ReflectorClass RenderTooltipEvent = new ReflectorClass("net.minecraftforge.client.event.RenderTooltipEvent");
    public static ReflectorMethod RenderTooltipEvent_getFont = RenderTooltipEvent.makeMethod("getFont");
    public static ReflectorMethod RenderTooltipEvent_getX = RenderTooltipEvent.makeMethod("getX");
    public static ReflectorMethod RenderTooltipEvent_getY = RenderTooltipEvent.makeMethod("getY");
    public static ReflectorClass RenderTooltipEvent_Pre = new ReflectorClass("net.minecraftforge.client.event.RenderTooltipEvent$Pre");
    public static ReflectorMethod RenderTooltipEvent_Pre_getBackground = RenderTooltipEvent_Pre.makeMethod("getBackground");
    public static ReflectorClass ScreenshotEvent = new ReflectorClass("net.minecraftforge.client.event.ScreenshotEvent");
    public static ReflectorField ScreenshotEvent_BUS = new ReflectorField(ScreenshotEvent, "BUS");
    public static ReflectorConstructor ScreenshotEvent_Constructor = new ReflectorConstructor(ScreenshotEvent, new Class[]{NativeImage.class, File.class});
    public static ReflectorMethod ScreenshotEvent_getCancelMessage = new ReflectorMethod(ScreenshotEvent, "getCancelMessage");
    public static ReflectorMethod ScreenshotEvent_getScreenshotFile = new ReflectorMethod(ScreenshotEvent, "getScreenshotFile");
    public static ReflectorMethod ScreenshotEvent_getResultMessage = new ReflectorMethod(ScreenshotEvent, "getResultMessage");
    public static ReflectorClass ServerLifecycleHooks = new ReflectorClass("net.minecraftforge.server.ServerLifecycleHooks");
    public static ReflectorMethod ServerLifecycleHooks_handleServerAboutToStart = new ReflectorMethod(ServerLifecycleHooks, "handleServerAboutToStart");
    public static ReflectorMethod ServerLifecycleHooks_handleServerStarting = new ReflectorMethod(ServerLifecycleHooks, "handleServerStarting");
    public static ReflectorClass TerrainParticle = new ReflectorClass(TerrainParticle.class);
    public static ReflectorMethod TerrainParticle_updateSprite = TerrainParticle.makeMethod("updateSprite");
    private static boolean logVanilla = registerResolvable("*** Reflector Vanilla ***");
    public static ReflectorClass ArrowRenderer = new ReflectorClass(ArrowRenderer.class);
    public static ReflectorField ArrowRenderer_model = ArrowRenderer.makeField(ArrowModel.class);
    public static ReflectorClass BannerBlockEntity = new ReflectorClass(BannerBlockEntity.class);
    public static ReflectorField BannerBlockEntity_customName = BannerBlockEntity.makeField(Component.class, 1);
    public static ReflectorClass BaseContainerBlockEntity = new ReflectorClass(BaseContainerBlockEntity.class);
    public static ReflectorField BaseContainerBlockEntity_customName = BaseContainerBlockEntity.makeField(Component.class);
    public static ReflectorClass BellRenderer = new ReflectorClass(BellRenderer.class);
    public static ReflectorField BellRenderer_model = BellRenderer.makeField(BellModel.class);
    public static ReflectorClass Biome_ClimateSettings = new ReflectorClass("net.minecraft.world.level.biome.Biome$ClimateSettings");
    public static ReflectorField Biome_ClimateSettings_downfall = Biome_ClimateSettings.makeField("downfall");
    public static ReflectorClass Biome = new ReflectorClass(Biome.class);
    public static ReflectorField Biome_climateSettings = Biome.makeField("climateSettings");
    public static ReflectorClass BlockBehaviour_Properties = new ReflectorClass(Properties.class);
    public static ReflectorField BlockBehaviour_Properties_emissiveRendering = BlockBehaviour_Properties.makeField("emissiveRendering");
    public static ReflectorClass CopperGolemStatueBlockRenderer = new ReflectorClass(CopperGolemStatueBlockRenderer.class);
    public static ReflectorField CopperGolemStatueBlockRenderer_models = CopperGolemStatueBlockRenderer.makeField(Map.class);
    public static ReflectorClass CowRenderer = new ReflectorClass(CowRenderer.class);
    public static ReflectorField CowRenderer_models = CowRenderer.makeField(Map.class);
    public static ReflectorClass DrownedOuterLayer = new ReflectorClass(DrownedOuterLayer.class);
    public static ReflectorField DrownedOuterLayer_adultModel = new ReflectorField(DrownedOuterLayer, DrownedModel.class, 0);
    public static ReflectorField DrownedOuterLayer_babyModel = new ReflectorField(DrownedOuterLayer, DrownedModel.class, 1);
    public static ReflectorClass Enchantments = new ReflectorClass(Enchantments.class);
    public static ReflectorFields Enchantments_ResourceKeys = new ReflectorFields(Enchantments, ResourceKey.class, -1);
    public static ReflectorClass EntityItem = new ReflectorClass(ItemEntity.class);
    public static ReflectorField EntityItem_ITEM = new ReflectorField(EntityItem, EntityDataAccessor.class);
    public static ReflectorClass EnderDragonRenderer = new ReflectorClass(EnderDragonRenderer.class);
    public static ReflectorField EnderDragonRenderer_model = new ReflectorField(EnderDragonRenderer, EnderDragonModel.class);
    public static ReflectorClass GuiEnchantment = new ReflectorClass(EnchantmentScreen.class);
    public static ReflectorField GuiEnchantment_bookModel = new ReflectorField(GuiEnchantment, BookModel.class);
    public static ReflectorClass ItemStack = new ReflectorClass(ItemStack.class);
    public static ReflectorField ItemStack_components = ItemStack.makeField(PatchedDataComponentMap.class);
    public static ReflectorClass LlamaDecorLayer = new ReflectorClass(LlamaDecorLayer.class);
    public static ReflectorField LlamaDecorLayer_adultModel = new ReflectorField(LlamaDecorLayer, LlamaModel.class, 0);
    public static ReflectorField LlamaDecorLayer_babyModel = new ReflectorField(LlamaDecorLayer, LlamaModel.class, 1);
    public static ReflectorClass LivingEntityEmissiveLayer = new ReflectorClass(LivingEntityEmissiveLayer.class);
    public static ReflectorField LivingEntityEmissiveLayer_model = new ReflectorField(LivingEntityEmissiveLayer, EntityModel.class);
    public static ReflectorClass Minecraft = new ReflectorClass(Minecraft.class);
    public static ReflectorField Minecraft_debugUpdateTime = new ReflectorField(
        new FieldLocatorTypes(Minecraft.class, new Class[]{long.class}, long.class, new Class[]{int.class}, "debugUpdateTime")
    );
    public static ReflectorField Minecraft_fontResourceManager = new ReflectorField(Minecraft, FontManager.class);
    public static ReflectorClass RenderEnderCrystal = new ReflectorClass(EndCrystalRenderer.class);
    public static ReflectorField RenderEnderCrystal_model = new ReflectorField(RenderEnderCrystal, EndCrystalModel.class);
    public static ReflectorClass RenderLeashKnot = new ReflectorClass(LeashKnotRenderer.class);
    public static ReflectorField RenderLeashKnot_leashKnotModel = new ReflectorField(RenderLeashKnot, LeashKnotModel.class);
    public static ReflectorClass OptiFineResourceLocator = ReflectorForge.getReflectorClassOptiFineResourceLocator();
    public static ReflectorMethod OptiFineResourceLocator_getOptiFineResourceStream = new ReflectorMethod(OptiFineResourceLocator, "getOptiFineResourceStream");
    public static ReflectorClass Potion = new ReflectorClass(Potion.class);
    public static ReflectorField Potion_baseName = Potion.makeField(String.class);
    public static ReflectorClass RenderArmorStand = new ReflectorClass(ArmorStandRenderer.class);
    public static ReflectorField RenderArmorStand_bigModel = new ReflectorField(RenderArmorStand, ArmorStandArmorModel.class, 0);
    public static ReflectorField RenderArmorStand_smallModel = new ReflectorField(RenderArmorStand, ArmorStandArmorModel.class, 1);
    public static ReflectorClass RenderBoat = new ReflectorClass(BoatRenderer.class);
    public static ReflectorField RenderBoat_patchModel = new ReflectorField(RenderBoat, Model.Simple.class);
    public static ReflectorField RenderBoat_model = new ReflectorField(RenderBoat, EntityModel.class);
    public static ReflectorClass RenderEvokerFangs = new ReflectorClass(EvokerFangsRenderer.class);
    public static ReflectorField RenderEvokerFangs_model = new ReflectorField(RenderEvokerFangs, EvokerFangsModel.class);
    public static ReflectorClass RenderLlamaSpit = new ReflectorClass(LlamaSpitRenderer.class);
    public static ReflectorField RenderLlamaSpit_model = new ReflectorField(RenderLlamaSpit, LlamaSpitModel.class);
    public static ReflectorClass RenderPufferfish = new ReflectorClass(PufferfishRenderer.class);
    public static ReflectorField RenderPufferfish_modelSmall = new ReflectorField(RenderPufferfish, EntityModel.class, 0);
    public static ReflectorField RenderPufferfish_modelMedium = new ReflectorField(RenderPufferfish, EntityModel.class, 1);
    public static ReflectorField RenderPufferfish_modelBig = new ReflectorField(RenderPufferfish, EntityModel.class, 2);
    public static ReflectorClass RenderMinecart = new ReflectorClass(AbstractMinecartRenderer.class);
    public static ReflectorField RenderMinecart_modelMinecart = new ReflectorField(RenderMinecart, MinecartModel.class);
    public static ReflectorClass RenderRaft = new ReflectorClass(RaftRenderer.class);
    public static ReflectorField RenderRaft_model = new ReflectorField(RenderRaft, EntityModel.class);
    public static ReflectorClass RopesLayer = new ReflectorClass(RopesLayer.class);
    public static ReflectorField RopesLayer_ropes = new ReflectorField(RopesLayer, RenderType.class);
    public static ReflectorField RopesLayer_adultModel = new ReflectorField(RopesLayer, HappyGhastModel.class, 0);
    public static ReflectorField RopesLayer_babyModel = new ReflectorField(RopesLayer, HappyGhastModel.class, 1);
    public static ReflectorClass SheepWoolUndercoatLayer = new ReflectorClass(SheepWoolUndercoatLayer.class);
    public static ReflectorField SheepWoolUndercoatLayer_model = new ReflectorField(SheepWoolUndercoatLayer, EntityModel.class, 0);
    public static ReflectorClass SalmonRenderer = new ReflectorClass(SalmonRenderer.class);
    public static ReflectorField SalmonRenderer_modelSmall = new ReflectorField(SalmonRenderer, SalmonModel.class, 0);
    public static ReflectorField SalmonRenderer_modelMedium = new ReflectorField(SalmonRenderer, SalmonModel.class, 1);
    public static ReflectorField SalmonRenderer_modelLarge = new ReflectorField(SalmonRenderer, SalmonModel.class, 2);
    public static ReflectorClass RenderShulkerBullet = new ReflectorClass(ShulkerBulletRenderer.class);
    public static ReflectorField RenderShulkerBullet_model = new ReflectorField(RenderShulkerBullet, ShulkerBulletModel.class);
    public static ReflectorClass RenderTrident = new ReflectorClass(ThrownTridentRenderer.class);
    public static ReflectorField RenderTrident_modelTrident = new ReflectorField(RenderTrident, TridentModel.class);
    public static ReflectorClass RenderTropicalFish = new ReflectorClass(TropicalFishRenderer.class);
    public static ReflectorField RenderTropicalFish_modelA = new ReflectorField(RenderTropicalFish, EntityModel.class, 0);
    public static ReflectorField RenderTropicalFish_modelB = new ReflectorField(RenderTropicalFish, EntityModel.class, 1);
    public static ReflectorClass RenderWindCharge = new ReflectorClass(WindChargeRenderer.class);
    public static ReflectorField RenderWindCharge_model = new ReflectorField(RenderWindCharge, WindChargeModel.class);
    public static ReflectorClass TropicalFishPatternLayer = new ReflectorClass(TropicalFishPatternLayer.class);
    public static ReflectorField TropicalFishPatternLayer_modelA = new ReflectorField(TropicalFishPatternLayer, TropicalFishSmallModel.class);
    public static ReflectorField TropicalFishPatternLayer_modelB = new ReflectorField(TropicalFishPatternLayer, TropicalFishLargeModel.class);
    public static ReflectorClass RenderWitherSkull = new ReflectorClass(WitherSkullRenderer.class);
    public static ReflectorField RenderWitherSkull_model = new ReflectorField(RenderWitherSkull, SkullModel.class);
    public static ReflectorClass TileEntityBannerRenderer = new ReflectorClass(BannerRenderer.class);
    public static ReflectorFields TileEntityBannerRenderer_bannerModels = new ReflectorFields(TileEntityBannerRenderer, BannerModel.class, 2);
    public static ReflectorFields TileEntityBannerRenderer_bannerFlagModels = new ReflectorFields(TileEntityBannerRenderer, BannerFlagModel.class, 2);
    public static ReflectorClass TileEntityBeacon = new ReflectorClass(BeaconBlockEntity.class);
    public static ReflectorField TileEntityBeacon_customName = new ReflectorField(TileEntityBeacon, Component.class);
    public static ReflectorField TileEntityBeacon_levels = new ReflectorField(
        new FieldLocatorTypes(BeaconBlockEntity.class, new Class[]{List.class}, int.class, new Class[]{int.class}, "BeaconBlockEntity.levels")
    );
    public static ReflectorClass TileEntityChestRenderer = new ReflectorClass(ChestRenderer.class);
    public static ReflectorField TileEntityChestRenderer_singleModel = new ReflectorField(TileEntityChestRenderer, ChestModel.class, 0);
    public static ReflectorField TileEntityChestRenderer_doubleLeftModel = new ReflectorField(TileEntityChestRenderer, ChestModel.class, 1);
    public static ReflectorField TileEntityChestRenderer_doubleRightModel = new ReflectorField(TileEntityChestRenderer, ChestModel.class, 2);
    public static ReflectorClass TileEntityConduitRenderer = new ReflectorClass(ConduitRenderer.class);
    public static ReflectorFields TileEntityConduitRenderer_modelRenderers = new ReflectorFields(TileEntityConduitRenderer, ModelPart.class, 4);
    public static ReflectorClass TileEntityDecoratedPotRenderer = new ReflectorClass(DecoratedPotRenderer.class);
    public static ReflectorFields TileEntityDecoratedPotRenderer_modelRenderers = new ReflectorFields(TileEntityDecoratedPotRenderer, ModelPart.class, 7);
    public static ReflectorClass TileEntityEnchantmentTableRenderer = new ReflectorClass(EnchantTableRenderer.class);
    public static ReflectorField TileEntityEnchantmentTableRenderer_modelBook = new ReflectorField(TileEntityEnchantmentTableRenderer, BookModel.class);
    public static ReflectorClass TileEntityLecternRenderer = new ReflectorClass(LecternRenderer.class);
    public static ReflectorField TileEntityLecternRenderer_modelBook = new ReflectorField(TileEntityLecternRenderer, BookModel.class);
    public static ReflectorClass TileEntityShulkerBoxRenderer = new ReflectorClass(ShulkerBoxRenderer.class);
    public static ReflectorField TileEntityShulkerBoxRenderer_model = new ReflectorField(TileEntityShulkerBoxRenderer, ShulkerBoxRenderer.ShulkerBoxModel.class);
    public static ReflectorClass TransientEntitySectionManager = new ReflectorClass(TransientEntitySectionManager.class);
    public static ReflectorField TransientEntitySectionManager_sectionStorage = TransientEntitySectionManager.makeField("sectionStorage");
    public static ReflectorClass VillagerProfessionLayer = new ReflectorClass(VillagerProfessionLayer.class);
    public static ReflectorField VillagerProfessionLayer_adultModel = new ReflectorField(VillagerProfessionLayer, EntityModel.class, 0);
    public static ReflectorField VillagerProfessionLayer_babyModel = new ReflectorField(VillagerProfessionLayer, EntityModel.class, 1);
    public static ReflectorClass WolfArmorLayer = new ReflectorClass(WolfArmorLayer.class);
    public static ReflectorField WolfArmorLayer_adultModel = new ReflectorField(WolfArmorLayer, WolfModel.class, 0);
    public static ReflectorClass ZombieNautilusRenderer = new ReflectorClass(ZombieNautilusRenderer.class);
    public static ReflectorField ZombieNautilusRenderer_models = ZombieNautilusRenderer.makeField(Map.class);

    public static void callVoid(ReflectorMethod refMethod, Object... params) {
        try {
            Method m = refMethod.getTargetMethod();
            if (m == null) {
                return;
            }

            m.invoke(null, params);
        } catch (Throwable e) {
            handleException(e, null, refMethod, params);
        }
    }

    public static boolean callBoolean(ReflectorMethod refMethod, Object... params) {
        try {
            Method method = refMethod.getTargetMethod();
            if (method == null) {
                return false;
            }

            Boolean retVal = (Boolean)method.invoke(null, params);
            return retVal;
        } catch (Throwable e) {
            handleException(e, null, refMethod, params);
            return false;
        }
    }

    public static int callInt(ReflectorMethod refMethod, Object... params) {
        try {
            Method method = refMethod.getTargetMethod();
            if (method == null) {
                return 0;
            }

            Integer retVal = (Integer)method.invoke(null, params);
            return retVal;
        } catch (Throwable e) {
            handleException(e, null, refMethod, params);
            return 0;
        }
    }

    public static long callLong(ReflectorMethod refMethod, Object... params) {
        try {
            Method method = refMethod.getTargetMethod();
            if (method == null) {
                return 0L;
            }

            Long retVal = (Long)method.invoke(null, params);
            return retVal;
        } catch (Throwable e) {
            handleException(e, null, refMethod, params);
            return 0L;
        }
    }

    public static float callFloat(ReflectorMethod refMethod, Object... params) {
        try {
            Method method = refMethod.getTargetMethod();
            if (method == null) {
                return 0.0F;
            }

            Float retVal = (Float)method.invoke(null, params);
            return retVal;
        } catch (Throwable e) {
            handleException(e, null, refMethod, params);
            return 0.0F;
        }
    }

    public static double callDouble(ReflectorMethod refMethod, Object... params) {
        try {
            Method method = refMethod.getTargetMethod();
            if (method == null) {
                return 0.0;
            }

            Double retVal = (Double)method.invoke(null, params);
            return retVal;
        } catch (Throwable e) {
            handleException(e, null, refMethod, params);
            return 0.0;
        }
    }

    public static String callString(ReflectorMethod refMethod, Object... params) {
        try {
            Method method = refMethod.getTargetMethod();
            return method == null ? null : (String)method.invoke(null, params);
        } catch (Throwable e) {
            handleException(e, null, refMethod, params);
            return null;
        }
    }

    public static Object call(ReflectorMethod refMethod, Object... params) {
        try {
            Method method = refMethod.getTargetMethod();
            return method == null ? null : method.invoke(null, params);
        } catch (Throwable e) {
            handleException(e, null, refMethod, params);
            return null;
        }
    }

    public static void callVoid(Object obj, ReflectorMethod refMethod, Object... params) {
        try {
            if (obj == null) {
                return;
            }

            Method method = refMethod.getTargetMethod();
            if (method == null) {
                return;
            }

            method.invoke(obj, params);
        } catch (Throwable e) {
            handleException(e, obj, refMethod, params);
        }
    }

    public static boolean callBoolean(Object obj, ReflectorMethod refMethod, Object... params) {
        try {
            Method method = refMethod.getTargetMethod();
            if (method == null) {
                return false;
            }

            Boolean retVal = (Boolean)method.invoke(obj, params);
            return retVal;
        } catch (Throwable e) {
            handleException(e, obj, refMethod, params);
            return false;
        }
    }

    public static int callInt(Object obj, ReflectorMethod refMethod, Object... params) {
        try {
            Method method = refMethod.getTargetMethod();
            if (method == null) {
                return 0;
            }

            Integer retVal = (Integer)method.invoke(obj, params);
            return retVal;
        } catch (Throwable e) {
            handleException(e, obj, refMethod, params);
            return 0;
        }
    }

    public static long callLong(Object obj, ReflectorMethod refMethod, Object... params) {
        try {
            Method method = refMethod.getTargetMethod();
            if (method == null) {
                return 0L;
            }

            Long retVal = (Long)method.invoke(obj, params);
            return retVal;
        } catch (Throwable e) {
            handleException(e, obj, refMethod, params);
            return 0L;
        }
    }

    public static float callFloat(Object obj, ReflectorMethod refMethod, Object... params) {
        try {
            Method method = refMethod.getTargetMethod();
            if (method == null) {
                return 0.0F;
            }

            Float retVal = (Float)method.invoke(obj, params);
            return retVal;
        } catch (Throwable e) {
            handleException(e, obj, refMethod, params);
            return 0.0F;
        }
    }

    public static double callDouble(Object obj, ReflectorMethod refMethod, Object... params) {
        try {
            Method method = refMethod.getTargetMethod();
            if (method == null) {
                return 0.0;
            }

            Double retVal = (Double)method.invoke(obj, params);
            return retVal;
        } catch (Throwable e) {
            handleException(e, obj, refMethod, params);
            return 0.0;
        }
    }

    public static String callString(Object obj, ReflectorMethod refMethod, Object... params) {
        try {
            Method method = refMethod.getTargetMethod();
            return method == null ? null : (String)method.invoke(obj, params);
        } catch (Throwable e) {
            handleException(e, obj, refMethod, params);
            return null;
        }
    }

    public static Object call(Object obj, ReflectorMethod refMethod, Object... params) {
        try {
            Method method = refMethod.getTargetMethod();
            return method == null ? null : method.invoke(obj, params);
        } catch (Throwable e) {
            handleException(e, obj, refMethod, params);
            return null;
        }
    }

    public static Object getFieldValue(ReflectorField refField) {
        return getFieldValue(null, refField);
    }

    public static Object getFieldValue(Object obj, ReflectorField refField) {
        try {
            Field field = refField.getTargetField();
            return field == null ? null : field.get(obj);
        } catch (Throwable e) {
            Log.error("", e);
            return null;
        }
    }

    public static boolean getFieldValueBoolean(Object obj, ReflectorField refField, boolean def) {
        try {
            Field field = refField.getTargetField();
            return field == null ? def : field.getBoolean(obj);
        } catch (Throwable e) {
            Log.error("", e);
            return def;
        }
    }

    public static Object getFieldValue(ReflectorFields refFields, int index) {
        ReflectorField refField = refFields.getReflectorField(index);
        return refField == null ? null : getFieldValue(refField);
    }

    public static Object getFieldValue(Object obj, ReflectorFields refFields, int index) {
        ReflectorField refField = refFields.getReflectorField(index);
        return refField == null ? null : getFieldValue(obj, refField);
    }

    public static float getFieldValueFloat(Object obj, ReflectorField refField, float def) {
        try {
            Field field = refField.getTargetField();
            return field == null ? def : field.getFloat(obj);
        } catch (Throwable e) {
            Log.error("", e);
            return def;
        }
    }

    public static int getFieldValueInt(ReflectorField refField, int def) {
        return getFieldValueInt(null, refField, def);
    }

    public static int getFieldValueInt(Object obj, ReflectorField refField, int def) {
        try {
            Field field = refField.getTargetField();
            return field == null ? def : field.getInt(obj);
        } catch (Throwable e) {
            Log.error("", e);
            return def;
        }
    }

    public static long getFieldValueLong(ReflectorField refField, int def) {
        return getFieldValueLong(null, refField, def);
    }

    public static long getFieldValueLong(Object obj, ReflectorField refField, long def) {
        try {
            Field field = refField.getTargetField();
            return field == null ? def : field.getLong(obj);
        } catch (Throwable e) {
            Log.error("", e);
            return def;
        }
    }

    public static boolean setFieldValue(ReflectorField refField, Object value) {
        return setFieldValue(null, refField, value);
    }

    public static boolean setFieldValue(Object obj, ReflectorFields refFields, int index, Object value) {
        ReflectorField refField = refFields.getReflectorField(index);
        if (refField == null) {
            return false;
        }

        setFieldValue(obj, refField, value);
        return true;
    }

    public static boolean setFieldValue(Object obj, ReflectorField refField, Object value) {
        try {
            Field field = refField.getTargetField();
            if (field == null) {
                return false;
            }

            field.set(obj, value);
            return true;
        } catch (Throwable e) {
            handleException(e, obj, refField, value);
            return false;
        }
    }

    public static boolean setFieldValueInt(ReflectorField refField, int value) {
        return setFieldValueInt(null, refField, value);
    }

    public static boolean setFieldValueInt(Object obj, ReflectorField refField, int value) {
        try {
            Field field = refField.getTargetField();
            if (field == null) {
                return false;
            }

            field.setInt(obj, value);
            return true;
        } catch (Throwable e) {
            handleException(e, obj, refField, value);
            return false;
        }
    }

    public static boolean postForgeBusEvent(ReflectorField fieldBus, ReflectorConstructor constr, Object... params) {
        Object event = newInstance(constr, params);
        return event == null ? false : postForgeBusEvent(fieldBus, event);
    }

    public static boolean postForgeBusEvent(ReflectorField fieldBus, Object event) {
        if (event == null) {
            return false;
        } else {
            Object eventBus = getFieldValue(fieldBus);
            if (eventBus == null) {
                return false;
            } else {
                return !(call(eventBus, EventBus_post, event) instanceof Boolean retBool) ? false : retBool;
            }
        }
    }

    public static Object newInstance(ReflectorConstructor constr, Object... params) {
        Constructor c = constr.getTargetConstructor();
        if (c == null) {
            return null;
        }

        try {
            return c.newInstance(params);
        } catch (Throwable e) {
            handleException(e, constr, params);
            return null;
        }
    }

    public static boolean matchesTypes(Class[] pTypes, Class[] cTypes) {
        if (pTypes.length != cTypes.length) {
            return false;
        }

        for (int i = 0; i < cTypes.length; i++) {
            Class pType = pTypes[i];
            Class cType = cTypes[i];
            if (pType != cType) {
                return false;
            }
        }

        return true;
    }

    private static void dbgCall(boolean isStatic, String callType, ReflectorMethod refMethod, Object[] params, Object retVal) {
        String className = refMethod.getTargetMethod().getDeclaringClass().getName();
        String methodName = refMethod.getTargetMethod().getName();
        String staticStr = "";
        if (isStatic) {
            staticStr = " static";
        }

        Log.dbg(callType + staticStr + " " + className + "." + methodName + "(" + ArrayUtils.arrayToString(params) + ") => " + retVal);
    }

    private static void dbgCallVoid(boolean isStatic, String callType, ReflectorMethod refMethod, Object[] params) {
        String className = refMethod.getTargetMethod().getDeclaringClass().getName();
        String methodName = refMethod.getTargetMethod().getName();
        String staticStr = "";
        if (isStatic) {
            staticStr = " static";
        }

        Log.dbg(callType + staticStr + " " + className + "." + methodName + "(" + ArrayUtils.arrayToString(params) + ")");
    }

    private static void dbgFieldValue(boolean isStatic, String accessType, ReflectorField refField, Object val) {
        String className = refField.getTargetField().getDeclaringClass().getName();
        String fieldName = refField.getTargetField().getName();
        String staticStr = "";
        if (isStatic) {
            staticStr = " static";
        }

        Log.dbg(accessType + staticStr + " " + className + "." + fieldName + " => " + val);
    }

    private static void handleException(Throwable e, Object obj, ReflectorField refField, Object value) {
        Log.warn("*** Exception setting field value ***");
        Log.warn("Field deactivated: " + refField.getTargetField());
        refField.deactivate();
        if (e instanceof IllegalArgumentException) {
            Log.warn("*** IllegalArgumentException ***");
            Log.warn("Field: " + refField.getTargetField());
            Log.warn("Object: " + obj);
            Log.warn("Value class: " + getClass(value));
            Log.warn("Value: " + value);
        }

        Log.warn("", e);
    }

    private static void handleException(Throwable e, Object obj, ReflectorMethod refMethod, Object[] params) {
        if (e instanceof InvocationTargetException) {
            if (e.getCause() instanceof RuntimeException causeRuntime) {
                throw causeRuntime;
            } else {
                Log.error("", e);
            }
        } else {
            Log.warn("*** Exception outside of method ***");
            Log.warn("Method deactivated: " + refMethod.getTargetMethod());
            refMethod.deactivate();
            if (e instanceof IllegalArgumentException) {
                Log.warn("*** IllegalArgumentException ***");
                Log.warn("Method: " + refMethod.getTargetMethod());
                Log.warn("Object: " + obj);
                Log.warn("Parameter classes: " + ArrayUtils.arrayToString(getClasses(params)));
                Log.warn("Parameter values : " + ArrayUtils.arrayToString(params));
            }

            Log.warn("", e);
        }
    }

    private static void handleException(Throwable e, ReflectorConstructor refConstr, Object[] params) {
        if (e instanceof InvocationTargetException) {
            Log.error("", e);
        } else {
            Log.warn("*** Exception outside of constructor ***");
            Log.warn("Constructor deactivated: " + refConstr.getTargetConstructor());
            refConstr.deactivate();
            if (e instanceof IllegalArgumentException) {
                Log.warn("*** IllegalArgumentException ***");
                Log.warn("Constructor: " + refConstr.getTargetConstructor());
                Log.warn("Parameter classes: " + ArrayUtils.arrayToString(getClasses(params)));
                Log.warn("Parameters: " + ArrayUtils.arrayToString(params));
            }

            Log.warn("", e);
        }
    }

    private static Object[] getClasses(Object[] objs) {
        if (objs == null) {
            return new Class[0];
        }

        Class[] classes = new Class[objs.length];

        for (int i = 0; i < classes.length; i++) {
            Object obj = objs[i];
            classes[i] = getClass(obj);
        }

        return classes;
    }

    private static Class getClass(Object obj) {
        return obj == null ? null : obj.getClass();
    }

    private static ReflectorField[] getReflectorFields(ReflectorClass parentClass, Class fieldType, int count) {
        ReflectorField[] rfs = new ReflectorField[count];

        for (int i = 0; i < rfs.length; i++) {
            rfs[i] = new ReflectorField(parentClass, fieldType, i);
        }

        return rfs;
    }

    private static boolean registerResolvable(final String str) {
        String msg = str;
        IResolvable ir = new IResolvable() {
            @Override
            public void resolve() {
                Reflector.LOGGER.info("[OptiFine] " + str);
            }
        };
        ReflectorResolver.register(ir);
        return true;
    }
}
