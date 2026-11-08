package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.vehicle.boat.Boat;

public enum BoatType {
    OAK("oak", EntityTypes.OAK_BOAT, ModelLayers.OAK_BOAT),
    SPRUCE("spruce", EntityTypes.SPRUCE_BOAT, ModelLayers.SPRUCE_BOAT),
    BIRCH("birch", EntityTypes.BIRCH_BOAT, ModelLayers.BIRCH_BOAT),
    JUNGLE("jungle", EntityTypes.JUNGLE_BOAT, ModelLayers.JUNGLE_BOAT),
    ACACIA("acacia", EntityTypes.ACACIA_BOAT, ModelLayers.ACACIA_BOAT),
    CHERRY("cherry", EntityTypes.CHERRY_BOAT, ModelLayers.CHERRY_BOAT),
    DARK_OAK("dark_oak", EntityTypes.DARK_OAK_BOAT, ModelLayers.DARK_OAK_BOAT),
    PALE_OAK("pale_oak", EntityTypes.PALE_OAK_BOAT, ModelLayers.PALE_OAK_BOAT),
    MANGROVE("mangrove", EntityTypes.MANGROVE_BOAT, ModelLayers.MANGROVE_BOAT);

    private String name;
    private EntityType<Boat> entityType;
    private ModelLayerLocation modelLayer;

    BoatType(String name, EntityType<Boat> entityType, ModelLayerLocation modelLayer) {
        this.name = name;
        this.entityType = entityType;
        this.modelLayer = modelLayer;
    }

    public String getName() {
        return this.name;
    }

    public EntityType<Boat> getEntityType() {
        return this.entityType;
    }

    public ModelLayerLocation getModelLayer() {
        return this.modelLayer;
    }
}
