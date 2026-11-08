package net.optifine.util;

import com.google.common.collect.Lists;
import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Biome.BiomeBuilder;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.biome.BiomeSpecialEffects.Builder;
import net.optifine.config.BiomeId;
import net.optifine.override.ChunkCacheOF;
import net.optifine.reflect.Reflector;

public class BiomeUtils {
   private static Registry<Biome> defaultBiomeRegistry = makeDefaultBiomeRegistry();
   private static Registry<Biome> biomeRegistry = getBiomeRegistry(Minecraft.getInstance().level);
   private static Level biomeWorld = Minecraft.getInstance().level;
   public static Biome PLAINS = biomeRegistry.get(Biomes.PLAINS).get().value();
   public static Biome SUNFLOWER_PLAINS = biomeRegistry.get(Biomes.SUNFLOWER_PLAINS).get().value();
   public static Biome SNOWY_PLAINS = biomeRegistry.get(Biomes.SNOWY_PLAINS).get().value();
   public static Biome ICE_SPIKES = biomeRegistry.get(Biomes.ICE_SPIKES).get().value();
   public static Biome DESERT = biomeRegistry.get(Biomes.DESERT).get().value();
   public static Biome WINDSWEPT_HILLS = biomeRegistry.get(Biomes.WINDSWEPT_HILLS).get().value();
   public static Biome WINDSWEPT_GRAVELLY_HILLS = biomeRegistry.get(Biomes.WINDSWEPT_GRAVELLY_HILLS).get().value();
   public static Biome MUSHROOM_FIELDS = biomeRegistry.get(Biomes.MUSHROOM_FIELDS).get().value();
   public static Biome SWAMP = biomeRegistry.get(Biomes.SWAMP).get().value();
   public static Biome MANGROVE_SWAMP = biomeRegistry.get(Biomes.MANGROVE_SWAMP).get().value();
   public static Biome THE_VOID = biomeRegistry.get(Biomes.THE_VOID).get().value();
   public static Biome DRIPSTONE_CAVES = biomeRegistry.get(Biomes.DRIPSTONE_CAVES).get().value();
   public static Biome LUSH_CAVES = biomeRegistry.get(Biomes.LUSH_CAVES).get().value();
   public static Biome DEEP_DARK = biomeRegistry.get(Biomes.DEEP_DARK).get().value();

   public static void onWorldChanged(Level worldIn) {
      biomeRegistry = getBiomeRegistry(worldIn);
      biomeWorld = worldIn;
      PLAINS = biomeRegistry.get(Biomes.PLAINS).get().value();
      SUNFLOWER_PLAINS = biomeRegistry.get(Biomes.SUNFLOWER_PLAINS).get().value();
      SNOWY_PLAINS = biomeRegistry.get(Biomes.SNOWY_PLAINS).get().value();
      ICE_SPIKES = biomeRegistry.get(Biomes.ICE_SPIKES).get().value();
      DESERT = biomeRegistry.get(Biomes.DESERT).get().value();
      WINDSWEPT_HILLS = biomeRegistry.get(Biomes.WINDSWEPT_HILLS).get().value();
      WINDSWEPT_GRAVELLY_HILLS = biomeRegistry.get(Biomes.WINDSWEPT_GRAVELLY_HILLS).get().value();
      MUSHROOM_FIELDS = biomeRegistry.get(Biomes.MUSHROOM_FIELDS).get().value();
      SWAMP = biomeRegistry.get(Biomes.SWAMP).get().value();
      MANGROVE_SWAMP = biomeRegistry.get(Biomes.MANGROVE_SWAMP).get().value();
      THE_VOID = biomeRegistry.get(Biomes.THE_VOID).get().value();
      DRIPSTONE_CAVES = biomeRegistry.get(Biomes.DRIPSTONE_CAVES).get().value();
      LUSH_CAVES = biomeRegistry.get(Biomes.LUSH_CAVES).get().value();
      DEEP_DARK = biomeRegistry.get(Biomes.DEEP_DARK).get().value();
   }

   private static Biome getBiomeSafe(Registry<Biome> registry, ResourceKey<Biome> biomeKey, Supplier<Biome> biomeDefault) {
      Biome biome = registry.get(biomeKey).get().value();
      if (biome == null) {
         biome = biomeDefault.get();
      }

      return biome;
   }

   public static Registry<Biome> getBiomeRegistry(Level worldIn) {
      if (worldIn != null) {
         if (worldIn == biomeWorld) {
            return biomeRegistry;
         }

         Registry<Biome> worldBiomeRegistry = worldIn.registryAccess().lookup(Registries.BIOME).get();
         return fixBiomeIds(defaultBiomeRegistry, worldBiomeRegistry);
      } else {
         return defaultBiomeRegistry;
      }
   }

   private static Registry<Biome> makeDefaultBiomeRegistry() {
      MappedRegistry<Biome> registry = new MappedRegistry<>(ResourceKey.createRegistryKey(new Identifier("biomes")), Lifecycle.stable(), true);

      for (ResourceKey<Biome> biomeKey : Biomes.getBiomeKeys()) {
         BiomeBuilder bb = new BiomeBuilder();
         bb.hasPrecipitation(false);
         bb.temperature(0.0F);
         bb.downfall(0.0F);
         bb.specialEffects(new Builder().waterColor(0).build());
         bb.mobSpawnSettings(new net.minecraft.world.level.biome.MobSpawnSettings.Builder().build());
         bb.generationSettings(new net.minecraft.world.level.biome.BiomeGenerationSettings.Builder(null, null).build());
         Biome biome = bb.build();
         registry.createIntrusiveHolder(biome);
         Reference var6 = registry.register(biomeKey, biome, RegistrationInfo.BUILT_IN);
      }

      return registry;
   }

   private static Registry<Biome> fixBiomeIds(Registry<Biome> idRegistry, Registry<Biome> valueRegistry) {
      MappedRegistry<Biome> registry = new MappedRegistry<>(ResourceKey.createRegistryKey(new Identifier("biomes")), Lifecycle.stable(), true);

      for (ResourceKey<Biome> biomeKey : Biomes.getBiomeKeys()) {
         Biome biome = idRegistry.get(biomeKey).get().value();
         Optional<Reference<Biome>> optBiome = valueRegistry.get(biomeKey);
         if (!optBiome.isEmpty()) {
            biome = optBiome.get().value();
         }

         registry.createIntrusiveHolder(biome);
         Reference var8 = registry.register(biomeKey, biome, RegistrationInfo.BUILT_IN);
      }

      for (ResourceKey<Biome> biomeKey : valueRegistry.registryKeySet()) {
         if (!registry.containsKey(biomeKey)) {
            Biome biome = valueRegistry.get(biomeKey).get().value();
            registry.createIntrusiveHolder(biome);
            Reference var13 = registry.register(biomeKey, biome, RegistrationInfo.BUILT_IN);
         }
      }

      return registry;
   }

   public static Registry<Biome> getBiomeRegistry() {
      return biomeRegistry;
   }

   public static Identifier getLocation(Biome biome) {
      return getBiomeRegistry().getKey(biome);
   }

   public static int getId(Biome biome) {
      return getBiomeRegistry().getId(biome);
   }

   public static int getId(Identifier loc) {
      Biome biome = getBiome(loc);
      return getBiomeRegistry().getId(biome);
   }

   public static BiomeId getBiomeId(Identifier loc) {
      return BiomeId.make(loc);
   }

   public static Biome getBiome(Identifier loc) {
      return getBiomeRegistry().getValue(loc);
   }

   public static Set<Identifier> getLocations() {
      return getBiomeRegistry().keySet();
   }

   public static List<Biome> getBiomes() {
      return Lists.newArrayList(biomeRegistry);
   }

   public static List<BiomeId> getBiomeIds() {
      return getBiomeIds(getLocations());
   }

   public static List<BiomeId> getBiomeIds(Collection<Identifier> locations) {
      List<BiomeId> biomeIds = new ArrayList<>();

      for (Identifier loc : locations) {
         BiomeId bi = BiomeId.make(loc);
         if (bi != null) {
            biomeIds.add(bi);
         }
      }

      return biomeIds;
   }

   public static Biome getBiome(BlockAndTintGetter lightReader, BlockPos blockPos) {
      Biome biome = PLAINS;
      if (lightReader instanceof ChunkCacheOF) {
         biome = ((ChunkCacheOF)lightReader).getBiome(blockPos);
      } else if (lightReader instanceof LevelReader) {
         biome = ((LevelReader)lightReader).getBiome(blockPos).value();
      }

      return biome;
   }

   public static BiomeCategory getBiomeCategory(Holder<Biome> holder) {
      if (holder.value() == THE_VOID) {
         return BiomeCategory.NONE;
      } else if (holder.is(BiomeTags.IS_TAIGA)) {
         return BiomeCategory.TAIGA;
      } else if (holder.value() == WINDSWEPT_HILLS || holder.value() == WINDSWEPT_GRAVELLY_HILLS) {
         return BiomeCategory.EXTREME_HILLS;
      } else if (holder.is(BiomeTags.IS_JUNGLE)) {
         return BiomeCategory.JUNGLE;
      } else if (holder.is(BiomeTags.IS_BADLANDS)) {
         return BiomeCategory.MESA;
      } else if (holder.value() == PLAINS || holder.value() == PLAINS) {
         return BiomeCategory.PLAINS;
      } else if (holder.is(BiomeTags.IS_SAVANNA)) {
         return BiomeCategory.SAVANNA;
      } else if (holder.value() == SNOWY_PLAINS || holder.value() == ICE_SPIKES) {
         return BiomeCategory.ICY;
      } else if (holder.is(BiomeTags.IS_END)) {
         return BiomeCategory.THEEND;
      } else if (holder.is(BiomeTags.IS_BEACH)) {
         return BiomeCategory.BEACH;
      } else if (holder.is(BiomeTags.IS_FOREST)) {
         return BiomeCategory.FOREST;
      } else if (holder.is(BiomeTags.IS_OCEAN) || holder.is(BiomeTags.IS_DEEP_OCEAN)) {
         return BiomeCategory.OCEAN;
      } else if (holder.value() == DESERT) {
         return BiomeCategory.DESERT;
      } else if (holder.is(BiomeTags.IS_RIVER)) {
         return BiomeCategory.RIVER;
      } else if (holder.value() == SWAMP || holder.value() == MANGROVE_SWAMP) {
         return BiomeCategory.SWAMP;
      } else if (holder.value() == MUSHROOM_FIELDS) {
         return BiomeCategory.MUSHROOM;
      } else if (holder.is(BiomeTags.IS_NETHER)) {
         return BiomeCategory.NETHER;
      } else if (holder.value() == DRIPSTONE_CAVES || holder.value() == LUSH_CAVES || holder.value() == DEEP_DARK) {
         return BiomeCategory.UNDERGROUND;
      } else {
         return holder.is(BiomeTags.IS_MOUNTAIN) ? BiomeCategory.MOUNTAIN : BiomeCategory.PLAINS;
      }
   }

   public static float getDownfall(Biome biome) {
      Object bcs = Reflector.Biome_climateSettings.getValue(biome);
      if (bcs == null) {
         return 0.0F;
      } else {
         return !Reflector.Biome_ClimateSettings_downfall.exists() ? 0.0F : Reflector.getFieldValueFloat(bcs, Reflector.Biome_ClimateSettings_downfall, 0.0F);
      }
   }

   public static Precipitation getPrecipitation(Biome biome) {
      if (!biome.hasPrecipitation()) {
         return Precipitation.NONE;
      } else {
         return biome.getBaseTemperature() < 0.1 ? Precipitation.SNOW : Precipitation.RAIN;
      }
   }
}
