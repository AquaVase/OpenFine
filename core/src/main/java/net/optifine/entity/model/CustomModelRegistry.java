package net.optifine.entity.model;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.CopperGolemStatueBlock.Pose;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.optifine.Config;
import net.optifine.util.Either;
import net.optifine.util.TileEntityUtils;

public class CustomModelRegistry {
    private static Map<String, ModelAdapter> mapModelAdapters = makeMapModelAdapters();

    private static Map<String, ModelAdapter> makeMapModelAdapters() {
        Map<String, ModelAdapter> map = new LinkedHashMap<>();
        addModelAdapter(map, new ModelAdapterAllay());
        addModelAdapter(map, new ModelAdapterArmadillo());
        addModelAdapter(map, new ModelAdapterArmorStand());
        addModelAdapter(map, new ModelAdapterArrow());
        addModelAdapter(map, new ModelAdapterAxolotl());
        addModelAdapter(map, new ModelAdapterBat());
        addModelAdapter(map, new ModelAdapterBee());
        addModelAdapter(map, new ModelAdapterBlaze());
        addModelAdapter(map, new ModelAdapterBogged());
        addModelAdapter(map, new ModelAdapterBreeze());
        addModelAdapter(map, new ModelAdapterBreezeWindCharge());
        addModelAdapter(map, new ModelAdapterCamel());
        addModelAdapter(map, new ModelAdapterCamelHusk());
        addModelAdapter(map, new ModelAdapterCat());
        addModelAdapter(map, new ModelAdapterCaveSpider());
        addModelAdapter(map, new ModelAdapterChicken());
        addModelAdapter(map, new ModelAdapterChickenCold());
        addModelAdapter(map, new ModelAdapterChickenWarm());
        addModelAdapter(map, new ModelAdapterCod());
        addModelAdapter(map, new ModelAdapterCopperGolem());
        addModelAdapter(map, new ModelAdapterCow());
        addModelAdapter(map, new ModelAdapterCowCold());
        addModelAdapter(map, new ModelAdapterCowWarm());
        addModelAdapter(map, new ModelAdapterCreaking());
        addModelAdapter(map, new ModelAdapterCreeper());
        addModelAdapter(map, new ModelAdapterDragon());
        addModelAdapter(map, new ModelAdapterDonkey());
        addModelAdapter(map, new ModelAdapterDolphin());
        addModelAdapter(map, new ModelAdapterDrowned());
        addModelAdapter(map, new ModelAdapterElderGuardian());
        addModelAdapter(map, new ModelAdapterEnderCrystal());
        addModelAdapter(map, new ModelAdapterEnderman());
        addModelAdapter(map, new ModelAdapterEndermite());
        addModelAdapter(map, new ModelAdapterEvoker());
        addModelAdapter(map, new ModelAdapterEvokerFangs());
        addModelAdapter(map, new ModelAdapterFox());
        addModelAdapter(map, new ModelAdapterFrog());
        addModelAdapter(map, new ModelAdapterGhast());
        addModelAdapter(map, new ModelAdapterGiant());
        addModelAdapter(map, new ModelAdapterGlowSquid());
        addModelAdapter(map, new ModelAdapterGoat());
        addModelAdapter(map, new ModelAdapterGuardian());
        addModelAdapter(map, new ModelAdapterHappyGhast());
        addModelAdapter(map, new ModelAdapterHappyGhastHarness());
        addModelAdapter(map, new ModelAdapterHappyGhastRopes());
        addModelAdapter(map, new ModelAdapterHoglin());
        addModelAdapter(map, new ModelAdapterHorse());
        addModelAdapter(map, new ModelAdapterHusk());
        addModelAdapter(map, new ModelAdapterIllusioner());
        addModelAdapter(map, new ModelAdapterIronGolem());
        addModelAdapter(map, new ModelAdapterLeadKnot());
        addModelAdapter(map, new ModelAdapterLlama());
        addModelAdapter(map, new ModelAdapterLlamaSpit());
        addModelAdapter(map, new ModelAdapterMagmaCube());
        addModelAdapter(map, new ModelAdapterMinecart());
        addModelAdapter(map, new ModelAdapterMinecartChest());
        addModelAdapter(map, new ModelAdapterMinecartCommandBlock());
        addModelAdapter(map, new ModelAdapterMinecartFurnace());
        addModelAdapter(map, new ModelAdapterMinecartHopper());
        addModelAdapter(map, new ModelAdapterMinecartTnt());
        addModelAdapter(map, new ModelAdapterMinecartMobSpawner());
        addModelAdapter(map, new ModelAdapterMooshroom());
        addModelAdapter(map, new ModelAdapterMule());
        addModelAdapter(map, new ModelAdapterNautilus());
        addModelAdapter(map, new ModelAdapterOcelot());
        addModelAdapter(map, new ModelAdapterPanda());
        addModelAdapter(map, new ModelAdapterParched());
        addModelAdapter(map, new ModelAdapterParrot());
        addModelAdapter(map, new ModelAdapterPhantom());
        addModelAdapter(map, new ModelAdapterPig());
        addModelAdapter(map, new ModelAdapterPigCold());
        addModelAdapter(map, new ModelAdapterPigWarm());
        addModelAdapter(map, new ModelAdapterPiglin());
        addModelAdapter(map, new ModelAdapterPiglinBrute());
        addModelAdapter(map, new ModelAdapterPolarBear());
        addModelAdapter(map, new ModelAdapterPillager());
        addModelAdapter(map, new ModelAdapterPufferFishBig());
        addModelAdapter(map, new ModelAdapterPufferFishMedium());
        addModelAdapter(map, new ModelAdapterPufferFishSmall());
        addModelAdapter(map, new ModelAdapterRabbit());
        addModelAdapter(map, new ModelAdapterRavager());
        addModelAdapter(map, new ModelAdapterSalmon());
        addModelAdapter(map, new ModelAdapterSalmonSmall());
        addModelAdapter(map, new ModelAdapterSalmonLarge());
        addModelAdapter(map, new ModelAdapterSheep());
        addModelAdapter(map, new ModelAdapterShulker());
        addModelAdapter(map, new ModelAdapterShulkerBullet());
        addModelAdapter(map, new ModelAdapterSilverfish());
        addModelAdapter(map, new ModelAdapterSkeleton());
        addModelAdapter(map, new ModelAdapterSkeletonHorse());
        addModelAdapter(map, new ModelAdapterSlime());
        addModelAdapter(map, new ModelAdapterSnowman());
        addModelAdapter(map, new ModelAdapterSniffer());
        addModelAdapter(map, new ModelAdapterSpectralArrow());
        addModelAdapter(map, new ModelAdapterSpider());
        addModelAdapter(map, new ModelAdapterSquid());
        addModelAdapter(map, new ModelAdapterStray());
        addModelAdapter(map, new ModelAdapterStrider());
        addModelAdapter(map, new ModelAdapterTadpole());
        addModelAdapter(map, new ModelAdapterTraderLlama());
        addModelAdapter(map, new ModelAdapterTrident());
        addModelAdapter(map, new ModelAdapterTropicalFishA());
        addModelAdapter(map, new ModelAdapterTropicalFishB());
        addModelAdapter(map, new ModelAdapterTurtle());
        addModelAdapter(map, new ModelAdapterVex());
        addModelAdapter(map, new ModelAdapterVillager());
        addModelAdapter(map, new ModelAdapterVindicator());
        addModelAdapter(map, new ModelAdapterWanderingTrader());
        addModelAdapter(map, new ModelAdapterWarden());
        addModelAdapter(map, new ModelAdapterWindCharge());
        addModelAdapter(map, new ModelAdapterWitch());
        addModelAdapter(map, new ModelAdapterWither());
        addModelAdapter(map, new ModelAdapterWitherSkeleton());
        addModelAdapter(map, new ModelAdapterWitherSkull());
        addModelAdapter(map, new ModelAdapterWolf());
        addModelAdapter(map, new ModelAdapterZoglin());
        addModelAdapter(map, new ModelAdapterZombie());
        addModelAdapter(map, new ModelAdapterZombieHorse());
        addModelAdapter(map, new ModelAdapterZombieNautilus());
        addModelAdapter(map, new ModelAdapterZombieNautilusCoral());
        addModelAdapter(map, new ModelAdapterZombieVillager());
        addModelAdapter(map, new ModelAdapterZombifiedPiglin());

        for (BoatType type : BoatType.values()) {
            addModelAdapter(map, new ModelAdapterBoat(type.getEntityType(), type.getName(), type.getModelLayer()));
        }

        for (BoatType type : BoatType.values()) {
            addModelAdapter(map, new ModelAdapterBoatPatch(type.getEntityType(), type.getName(), type.getModelLayer()));
        }

        for (ChestBoatType type : ChestBoatType.values()) {
            addModelAdapter(map, new ModelAdapterChestBoat(type.getEntityType(), type.getName(), type.getModelLayer()));
        }

        for (ChestBoatType type : ChestBoatType.values()) {
            addModelAdapter(map, new ModelAdapterChestBoatPatch(type.getEntityType(), type.getName(), type.getModelLayer()));
        }

        addModelAdapter(map, new ModelAdapterRaft());
        addModelAdapter(map, new ModelAdapterChestRaft());
        addModelAdapter(map, new ModelAdapterBoggedOuter());
        addModelAdapter(map, new ModelAdapterBreezeEyes());
        addModelAdapter(map, new ModelAdapterBreezeWind());
        addModelAdapter(map, new ModelAdapterCamelSaddle());
        addModelAdapter(map, new ModelAdapterCamelHuskSaddle());
        addModelAdapter(map, new ModelAdapterCatCollar());
        addModelAdapter(map, new ModelAdapterCreeperCharge());
        addModelAdapter(map, new ModelAdapterDonkeySaddle());
        addModelAdapter(map, new ModelAdapterDrownedOuter());
        addModelAdapter(map, new ModelAdapterHorseArmor());
        addModelAdapter(map, new ModelAdapterHorseSaddle());
        addModelAdapter(map, new ModelAdapterLlamaDecor());
        addModelAdapter(map, new ModelAdapterMuleSaddle());
        addModelAdapter(map, new ModelAdapterNautilusSaddle());
        addModelAdapter(map, new ModelAdapterPigSaddle());
        addModelAdapter(map, new ModelAdapterSheepWool());
        addModelAdapter(map, new ModelAdapterSheepWoolUndercoat());
        addModelAdapter(map, new ModelAdapterSkeletonHorseArmor());
        addModelAdapter(map, new ModelAdapterSkeletonHorseSaddle());
        addModelAdapter(map, new ModelAdapterSlimeOuter());
        addModelAdapter(map, new ModelAdapterStrayOuter());
        addModelAdapter(map, new ModelAdapterStriderSaddle());
        addModelAdapter(map, new ModelAdapterTraderLlamaDecor());
        addModelAdapter(map, new ModelAdapterTropicalFishPatternA());
        addModelAdapter(map, new ModelAdapterTropicalFishPatternB());
        addModelAdapter(map, new ModelAdapterVillagerNoHat());
        addModelAdapter(map, new ModelAdapterWitherArmor());
        addModelAdapter(map, new ModelAdapterWardenLayer(ModelLayers.WARDEN_BIOLUMINESCENT));
        addModelAdapter(map, new ModelAdapterWardenLayer(ModelLayers.WARDEN_PULSATING_SPOTS));
        addModelAdapter(map, new ModelAdapterWardenLayer(ModelLayers.WARDEN_TENDRILS));
        addModelAdapter(map, new ModelAdapterWardenLayer(ModelLayers.WARDEN_HEART));
        addModelAdapter(map, new ModelAdapterWolfArmor());
        addModelAdapter(map, new ModelAdapterWolfCollar());
        addModelAdapter(map, new ModelAdapterZombieHorseArmor());
        addModelAdapter(map, new ModelAdapterZombieHorseSaddle());
        addModelAdapter(map, new ModelAdapterZombieVillagerNoHat());
        addModelAdapter(map, new ModelAdapterBannerBase(true));
        addModelAdapter(map, new ModelAdapterBannerBase(false));
        addModelAdapter(map, new ModelAdapterBannerFlag(true));
        addModelAdapter(map, new ModelAdapterBannerFlag(false));
        addModelAdapter(map, new ModelAdapterBell());
        addModelAdapter(map, new ModelAdapterBook());
        addModelAdapter(map, new ModelAdapterBookLectern());
        addModelAdapter(map, new ModelAdapterChest());
        addModelAdapter(map, new ModelAdapterChestDoubleLeft());
        addModelAdapter(map, new ModelAdapterChestDoubleRight());
        addModelAdapter(map, new ModelAdapterConduit());
        addModelAdapter(map, new ModelAdapterDecoratedPot());
        addModelAdapter(map, new ModelAdapterEnderChest());
        addModelAdapter(map, new ModelAdapterHeadCreeper());
        addModelAdapter(map, new ModelAdapterHeadDragon());
        addModelAdapter(map, new ModelAdapterHeadPiglin());
        addModelAdapter(map, new ModelAdapterHeadPlayer());
        addModelAdapter(map, new ModelAdapterHeadSkeleton());
        addModelAdapter(map, new ModelAdapterHeadWitherSkeleton());
        addModelAdapter(map, new ModelAdapterHeadZombie());
        addModelAdapter(map, new ModelAdapterShulkerBox());
        addModelAdapter(map, new ModelAdapterTrappedChest());
        addModelAdapter(map, new ModelAdapterTrappedChestLeft());
        addModelAdapter(map, new ModelAdapterTrappedChestRight());

        for (Pose pose : Pose.values()) {
            addModelAdapter(map, new ModelAdapterCopperGolemStatue(pose));
        }

        addModelAdapter(map, new ModelAdapterBookScreen());
        addModelAdapter(map, new ModelAdapterParrotShoulder());
        addModelAdapter(map, new ModelAdapterShield());
        addModelAdapter(map, new ModelAdapterTridentHand());
        return map;
    }

    private static void addModelAdapter(Map<String, ModelAdapter> map, ModelAdapter modelAdapter) {
        try {
            addModelAdapterSingle(map, modelAdapter);
            if (modelAdapter instanceof IModelAdapterAgeable ageable) {
                ModelAdapter baby = ageable.makeBaby();
                if (baby != null) {
                    addModelAdapterSingle(map, baby);
                }
            }
        } catch (Exception e) {
            Config.warn("Error registering CEM model: " + modelAdapter.getName() + ", " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private static void addModelAdapterSingle(Map<String, ModelAdapter> map, ModelAdapter modelAdapter) {
        addModelAdapter(map, modelAdapter, modelAdapter.getName());
        Model model = modelAdapter.makeModel();
        String[] names = modelAdapter.getModelRendererNames();
        Set<ModelPart> parts = new HashSet<>();

        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            ModelPart mr = modelAdapter.getModelRenderer(model, name);
            if (mr == null) {
                Config.warn("Model renderer not found, model: " + modelAdapter.getName() + ", name: " + name);
            } else if (parts.contains(mr)) {
                Config.warn("Model renderer duplicate, model: " + modelAdapter.getName() + ", name: " + name);
            } else {
                parts.add(mr);
            }
        }
    }

    private static void addModelAdapter(Map<String, ModelAdapter> map, ModelAdapter modelAdapter, String name) {
        if (map.containsKey(name)) {
            String typeStr = "?";
            Either<EntityType, BlockEntityType> type = modelAdapter.getType();
            if (type == null) {
                typeStr = "";
            } else if (type.getLeft().isPresent()) {
                typeStr = type.getLeft().get().getDescriptionId();
            } else if (type.getRight().isPresent()) {
                typeStr = TileEntityUtils.getTypeKey(type.getRight().get()) + "";
            }

            Config.warn("Model adapter already registered for id: " + name + ", type: " + typeStr);
        }

        map.put(name, modelAdapter);
    }

    public static ModelAdapter getModelAdapter(String name) {
        return mapModelAdapters.get(name);
    }

    public static String[] getModelNames() {
        Set<String> setNames = mapModelAdapters.keySet();
        return setNames.toArray(new String[setNames.size()]);
    }

    public static String[] getAliases(String name) {
        ModelAdapter ma = getModelAdapter(name);
        return ma == null ? null : ma.getAliases();
    }
}
