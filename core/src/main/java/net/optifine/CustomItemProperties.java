package net.optifine;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.opengl.GlStateManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.cuboid.ItemModelGenerator;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.client.resources.model.sprite.Material.Baked;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import net.optifine.config.IParserInt;
import net.optifine.config.NbtTagValue;
import net.optifine.config.ParserEnchantmentId;
import net.optifine.config.RangeInt;
import net.optifine.config.RangeListInt;
import net.optifine.render.Blender;
import net.optifine.util.ArrayUtils;
import net.optifine.util.ItemUtils;
import net.optifine.util.Json;
import net.optifine.util.PathUtils;
import net.optifine.util.StrUtils;
import net.optifine.util.TextureUtils;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.opengl.GL11;

public class CustomItemProperties {
    public String name = null;
    public String basePath = null;
    public int type = 1;
    public int[] items = null;
    public String texture = null;
    public Map<String, String> mapTextures = null;
    public String model = null;
    public Map<String, String> mapModels = null;
    public RangeListInt damage = null;
    public boolean damagePercent = false;
    public int damageMask = 0;
    public RangeListInt stackSize = null;
    public int[] enchantmentIds = null;
    public RangeListInt enchantmentLevels = null;
    public NbtTagValue[] nbtTagValues = null;
    public int hand = 0;
    public int blend = 1;
    public float speed = 0.0F;
    public float rotation = 0.0F;
    public int layer = 0;
    public float duration = 1.0F;
    public int weight = 0;
    public Identifier textureLocation = null;
    public Map mapTextureLocations = null;
    public TextureAtlasSprite sprite = null;
    public Map mapSprites = null;
    public ItemModel bakedModelTexture = null;
    public Map<String, ItemModel> mapBakedModelsTexture = null;
    public ItemModel bakedModelFull = null;
    public Map<String, ItemModel> mapBakedModelsFull = null;
    public Set<Identifier> modelSpriteLocations = null;
    private int textureWidth = 0;
    private int textureHeight = 0;
    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_ITEM = 1;
    public static final int TYPE_ENCHANTMENT = 2;
    public static final int TYPE_ARMOR = 3;
    public static final int TYPE_ELYTRA = 4;
    public static final int HAND_ANY = 0;
    public static final int HAND_MAIN = 1;
    public static final int HAND_OFF = 2;
    public static final String INVENTORY = "inventory";
    private static final String PREFIX_NBT = "nbt.";
    private static final String PREFIX_COMPONENTS = "components.";

    public CustomItemProperties(Properties props, String path) {
        this.name = parseName(path);
        this.basePath = parseBasePath(path);
        this.type = this.parseType(props.getProperty("type"));
        this.items = this.parseItems(props.getProperty("items"), props.getProperty("matchItems"));
        this.mapModels = parseModels(props, this.basePath);
        this.model = parseModel(props.getProperty("model"), path, this.basePath, this.type, this.mapModels);
        this.mapTextures = parseTextures(props, this.basePath);
        boolean textureFromPath = this.mapModels == null && this.model == null;
        this.texture = parseTexture(
            props.getProperty("texture"),
            props.getProperty("tile"),
            props.getProperty("source"),
            path,
            this.basePath,
            this.type,
            this.mapTextures,
            textureFromPath
        );
        String damageStr = props.getProperty("damage");
        if (damageStr != null) {
            this.damagePercent = damageStr.contains("%");
            damageStr = damageStr.replace("%", "");
            this.damage = this.parseRangeListInt(damageStr);
            this.damageMask = this.parseInt(props.getProperty("damageMask"), 0);
        }

        this.stackSize = this.parseRangeListInt(props.getProperty("stackSize"));
        this.enchantmentIds = this.parseInts(getProperty(props, "enchantmentIDs", "enchantments"), new ParserEnchantmentId());
        this.enchantmentLevels = this.parseRangeListInt(props.getProperty("enchantmentLevels"));
        this.nbtTagValues = this.parseNbtTagValues(props);
        this.hand = this.parseHand(props.getProperty("hand"));
        this.blend = Blender.parseBlend(props.getProperty("blend"));
        this.speed = this.parseFloat(props.getProperty("speed"), 0.0F);
        this.rotation = this.parseFloat(props.getProperty("rotation"), 0.0F);
        this.layer = this.parseInt(props.getProperty("layer"), 0);
        this.weight = this.parseInt(props.getProperty("weight"), 0);
        this.duration = this.parseFloat(props.getProperty("duration"), 1.0F);
    }

    private static String getProperty(Properties props, String... names) {
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            String val = props.getProperty(name);
            if (val != null) {
                return val;
            }
        }

        return null;
    }

    private static String parseName(String path) {
        String str = path;
        int pos = str.lastIndexOf(47);
        if (pos >= 0) {
            str = str.substring(pos + 1);
        }

        int pos2 = str.lastIndexOf(46);
        if (pos2 >= 0) {
            str = str.substring(0, pos2);
        }

        return str;
    }

    private static String parseBasePath(String path) {
        int pos = path.lastIndexOf(47);
        return pos < 0 ? "" : path.substring(0, pos);
    }

    private int parseType(String str) {
        if (str == null) {
            return 1;
        }

        if (str.equals("item")) {
            return 1;
        }

        if (str.equals("enchantment")) {
            return 2;
        }

        if (str.equals("armor")) {
            return 3;
        }

        if (str.equals("elytra")) {
            return 4;
        }

        Config.warn("Unknown method: " + str);
        return 0;
    }

    private int[] parseItems(String str, String str2) {
        if (str == null) {
            str = str2;
        }

        if (str == null) {
            return null;
        }

        str = str.trim();
        Set setItemIds = new TreeSet();
        String[] tokens = Config.tokenize(str, " ");

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            Item item = this.getItemByName(token);
            if (item == null) {
                Config.warn("Item not found: " + token);
            } else {
                int id = Item.getId(item);
                if (id < 0) {
                    Config.warn("Item ID not found: " + token);
                } else {
                    setItemIds.add(new Integer(id));
                }
            }
        }

        Integer[] integers = (Integer[])setItemIds.toArray(new Integer[setItemIds.size()]);
        int[] ints = new int[integers.length];

        for (int i = 0; i < ints.length; i++) {
            ints[i] = integers[i];
        }

        return ints;
    }

    private Item getItemByName(String name) {
        Identifier loc = new Identifier(name);
        return !BuiltInRegistries.ITEM.containsKey(loc) ? null : BuiltInRegistries.ITEM.getValue(loc);
    }

    private static String parseTexture(
        String texStr, String texStr2, String texStr3, String path, String basePath, int type, Map<String, String> mapTexs, boolean textureFromPath
    ) {
        if (texStr == null) {
            texStr = texStr2;
        }

        if (texStr == null) {
            texStr = texStr3;
        }

        if (texStr != null) {
            String png = ".png";
            if (texStr.endsWith(png)) {
                texStr = texStr.substring(0, texStr.length() - png.length());
            }

            return fixTextureName(texStr, basePath);
        } else {
            if (type == 3) {
                return null;
            }

            if (mapTexs != null) {
                String bowStandbyTex = mapTexs.get("texture.bow_standby");
                if (bowStandbyTex != null) {
                    return bowStandbyTex;
                }
            }

            if (!textureFromPath) {
                return null;
            }

            String str = path;
            int pos = str.lastIndexOf(47);
            if (pos >= 0) {
                str = str.substring(pos + 1);
            }

            int pos2 = str.lastIndexOf(46);
            if (pos2 >= 0) {
                str = str.substring(0, pos2);
            }

            return fixTextureName(str, basePath);
        }
    }

    private static Map parseTextures(Properties props, String basePath) {
        String prefix = "texture.";
        Map mapProps = getMatchingProperties(props, prefix);
        if (mapProps.size() <= 0) {
            return null;
        }

        Set keySet = mapProps.keySet();
        Map mapTex = new LinkedHashMap();

        for (String key : (java.lang.Iterable<String>)(java.lang.Iterable<?>)(keySet)) {
            String val = (String)mapProps.get(key);
            val = fixTextureName(val, basePath);
            mapTex.put(key, val);
        }

        return mapTex;
    }

    private static String fixTextureName(String iconName, String basePath) {
        iconName = TextureUtils.fixResourcePath(iconName, basePath);
        if (!iconName.startsWith(basePath) && !iconName.startsWith("textures/") && !iconName.startsWith("optifine/")) {
            iconName = basePath + "/" + iconName;
        }

        if (iconName.endsWith(".png")) {
            iconName = iconName.substring(0, iconName.length() - 4);
        }

        if (iconName.startsWith("/")) {
            iconName = iconName.substring(1);
        }

        return iconName;
    }

    private static String parseModel(String modelStr, String path, String basePath, int type, Map<String, String> mapModelNames) {
        if (modelStr != null) {
            String json = ".json";
            if (modelStr.endsWith(json)) {
                modelStr = modelStr.substring(0, modelStr.length() - json.length());
            }

            return fixModelName(modelStr, basePath);
        } else {
            if (type == 3) {
                return null;
            }

            if (mapModelNames != null) {
                String bowStandbyModel = mapModelNames.get("model.bow_standby");
                if (bowStandbyModel != null) {
                    return bowStandbyModel;
                }
            }

            return modelStr;
        }
    }

    private static Map parseModels(Properties props, String basePath) {
        String prefix = "model.";
        Map mapProps = getMatchingProperties(props, prefix);
        if (mapProps.size() <= 0) {
            return null;
        }

        Set keySet = mapProps.keySet();
        Map mapTex = new LinkedHashMap();

        for (String key : (java.lang.Iterable<String>)(java.lang.Iterable<?>)(keySet)) {
            String val = (String)mapProps.get(key);
            val = fixModelName(val, basePath);
            mapTex.put(key, val);
        }

        return mapTex;
    }

    private static String fixModelName(String modelName, String basePath) {
        modelName = TextureUtils.fixResourcePath(modelName, basePath);
        boolean isVanilla = modelName.startsWith("block/") || modelName.startsWith("item/");
        if (!modelName.startsWith(basePath) && !isVanilla && !modelName.startsWith("optifine/")) {
            modelName = basePath + "/" + modelName;
        }

        String json = ".json";
        if (modelName.endsWith(json)) {
            modelName = modelName.substring(0, modelName.length() - json.length());
        }

        if (modelName.startsWith("/")) {
            modelName = modelName.substring(1);
        }

        return modelName;
    }

    private int parseInt(String str, int defVal) {
        if (str == null) {
            return defVal;
        } else {
            str = str.trim();
            int val = Config.parseInt(str, Integer.MIN_VALUE);
            if (val == Integer.MIN_VALUE) {
                Config.warn("Invalid integer: " + str);
                return defVal;
            } else {
                return val;
            }
        }
    }

    private float parseFloat(String str, float defVal) {
        if (str == null) {
            return defVal;
        } else {
            str = str.trim();
            float val = Config.parseFloat(str, Float.MIN_VALUE);
            if (val == Float.MIN_VALUE) {
                Config.warn("Invalid float: " + str);
                return defVal;
            } else {
                return val;
            }
        }
    }

    private int[] parseInts(String str, IParserInt parser) {
        if (str == null) {
            return null;
        }

        String[] tokens = Config.tokenize(str, " ");
        List<Integer> list = new ArrayList<>();

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            int val = parser.parse(token, Integer.MIN_VALUE);
            if (val == Integer.MIN_VALUE) {
                Config.warn("Invalid value: " + token);
            } else {
                list.add(val);
            }
        }

        Integer[] intArr = list.toArray(new Integer[list.size()]);
        return Config.toPrimitive(intArr);
    }

    private RangeListInt parseRangeListInt(String str) {
        if (str == null) {
            return null;
        }

        String[] tokens = Config.tokenize(str, " ");
        RangeListInt rangeList = new RangeListInt();

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            RangeInt range = this.parseRangeInt(token);
            if (range == null) {
                Config.warn("Invalid range list: " + str);
                return null;
            }

            rangeList.addRange(range);
        }

        return rangeList;
    }

    private RangeInt parseRangeInt(String str) {
        if (str == null) {
            return null;
        }

        str = str.trim();
        int countMinus = str.length() - str.replace("-", "").length();
        if (countMinus > 1) {
            Config.warn("Invalid range: " + str);
            return null;
        }

        String[] tokens = Config.tokenize(str, "- ");
        int[] vals = new int[tokens.length];

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            int val = Config.parseInt(token, -1);
            if (val < 0) {
                Config.warn("Invalid range: " + str);
                return null;
            }

            vals[i] = val;
        }

        if (vals.length == 1) {
            int val = vals[0];
            if (str.startsWith("-")) {
                return new RangeInt(0, val);
            } else {
                return str.endsWith("-") ? new RangeInt(val, 65535) : new RangeInt(val, val);
            }
        } else if (vals.length == 2) {
            int min = Math.min(vals[0], vals[1]);
            int max = Math.max(vals[0], vals[1]);
            return new RangeInt(min, max);
        } else {
            Config.warn("Invalid range: " + str);
            return null;
        }
    }

    private NbtTagValue[] parseNbtTagValues(Properties props) {
        Map<String, String> mapComponents = getMatchingProperties(props, "components.");
        Map<String, String> mapNbt = getMatchingProperties(props, "nbt.");
        Map<String, String> mapNbtComponents = this.generateComponentsFromNbt(mapNbt);
        if (mapComponents.isEmpty()) {
            if (mapNbt.size() != mapNbtComponents.size()) {
                return new NbtTagValue[0];
            }

            mapComponents.putAll(mapNbtComponents);
            if (mapComponents.isEmpty()) {
                return null;
            }
        }

        List<NbtTagValue> listNbts = new ArrayList<>();

        for (Entry<String, String> entry : mapComponents.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            String id = key.substring("components.".length());
            id = this.fixNamespaces(id);
            NbtTagValue nbt = new NbtTagValue(id, val);
            listNbts.add(nbt);
        }

        return listNbts.toArray(new NbtTagValue[listNbts.size()]);
    }

    private Map<String, String> generateComponentsFromNbt(Map<String, String> mapNbt) {
        Map<String, String> map = new LinkedHashMap<>();

        for (Entry<String, String> entry : mapNbt.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            String id = key.substring("nbt.".length());
            int mapSizePrev = map.size();
            if (id.equals("display.Name")) {
                map.putIfAbsent("components.minecraft:custom_name", val);
            } else if (id.equals("display.Lore")) {
                map.putIfAbsent("components.minecraft:lore", val);
            } else if (id.equals("Potion")) {
                map.putIfAbsent("components.minecraft:potion_contents.potion", val);
            }

            if (id.equals("Damage")) {
                map.putIfAbsent("components.minecraft:damage", val);
            }

            if (id.equals("Variant") && this.type == 1 && ArrayUtils.contains(this.items, Item.getId(Items.AXOLOTL_BUCKET))) {
                map.putIfAbsent("components.minecraft:bucket_entity_data.Variant", val);
            }

            if (map.size() > mapSizePrev) {
                Config.warn("Deprecated NBT check: " + key + "=" + val);
            } else {
                Config.warn("Invalid NBT check: " + key + "=" + val);
            }
        }

        return map;
    }

    private String fixNamespaces(String id) {
        id = id.replace("~", "minecraft:");
        if (id.startsWith("*")) {
            return id;
        }

        int posPoint = id.indexOf(46);
        int posNameEnd = posPoint >= 0 ? posPoint : id.length();
        if (id.indexOf(58, 0, posNameEnd) < 0) {
            id = "minecraft:" + id;
        }

        return id;
    }

    private static Map<String, String> getMatchingProperties(Properties props, String keyPrefix) {
        Map map = new LinkedHashMap();

        for (String key : (java.lang.Iterable<String>)(java.lang.Iterable<?>)(props.keySet())) {
            String val = props.getProperty(key);
            if (key.startsWith(keyPrefix)) {
                map.put(key, val);
            }
        }

        return map;
    }

    private int parseHand(String str) {
        if (str == null) {
            return 0;
        }

        str = str.toLowerCase();
        if (str.equals("any")) {
            return 0;
        }

        if (str.equals("main")) {
            return 1;
        }

        if (str.equals("off")) {
            return 2;
        }

        Config.warn("Invalid hand: " + str);
        return 0;
    }

    public boolean isValid(String path) {
        if (!Identifier.isValidPath(path)) {
            Config.warn("Invalid path location: " + path);
            return false;
        }

        if (this.name == null || this.name.length() <= 0) {
            Config.warn("No name found: " + path);
            return false;
        }

        if (this.basePath == null) {
            Config.warn("No base path found: " + path);
            return false;
        }

        if (this.type == 0) {
            Config.warn("No type defined: " + path);
            return false;
        }

        if (this.type == 4 && this.items == null) {
            this.items = new int[]{Item.getId(Items.ELYTRA)};
        }

        if (this.type == 1 || this.type == 3 || this.type == 4) {
            if (this.items == null) {
                this.items = this.detectItems();
            }

            if (this.items == null) {
                Config.warn("No items defined: " + path);
                return false;
            }
        }

        if (this.texture == null && this.mapTextures == null && this.model == null && this.mapModels == null) {
            Config.warn("No texture or model specified: " + path);
            return false;
        }

        if (this.texture != null && !this.getTextureLocation(this.texture).isValid()) {
            Config.warn("Invalid texture location: " + this.texture);
            return false;
        }

        if (this.mapTextures != null) {
            for (String tex : this.mapTextures.values()) {
                if (!this.getTextureLocation(tex).isValid()) {
                    Config.warn("Invalid texture location: " + tex);
                    return false;
                }
            }
        }

        if (this.model != null && !getModelLocation(this.model).isValid()) {
            Config.warn("Invalid model location: " + this.model);
            return false;
        }

        if (this.mapModels != null) {
            for (String mod : this.mapModels.values()) {
                if (!getModelLocation(mod).isValid()) {
                    Config.warn("Invalid model location: " + mod);
                    return false;
                }
            }
        }

        if (this.type == 2 && this.enchantmentIds == null) {
            Config.warn("No enchantmentIDs specified: " + path);
            return false;
        } else if (this.nbtTagValues != null && this.nbtTagValues.length == 0) {
            Config.warn("Invalid NBT checks: " + path);
            return false;
        } else {
            return true;
        }
    }

    private int[] detectItems() {
        Item item = this.getItemByName(this.name);
        if (item == null) {
            return null;
        }

        int id = Item.getId(item);
        return id < 0 ? null : new int[]{id};
    }

    public void registerIcons(TextureAtlas textureMap) {
        if (this.texture != null) {
            this.textureLocation = this.getTextureLocation(this.texture);
            if (this.type == 1) {
                Identifier spriteLocation = this.getSpriteLocation(this.textureLocation);
                this.sprite = textureMap.registerSprite(spriteLocation);
            }
        }

        if (this.mapTextures != null) {
            this.mapTextureLocations = new HashMap();
            this.mapSprites = new HashMap();

            for (String key : this.mapTextures.keySet()) {
                String val = this.mapTextures.get(key);
                Identifier locTex = this.getTextureLocation(val);
                this.mapTextureLocations.put(key, locTex);
                if (this.type == 1) {
                    Identifier locSprite = this.getSpriteLocation(locTex);
                    TextureAtlasSprite icon = textureMap.registerSprite(locSprite);
                    this.mapSprites.put(key, icon);
                }
            }
        }

        if (this.modelSpriteLocations != null) {
            for (Identifier loc : this.modelSpriteLocations) {
                textureMap.registerSprite(loc);
            }
        }
    }

    public void updateIcons(TextureAtlas textureMap) {
        if (this.sprite != null) {
            this.sprite = textureMap.getSprite(this.sprite.getName());
        }

        if (this.mapSprites != null) {
            for (String key : (java.lang.Iterable<String>)(java.lang.Iterable<?>)(this.mapSprites.keySet())) {
                TextureAtlasSprite sprite = (TextureAtlasSprite)this.mapSprites.get(key);
                if (sprite != null) {
                    Identifier loc = sprite.getName();
                    TextureAtlasSprite spriteNew = textureMap.getSprite(loc);
                    if (spriteNew == null || MissingTextureAtlasSprite.isMisingSprite(spriteNew)) {
                        Config.warn("Missing CIT sprite: " + loc + ", properties: " + this.basePath);
                    }

                    this.mapSprites.put(key, spriteNew);
                }
            }
        }
    }

    private Identifier getTextureLocation(String texName) {
        if (texName == null) {
            return null;
        }

        Identifier resLoc = new Identifier(texName);
        String domain = resLoc.getNamespace();
        String path = resLoc.getPath();
        if (!path.contains("/")) {
            path = "textures/item/" + path;
        }

        String filePath = path + ".png";
        Identifier locFile = new Identifier(domain, filePath);
        boolean exists = Config.hasResource(locFile);
        if (!exists) {
            Config.warn("File not found: " + filePath);
        }

        return locFile;
    }

    private Identifier getSpriteLocation(Identifier resLoc) {
        String pathTex = resLoc.getPath();
        pathTex = StrUtils.removePrefix(pathTex, "textures/");
        pathTex = StrUtils.removeSuffix(pathTex, ".png");
        return new Identifier(resLoc.getNamespace(), pathTex);
    }

    public void updateModelTexture(TextureAtlas textureMap, ItemModelGenerator itemModelGenerator) {
        if (this.texture != null || this.mapTextures != null) {
            String[] textures = this.getModelTextures();
            boolean useTint = this.isUseTint();
            this.bakedModelTexture = makeBakedModel(textureMap, itemModelGenerator, textures, useTint);
            if (this.type == 1 && this.mapTextures != null) {
                for (String key : this.mapTextures.keySet()) {
                    String tex = this.mapTextures.get(key);
                    String path = StrUtils.removePrefix(key, "texture.");
                    if (this.isSubTexture(path)) {
                        String[] texNames = new String[]{tex};
                        CuboidItemModelWrapper modelTex = makeBakedModel(textureMap, itemModelGenerator, texNames, useTint);
                        if (this.mapBakedModelsTexture == null) {
                            this.mapBakedModelsTexture = new HashMap<>();
                        }

                        String location = "item/" + path;
                        this.mapBakedModelsTexture.put(location, modelTex);
                    }
                }
            }
        }
    }

    private boolean isSubTexture(String path) {
        return true;
    }

    private boolean isUseTint() {
        return true;
    }

    private static CuboidItemModelWrapper makeBakedModel(
        final TextureAtlas textureMap, ItemModelGenerator itemModelGenerator, String[] textures, boolean useTint
    ) {
        Map<String, Material> mapMaterials = new HashMap<>();
        final Identifier atlasLoc = textureMap.location();
        String modelName = "custom";

        for (int i = 0; i < textures.length; i++) {
            String texture = textures[i];
            String spriteName = StrUtils.removePrefix(texture, "textures/");
            Identifier spriteLoc = Identifier.withDefaultNamespace(spriteName);
            Material material = new Material(spriteLoc);
            String key = "layer" + i;
            mapMaterials.put(key, material);
            if (i == 0) {
                modelName = texture;
                mapMaterials.put("particle", material);
            }
        }

        TextureSlots textureSlots = new TextureSlots(mapMaterials);
        ModelDebugName debugName = () -> ArrayUtils.arrayToString(textures);
        ModelBaker modelBaker = new ModelBaker() {
            ModelBakery.InternerImpl interner = new ModelBakery.InternerImpl();
            ModelBakery.MissingModels missingModels = Minecraft.getInstance().getModelManager().getMissingModels();

            @Override
            public MaterialBaker materials() {
                return new MaterialBaker(textureMap.missingSprite()) {
                    @Override
                    protected Baked bake(Material material) {
                        TextureAtlasSprite sprite = CustomItemProperties.getSprite(atlasLoc, material);
                        return sprite == null ? null : new Baked(sprite, material.forceTranslucent());
                    }
                };
            }

            @Override
            public ResolvedModel getModel(Identifier locIn) {
                return null;
            }

            @Override
            public <T> T compute(ModelBaker.SharedOperationKey<T> keyIn) {
                return keyIn.compute(this);
            }

            @Override
            public ModelBaker.Interner interner() {
                return this.interner;
            }

            @Override
            public BlockStateModelPart missingBlockModelPart() {
                return this.missingModels.blockPart();
            }
        };
        ModelState modelState = BlockModelRotation.IDENTITY;
        QuadCollection qc = ItemModelGenerator.bake(textureSlots, modelBaker, modelState, debugName);
        List<BakedQuad> quads = qc.getAll();
        boolean blockLight = false;
        String spriteName = StrUtils.removePrefix(textures[0], "textures/");
        Identifier spriteLoc = Identifier.withDefaultNamespace(spriteName);
        TextureAtlasSprite particleSprite = textureMap.getRegisteredSprite(spriteLoc);
        Baked particleMaterial = new Baked(particleSprite, false);
        ItemTransforms transforms = ItemTransforms.NO_TRANSFORMS;
        ModelRenderProperties props = new ModelRenderProperties(blockLight, particleMaterial, transforms);
        List<ItemTintSource> tints = new ArrayList<>();
        Identifier modelLoc = Identifier.withDefaultNamespace(modelName);
        CuboidItemModelWrapper.validateAtlasUsage(quads);
        Matrix4fc modelTransform = new Matrix4f();
        return new CuboidItemModelWrapper(tints, qc, props, modelTransform, modelLoc, qc.isBuiltinGenerated());
    }

    public static TextureAtlasSprite getSprite(Identifier atlasLoc, Material material) {
        SpriteId spriteId = new SpriteId(atlasLoc, material.sprite());
        return Config.getAtlasManager().get(spriteId);
    }

    private String[] getModelTextures() {
        if (this.type == 1 && this.items.length == 1) {
            Item item = Item.byId(this.items[0]);
            boolean isPotionItem = item == Items.POTION || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION;
            if (isPotionItem && this.damage != null && this.damage.getCountRanges() > 0) {
                RangeInt range = this.damage.getRange(0);
                int valDamage = range.getMin();
                boolean splash = (valDamage & 16384) != 0;
                String texOverlay = this.getMapTexture(this.mapTextures, "texture.potion_overlay", "item/potion_overlay");
                String texMain = null;
                if (splash) {
                    texMain = this.getMapTexture(this.mapTextures, "texture.potion_bottle_splash", "item/potion_bottle_splash");
                } else {
                    texMain = this.getMapTexture(this.mapTextures, "texture.potion_bottle_drinkable", "item/potion_bottle_drinkable");
                }

                return new String[]{texOverlay, texMain};
            }

            Equippable equip = ItemUtils.getEquippable(item);
            if (equip != null) {
                String material = ItemUtils.getMaterial(equip);
                if (Config.equals(material, "leather")) {
                    String type = "helmet";
                    EquipmentSlot equipmentSlot = equip.slot();
                    if (equipmentSlot == EquipmentSlot.HEAD) {
                        type = "helmet";
                    }

                    if (equipmentSlot == EquipmentSlot.CHEST) {
                        type = "chestplate";
                    }

                    if (equipmentSlot == EquipmentSlot.LEGS) {
                        type = "leggings";
                    }

                    if (equipmentSlot == EquipmentSlot.FEET) {
                        type = "boots";
                    }

                    String key = material + "_" + type;
                    String texMain = this.getMapTexture(this.mapTextures, "texture." + key, "item/" + key);
                    String texOverlay = this.getMapTexture(this.mapTextures, "texture." + key + "_overlay", "item/" + key + "_overlay");
                    return new String[]{texMain, texOverlay};
                }
            }
        }

        return new String[]{this.texture};
    }

    private String getMapTexture(Map<String, String> map, String key, String def) {
        if (map == null) {
            return def;
        }

        String str = map.get(key);
        return str == null ? def : str;
    }

    @Override
    public String toString() {
        return this.basePath + "/" + this.name + ", type: " + this.type + ", items: [" + Config.arrayToString(this.items) + "], texture: " + this.texture;
    }

    public float getTextureWidth(TextureManager textureManager) {
        if (this.textureWidth <= 0) {
            if (this.textureLocation != null) {
                AbstractTexture tex = textureManager.getTexture(this.textureLocation);
                this.textureWidth = tex.getTexture().getWidth();
            }

            if (this.textureWidth <= 0) {
                this.textureWidth = 16;
            }
        }

        return this.textureWidth;
    }

    public float getTextureHeight(TextureManager textureManager) {
        if (this.textureHeight <= 0) {
            if (this.textureLocation != null) {
                AbstractTexture tex = textureManager.getTexture(this.textureLocation);
                this.textureHeight = tex.getTexture().getHeight();
            }

            if (this.textureHeight <= 0) {
                this.textureHeight = 16;
            }
        }

        return this.textureHeight;
    }

    public ItemModel getBakedModel(Identifier modelLocation, boolean fullModel) {
        ItemModel bakedModel;
        Map<String, ItemModel> mapBakedModels;
        if (fullModel) {
            bakedModel = this.bakedModelFull;
            mapBakedModels = this.mapBakedModelsFull;
        } else {
            bakedModel = this.bakedModelTexture;
            mapBakedModels = this.mapBakedModelsTexture;
        }

        if (modelLocation != null && mapBakedModels != null) {
            String modelPath = modelLocation.getPath();
            ItemModel customModel = mapBakedModels.get(modelPath);
            if (customModel != null) {
                return customModel;
            }
        }

        return bakedModel;
    }

    public void registerModels(Map<Identifier, Resource> mapModelsIn, boolean checkParents) {
        for (Identifier loc : this.getModelLocations(checkParents)) {
            Identifier locJson = this.getModelLocationJson(loc);
            if (!mapModelsIn.containsKey(locJson)) {
                Optional<Resource> opt = Config.getResourceSafe(locJson);
                if (opt.isEmpty()) {
                    Config.warn("Model not found: " + loc);
                } else {
                    Resource res = opt.get();
                    mapModelsIn.put(locJson, res);
                }
            }
        }
    }

    public void collectModelSprites(Map<Identifier, ResolvedModel> mapModelsIn) {
        this.modelSpriteLocations = new LinkedHashSet<>();

        for (Identifier loc : this.getModelLocations(false)) {
            ResolvedModel rm = mapModelsIn.get(loc);
            if (rm == null) {
                Config.warn("Resolved model not found: " + loc);
            } else {
                this.collectModelSprites(rm, loc);
            }
        }
    }

    public Set<Identifier> getModelLocations(boolean checkParents) {
        Set<Identifier> set = new LinkedHashSet<>();
        if (this.model != null) {
            Identifier loc = getModelLocation(this.model);
            this.addModelLocation(set, loc, checkParents);
        }

        if (this.type == 1 && this.mapModels != null) {
            for (String key : this.mapModels.keySet()) {
                String mod = this.mapModels.get(key);
                String path = StrUtils.removePrefix(key, "model.");
                if (this.isSubTexture(path)) {
                    Identifier loc = getModelLocation(mod);
                    this.addModelLocation(set, loc, checkParents);
                }
            }
        }

        return set;
    }

    private void addModelLocation(Set<Identifier> set, Identifier loc, boolean checkParents) {
        if (!set.contains(loc)) {
            set.add(loc);
            if (checkParents) {
                Identifier locJson = this.getModelLocationJson(loc);
                Optional<Resource> res = Config.getResourceSafe(locJson);
                if (!res.isEmpty()) {
                    try {
                        JsonParser jp = new JsonParser();
                        JsonObject json = (JsonObject)jp.parse(res.get().openAsReader());
                        String parent = Json.getString(json, "parent");
                        if (parent != null && !parent.startsWith("builtin/")) {
                            parent = PathUtils.resolveRelative(parent, this.basePath);
                            Identifier locParent = getModelLocation(parent);
                            this.addModelLocation(set, locParent, checkParents);
                        }
                    } catch (Exception e) {
                        Config.warn("Error loading custom model: " + locJson + ", " + e.getClass().getName() + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    public void updateModelsFull() {
        ModelManager modelManager = Config.getModelManager();
        ItemModel missingModel = modelManager.getMissingItemModel();
        if (this.model != null) {
            Identifier locItem = getItemLocation(this.model);
            this.bakedModelFull = modelManager.getItemModel(locItem);
            if (this.bakedModelFull == missingModel) {
                Config.warn("Custom Items: Model not found " + locItem.toString());
                this.bakedModelFull = null;
            }
        }

        if (this.type == 1 && this.mapModels != null) {
            for (String key : this.mapModels.keySet()) {
                String mod = this.mapModels.get(key);
                String path = StrUtils.removePrefix(key, "model.");
                if (this.isSubTexture(path)) {
                    Identifier locItem = getItemLocation(mod);
                    ItemModel bm = modelManager.getItemModel(locItem);
                    if (bm == missingModel) {
                        Config.warn("Custom Items: Model not found " + locItem.toString());
                    } else {
                        if (this.mapBakedModelsFull == null) {
                            this.mapBakedModelsFull = new HashMap<>();
                        }

                        String location = "item/" + path;
                        this.mapBakedModelsFull.put(location, bm);
                    }
                }
            }
        }
    }

    private void collectModelSprites(ResolvedModel modelIn, Identifier locIn) {
        TextureSlots textureSlots = modelIn.getTopTextureSlots();
        if (textureSlots != null) {
            Map<String, Material> mapMaterials = textureSlots.getResolvedValues();

            for (String key : mapMaterials.keySet()) {
                Material material = mapMaterials.get(key);
                Identifier textureLocation = material.sprite();
                this.modelSpriteLocations.add(textureLocation);
            }
        }
    }

    private static Identifier getModelLocation(String name) {
        return new Identifier(name);
    }

    private Identifier getModelLocationJson(Identifier loc) {
        String path = StrUtils.addSuffixCheck(loc.getPath(), ".json");
        path = PathUtils.resolveRelative(path, this.basePath);
        if (!path.startsWith("optifine/")) {
            path = StrUtils.addPrefixCheck("models/", path);
        }

        return loc.withPath(path);
    }

    private static Identifier getItemLocation(String modelName) {
        modelName = StrUtils.removePrefix(modelName, "item/");
        return getModelLocation(modelName);
    }
}
