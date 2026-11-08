package net.optifine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import net.minecraft.resources.Identifier;
import net.optifine.config.ConnectedParser;
import net.optifine.util.PropertiesOrdered;
import net.optifine.util.StrUtils;

public class RandomEntityProperties<T> {
    private String name = null;
    private String basePath = null;
    private RandomEntityContext<T> context;
    private T[] resources = null;
    private RandomEntityRule<T>[] rules = null;
    private int matchingRuleIndex = -1;

    public RandomEntityProperties(String name, Identifier baseLoc, int[] variants, RandomEntityContext<T> context) {
        ConnectedParser cp = new ConnectedParser(context.getName());
        this.name = name;
        this.basePath = cp.parseBasePath(baseLoc.getPath());
        this.context = context;
        this.resources = (T[])(new Object[variants.length]);

        for (int i = 0; i < variants.length; i++) {
            int index = variants[i];
            this.resources[i] = context.makeResource(name, baseLoc, index);
        }
    }

    public RandomEntityProperties(Properties props, String path, Identifier baseResLoc, RandomEntityContext<T> context) {
        ConnectedParser cp = context.getConnectedParser();
        this.name = cp.parseName(path);
        this.basePath = cp.parseBasePath(path);
        this.context = context;
        this.rules = this.parseRules(props, path, baseResLoc);
    }

    public String getName() {
        return this.name;
    }

    public String getBasePath() {
        return this.basePath;
    }

    public T[] getResources() {
        return this.resources;
    }

    public List<T> getAllResources() {
        List<T> list = new ArrayList<>();
        if (this.resources != null) {
            list.addAll(Arrays.asList(this.resources));
        }

        if (this.rules != null) {
            for (int i = 0; i < this.rules.length; i++) {
                RandomEntityRule<T> rule = this.rules[i];
                if (rule.getResources() != null) {
                    list.addAll(Arrays.asList(rule.getResources()));
                }
            }
        }

        return list;
    }

    public T getResource(IRandomEntity randomEntity, T resDef) {
        this.matchingRuleIndex = 0;
        if (this.rules != null) {
            for (int i = 0; i < this.rules.length; i++) {
                RandomEntityRule<T> rule = this.rules[i];
                if (rule.matches(randomEntity)) {
                    this.matchingRuleIndex = rule.getIndex();
                    int randomId = getRandomId(randomEntity, rule.isSeedSourceVehicle(), rule.getSeedOffset());
                    return rule.getResource(randomId, resDef);
                }
            }
        }

        if (this.resources != null) {
            int randomId = getRandomId(randomEntity, false, 0);
            int index = randomId % this.resources.length;
            return this.resources[index];
        } else {
            return resDef;
        }
    }

    private static int getRandomId(IRandomEntity randomEntityIn, boolean sourceVehicleIn, int seedOffsetIn) {
        int randomId = sourceVehicleIn ? randomEntityIn.getVehicleId() : randomEntityIn.getId();
        if (seedOffsetIn != 0) {
            randomId ^= Config.intHash(seedOffsetIn);
        }

        return randomId;
    }

    private RandomEntityRule<T>[] parseRules(Properties props, String pathProps, Identifier baseResLoc) {
        List list = new ArrayList();
        SortedMap<Integer, String> mapIndexTextures = this.collectIndexTextures(props);

        for (Entry<Integer, String> entry : mapIndexTextures.entrySet()) {
            int index = entry.getKey();
            String valTextures = entry.getValue();
            if (valTextures != null) {
                RandomEntityRule<T> rule = new RandomEntityRule<>(props, pathProps, baseResLoc, index, valTextures, this.context);
                list.add(rule);
            }
        }

        return (RandomEntityRule<T>[])list.toArray(new RandomEntityRule[list.size()]);
    }

    private SortedMap<Integer, String> collectIndexTextures(Properties props) {
        String[] keyPrefixes = this.context.getResourceKeys();
        SortedMap<Integer, String> indexMap = new TreeMap<>();

        for (Entry entry : props.entrySet()) {
            String entryKey = (String)entry.getKey();
            String entryKeyNoPrefix = StrUtils.removePrefix(entryKey, keyPrefixes);
            if (!Config.equals(entryKey, entryKeyNoPrefix) && entryKeyNoPrefix.startsWith(".")) {
                String indexStr = entryKeyNoPrefix.substring(1);
                int index = Config.parseInt(indexStr, 0);
                if (index > 0) {
                    String entryVal = (String)entry.getValue();
                    indexMap.put(index, entryVal);
                }
            }
        }

        return indexMap;
    }

    public boolean isValid(String path) {
        String resNamePlural = this.context.getResourceNamePlural();
        if (this.resources == null && this.rules == null) {
            Config.warn("No " + resNamePlural + " specified: " + path);
            return false;
        }

        if (this.rules != null) {
            for (int i = 0; i < this.rules.length; i++) {
                RandomEntityRule rule = this.rules[i];
                if (!rule.isValid(path)) {
                    return false;
                }
            }
        }

        if (this.resources != null) {
            for (int i = 0; i < this.resources.length; i++) {
                T res = this.resources[i];
                if (res == null) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean isDefault() {
        return this.rules != null ? false : this.resources == null;
    }

    public int getMatchingRuleIndex() {
        return this.matchingRuleIndex;
    }

    public static RandomEntityProperties parse(Identifier propLoc, Identifier resLoc, RandomEntityContext context) {
        String contextName = context.getName();

        try {
            String path = propLoc.getPath();
            Config.dbg(contextName + ": " + resLoc.getPath() + ", properties: " + path);
            InputStream in = Config.getResourceStream(propLoc);
            if (in == null) {
                Config.warn(contextName + ": Properties not found: " + path);
                return null;
            } else {
                Properties props = new PropertiesOrdered();
                props.load(in);
                in.close();
                RandomEntityProperties rep = new RandomEntityProperties(props, path, resLoc, context);
                return !rep.isValid(path) ? null : rep;
            }
        } catch (FileNotFoundException e) {
            Config.warn(contextName + ": File not found: " + propLoc.getPath());
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return this.name + ", path: " + this.basePath;
    }
}
