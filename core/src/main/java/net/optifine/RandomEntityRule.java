package net.optifine;

import java.util.Properties;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.optifine.config.BiomeId;
import net.optifine.config.ConnectedParser;
import net.optifine.config.MatchBlock;
import net.optifine.config.MatchProfession;
import net.optifine.config.Matches;
import net.optifine.config.NbtTagValue;
import net.optifine.config.RangeInt;
import net.optifine.config.RangeListInt;
import net.optifine.config.Weather;
import net.optifine.util.ArrayUtils;
import net.optifine.util.MathUtils;
import net.optifine.util.WorldUtils;

public class RandomEntityRule<T> {
    private String pathProps = null;
    private Identifier baseResLoc = null;
    private int index;
    private RandomEntityContext<T> context;
    private int[] textures = null;
    private T[] resources = null;
    private int[] weights = null;
    private boolean seedSourceVehicle;
    private int seedOffset;
    private BiomeId[] biomes = null;
    private RangeListInt heights = null;
    private RangeListInt healthRange = null;
    private boolean healthPercent = false;
    private NbtTagValue nbtName = null;
    public int[] sumWeights = null;
    public int sumAllWeights = 1;
    private MatchProfession[] professions = null;
    private DyeColor[] colors = null;
    private Boolean baby = null;
    private RangeListInt moonPhases = null;
    private RangeListInt dayTimes = null;
    private Weather[] weatherList = null;
    private RangeListInt sizes = null;
    public NbtTagValue[] nbtTagValues = null;
    public MatchBlock[] blocks = null;

    public RandomEntityRule(Properties props, String pathProps, Identifier baseResLoc, int index, String valTextures, RandomEntityContext<T> context) {
        this.pathProps = pathProps;
        this.baseResLoc = baseResLoc;
        this.index = index;
        this.context = context;
        ConnectedParser cp = context.getConnectedParser();
        this.textures = cp.parseIntList(valTextures);
        this.weights = cp.parseIntList(props.getProperty("weights." + index));
        this.seedSourceVehicle = Config.equals(props.getProperty("seedSource." + index), "vehicle");
        this.seedOffset = cp.parseInt(props.getProperty("seedOffset." + index), 0);
        this.biomes = cp.parseBiomes(props.getProperty("biomes." + index));
        this.heights = cp.parseRangeListIntNeg(props.getProperty("heights." + index));
        if (this.heights == null) {
            this.heights = this.parseMinMaxHeight(props, index);
        }

        String healthStr = props.getProperty("health." + index);
        if (healthStr != null) {
            this.healthPercent = healthStr.contains("%");
            healthStr = healthStr.replace("%", "");
            this.healthRange = cp.parseRangeListInt(healthStr);
        }

        this.nbtName = cp.parseNbtTagValue("name", props.getProperty("name." + index));
        this.professions = cp.parseProfessions(props.getProperty("professions." + index));
        this.colors = cp.parseDyeColors(props.getProperty("colors." + index), "color", ConnectedParser.DYE_COLORS_INVALID);
        if (this.colors == null) {
            this.colors = cp.parseDyeColors(props.getProperty("collarColors." + index), "collar color", ConnectedParser.DYE_COLORS_INVALID);
        }

        this.baby = cp.parseBooleanObject(props.getProperty("baby." + index));
        this.moonPhases = cp.parseRangeListInt(props.getProperty("moonPhase." + index));
        this.dayTimes = cp.parseRangeListInt(props.getProperty("dayTime." + index));
        this.weatherList = cp.parseWeather(props.getProperty("weather." + index), "weather." + index, null);
        this.sizes = cp.parseRangeListInt(props.getProperty("sizes." + index));
        this.nbtTagValues = cp.parseNbtTagValues(props, "nbt." + index + ".");
        this.blocks = cp.parseMatchBlocks(props.getProperty("blocks." + index));
    }

    public int getIndex() {
        return this.index;
    }

    private RangeListInt parseMinMaxHeight(Properties props, int index) {
        String minHeightStr = props.getProperty("minHeight." + index);
        String maxHeightStr = props.getProperty("maxHeight." + index);
        if (minHeightStr == null && maxHeightStr == null) {
            return null;
        }

        int minHeight = 0;
        if (minHeightStr != null) {
            minHeight = Config.parseInt(minHeightStr, -1);
            if (minHeight < 0) {
                Config.warn("Invalid minHeight: " + minHeightStr);
                return null;
            }
        }

        int maxHeight = 256;
        if (maxHeightStr != null) {
            maxHeight = Config.parseInt(maxHeightStr, -1);
            if (maxHeight < 0) {
                Config.warn("Invalid maxHeight: " + maxHeightStr);
                return null;
            }
        }

        if (maxHeight < 0) {
            Config.warn("Invalid minHeight, maxHeight: " + minHeightStr + ", " + maxHeightStr);
            return null;
        } else {
            RangeListInt list = new RangeListInt();
            list.addRange(new RangeInt(minHeight, maxHeight));
            return list;
        }
    }

    public boolean isValid(String path) {
        String resourceName = this.context.getResourceName();
        String resourceNamePlural = this.context.getResourceNamePlural();
        if (this.textures != null && this.textures.length != 0) {
            this.resources = (T[])(new Object[this.textures.length]);

            for (int i = 0; i < this.textures.length; i++) {
                int index = this.textures[i];
                String name = this.context.getConnectedParser().parseName(this.baseResLoc.getPath());
                T res = this.context.makeResource(name, this.baseResLoc, index);
                if (res == null) {
                    return false;
                }

                this.resources[i] = res;
            }

            if (this.weights != null) {
                if (this.weights.length > this.resources.length) {
                    Config.warn("More weights defined than " + resourceNamePlural + ", trimming weights: " + path);
                    int[] weights2 = new int[this.resources.length];
                    System.arraycopy(this.weights, 0, weights2, 0, weights2.length);
                    this.weights = weights2;
                }

                if (this.weights.length < this.resources.length) {
                    Config.warn("Less weights defined than " + resourceNamePlural + ", expanding weights: " + path);
                    int[] weights2 = new int[this.resources.length];
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
                    if (this.weights[i] < 0) {
                        Config.warn("Invalid weight: " + this.weights[i]);
                        return false;
                    }

                    sum += this.weights[i];
                    this.sumWeights[i] = sum;
                }

                this.sumAllWeights = sum;
                if (this.sumAllWeights <= 0) {
                    Config.warn("Invalid sum of all weights: " + sum);
                    this.sumAllWeights = 1;
                }
            }

            if (this.professions == ConnectedParser.PROFESSIONS_INVALID) {
                Config.warn("Invalid professions or careers: " + path);
                return false;
            } else if (this.colors == ConnectedParser.DYE_COLORS_INVALID) {
                Config.warn("Invalid colors: " + path);
                return false;
            } else {
                return true;
            }
        } else {
            Config.warn("Invalid " + resourceNamePlural + " for rule: " + this.index);
            return false;
        }
    }

    public boolean matches(IRandomEntity randomEntity) {
        if (this.biomes != null && !Matches.biome(randomEntity.getSpawnBiome(), this.biomes)) {
            return false;
        }

        if (this.heights != null) {
            BlockPos pos = randomEntity.getSpawnPosition();
            if (pos != null && !this.heights.isInRange(pos.getY())) {
                return false;
            }
        }

        if (this.healthRange != null) {
            int health = randomEntity.getHealth();
            if (this.healthPercent) {
                int healthMax = randomEntity.getMaxHealth();
                if (healthMax > 0) {
                    health = (int)((double)(health * 100) / healthMax);
                }
            }

            if (!this.healthRange.isInRange(health)) {
                return false;
            }
        }

        if (this.nbtName != null) {
            String name = randomEntity.getName();
            if (!this.nbtName.matchesValue(name)) {
                return false;
            }
        }

        if (this.professions != null && randomEntity instanceof RandomEntity rme && rme.getEntity() instanceof Villager entityVillager) {
            VillagerData vd = entityVillager.getVillagerData();
            VillagerProfession vp = vd.profession().value();
            int level = vd.level();
            if (!MatchProfession.matchesOne(vp, level, this.professions)) {
                return false;
            }
        }

        if (this.colors != null) {
            DyeColor col = randomEntity.getColor();
            if (col != null && !Config.equalsOne(col, this.colors)) {
                return false;
            }
        }

        if (this.baby != null
            && randomEntity instanceof RandomEntity rme
            && rme.getEntity() instanceof LivingEntity livingEntity
            && livingEntity.isBaby() != this.baby) {
            return false;
        } else {
            if (this.moonPhases != null) {
                Level world = Config.getMinecraft().level;
                if (world != null) {
                    int moonPhase = WorldUtils.getMoonPhaseInt();
                    if (!this.moonPhases.isInRange(moonPhase)) {
                        return false;
                    }
                }
            }

            if (this.dayTimes != null) {
                Level world = Config.getMinecraft().level;
                if (world != null) {
                    int dayTime = (int)(Config.getDayTime(world) % 24000L);
                    if (!this.dayTimes.isInRange(dayTime)) {
                        return false;
                    }
                }
            }

            if (this.weatherList != null) {
                Level world = Config.getMinecraft().level;
                if (world != null) {
                    Weather weather = Weather.getWeather(world, 0.0F);
                    if (!ArrayUtils.contains(this.weatherList, weather)) {
                        return false;
                    }
                }
            }

            if (this.sizes != null && randomEntity instanceof RandomEntity rme) {
                Entity entity = rme.getEntity();
                int size = this.getEntitySize(entity);
                if (size >= 0 && !this.sizes.isInRange(size)) {
                    return false;
                }
            }

            if (this.nbtTagValues != null) {
                CompoundTag nbt = randomEntity.getNbtTag();
                if (nbt != null) {
                    for (int i = 0; i < this.nbtTagValues.length; i++) {
                        NbtTagValue ntv = this.nbtTagValues[i];
                        if (!ntv.matches(nbt)) {
                            return false;
                        }
                    }
                }
            }

            if (this.blocks != null) {
                BlockState blockState = randomEntity.getBlockState();
                if (blockState != null && !Matches.block(blockState, this.blocks)) {
                    return false;
                }
            }

            return true;
        }
    }

    public static DyeColor getEntityColor(Entity entity) {
        if (entity instanceof Wolf entityWolf) {
            return !entityWolf.isTame() ? null : entityWolf.getCollarColor();
        } else if (entity instanceof Cat entityCat) {
            return !entityCat.isTame() ? null : entityCat.getCollarColor();
        } else if (entity instanceof Sheep entitySheep) {
            return entitySheep.getColor();
        } else {
            return entity instanceof Llama entityLlama ? getLlamaColor(entityLlama) : null;
        }
    }

    public static DyeColor getLlamaColor(Llama llama) {
        ItemStack itemStack = llama.getItemBySlot(EquipmentSlot.BODY);
        Block block = Block.byItem(itemStack.getItem());
        return block instanceof WoolCarpetBlock ? ((WoolCarpetBlock)block).getColor() : null;
    }

    public static DyeColor getBlockEntityColor(BlockEntity entity) {
        return entity instanceof ShulkerBoxBlockEntity entityShulkerBox ? entityShulkerBox.getColor() : null;
    }

    private int getEntitySize(Entity entity) {
        if (entity instanceof Slime entitySlime) {
            return entitySlime.getSize() - 1;
        } else {
            return entity instanceof Phantom entityPhantom ? entityPhantom.getPhantomSize() : -1;
        }
    }

    public T getResource(int randomId, T resDef) {
        if (this.resources != null && this.resources.length != 0) {
            int index = this.getResourceIndex(randomId);
            return this.resources[index];
        } else {
            return resDef;
        }
    }

    private int getResourceIndex(int randomId) {
        int index = 0;
        if (this.weights == null) {
            index = randomId % this.resources.length;
        } else {
            int randWeight = randomId % this.sumAllWeights;

            for (int i = 0; i < this.sumWeights.length; i++) {
                if (this.sumWeights[i] > randWeight) {
                    index = i;
                    break;
                }
            }
        }

        return index;
    }

    public T[] getResources() {
        return this.resources;
    }

    public boolean isSeedSourceVehicle() {
        return this.seedSourceVehicle;
    }

    public int getSeedOffset() {
        return this.seedOffset;
    }

    @Override
    public String toString() {
        return this.pathProps + ", index: " + this.index + ", textures: " + ArrayUtils.arrayToString(this.textures);
    }
}
