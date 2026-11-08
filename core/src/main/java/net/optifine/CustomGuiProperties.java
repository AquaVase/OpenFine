package net.optifine;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.equine.Mule;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.optifine.config.BiomeId;
import net.optifine.config.ConnectedParser;
import net.optifine.config.MatchProfession;
import net.optifine.config.Matches;
import net.optifine.config.NbtTagValue;
import net.optifine.config.RangeListInt;
import net.optifine.reflect.Reflector;
import net.optifine.util.StrUtils;
import net.optifine.util.TextureUtils;

public class CustomGuiProperties {
    private String fileName = null;
    private String basePath = null;
    private CustomGuiProperties.EnumContainer container = null;
    private Map<Identifier, Identifier> textureLocations = null;
    private NbtTagValue nbtName = null;
    private BiomeId[] biomes = null;
    private RangeListInt heights = null;
    private Boolean large = null;
    private Boolean trapped = null;
    private Boolean christmas = null;
    private Boolean ender = null;
    private RangeListInt levels = null;
    private MatchProfession[] professions = null;
    private CustomGuiProperties.EnumVariant[] variants = null;
    private DyeColor[] colors = null;
    private static final CustomGuiProperties.EnumVariant[] VARIANTS_HORSE = new CustomGuiProperties.EnumVariant[]{
        CustomGuiProperties.EnumVariant.HORSE,
        CustomGuiProperties.EnumVariant.DONKEY,
        CustomGuiProperties.EnumVariant.MULE,
        CustomGuiProperties.EnumVariant.LLAMA
    };
    private static final CustomGuiProperties.EnumVariant[] VARIANTS_DISPENSER = new CustomGuiProperties.EnumVariant[]{
        CustomGuiProperties.EnumVariant.DISPENSER, CustomGuiProperties.EnumVariant.DROPPER
    };
    private static final CustomGuiProperties.EnumVariant[] VARIANTS_INVALID = new CustomGuiProperties.EnumVariant[0];
    private static final DyeColor[] COLORS_INVALID = new DyeColor[0];
    private static final Identifier ANVIL_GUI_TEXTURE = new Identifier("textures/gui/container/anvil.png");
    private static final Identifier BEACON_GUI_TEXTURE = new Identifier("textures/gui/container/beacon.png");
    private static final Identifier BREWING_STAND_GUI_TEXTURE = new Identifier("textures/gui/container/brewing_stand.png");
    private static final Identifier CHEST_GUI_TEXTURE = new Identifier("textures/gui/container/generic_54.png");
    private static final Identifier CRAFTING_TABLE_GUI_TEXTURE = new Identifier("textures/gui/container/crafting_table.png");
    private static final Identifier HORSE_GUI_TEXTURE = new Identifier("textures/gui/container/horse.png");
    private static final Identifier DISPENSER_GUI_TEXTURE = new Identifier("textures/gui/container/dispenser.png");
    private static final Identifier ENCHANTMENT_TABLE_GUI_TEXTURE = new Identifier("textures/gui/container/enchanting_table.png");
    private static final Identifier FURNACE_GUI_TEXTURE = new Identifier("textures/gui/container/furnace.png");
    private static final Identifier HOPPER_GUI_TEXTURE = new Identifier("textures/gui/container/hopper.png");
    private static final Identifier INVENTORY_GUI_TEXTURE = new Identifier("textures/gui/container/inventory.png");
    private static final Identifier SHULKER_BOX_GUI_TEXTURE = new Identifier("textures/gui/container/shulker_box.png");
    private static final Identifier VILLAGER_GUI_TEXTURE = new Identifier("textures/gui/container/villager2.png");

    public CustomGuiProperties(Properties props, String path) {
        ConnectedParser cp = new ConnectedParser("CustomGuis");
        this.fileName = cp.parseName(path);
        this.basePath = cp.parseBasePath(path);
        this.container = (CustomGuiProperties.EnumContainer)cp.parseEnum(props.getProperty("container"), CustomGuiProperties.EnumContainer.values(), "container");
        this.textureLocations = parseTextureLocations(props, "texture", this.container, "textures/gui/", this.basePath);
        this.nbtName = cp.parseNbtTagValue("name", props.getProperty("name"));
        this.biomes = cp.parseBiomes(props.getProperty("biomes"));
        this.heights = cp.parseRangeListIntNeg(props.getProperty("heights"));
        this.large = cp.parseBooleanObject(props.getProperty("large"));
        this.trapped = cp.parseBooleanObject(props.getProperty("trapped"));
        this.christmas = cp.parseBooleanObject(props.getProperty("christmas"));
        this.ender = cp.parseBooleanObject(props.getProperty("ender"));
        this.levels = cp.parseRangeListInt(props.getProperty("levels"));
        this.professions = cp.parseProfessions(props.getProperty("professions"));
        CustomGuiProperties.EnumVariant[] vars = getContainerVariants(this.container);
        this.variants = (CustomGuiProperties.EnumVariant[])cp.parseEnums(props.getProperty("variants"), vars, "variants", VARIANTS_INVALID);
        this.colors = parseEnumDyeColors(props.getProperty("colors"));
    }

    private static CustomGuiProperties.EnumVariant[] getContainerVariants(CustomGuiProperties.EnumContainer cont) {
        if (cont == CustomGuiProperties.EnumContainer.HORSE) {
            return VARIANTS_HORSE;
        } else {
            return cont == CustomGuiProperties.EnumContainer.DISPENSER ? VARIANTS_DISPENSER : new CustomGuiProperties.EnumVariant[0];
        }
    }

    private static DyeColor[] parseEnumDyeColors(String str) {
        if (str == null) {
            return null;
        }

        str = str.toLowerCase();
        String[] tokens = Config.tokenize(str, " ");
        DyeColor[] cols = new DyeColor[tokens.length];

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            DyeColor col = parseEnumDyeColor(token);
            if (col == null) {
                warn("Invalid color: " + token);
                return COLORS_INVALID;
            }

            cols[i] = col;
        }

        return cols;
    }

    private static DyeColor parseEnumDyeColor(String str) {
        if (str == null) {
            return null;
        }

        DyeColor[] colors = DyeColor.values();

        for (int i = 0; i < colors.length; i++) {
            DyeColor enumDyeColor = colors[i];
            if (enumDyeColor.getSerializedName().equals(str)) {
                return enumDyeColor;
            }

            if (enumDyeColor.getName().equals(str)) {
                return enumDyeColor;
            }
        }

        return null;
    }

    private static Identifier parseTextureLocation(String str, String basePath) {
        if (str == null) {
            return null;
        }

        str = str.trim();
        String tex = TextureUtils.fixResourcePath(str, basePath);
        if (!tex.endsWith(".png")) {
            tex = tex + ".png";
        }

        return new Identifier(basePath + "/" + tex);
    }

    private static Map<Identifier, Identifier> parseTextureLocations(
        Properties props, String property, CustomGuiProperties.EnumContainer container, String pathPrefix, String basePath
    ) {
        Map<Identifier, Identifier> map = new HashMap<>();
        String propVal = props.getProperty(property);
        if (propVal != null) {
            Identifier locKey = getGuiTextureLocation(container);
            Identifier locVal = parseTextureLocation(propVal, basePath);
            if (locKey != null && locVal != null) {
                map.put(locKey, locVal);
            }
        }

        String keyPrefix = property + ".";

        for (String key : (java.lang.Iterable<String>)(java.lang.Iterable<?>)(props.keySet())) {
            if (key.startsWith(keyPrefix)) {
                String pathRel = key.substring(keyPrefix.length());
                pathRel = pathRel.replace('\\', '/');
                pathRel = StrUtils.removePrefixSuffix(pathRel, "/", ".png");
                String path = pathPrefix + pathRel + ".png";
                String val = props.getProperty(key);
                Identifier locKey = new Identifier(path);
                Identifier locVal = parseTextureLocation(val, basePath);
                map.put(locKey, locVal);
            }
        }

        return map;
    }

    private static Identifier getGuiTextureLocation(CustomGuiProperties.EnumContainer container) {
        if (container == null) {
            return null;
        }

        switch (container) {
            case ANVIL:
                return ANVIL_GUI_TEXTURE;
            case BEACON:
                return BEACON_GUI_TEXTURE;
            case BREWING_STAND:
                return BREWING_STAND_GUI_TEXTURE;
            case CHEST:
                return CHEST_GUI_TEXTURE;
            case CRAFTING:
                return CRAFTING_TABLE_GUI_TEXTURE;
            case DISPENSER:
                return DISPENSER_GUI_TEXTURE;
            case ENCHANTMENT:
                return ENCHANTMENT_TABLE_GUI_TEXTURE;
            case FURNACE:
                return FURNACE_GUI_TEXTURE;
            case HOPPER:
                return HOPPER_GUI_TEXTURE;
            case HORSE:
                return HORSE_GUI_TEXTURE;
            case VILLAGER:
                return VILLAGER_GUI_TEXTURE;
            case SHULKER_BOX:
                return SHULKER_BOX_GUI_TEXTURE;
            case CREATIVE:
                return null;
            case INVENTORY:
                return INVENTORY_GUI_TEXTURE;
            default:
                return null;
        }
    }

    public boolean isValid(String path) {
        if (this.fileName == null || this.fileName.length() <= 0) {
            warn("No name found: " + path);
            return false;
        } else if (this.basePath == null) {
            warn("No base path found: " + path);
            return false;
        } else if (this.container == null) {
            warn("No container found: " + path);
            return false;
        } else if (this.textureLocations.isEmpty()) {
            warn("No texture found: " + path);
            return false;
        } else if (this.professions == ConnectedParser.PROFESSIONS_INVALID) {
            warn("Invalid professions or careers: " + path);
            return false;
        } else if (this.variants == VARIANTS_INVALID) {
            warn("Invalid variants: " + path);
            return false;
        } else if (this.colors == COLORS_INVALID) {
            warn("Invalid colors: " + path);
            return false;
        } else {
            return true;
        }
    }

    private static void warn(String str) {
        Config.warn("[CustomGuis] " + str);
    }

    private boolean matchesGeneral(CustomGuiProperties.EnumContainer ec, BlockPos pos, LevelReader blockAccess) {
        if (this.container != ec) {
            return false;
        }

        if (this.biomes != null) {
            Biome biome = blockAccess.getBiome(pos).value();
            if (!Matches.biome(biome, this.biomes)) {
                return false;
            }
        }

        return this.heights == null || this.heights.isInRange(pos.getY());
    }

    public boolean matchesPos(CustomGuiProperties.EnumContainer ec, BlockPos pos, LevelReader blockAccess, Screen screen) {
        if (!this.matchesGeneral(ec, pos, blockAccess)) {
            return false;
        }

        if (this.nbtName != null) {
            String name = getName(screen);
            if (!this.nbtName.matchesValue(name)) {
                return false;
            }
        }

        switch (ec) {
            case BEACON:
                return this.matchesBeacon(pos, blockAccess);
            case CHEST:
                return this.matchesChest(pos, blockAccess);
            case DISPENSER:
                return this.matchesDispenser(pos, blockAccess);
            case SHULKER_BOX:
                return this.matchesShulker(pos, blockAccess);
            default:
                return true;
        }
    }

    public static String getName(Screen screen) {
        Component itc = screen.getTitle();
        return itc == null ? null : itc.getString();
    }

    private boolean matchesBeacon(BlockPos pos, LevelReader blockAccess) {
        if (!(blockAccess.getBlockEntity(pos) instanceof BeaconBlockEntity teb)) {
            return false;
        } else {
            if (this.levels != null) {
                if (!Reflector.TileEntityBeacon_levels.exists()) {
                    return false;
                }

                int l = Reflector.getFieldValueInt(teb, Reflector.TileEntityBeacon_levels, -1);
                if (!this.levels.isInRange(l)) {
                    return false;
                }
            }

            return true;
        }
    }

    private boolean matchesChest(BlockPos pos, LevelReader blockAccess) {
        BlockEntity te = blockAccess.getBlockEntity(pos);
        if (te instanceof ChestBlockEntity tec) {
            return this.matchesChest(tec, pos, blockAccess);
        } else {
            return te instanceof EnderChestBlockEntity teec ? this.matchesEnderChest(teec, pos, blockAccess) : false;
        }
    }

    private boolean matchesChest(ChestBlockEntity tec, BlockPos pos, LevelReader blockAccess) {
        BlockState blockState = blockAccess.getBlockState(pos);
        ChestType chestType = blockState.hasProperty(ChestBlock.TYPE) ? blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
        boolean isLarge = chestType != ChestType.SINGLE;
        boolean isTrapped = tec instanceof TrappedChestBlockEntity;
        boolean isChristmas = CustomGuis.isChristmas;
        boolean isEnder = false;
        return this.matchesChest(isLarge, isTrapped, isChristmas, isEnder);
    }

    private boolean matchesEnderChest(EnderChestBlockEntity teec, BlockPos pos, LevelReader blockAccess) {
        return this.matchesChest(false, false, false, true);
    }

    private boolean matchesChest(boolean isLarge, boolean isTrapped, boolean isChristmas, boolean isEnder) {
        if (this.large != null && this.large != isLarge) {
            return false;
        } else if (this.trapped != null && this.trapped != isTrapped) {
            return false;
        } else {
            return this.christmas != null && this.christmas != isChristmas ? false : this.ender == null || this.ender == isEnder;
        }
    }

    private boolean matchesDispenser(BlockPos pos, LevelReader blockAccess) {
        if (!(blockAccess.getBlockEntity(pos) instanceof DispenserBlockEntity ted)) {
            return false;
        } else {
            if (this.variants != null) {
                CustomGuiProperties.EnumVariant var = this.getDispenserVariant(ted);
                if (!Config.equalsOne(var, this.variants)) {
                    return false;
                }
            }

            return true;
        }
    }

    private CustomGuiProperties.EnumVariant getDispenserVariant(DispenserBlockEntity ted) {
        return ted instanceof DropperBlockEntity ? CustomGuiProperties.EnumVariant.DROPPER : CustomGuiProperties.EnumVariant.DISPENSER;
    }

    private boolean matchesShulker(BlockPos pos, LevelReader blockAccess) {
        if (!(blockAccess.getBlockEntity(pos) instanceof ShulkerBoxBlockEntity tesb)) {
            return false;
        } else {
            if (this.colors != null) {
                DyeColor col = tesb.getColor();
                if (!Config.equalsOne(col, this.colors)) {
                    return false;
                }
            }

            return true;
        }
    }

    public boolean matchesEntity(CustomGuiProperties.EnumContainer ec, Entity entity, LevelReader blockAccess) {
        if (!this.matchesGeneral(ec, entity.blockPosition(), blockAccess)) {
            return false;
        }

        if (this.nbtName != null) {
            String entityName = entity.getScoreboardName();
            if (!this.nbtName.matchesValue(entityName)) {
                return false;
            }
        }

        switch (ec) {
            case HORSE:
                return this.matchesHorse(entity, blockAccess);
            case VILLAGER:
                return this.matchesVillager(entity, blockAccess);
            default:
                return true;
        }
    }

    private boolean matchesVillager(Entity entity, LevelReader blockAccess) {
        if (!(entity instanceof Villager entityVillager)) {
            return false;
        } else {
            if (this.professions != null) {
                VillagerData vd = entityVillager.getVillagerData();
                VillagerProfession vp = vd.profession().value();
                int level = vd.level();
                if (!MatchProfession.matchesOne(vp, level, this.professions)) {
                    return false;
                }
            }

            return true;
        }
    }

    private boolean matchesHorse(Entity entity, LevelReader blockAccess) {
        if (!(entity instanceof AbstractHorse ah)) {
            return false;
        } else {
            if (this.variants != null) {
                CustomGuiProperties.EnumVariant var = this.getHorseVariant(ah);
                if (!Config.equalsOne(var, this.variants)) {
                    return false;
                }
            }

            if (this.colors != null && ah instanceof Llama el) {
                DyeColor col = RandomEntityRule.getLlamaColor(el);
                if (!Config.equalsOne(col, this.colors)) {
                    return false;
                }
            }

            return true;
        }
    }

    private CustomGuiProperties.EnumVariant getHorseVariant(AbstractHorse entity) {
        if (entity instanceof Horse) {
            return CustomGuiProperties.EnumVariant.HORSE;
        } else if (entity instanceof Donkey) {
            return CustomGuiProperties.EnumVariant.DONKEY;
        } else if (entity instanceof Mule) {
            return CustomGuiProperties.EnumVariant.MULE;
        } else {
            return entity instanceof Llama ? CustomGuiProperties.EnumVariant.LLAMA : null;
        }
    }

    public CustomGuiProperties.EnumContainer getContainer() {
        return this.container;
    }

    public Identifier getTextureLocation(Identifier loc) {
        Identifier locNew = this.textureLocations.get(loc);
        return locNew == null ? loc : locNew;
    }

    @Override
    public String toString() {
        return "name: " + this.fileName + ", container: " + this.container + ", textures: " + this.textureLocations;
    }

    public enum EnumContainer {
        ANVIL,
        BEACON,
        BREWING_STAND,
        CHEST,
        CRAFTING,
        DISPENSER,
        ENCHANTMENT,
        FURNACE,
        HOPPER,
        HORSE,
        VILLAGER,
        SHULKER_BOX,
        CREATIVE,
        INVENTORY;
    }

    private enum EnumVariant {
        HORSE,
        DONKEY,
        MULE,
        LLAMA,
        DISPENSER,
        DROPPER;
    }
}
