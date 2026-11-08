package net.optifine.entity.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SkullBlock.Type;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.optifine.Config;
import net.optifine.IRandomEntity;
import net.optifine.Log;
import net.optifine.RandomEntities;
import net.optifine.RandomEntityContext;
import net.optifine.RandomEntityProperties;
import net.optifine.entity.model.anim.IModelRendererVariable;
import net.optifine.entity.model.anim.ModelResolver;
import net.optifine.entity.model.anim.ModelUpdater;
import net.optifine.entity.model.anim.ModelVariableUpdater;
import net.optifine.reflect.Reflector;
import net.optifine.util.ArrayUtils;
import net.optifine.util.DebugUtils;
import net.optifine.util.Either;
import net.optifine.util.StrUtils;

public class CustomEntityModels {
   private static boolean active = false;
   private static Map<EntityType, RandomEntityProperties<IEntityRenderer>> mapEntityProperties = new HashMap<>();
   private static Map<BlockEntityType, RandomEntityProperties<IEntityRenderer>> mapBlockEntityProperties = new HashMap<>();
   private static int matchingRuleIndex;
   private static Renderers originalRenderers;
   private static List<BlockEntityType> customBlockEntityTypes = new ArrayList<>();
   private static boolean debugModels = Boolean.getBoolean("cem.debug.models");
   public static final String PREFIX_OPTIFINE_CEM = "optifine/cem/";
   public static final String SUFFIX_JEM = ".jem";
   public static final String SUFFIX_PROPERTIES = ".properties";

   public static void update() {
      if (originalRenderers == null) {
         originalRenderers = Renderers.collectRenderers();
      }

      active = false;
      originalRenderers.restoreRenderers();
      CustomStaticModels.clear();
      customBlockEntityTypes.clear();
      BlockEntityRenderer.CACHED_TYPES.clear();
      if (Minecraft.getInstance().level != null) {
         for (Entity entity : Minecraft.getInstance().level.entitiesForRendering()) {
            Map modelVariables = entity.getEntityData().modelVariables;
            if (modelVariables != null) {
               modelVariables.clear();
            }
         }
      }

      mapEntityProperties.clear();
      mapBlockEntityProperties.clear();
      if (Config.isCustomEntityModels()) {
         RandomEntityContext.Models context = new RandomEntityContext.Models();
         RendererCache rendererCache = context.getRendererCache();
         Map<String, Identifier> modelLocations = getModelLocations();

         for (String name : modelLocations.keySet()) {
            Identifier loc = modelLocations.get(name);
            Config.dbg("CustomEntityModel: " + name + ", model: " + loc.getPath());
            IEntityRenderer rc = parseEntityRender(name, loc, rendererCache, 0);
            if (rc != null) {
               if (rc instanceof EntityRenderer er) {
                  EntityType type = rc.getType().getLeft().get();
                  getEntityRenderMap().put(type, er);
                  rendererCache.put(type, 0, er);
               } else if (rc instanceof BlockEntityRenderer ber) {
                  BlockEntityType type = rc.getType().getRight().get();
                  getBlockEntityRenderMap().put(type, ber);
                  rendererCache.put(type, 0, ber);
                  customBlockEntityTypes.add(type);
               } else {
                  if (!(rc instanceof VirtualEntityRenderer ver)) {
                     Config.warn("Unknown renderer type: " + rc.getClass().getName());
                     continue;
                  }

                  ver.register();
               }

               active = true;
            }
         }

         updateRandomProperties(context);
      }
   }

   private static void updateRandomProperties(RandomEntityContext.Models context) {
      String[] prefixes = new String[]{"optifine/cem/"};
      prefixes = new String[]{".jem", ".properties"};
      String[] names = CustomModelRegistry.getModelNames();

      for (int i = 0; i < names.length; i++) {
         String name = names[i];
         String[] aliases = CustomModelRegistry.getAliases(name);
         ModelAdapter modelAdapter = CustomModelRegistry.getModelAdapter(name);
         Either<EntityType, BlockEntityType> type = modelAdapter.getType();
         String[] fileNames = makeNameVariants(name, aliases);
         RandomEntityProperties props = makeProperties(name, fileNames, context);
         if (props != null) {
            if (type != null && type.getLeft().isPresent()) {
               mapEntityProperties.put(type.getLeft().get(), props);
            }

            if (type != null && type.getRight().isPresent()) {
               mapBlockEntityProperties.put(type.getRight().get(), props);
            }
         }
      }
   }

   private static String[] makeNameVariants(String baseName, String[] aliases) {
      List<String> variants = new ArrayList<>();
      aliases = ArrayUtils.normalize(aliases);
      String[] names = (String[])ArrayUtils.addObjectToArray(aliases, baseName, 0);

      for (int i = 0; i < names.length; i++) {
         String name = names[i];
         variants.add(name);
         variants.add(name + "/" + name);
      }

      return variants.toArray(new String[variants.size()]);
   }

   private static RandomEntityProperties makeProperties(String name, String[] fileNames, RandomEntityContext.Models context) {
      for (int i = 0; i < fileNames.length; i++) {
         String fileName = fileNames[i];
         RandomEntityProperties props = makeProperties(name, fileName, context);
         if (props != null) {
            return props;
         }
      }

      return null;
   }

   private static RandomEntityProperties makeProperties(String name, String fileName, RandomEntityContext.Models context) {
      Identifier locJem = new Identifier("optifine/cem/" + name + ".jem");
      Identifier locProps = new Identifier("optifine/cem/" + name + ".properties");
      if (Config.hasResource(locProps)) {
         RandomEntityProperties props = RandomEntityProperties.parse(locProps, locJem, context);
         if (props != null) {
            return props;
         }
      }

      if (!Config.hasResource(locJem)) {
         return null;
      }

      int[] variants = RandomEntities.getLocationsVariants(locJem, false, context);
      if (variants == null) {
         return null;
      }

      RandomEntityProperties<IEntityRenderer> props = new RandomEntityProperties<>(name, locJem, variants, context);
      return !props.isValid(locJem.getPath()) ? null : props;
   }

   public static Map<EntityType, EntityRenderer> getEntityRenderMap() {
      return RendererUtils.getEntityRenderMap();
   }

   public static Map<BlockEntityType, BlockEntityRenderer> getBlockEntityRenderMap() {
      return RendererUtils.getBlockEntityRenderMap();
   }

   public static Map<Type, SkullModelBase> getSkullModelMap() {
      return RendererUtils.getSkullModelMap();
   }

   private static Map<String, Identifier> getModelLocations() {
      String prefix = "optifine/cem/";
      String suffix = ".jem";
      Map<String, Identifier> resources = new LinkedHashMap<>();
      String[] names = CustomModelRegistry.getModelNames();

      for (String name : names) {
         String path = prefix + name + suffix;
         Identifier loc = new Identifier(path);
         if (debugModels) {
            resources.put(name, loc);
         } else if (Config.hasResource(loc)) {
            resources.put(name, loc);
         } else {
            String[] aliases = CustomModelRegistry.getAliases(name);
            if (aliases != null) {
               for (String alias : aliases) {
                  String pathAlias = prefix + alias + suffix;
                  Identifier locAlias = new Identifier(pathAlias);
                  if (Config.hasResource(locAlias)) {
                     Config.log("CustomEntityModel alias: " + name + " -> " + alias);
                     resources.put(name, locAlias);
                     break;
                  }
               }
            }
         }
      }

      return resources;
   }

   public static IEntityRenderer parseEntityRender(String name, Identifier location, RendererCache rendererCache, int index) {
      try {
         if (debugModels && index == 0) {
            return makeDebugEntityRenderer(location, rendererCache, index);
         }

         JsonObject jo = CustomEntityModelParser.loadJson(location);
         return parseEntityRender(name, location.getPath(), jo, rendererCache, index);
      } catch (IOException e) {
         Config.error(e.getClass().getName() + ": " + e.getMessage());
         return null;
      } catch (JsonParseException e) {
         Config.error(e.getClass().getName() + ": " + e.getMessage());
         return null;
      } catch (Exception e) {
         Log.warn("Error loading CEM: " + location, e);
         return null;
      }
   }

   private static IEntityRenderer makeDebugEntityRenderer(Identifier loc, RendererCache rendererCache, int index) {
      String path = loc.getPath();
      String nameJem = StrUtils.removePrefix(path, "optifine/cem/");
      String name = StrUtils.removeSuffix(nameJem, ".jem");
      ModelAdapter ma = CustomModelRegistry.getModelAdapter(name);
      Model model = ma.makeModel();
      DyeColor[] colors = DyeColor.values();
      colors = (DyeColor[])ArrayUtils.removeObjectFromArray(colors, DyeColor.BLACK);
      int offset = Math.abs(loc.hashCode()) % 256;
      String[] partNames = ma.getModelRendererNames();

      for (int i = 0; i < partNames.length; i++) {
         String partName = partNames[i];
         ModelPart part = ma.getModelRenderer(model, partName);
         if (part != null) {
            DyeColor col = colors[(i + offset) % colors.length];
            Identifier locTexture = new Identifier("textures/block/" + col.getSerializedName() + "_stained_glass.png");
            part.setTextureLocation(locTexture);
            Config.dbg("  " + partName + ": " + col.getSerializedName());
         }
      }

      IEntityRenderer er = ma.makeEntityRender(model, rendererCache, index);
      if (er == null) {
         return null;
      }

      er.setType(ma.getType());
      return er;
   }

   private static IEntityRenderer parseEntityRender(String name, String path, JsonObject obj, RendererCache rendererCache, int index) {
      CustomEntityRenderer cer = CustomEntityModelParser.parseEntityRender(obj, path);
      ModelAdapter modelAdapter = CustomModelRegistry.getModelAdapter(name);
      checkNull(modelAdapter, "Entity not found: " + name);
      Either<EntityType, BlockEntityType> type = modelAdapter.getType();
      IEntityRenderer render = makeEntityRender(modelAdapter, cer, rendererCache, index);
      if (render == null) {
         return null;
      }

      render.setType(type);
      return render;
   }

   private static IEntityRenderer makeEntityRender(ModelAdapter modelAdapter, CustomEntityRenderer cer, RendererCache rendererCache, int index) {
      Identifier textureLocation = cer.getTextureLocation();
      CustomModelRenderer[] modelRenderers = cer.getCustomModelRenderers();
      float shadowSize = cer.getShadowSize();
      Model model = modelAdapter.makeModel();
      if (model == null) {
         return null;
      }

      ModelResolver mr = new ModelResolver(modelAdapter, model, modelRenderers);
      if (!modifyModel(modelAdapter, model, modelRenderers, mr)) {
         return null;
      }

      IEntityRenderer r = modelAdapter.makeEntityRender(model, rendererCache, index);
      if (r == null) {
         throw new JsonParseException("Entity renderer is null, model: " + modelAdapter.getName() + ", adapter: " + modelAdapter.getClass().getName());
      }

      if (shadowSize >= 0.0F) {
         r.setShadowSize(shadowSize);
      }

      if (textureLocation != null) {
         setTextureLocation(modelAdapter, model, r, textureLocation);
      }

      return r;
   }

   private static void setTextureLocation(ModelAdapter modelAdapter, Model model, IEntityRenderer er, Identifier textureLocation) {
      if (!modelAdapter.setTextureLocation(er, textureLocation)) {
         if (er instanceof LivingEntityRenderer) {
            model.locationTextureCustom = textureLocation;
         } else {
            if (model.isRenderRoot() && er.getType() != null) {
               model.root().setTextureLocation(textureLocation);
            } else {
               setTextureTopModelRenderers(modelAdapter, model, textureLocation);
            }
         }
      }
   }

   public static void setTextureTopModelRenderers(ModelAdapter modelAdapter, Model model, Identifier textureLocation) {
      String[] parts = modelAdapter.getModelRendererNames();

      for (int i = 0; i < parts.length; i++) {
         String part = parts[i];
         ModelPart modelRenderer = modelAdapter.getModelRenderer(model, part);
         if (modelRenderer != null && modelRenderer.getTextureLocation() == null) {
            modelRenderer.setTextureLocation(textureLocation);
         }
      }
   }

   private static boolean modifyModel(ModelAdapter modelAdapter, Model model, CustomModelRenderer[] modelRenderers, ModelResolver mr) {
      List<ModelVariableUpdater> listVariableUpdaters = new ArrayList<>();

      for (int i = 0; i < modelRenderers.length; i++) {
         CustomModelRenderer cmr = modelRenderers[i];
         if (!modifyModel(modelAdapter, model, cmr, mr)) {
            return false;
         }

         if (cmr.getModelRenderer().getModelUpdater() != null) {
            listVariableUpdaters.addAll(Arrays.asList(cmr.getModelRenderer().getModelUpdater().getModelVariableUpdaters()));
         }
      }

      modelAdapter.finalizeModel(model);
      ModelVariableUpdater[] mvus = listVariableUpdaters.toArray(new ModelVariableUpdater[listVariableUpdaters.size()]);
      ModelUpdater globvalUpdater = new ModelUpdater(mvus);

      for (int i = 0; i < modelRenderers.length; i++) {
         CustomModelRenderer cmr = modelRenderers[i];
         if (cmr.getModelRenderer().getModelUpdater() != null) {
            cmr.getModelRenderer().setModelUpdater(globvalUpdater);
         }
      }

      for (int i = 0; i < mvus.length; i++) {
         ModelVariableUpdater mvu = mvus[i];
         if (mvu.getModelVariable() instanceof IModelRendererVariable mrv) {
            mrv.getModelRenderer().setModelUpdater(globvalUpdater);
         }
      }

      return true;
   }

   private static boolean modifyModel(ModelAdapter modelAdapter, Model model, CustomModelRenderer customModelRenderer, ModelResolver modelResolver) {
      String modelPart = customModelRenderer.getModelPart();
      ModelPart parent = modelAdapter.getModelRenderer(model, modelPart);
      if (parent == null) {
         if (modelAdapter.isPartOptional(modelPart)) {
            return true;
         }

         Config.warn("Model part not found: " + modelPart + ", model: " + DebugUtils.getClassName(model) + ", modelName: " + modelAdapter.getName());
         return false;
      } else {
         if (!customModelRenderer.isAttach()) {
            parent.cubes.clear();
            parent.spriteList.clear();
            if (!parent.children.isEmpty()) {
               ModelPart[] knownParts = modelAdapter.getModelRenderers(model);
               parent.retainChildren(knownParts);
            }
         }

         String childName = parent.getUniqueChildModelName("CEM-" + modelPart);
         parent.addChildModel(childName, customModelRenderer.getModelRenderer());
         ModelUpdater mu = customModelRenderer.getModelUpdater();
         if (mu != null) {
            modelResolver.setThisModelRenderer(customModelRenderer.getModelRenderer());
            modelResolver.setPartModelRenderer(parent);
            if (!mu.initialize(modelResolver)) {
               return false;
            }

            customModelRenderer.getModelRenderer().setModelUpdater(mu);
         }

         return true;
      }
   }

   private static void checkNull(Object obj, String msg) {
      if (obj == null) {
         throw new JsonParseException(msg);
      }
   }

   public static boolean isActive() {
      return active;
   }

   public static boolean isCustomModel(BlockState blockStateIn) {
      for (int i = 0; i < customBlockEntityTypes.size(); i++) {
         BlockEntityType type = customBlockEntityTypes.get(i);
         if (type.isValid(blockStateIn)) {
            return true;
         }
      }

      return false;
   }

   public static void onRenderScreen(Screen screen) {
      BookModel customBookModel = CustomStaticModels.getBookModel();
      if (customBookModel != null && screen instanceof EnchantmentScreen es) {
         Reflector.GuiEnchantment_bookModel.setValue(es, customBookModel);
      }
   }

   public static EntityRenderer getEntityRenderer(Entity entityIn, EntityRenderer renderer) {
      if (mapEntityProperties.isEmpty()) {
         return renderer;
      }

      IRandomEntity randomEntity = RandomEntities.getRandomEntity(entityIn);
      if (randomEntity == null) {
         return renderer;
      }

      RandomEntityProperties<IEntityRenderer> props = mapEntityProperties.get(entityIn.getType());
      if (props == null) {
         return renderer;
      }

      IEntityRenderer ier = props.getResource(randomEntity, renderer);
      if (!(ier instanceof EntityRenderer)) {
         return null;
      }

      matchingRuleIndex = props.getMatchingRuleIndex();
      return (EntityRenderer)ier;
   }

   public static BlockEntityRenderer getBlockEntityRenderer(BlockEntity entityIn, BlockEntityRenderer renderer) {
      if (mapBlockEntityProperties.isEmpty()) {
         return renderer;
      }

      IRandomEntity randomEntity = RandomEntities.getRandomBlockEntity(entityIn);
      if (randomEntity == null) {
         return renderer;
      }

      RandomEntityProperties<IEntityRenderer> props = mapBlockEntityProperties.get(entityIn.getType());
      if (props == null) {
         return renderer;
      }

      IEntityRenderer ier = props.getResource(randomEntity, renderer);
      if (!(ier instanceof BlockEntityRenderer)) {
         return null;
      }

      matchingRuleIndex = props.getMatchingRuleIndex();
      return (BlockEntityRenderer)ier;
   }

   public static int getMatchingRuleIndex() {
      return matchingRuleIndex;
   }
}
