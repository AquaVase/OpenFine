package net.optifine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.parrot.ShoulderRidingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.optifine.reflect.ReflectorRaw;
import net.optifine.render.RenderState;
import net.optifine.util.ArrayUtils;
import net.optifine.util.ResUtils;
import net.optifine.util.StrUtils;
import net.optifine.util.TextureUtils;

public class RandomEntities {
    private static Map<String, RandomEntityProperties<Identifier>> mapProperties = new HashMap<>();
    private static Map<String, RandomEntityProperties<Identifier>> mapSpriteProperties = new HashMap<>();
    private static boolean active = false;
    private static RandomEntity randomEntity = new RandomEntity();
    private static RandomTileEntity randomTileEntity = new RandomTileEntity();
    private static boolean working = false;
    public static final String SUFFIX_PNG = ".png";
    public static final String SUFFIX_PROPERTIES = ".properties";
    public static final String SEPARATOR_DIGITS = ".";
    public static final String PREFIX_TEXTURES_ENTITY = "textures/entity/";
    public static final String PREFIX_TEXTURES_PAINTING = "textures/painting/";
    public static final String PREFIX_TEXTURES = "textures/";
    public static final String PREFIX_OPTIFINE_RANDOM = "optifine/random/";
    public static final String PREFIX_OPTIFINE = "optifine/";
    public static final String PREFIX_OPTIFINE_MOB = "optifine/mob/";
    private static final String[] DEPENDANT_SUFFIXES = new String[]{
        "_armor", "_eyes", "_exploding", "_shooting", "_fur", "_eyes", "_invulnerable", "_angry", "_tame", "_collar"
    };
    private static final String PREFIX_DYNAMIC_TEXTURE_HORSE = "horse/";
    private static final String[] HORSE_TEXTURES = (String[])ReflectorRaw.getFieldValue(null, Horse.class, String[].class, 0);
    private static final String[] HORSE_TEXTURES_ABBR = (String[])ReflectorRaw.getFieldValue(null, Horse.class, String[].class, 1);

    public static void entityLoaded(Entity entity, Level world) {
        if (world != null) {
            SynchedEntityData edm = entity.getEntityData();
            edm.spawnPosition = entity.blockPosition();
            edm.spawnBiome = world.getBiome(edm.spawnPosition).value();
            if (entity instanceof ShoulderRidingEntity esr) {
                checkEntityShoulder(esr, false);
            }
        }
    }

    public static void entityUnloaded(Entity entity, Level world) {
        if (entity instanceof ShoulderRidingEntity esr) {
            checkEntityShoulder(esr, true);
        }
    }

    public static void checkEntityShoulder(ShoulderRidingEntity entity, boolean attach) {
        LivingEntity owner = entity.getOwner();
        if (owner == null) {
            owner = Config.getMinecraft().player;
        }

        if (owner instanceof AbstractClientPlayer player) {
            UUID entityUuid = entity.getUUID();
            if (attach) {
                player.lastAttachedEntity = entity;
            } else {
                SynchedEntityData edm = entity.getEntityData();
                if (player.entityShoulderLeft != null && Config.equals(player.entityShoulderLeft.getUUID(), entityUuid)) {
                    SynchedEntityData edmShoulderLeft = player.entityShoulderLeft.getEntityData();
                    edm.spawnPosition = edmShoulderLeft.spawnPosition;
                    edm.spawnBiome = edmShoulderLeft.spawnBiome;
                    player.entityShoulderLeft = null;
                }

                if (player.entityShoulderRight != null && Config.equals(player.entityShoulderRight.getUUID(), entityUuid)) {
                    SynchedEntityData edmShoulderRight = player.entityShoulderRight.getEntityData();
                    edm.spawnPosition = edmShoulderRight.spawnPosition;
                    edm.spawnBiome = edmShoulderRight.spawnBiome;
                    player.entityShoulderRight = null;
                }
            }
        }
    }

    public static void worldChanged(Level oldWorld, Level newWorld) {
        if (newWorld instanceof ClientLevel newWorldClient) {
            for (Entity entity : newWorldClient.entitiesForRendering()) {
                entityLoaded(entity, newWorld);
            }
        }

        randomEntity.setEntity(null);
        randomTileEntity.setTileEntity(null);
    }

    public static Identifier getTextureLocation(Identifier loc) {
        if (!active) {
            return loc;
        }

        IRandomEntity re = getRandomEntityRendered();
        if (re == null) {
            return loc;
        }

        if (working) {
            return loc;
        }

        try {
            working = true;
            String name = loc.getPath();
            if (name.startsWith("horse/")) {
                name = getHorseTexturePath(name, "horse/".length());
            }

            if (!name.startsWith("textures/entity/") && !name.startsWith("textures/painting/")) {
                return loc;
            }

            RandomEntityProperties<Identifier> props = mapProperties.get(name);
            return props == null ? loc : props.getResource(re, loc);
        } finally {
            working = false;
        }
    }

    private static String getHorseTexturePath(String path, int pos) {
        if (HORSE_TEXTURES != null && HORSE_TEXTURES_ABBR != null) {
            for (int i = 0; i < HORSE_TEXTURES_ABBR.length; i++) {
                String abbr = HORSE_TEXTURES_ABBR[i];
                if (path.startsWith(abbr, pos)) {
                    return HORSE_TEXTURES[i];
                }
            }

            return path;
        } else {
            return path;
        }
    }

    public static IRandomEntity getRandomEntityRendered() {
        if (RenderState.getEntity() != null) {
            randomEntity.setEntity(RenderState.getEntity());
            return randomEntity;
        }

        if (RenderState.getBlockEntity() != null) {
            BlockEntity te = RenderState.getBlockEntity();
            if (te.getLevel() != null) {
                randomTileEntity.setTileEntity(te);
                return randomTileEntity;
            }
        }

        return null;
    }

    public static IRandomEntity getRandomEntity(Entity entityIn) {
        randomEntity.setEntity(entityIn);
        return randomEntity;
    }

    public static IRandomEntity getRandomBlockEntity(BlockEntity tileEntityIn) {
        randomTileEntity.setTileEntity(tileEntityIn);
        return randomTileEntity;
    }

    private static RandomEntityProperties<Identifier> makeProperties(Identifier loc, RandomEntityContext.Textures context) {
        String path = loc.getPath();
        Identifier locProps = getLocationProperties(loc, context.isLegacy());
        if (locProps != null) {
            RandomEntityProperties props = RandomEntityProperties.parse(locProps, loc, context);
            if (props != null) {
                return props;
            }
        }

        int[] variants = getLocationsVariants(loc, context.isLegacy(), context);
        if (variants == null) {
            return null;
        }

        String name = context.getConnectedParser().parseName(loc.getPath());
        return new RandomEntityProperties<>(name, loc, variants, context);
    }

    private static Identifier getLocationProperties(Identifier loc, boolean legacy) {
        Identifier locMcp = getLocationRandom(loc, legacy);
        if (locMcp == null) {
            return null;
        }

        String domain = locMcp.getNamespace();
        String path = locMcp.getPath();
        String pathBase = StrUtils.removeSuffix(path, ".png");
        String pathProps = pathBase + ".properties";
        Identifier locProps = new Identifier(domain, pathProps);
        if (Config.hasResource(locProps)) {
            return locProps;
        }

        String pathParent = getParentTexturePath(pathBase);
        if (pathParent == null) {
            return null;
        }

        Identifier locParentProps = new Identifier(domain, pathParent + ".properties");
        return Config.hasResource(locParentProps) ? locParentProps : null;
    }

    protected static Identifier getLocationRandom(Identifier loc, boolean legacy) {
        String domain = loc.getNamespace();
        String path = loc.getPath();
        if (path.startsWith("optifine/")) {
            return loc;
        }

        String prefixTextures = "textures/";
        String prefixRandom = "optifine/random/";
        if (legacy) {
            prefixTextures = "textures/entity/";
            prefixRandom = "optifine/mob/";
        }

        if (!path.startsWith(prefixTextures)) {
            return null;
        }

        String pathRandom = StrUtils.replacePrefix(path, prefixTextures, prefixRandom);
        return new Identifier(domain, pathRandom);
    }

    private static String getPathBase(String pathRandom) {
        if (pathRandom.startsWith("optifine/random/")) {
            return StrUtils.replacePrefix(pathRandom, "optifine/random/", "textures/");
        } else {
            return pathRandom.startsWith("optifine/mob/") ? StrUtils.replacePrefix(pathRandom, "optifine/mob/", "textures/entity/") : null;
        }
    }

    protected static Identifier getLocationIndexed(Identifier loc, int index) {
        if (loc == null) {
            return null;
        }

        String path = loc.getPath();
        int pos = path.lastIndexOf(46);
        if (pos < 0) {
            return null;
        }

        String prefix = path.substring(0, pos);
        String suffix = path.substring(pos);
        String separator = StrUtils.endsWithDigit(prefix) ? "." : "";
        String pathNew = prefix + separator + index + suffix;
        return new Identifier(loc.getNamespace(), pathNew);
    }

    private static String getParentTexturePath(String path) {
        for (int i = 0; i < DEPENDANT_SUFFIXES.length; i++) {
            String suffix = DEPENDANT_SUFFIXES[i];
            if (path.endsWith(suffix)) {
                return StrUtils.removeSuffix(path, suffix);
            }
        }

        return null;
    }

    public static int[] getLocationsVariants(Identifier loc, boolean legacy, RandomEntityContext context) {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        Identifier locRandom = getLocationRandom(loc, legacy);
        if (locRandom == null) {
            return null;
        }

        for (int i = 1; i < list.size() + 10; i++) {
            int index = i + 1;
            Identifier locIndex = getLocationIndexed(locRandom, index);
            if (Config.hasResource(locIndex)) {
                list.add(index);
            }
        }

        if (list.size() <= 1) {
            return null;
        }

        Integer[] arr = list.toArray(new Integer[list.size()]);
        int[] intArr = ArrayUtils.toPrimitive(arr);
        Config.dbg(context.getName() + ": " + loc.getPath() + ", variants: " + intArr.length);
        return intArr;
    }

    public static void update() {
        mapProperties.clear();
        mapSpriteProperties.clear();
        active = false;
        if (Config.isRandomEntities()) {
            initialize();
        }
    }

    private static void initialize() {
        String[] prefixes = new String[]{"optifine/random/", "optifine/mob/"};
        String[] suffixes = new String[]{".png", ".properties"};
        String[] pathsRandom = ResUtils.collectFiles(prefixes, suffixes);
        Set basePathsChecked = new HashSet();

        for (int i = 0; i < pathsRandom.length; i++) {
            String path = pathsRandom[i];
            path = StrUtils.removeSuffix(path, suffixes);
            path = StrUtils.trimTrailing(path, "0123456789");
            path = StrUtils.removeSuffix(path, ".");
            path = path + ".png";
            String pathBase = getPathBase(path);
            if (!basePathsChecked.contains(pathBase)) {
                basePathsChecked.add(pathBase);
                Identifier locBase = new Identifier(pathBase);
                if (Config.hasResource(locBase)) {
                    RandomEntityProperties<Identifier> props = mapProperties.get(pathBase);
                    if (props == null) {
                        props = makeProperties(locBase, new RandomEntityContext.Textures(false));
                        if (props == null) {
                            props = makeProperties(locBase, new RandomEntityContext.Textures(true));
                        }

                        if (props != null) {
                            mapProperties.put(pathBase, props);
                        }
                    }
                }
            }
        }

        active = !mapProperties.isEmpty();
    }

    public static synchronized void registerSprites(Identifier atlasLocation, Set<Identifier> spriteLocations) {
        if (!mapProperties.isEmpty()) {
            String prefix = getTexturePrefix(atlasLocation);
            Set<Identifier> newLocations = new HashSet<>();

            for (Identifier loc : spriteLocations) {
                String pathFull = "textures/" + prefix + loc.getPath() + ".png";
                RandomEntityProperties<Identifier> props = mapProperties.get(pathFull);
                if (props != null) {
                    mapSpriteProperties.put(loc.getPath(), props);
                    List<Identifier> locs = props.getAllResources();
                    if (locs != null) {
                        for (int i = 0; i < locs.size(); i++) {
                            Identifier propLoc = locs.get(i);
                            Identifier locSprite = TextureUtils.getSpriteLocation(propLoc);
                            newLocations.add(locSprite);
                            mapSpriteProperties.put(locSprite.getPath(), props);
                        }
                    }
                }
            }

            spriteLocations.addAll(newLocations);
        }
    }

    private static String getTexturePrefix(Identifier atlasLocation) {
        return atlasLocation.getPath().endsWith("/paintings.png") ? "painting/" : "";
    }

    public static TextureAtlasSprite getRandomSprite(TextureAtlasSprite spriteIn) {
        if (!active) {
            return spriteIn;
        }

        IRandomEntity re = getRandomEntityRendered();
        if (re == null) {
            return spriteIn;
        }

        if (working) {
            return spriteIn;
        }

        try {
            working = true;
            Identifier locSpriteIn = spriteIn.getName();
            String name = locSpriteIn.getPath();
            RandomEntityProperties<Identifier> props = mapSpriteProperties.get(name);
            if (props == null) {
                return spriteIn;
            }

            Identifier loc = props.getResource(re, locSpriteIn);
            if (loc == locSpriteIn) {
                return spriteIn;
            }

            Identifier locSprite = TextureUtils.getSpriteLocation(loc);
            return spriteIn.getTextureAtlas().getSprite(locSprite);
        } finally {
            working = false;
        }
    }

    public static void dbg(String str) {
        Config.dbg("RandomEntities: " + str);
    }

    public static void warn(String str) {
        Config.warn("RandomEntities: " + str);
    }
}
