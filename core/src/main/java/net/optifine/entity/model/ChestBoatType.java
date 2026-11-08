package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;

public enum ChestBoatType {
    OAK("oak", EntityTypes.OAK_CHEST_BOAT, ModelLayers.OAK_CHEST_BOAT),
    SPRUCE("spruce", EntityTypes.SPRUCE_CHEST_BOAT, ModelLayers.SPRUCE_CHEST_BOAT),
    BIRCH("birch", EntityTypes.BIRCH_CHEST_BOAT, ModelLayers.BIRCH_CHEST_BOAT),
    JUNGLE("jungle", EntityTypes.JUNGLE_CHEST_BOAT, ModelLayers.JUNGLE_CHEST_BOAT),
    ACACIA("acacia", EntityTypes.ACACIA_CHEST_BOAT, ModelLayers.ACACIA_CHEST_BOAT),
    CHERRY("cherry", EntityTypes.CHERRY_CHEST_BOAT, ModelLayers.CHERRY_CHEST_BOAT),
    DARK_OAK("dark_oak", EntityTypes.DARK_OAK_CHEST_BOAT, ModelLayers.DARK_OAK_CHEST_BOAT),
    PALE_OAK("pale_oak", EntityTypes.PALE_OAK_CHEST_BOAT, ModelLayers.PALE_OAK_CHEST_BOAT),
    MANGROVE("mangrove", EntityTypes.MANGROVE_CHEST_BOAT, ModelLayers.MANGROVE_CHEST_BOAT);

    private String name;
    private EntityType<ChestBoat> entityType;
    private ModelLayerLocation modelLayer;

    ChestBoatType(String name, EntityType<ChestBoat> entityType, ModelLayerLocation modelLayer) {
        this.name = name;
        this.entityType = entityType;
        this.modelLayer = modelLayer;
    }

    public String getName() {
        return this.name;
    }

    public EntityType<ChestBoat> getEntityType() {
        return this.entityType;
    }

    public ModelLayerLocation getModelLayer() {
        return this.modelLayer;
    }
}
