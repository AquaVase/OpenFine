package net.optifine.reflect;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.optifine.Log;
import net.optifine.util.StrUtils;
import org.joml.Vector3f;

public class ReflectorForge {
    public static final boolean FORGE_ENTITY_CAN_UPDATE = Reflector.IForgeEntity_canUpdate.exists();

    public static void putLaunchBlackboard(String key, Object value) {
        Map blackboard = (Map)Reflector.getFieldValue(Reflector.Launch_blackboard);
        if (blackboard != null) {
            blackboard.put(key, value);
        }
    }

    public static InputStream getOptiFineResourceStream(String path) {
        if (!Reflector.OptiFineResourceLocator.exists()) {
            return null;
        }

        path = StrUtils.removePrefix(path, "/");
        return (InputStream)Reflector.call(Reflector.OptiFineResourceLocator_getOptiFineResourceStream, path);
    }

    public static ReflectorClass getReflectorClassOptiFineResourceLocator() {
        String className = "optifine.OptiFineResourceLocator";
        return System.getProperties().get(className + ".class") instanceof Class cls ? new ReflectorClass(cls) : new ReflectorClass(className);
    }

    public static String[] getForgeModIds() {
        if (!Reflector.ModList.exists()) {
            return new String[0];
        }

        Object modList = Reflector.ModList_get.call();
        List listMods = (List)Reflector.getFieldValue(modList, Reflector.ModList_mods);
        if (listMods == null) {
            return new String[0];
        }

        List<String> listModIds = new ArrayList<>();

        for (Object modContainer : listMods) {
            if (Reflector.ModContainer.isInstance(modContainer)) {
                String modId = Reflector.callString(modContainer, Reflector.ModContainer_getModId);
                if (modId != null && !modId.equals("minecraft") && !modId.equals("forge")) {
                    listModIds.add(modId);
                }
            }
        }

        return listModIds.toArray(new String[listModIds.size()]);
    }

    public static Button makeButtonMods(TitleScreen guiMainMenu, int yIn, int rowHeightIn) {
        return !Reflector.ModListScreen_Constructor.exists() ? null : Button.builder(Component.translatable("fml.menu.mods"), button -> {
            Screen modListScreen = (Screen)Reflector.ModListScreen_Constructor.newInstance(guiMainMenu);
            Minecraft.getInstance().gui.setScreen(modListScreen);
        }).pos(guiMainMenu.width / 2 - 100, yIn).size(98, 20).build();
    }

    public static void setForgeLightPipelineEnabled(boolean value) {
    }

    public static boolean getConfigClientBoolean(ReflectorField configField, boolean def) {
        if (!configField.exists()) {
            return def;
        }

        Object client = Reflector.ForgeConfig_CLIENT.getValue();
        if (client == null) {
            return def;
        }

        Object configValue = Reflector.getFieldValue(client, configField);
        return configValue == null ? def : Reflector.callBoolean(configValue, Reflector.ForgeConfigSpec_ConfigValue_get);
    }

    private static void setConfigClientBoolean(ReflectorField clientField, final boolean value) {
        if (clientField.exists()) {
            Object client = Reflector.ForgeConfig_CLIENT.getValue();
            if (client != null) {
                Object configValue = Reflector.getFieldValue(client, clientField);
                if (configValue != null) {
                    Supplier<Boolean> bs = new Supplier<Boolean>() {
                        public Boolean get() {
                            return value;
                        }
                    };
                    Reflector.setFieldValue(configValue, Reflector.ForgeConfigSpec_ConfigValue_defaultSupplier, bs);
                    Object spec = Reflector.getFieldValue(configValue, Reflector.ForgeConfigSpec_ConfigValue_spec);
                    if (spec != null) {
                        Reflector.setFieldValue(spec, Reflector.ForgeConfigSpec_childConfig, null);
                    }

                    Log.dbg("Set ForgeConfig.CLIENT." + clientField.getTargetField().getName() + "=" + value);
                }
            }
        }
    }

    public static boolean canUpdate(Entity entity) {
        return FORGE_ENTITY_CAN_UPDATE ? Reflector.callBoolean(entity, Reflector.IForgeEntity_canUpdate) : true;
    }

    private static Vector3f getVertexPos(int[] data, int vertex) {
        int step = data.length / 4;
        int idx = vertex * step;
        float x = Float.intBitsToFloat(data[idx]);
        float y = Float.intBitsToFloat(data[idx + 1]);
        float z = Float.intBitsToFloat(data[idx + 2]);
        return new Vector3f(x, y, z);
    }

    public static void postModLoaderEvent(ReflectorConstructor constr, Object... params) {
        Object event = Reflector.newInstance(constr, params);
        if (event != null) {
            postModLoaderEvent(event);
        }
    }

    public static void postModLoaderEvent(Object event) {
        if (event != null) {
            Object modLoader = Reflector.ModLoader_get.call();
            if (modLoader != null) {
                Reflector.callVoid(modLoader, Reflector.ModLoader_postEvent, event);
            }
        }
    }
}
