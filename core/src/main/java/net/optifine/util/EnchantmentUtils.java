package net.optifine.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class EnchantmentUtils {
   private static final ResourceKey[] ENCHANTMENT_KEYS = makeEnchantmentKeys();
   private static final Map<String, Integer> TRANSLATION_KEY_IDS = new HashMap<>();
   private static final Map<String, Enchantment> MAP_ENCHANTMENTS = new HashMap<>();
   private static final Map<Integer, String> LEGACY_ID_NAMES = makeLegacyIdsMap();
   private static final Pattern PATTERN_NUMBER = Pattern.compile("\\d+");

   public static Enchantment getEnchantment(String name) {
      if (PATTERN_NUMBER.matcher(name).matches()) {
         int id = Config.parseInt(name, -1);
         String legacyName = LEGACY_ID_NAMES.get(id);
         if (legacyName != null) {
            name = legacyName;
         }
      }

      Enchantment enchantment = MAP_ENCHANTMENTS.get(name);
      if (enchantment == null) {
         Provider holderlookup$provider = VanillaRegistries.createLookup();
         HolderGetter<Enchantment> holdergetter = holderlookup$provider.lookupOrThrow(Registries.ENCHANTMENT);
         Identifier loc = new Identifier(name);
         ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, loc);
         Optional<Reference<Enchantment>> optRef = holdergetter.get(key);
         if (optRef.isPresent()) {
            enchantment = optRef.get().value();
         }

         MAP_ENCHANTMENTS.put(name, enchantment);
      }

      return enchantment;
   }

   private static ResourceKey[] makeEnchantmentKeys() {
      return (ResourceKey[])Reflector.Enchantments_ResourceKeys.getFieldValues(null);
   }

   private static Map<Integer, String> makeLegacyIdsMap() {
      Map<Integer, String> map = new HashMap<>();
      map.put(0, "protection");
      map.put(1, "fire_protection");
      map.put(2, "feather_falling");
      map.put(3, "blast_protection");
      map.put(4, "projectile_protection");
      map.put(5, "respiration");
      map.put(6, "aqua_affinity");
      map.put(7, "thorns");
      map.put(8, "depth_strider");
      map.put(9, "frost_walker");
      map.put(10, "binding_curse");
      map.put(16, "sharpness");
      map.put(17, "smite");
      map.put(18, "bane_of_arthropods");
      map.put(19, "knockback");
      map.put(20, "fire_aspect");
      map.put(21, "looting");
      map.put(32, "efficiency");
      map.put(33, "silk_touch");
      map.put(34, "unbreaking");
      map.put(35, "fortune");
      map.put(48, "power");
      map.put(49, "punch");
      map.put(50, "flame");
      map.put(51, "infinity");
      map.put(61, "luck_of_the_sea");
      map.put(62, "lure");
      map.put(65, "loyalty");
      map.put(66, "impaling");
      map.put(67, "riptide");
      map.put(68, "channeling");
      map.put(70, "mending");
      map.put(71, "vanishing_curse");
      return map;
   }

   public static int getId(Enchantment en) {
      Component desc = en.description();
      String tranKey = ComponentUtils.getTranslationKey(desc);
      if (tranKey == null) {
         return -1;
      }

      Integer id = TRANSLATION_KEY_IDS.get(tranKey);
      if (id == null) {
         ResourceKey resKey = getResourceKeyByTranslation(tranKey);
         if (resKey == null) {
            return -1;
         }

         int index = ArrayUtils.indexOf(ENCHANTMENT_KEYS, resKey);
         if (index < 0) {
            return -1;
         }

         id = index;
         TRANSLATION_KEY_IDS.put(tranKey, id);
      }

      return id;
   }

   private static ResourceKey getResourceKeyByTranslation(String tranKey) {
      String[] parts = Config.tokenize(tranKey, ".");
      if (parts.length != 3) {
         return null;
      }

      String name = parts[2];

      for (int i = 0; i < ENCHANTMENT_KEYS.length; i++) {
         ResourceKey<Enchantment> rk = ENCHANTMENT_KEYS[i];
         if (Config.equals(rk.identifier().getPath(), name)) {
            return rk;
         }
      }

      return null;
   }

   public static int getMaxEnchantmentId() {
      return ENCHANTMENT_KEYS.length;
   }
}
