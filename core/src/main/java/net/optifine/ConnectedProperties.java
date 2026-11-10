package net.optifine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.optifine.config.BiomeId;
import net.optifine.config.ConnectedParser;
import net.optifine.config.MatchBlock;
import net.optifine.config.Matches;
import net.optifine.config.NbtTagValue;
import net.optifine.config.RangeInt;
import net.optifine.config.RangeListInt;
import net.optifine.util.BlockUtils;
import net.optifine.util.MathUtils;
import net.optifine.util.TextureUtils;

public class ConnectedProperties {
    public String name = null;
    public String basePath = null;
    public MatchBlock[] matchBlocks = null;
    public int[] metadatas = null;
    public String[] matchTiles = null;
    public int method = 0;
    public String[] tiles = null;
    public int connect = 0;
    public int faces = 63;
    public BiomeId[] biomes = null;
    public RangeListInt heights = null;
    public int renderPass = 0;
    public boolean innerSeams = false;
    public int[] ctmTileIndexes = null;
    public int width = 0;
    public int height = 0;
    public int[] weights = null;
    public int randomLoops = 0;
    public int symmetry = 1;
    public boolean linked = false;
    public NbtTagValue nbtName = null;
    public int[] sumWeights = null;
    public int sumAllWeights = 1;
    public TextureAtlasSprite[] matchTileIcons = null;
    public TextureAtlasSprite[] tileIcons = null;
    public MatchBlock[] connectBlocks = null;
    public String[] connectTiles = null;
    public TextureAtlasSprite[] connectTileIcons = null;
    public int tintIndex = -1;
    public BlockState tintBlockState = Blocks.AIR.defaultBlockState();
    public ChunkSectionLayer layer = null;
    public static final int METHOD_NONE = 0;
    public static final int METHOD_CTM = 1;
    public static final int METHOD_HORIZONTAL = 2;
    public static final int METHOD_TOP = 3;
    public static final int METHOD_RANDOM = 4;
    public static final int METHOD_REPEAT = 5;
    public static final int METHOD_VERTICAL = 6;
    public static final int METHOD_FIXED = 7;
    public static final int METHOD_HORIZONTAL_VERTICAL = 8;
    public static final int METHOD_VERTICAL_HORIZONTAL = 9;
    public static final int METHOD_CTM_COMPACT = 10;
    public static final int METHOD_OVERLAY = 11;
    public static final int METHOD_OVERLAY_FIXED = 12;
    public static final int METHOD_OVERLAY_RANDOM = 13;
    public static final int METHOD_OVERLAY_REPEAT = 14;
    public static final int METHOD_OVERLAY_CTM = 15;
    public static final int CONNECT_NONE = 0;
    public static final int CONNECT_BLOCK = 1;
    public static final int CONNECT_TILE = 2;
    public static final int CONNECT_STATE = 3;
    public static final int CONNECT_UNKNOWN = 128;
    public static final int FACE_BOTTOM = 1;
    public static final int FACE_TOP = 2;
    public static final int FACE_NORTH = 4;
    public static final int FACE_SOUTH = 8;
    public static final int FACE_WEST = 16;
    public static final int FACE_EAST = 32;
    public static final int FACE_SIDES = 60;
    public static final int FACE_ALL = 63;
    public static final int FACE_UNKNOWN = 128;
    public static final int SYMMETRY_NONE = 1;
    public static final int SYMMETRY_OPPOSITE = 2;
    public static final int SYMMETRY_ALL = 6;
    public static final int SYMMETRY_UNKNOWN = 128;
    public static final String TILE_SKIP_PNG = "<skip>.png";
    public static final String TILE_DEFAULT_PNG = "<default>.png";

    public ConnectedProperties(Properties props, String path) {
        ConnectedParser cp = new ConnectedParser("ConnectedTextures");
        this.name = cp.parseName(path);
        this.basePath = cp.parseBasePath(path);
        this.matchBlocks = cp.parseMatchBlocks(props.getProperty("matchBlocks"));
        this.metadatas = cp.parseIntList(props.getProperty("metadata"));
        this.matchTiles = this.parseMatchTiles(props.getProperty("matchTiles"));
        this.method = parseMethod(props.getProperty("method"));
        this.tiles = this.parseTileNames(props.getProperty("tiles"));
        this.connect = parseConnect(props.getProperty("connect"));
        this.faces = parseFaces(props.getProperty("faces"));
        this.biomes = cp.parseBiomes(props.getProperty("biomes"));
        this.heights = cp.parseRangeListIntNeg(props.getProperty("heights"));
        if (this.heights == null) {
            int minHeight = cp.parseIntNeg(props.getProperty("minHeight"), Integer.MIN_VALUE);
            int maxHeight = cp.parseIntNeg(props.getProperty("maxHeight"), Integer.MAX_VALUE);
            if (minHeight != Integer.MIN_VALUE || maxHeight != Integer.MAX_VALUE) {
                this.heights = new RangeListInt(new RangeInt(minHeight, maxHeight));
            }
        }

        this.renderPass = cp.parseInt(props.getProperty("renderPass"), -1);
        this.innerSeams = cp.parseBoolean(props.getProperty("innerSeams"), false);
        this.ctmTileIndexes = this.parseCtmTileIndexes(props);
        this.width = cp.parseInt(props.getProperty("width"), -1);
        this.height = cp.parseInt(props.getProperty("height"), -1);
        this.weights = cp.parseIntList(props.getProperty("weights"));
        this.randomLoops = cp.parseInt(props.getProperty("randomLoops"), 0);
        this.symmetry = parseSymmetry(props.getProperty("symmetry"));
        this.linked = cp.parseBoolean(props.getProperty("linked"), false);
        this.nbtName = cp.parseNbtTagValue("name", props.getProperty("name"));
        this.connectBlocks = cp.parseMatchBlocks(props.getProperty("connectBlocks"));
        this.connectTiles = this.parseMatchTiles(props.getProperty("connectTiles"));
        this.tintIndex = cp.parseInt(props.getProperty("tintIndex"), -1);
        this.tintBlockState = cp.parseBlockState(props.getProperty("tintBlock"), Blocks.AIR.defaultBlockState());
        this.layer = cp.parseBlockRenderLayer(props.getProperty("layer"), ChunkSectionLayer.CUTOUT);
    }

    private int[] parseCtmTileIndexes(Properties props) {
        if (this.tiles == null) {
            return null;
        }

        Map<Integer, Integer> mapTileIndexes = new HashMap<>();

        for (Object key : props.keySet()) {
            if (key instanceof String keyStr) {
                String PREFIX = "ctm.";
                if (keyStr.startsWith(PREFIX)) {
                    String ctmIndexStr = keyStr.substring(PREFIX.length());
                    String ctmTileIndexStr = props.getProperty(keyStr);
                    if (ctmTileIndexStr != null) {
                        ctmTileIndexStr = ctmTileIndexStr.trim();
                        int ctmIndex = Config.parseInt(ctmIndexStr, -1);
                        if (ctmIndex >= 0 && ctmIndex <= 46) {
                            int ctmTileIndex = Config.parseInt(ctmTileIndexStr, -1);
                            if (ctmTileIndex >= 0 && ctmTileIndex < this.tiles.length) {
                                mapTileIndexes.put(ctmIndex, ctmTileIndex);
                            } else {
                                Config.warn("Invalid CTM tile index: " + ctmTileIndexStr);
                            }
                        } else {
                            Config.warn("Invalid CTM index: " + ctmIndexStr);
                        }
                    }
                }
            }
        }

        if (mapTileIndexes.isEmpty()) {
            return null;
        }

        int[] tileIndexes = new int[47];

        for (int i = 0; i < tileIndexes.length; i++) {
            tileIndexes[i] = -1;
            if (mapTileIndexes.containsKey(i)) {
                tileIndexes[i] = mapTileIndexes.get(i);
            }
        }

        return tileIndexes;
    }

    private String[] parseMatchTiles(String str) {
        if (str == null) {
            return null;
        }

        String[] names = Config.tokenize(str, " ");

        for (int i = 0; i < names.length; i++) {
            String iconName = names[i];
            if (iconName.endsWith(".png")) {
                iconName = iconName.substring(0, iconName.length() - 4);
            }

            iconName = TextureUtils.fixResourcePath(iconName, this.basePath);
            names[i] = iconName;
        }

        return names;
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

    private String[] parseTileNames(String str) {
        if (str == null) {
            return null;
        }

        List list = new ArrayList();
        String[] iconStrs = Config.tokenize(str, " ,");

        for (int i = 0; i < iconStrs.length; i++) {
            String iconStr = iconStrs[i];
            if (iconStr.contains("-")) {
                String[] subStrs = Config.tokenize(iconStr, "-");
                if (subStrs.length == 2) {
                    int min = Config.parseInt(subStrs[0], -1);
                    int max = Config.parseInt(subStrs[1], -1);
                    if (min >= 0 && max >= 0) {
                        if (min > max) {
                            Config.warn("Invalid interval: " + iconStr + ", when parsing: " + str);
                            continue;
                        }

                        for (int n = min; n <= max; n++) {
                            list.add(String.valueOf(n));
                        }
                        continue;
                    }
                }
            }

            list.add(iconStr);
        }

        String[] names = (String[])list.toArray(new String[list.size()]);

        for (int i = 0; i < names.length; i++) {
            String iconName = names[i];
            iconName = TextureUtils.fixResourcePath(iconName, this.basePath);
            if (!iconName.startsWith(this.basePath) && !iconName.startsWith("textures/") && !iconName.startsWith("optifine/")) {
                iconName = this.basePath + "/" + iconName;
            }

            if (iconName.endsWith(".png")) {
                iconName = iconName.substring(0, iconName.length() - 4);
            }

            if (iconName.startsWith("/")) {
                iconName = iconName.substring(1);
            }

            names[i] = iconName;
        }

        return names;
    }

    private static int parseSymmetry(String str) {
        if (str == null) {
            return 1;
        }

        str = str.trim();
        if (str.equals("opposite")) {
            return 2;
        }

        if (str.equals("all")) {
            return 6;
        }

        Config.warn("Unknown symmetry: " + str);
        return 1;
    }

    private static int parseFaces(String str) {
        if (str == null) {
            return 63;
        }

        String[] faceStrs = Config.tokenize(str, " ,");
        int facesMask = 0;

        for (int i = 0; i < faceStrs.length; i++) {
            String faceStr = faceStrs[i];
            int faceMask = parseFace(faceStr);
            facesMask |= faceMask;
        }

        return facesMask;
    }

    private static int parseFace(String str) {
        str = str.toLowerCase();
        if (str.equals("bottom") || str.equals("down")) {
            return 1;
        }

        if (str.equals("top") || str.equals("up")) {
            return 2;
        }

        if (str.equals("north")) {
            return 4;
        }

        if (str.equals("south")) {
            return 8;
        }

        if (str.equals("east")) {
            return 32;
        }

        if (str.equals("west")) {
            return 16;
        }

        if (str.equals("sides")) {
            return 60;
        }

        if (str.equals("all")) {
            return 63;
        }

        Config.warn("Unknown face: " + str);
        return 128;
    }

    private static int parseConnect(String str) {
        if (str == null) {
            return 0;
        }

        str = str.trim();
        if (str.equals("block")) {
            return 1;
        }

        if (str.equals("tile")) {
            return 2;
        }

        if (str.equals("state")) {
            return 3;
        }

        Config.warn("Unknown connect: " + str);
        return 128;
    }

    public static Property getProperty(String key, Collection properties) {
        for (Property prop : (java.lang.Iterable<Property>)(java.lang.Iterable<?>)(properties)) {
            if (key.equals(prop.getName())) {
                return prop;
            }
        }

        return null;
    }

    private static int parseMethod(String str) {
        if (str == null) {
            return 1;
        }

        str = str.trim();
        if (str.equals("ctm") || str.equals("glass")) {
            return 1;
        }

        if (str.equals("ctm_compact")) {
            return 10;
        }

        if (str.equals("horizontal") || str.equals("bookshelf")) {
            return 2;
        }

        if (str.equals("vertical")) {
            return 6;
        }

        if (str.equals("top")) {
            return 3;
        }

        if (str.equals("random")) {
            return 4;
        }

        if (str.equals("repeat")) {
            return 5;
        }

        if (str.equals("fixed")) {
            return 7;
        }

        if (str.equals("horizontal+vertical") || str.equals("h+v")) {
            return 8;
        }

        if (str.equals("vertical+horizontal") || str.equals("v+h")) {
            return 9;
        }

        if (str.equals("overlay")) {
            return 11;
        }

        if (str.equals("overlay_fixed")) {
            return 12;
        }

        if (str.equals("overlay_random")) {
            return 13;
        }

        if (str.equals("overlay_repeat")) {
            return 14;
        }

        if (str.equals("overlay_ctm")) {
            return 15;
        }

        Config.warn("Unknown method: " + str);
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

        if (this.matchBlocks == null) {
            this.matchBlocks = this.detectMatchBlocks();
        }

        if (this.matchTiles == null && this.matchBlocks == null) {
            this.matchTiles = this.detectMatchTiles();
        }

        if (this.matchBlocks == null && this.matchTiles == null) {
            Config.warn("No matchBlocks or matchTiles specified: " + path);
            return false;
        }

        if (this.metadatas != null) {
            Config.warn("Metadata is not supported: " + Config.arrayToString(this.metadatas));
            return false;
        }

        if (this.method == 0) {
            Config.warn("No method: " + path);
            return false;
        }

        if (this.tiles != null && this.tiles.length > 0) {
            if (this.connect == 0) {
                this.connect = this.detectConnect();
            }

            if (this.connect == 128) {
                Config.warn("Invalid connect in: " + path);
                return false;
            }

            if (this.renderPass > 0) {
                Config.warn("Render pass not supported: " + this.renderPass);
                return false;
            }

            if ((this.faces & 128) != 0) {
                Config.warn("Invalid faces in: " + path);
                return false;
            }

            if ((this.symmetry & 128) != 0) {
                Config.warn("Invalid symmetry in: " + path);
                return false;
            }

            switch (this.method) {
                case 1:
                    return this.isValidCtm(path);
                case 2:
                    return this.isValidHorizontal(path);
                case 3:
                    return this.isValidTop(path);
                case 4:
                    return this.isValidRandom(path);
                case 5:
                    return this.isValidRepeat(path);
                case 6:
                    return this.isValidVertical(path);
                case 7:
                    return this.isValidFixed(path);
                case 8:
                    return this.isValidHorizontalVertical(path);
                case 9:
                    return this.isValidVerticalHorizontal(path);
                case 10:
                    return this.isValidCtmCompact(path);
                case 11:
                    return this.isValidOverlay(path);
                case 12:
                    return this.isValidOverlayFixed(path);
                case 13:
                    return this.isValidOverlayRandom(path);
                case 14:
                    return this.isValidOverlayRepeat(path);
                case 15:
                    return this.isValidOverlayCtm(path);
                default:
                    Config.warn("Unknown method: " + path);
                    return false;
            }
        } else {
            Config.warn("No tiles specified: " + path);
            return false;
        }
    }

    private int detectConnect() {
        if (this.matchBlocks != null) {
            return 1;
        } else {
            return this.matchTiles != null ? 2 : 128;
        }
    }

    private MatchBlock[] detectMatchBlocks() {
        int[] ids = this.detectMatchBlockIds();
        if (ids == null) {
            return null;
        }

        MatchBlock[] mbs = new MatchBlock[ids.length];

        for (int i = 0; i < mbs.length; i++) {
            mbs[i] = new MatchBlock(ids[i]);
        }

        return mbs;
    }

    private int[] detectMatchBlockIds() {
        String prefixBlock = "block_";
        if (!this.name.startsWith(prefixBlock)) {
            return null;
        }

        String blockName = this.name.substring(prefixBlock.length());
        Identifier loc = new Identifier(blockName);
        Block block = BlockUtils.getBlock(loc);
        if (block == null) {
            return null;
        }

        int id = BlockUtils.getBlockId(block);
        return new int[]{id};
    }

    private String[] detectMatchTiles() {
        TextureAtlasSprite icon = getIcon(this.name);
        return icon == null ? null : new String[]{this.name};
    }

    private static TextureAtlasSprite getIcon(String iconName) {
        TextureAtlas textureMapBlocks = Config.getTextureMapBlocks();
        TextureAtlasSprite icon = textureMapBlocks.getRegisteredSprite(iconName);
        return icon != null ? icon : textureMapBlocks.getRegisteredSprite("block/" + iconName);
    }

    private boolean isValidCtm(String path) {
        if (this.tiles == null) {
            this.tiles = this.parseTileNames("0-11 16-27 32-43 48-58");
        }

        if (this.tiles.length < 47) {
            Config.warn("Invalid tiles, must be at least 47: " + path);
            return false;
        } else {
            return true;
        }
    }

    private boolean isValidCtmCompact(String path) {
        if (this.tiles == null) {
            this.tiles = this.parseTileNames("0-4");
        }

        if (this.tiles.length < 5) {
            Config.warn("Invalid tiles, must be at least 5: " + path);
            return false;
        } else {
            return true;
        }
    }

    private boolean isValidOverlay(String path) {
        if (this.tiles == null) {
            this.tiles = this.parseTileNames("0-16");
        }

        if (this.tiles.length < 17) {
            Config.warn("Invalid tiles, must be at least 17: " + path);
            return false;
        }

        if (this.layer != null && this.layer != ChunkSectionLayer.SOLID) {
            return true;
        }

        Config.warn("Invalid overlay layer: " + this.layer);
        return false;
    }

    private boolean isValidOverlayFixed(String path) {
        if (!this.isValidFixed(path)) {
            return false;
        }

        if (this.layer != null && this.layer != ChunkSectionLayer.SOLID) {
            return true;
        }

        Config.warn("Invalid overlay layer: " + this.layer);
        return false;
    }

    private boolean isValidOverlayRandom(String path) {
        if (!this.isValidRandom(path)) {
            return false;
        }

        if (this.layer != null && this.layer != ChunkSectionLayer.SOLID) {
            return true;
        }

        Config.warn("Invalid overlay layer: " + this.layer);
        return false;
    }

    private boolean isValidOverlayRepeat(String path) {
        if (!this.isValidRepeat(path)) {
            return false;
        }

        if (this.layer != null && this.layer != ChunkSectionLayer.SOLID) {
            return true;
        }

        Config.warn("Invalid overlay layer: " + this.layer);
        return false;
    }

    private boolean isValidOverlayCtm(String path) {
        if (!this.isValidCtm(path)) {
            return false;
        }

        if (this.layer != null && this.layer != ChunkSectionLayer.SOLID) {
            return true;
        }

        Config.warn("Invalid overlay layer: " + this.layer);
        return false;
    }

    private boolean isValidHorizontal(String path) {
        if (this.tiles == null) {
            this.tiles = this.parseTileNames("12-15");
        }

        if (this.tiles.length != 4) {
            Config.warn("Invalid tiles, must be exactly 4: " + path);
            return false;
        } else {
            return true;
        }
    }

    private boolean isValidVertical(String path) {
        if (this.tiles == null) {
            Config.warn("No tiles defined for vertical: " + path);
            return false;
        } else if (this.tiles.length != 4) {
            Config.warn("Invalid tiles, must be exactly 4: " + path);
            return false;
        } else {
            return true;
        }
    }

    private boolean isValidHorizontalVertical(String path) {
        if (this.tiles == null) {
            Config.warn("No tiles defined for horizontal+vertical: " + path);
            return false;
        } else if (this.tiles.length != 7) {
            Config.warn("Invalid tiles, must be exactly 7: " + path);
            return false;
        } else {
            return true;
        }
    }

    private boolean isValidVerticalHorizontal(String path) {
        if (this.tiles == null) {
            Config.warn("No tiles defined for vertical+horizontal: " + path);
            return false;
        } else if (this.tiles.length != 7) {
            Config.warn("Invalid tiles, must be exactly 7: " + path);
            return false;
        } else {
            return true;
        }
    }

    private boolean isValidRandom(String path) {
        if (this.tiles != null && this.tiles.length > 0) {
            if (this.weights != null) {
                if (this.weights.length > this.tiles.length) {
                    Config.warn("More weights defined than tiles, trimming weights: " + path);
                    int[] weights2 = new int[this.tiles.length];
                    System.arraycopy(this.weights, 0, weights2, 0, weights2.length);
                    this.weights = weights2;
                }

                if (this.weights.length < this.tiles.length) {
                    Config.warn("Less weights defined than tiles, expanding weights: " + path);
                    int[] weights2 = new int[this.tiles.length];
                    System.arraycopy(this.weights, 0, weights2, 0, this.weights.length);
                    int avgWeight = MathUtils.getAverage(this.weights);

                    for (int i = this.weights.length; i < weights2.length; i++) {
                        weights2[i] = avgWeight;
                    }

                    this.weights = weights2;
                }

                this.sumWeights = new int[this.weights.length];
                int sum = 0;

                for (int i = 0; i < this.weights.length; i++) {
                    sum += this.weights[i];
                    this.sumWeights[i] = sum;
                }

                this.sumAllWeights = sum;
                if (this.sumAllWeights <= 0) {
                    Config.warn("Invalid sum of all weights: " + sum);
                    this.sumAllWeights = 1;
                }
            }

            if (this.randomLoops >= 0 && this.randomLoops <= 9) {
                return true;
            }

            Config.warn("Invalid randomLoops: " + this.randomLoops);
            return false;
        } else {
            Config.warn("Tiles not defined: " + path);
            return false;
        }
    }

    private boolean isValidRepeat(String path) {
        if (this.tiles == null) {
            Config.warn("Tiles not defined: " + path);
            return false;
        } else if (this.width <= 0) {
            Config.warn("Invalid width: " + path);
            return false;
        } else if (this.height <= 0) {
            Config.warn("Invalid height: " + path);
            return false;
        } else if (this.tiles.length != this.width * this.height) {
            Config.warn("Number of tiles does not equal width x height: " + path);
            return false;
        } else {
            return true;
        }
    }

    private boolean isValidFixed(String path) {
        if (this.tiles == null) {
            Config.warn("Tiles not defined: " + path);
            return false;
        } else if (this.tiles.length != 1) {
            Config.warn("Number of tiles should be 1 for method: fixed.");
            return false;
        } else {
            return true;
        }
    }

    private boolean isValidTop(String path) {
        if (this.tiles == null) {
            this.tiles = this.parseTileNames("66");
        }

        if (this.tiles.length != 1) {
            Config.warn("Invalid tiles, must be exactly 1: " + path);
            return false;
        } else {
            return true;
        }
    }

    public void updateIcons(TextureAtlas textureMap) {
        if (this.matchTiles != null) {
            this.matchTileIcons = registerIcons(this.matchTiles, textureMap, false, false);
        }

        if (this.connectTiles != null) {
            this.connectTileIcons = registerIcons(this.connectTiles, textureMap, false, false);
        }

        if (this.tiles != null) {
            this.tileIcons = registerIcons(this.tiles, textureMap, true, !isMethodOverlay(this.method));
        }
    }

    public void refreshIcons(TextureAtlas textureMap) {
        this.refreshIcons(this.matchTileIcons, textureMap);
        this.refreshIcons(this.connectTileIcons, textureMap);
        this.refreshIcons(this.tileIcons, textureMap);
    }

    private void refreshIcons(TextureAtlasSprite[] sprites, TextureAtlas textureMap) {
        if (sprites != null) {
            for (int i = 0; i < sprites.length; i++) {
                TextureAtlasSprite sprite = sprites[i];
                if (sprite != null && sprite != ConnectedTextures.SPRITE_DEFAULT) {
                    Identifier loc = sprite.getName();
                    TextureAtlasSprite spriteNew = textureMap.getSprite(loc);
                    if (spriteNew == null || MissingTextureAtlasSprite.isMisingSprite(spriteNew)) {
                        Config.warn("Missing CTM sprite: " + loc + ", properties: " + this.basePath);
                    }

                    sprites[i] = spriteNew;
                }
            }
        }
    }

    private static boolean isMethodOverlay(int method) {
        switch (method) {
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
                return true;
            default:
                return false;
        }
    }

    private static TextureAtlasSprite[] registerIcons(String[] tileNames, TextureAtlas textureMap, boolean skipTiles, boolean defaultTiles) {
        if (tileNames == null) {
            return null;
        }

        List iconList = new ArrayList();

        for (int i = 0; i < tileNames.length; i++) {
            String iconName = tileNames[i];
            Identifier resLoc = new Identifier(iconName);
            String domain = resLoc.getNamespace();
            String path = resLoc.getPath();
            if (!path.contains("/")) {
                path = "textures/block/" + path;
            }

            String filePath = path + ".png";
            if (skipTiles && filePath.endsWith("<skip>.png")) {
                iconList.add(null);
            } else if (defaultTiles && filePath.endsWith("<default>.png")) {
                iconList.add(ConnectedTextures.SPRITE_DEFAULT);
            } else {
                Identifier locFile = new Identifier(domain, filePath);
                boolean exists = Config.hasResource(locFile);
                if (!exists) {
                    Config.warn("File not found: " + filePath);
                }

                String prefixTextures = "textures/";
                String pathSprite = path;
                if (pathSprite.startsWith(prefixTextures)) {
                    pathSprite = pathSprite.substring(prefixTextures.length());
                }

                Identifier locSprite = new Identifier(domain, pathSprite);
                TextureAtlasSprite icon = textureMap.registerSprite(locSprite);
                iconList.add(icon);
            }
        }

        return (TextureAtlasSprite[])iconList.toArray(new TextureAtlasSprite[iconList.size()]);
    }

    public boolean matchesBlockId(int blockId) {
        return Matches.blockId(blockId, this.matchBlocks);
    }

    public boolean matchesBlock(int blockId, int metadata) {
        return Matches.block(blockId, metadata, this.matchBlocks);
    }

    public boolean matchesIcon(TextureAtlasSprite icon) {
        return Matches.sprite(icon, this.matchTileIcons);
    }

    @Override
    public String toString() {
        return "CTM name: "
            + this.name
            + ", basePath: "
            + this.basePath
            + ", matchBlocks: "
            + Config.arrayToString(this.matchBlocks)
            + ", matchTiles: "
            + Config.arrayToString(this.matchTiles);
    }

    public boolean matchesBiome(Biome biome) {
        return Matches.biome(biome, this.biomes);
    }

}
