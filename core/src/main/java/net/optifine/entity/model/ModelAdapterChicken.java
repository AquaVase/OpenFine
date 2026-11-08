package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.chicken.AdultChickenModel;
import net.minecraft.client.model.animal.chicken.BabyChickenModel;
import net.minecraft.client.model.animal.chicken.ChickenModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.ChickenRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterChicken extends ModelAdapterMultiLiving {
    public ModelAdapterChicken() {
        super(EntityTypes.CHICKEN, "chicken", ModelLayers.CHICKEN, ChickenType.NORMAL);
    }

    protected ModelAdapterChicken(EntityType entityType, String name, ModelLayerLocation modelLayer, ChickenType type) {
        super(entityType, name, modelLayer, type);
    }

    @Override
    public ModelAdapter makeBaby() {
        ModelAdapterChicken ma = new ModelAdapterChicken(this.getEntityType(), "chicken_baby", ModelLayers.CHICKEN_BABY, ChickenType.NORMAL);
        ma.setBaby(true);
        ma.setAlias(this.getName());
        return ma;
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return this.isBaby() ? new BabyChickenModel(root) : new AdultChickenModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("head", "head");
        map.put("body", "body");
        map.put("right_leg", "right_leg");
        map.put("left_leg", "left_leg");
        map.put("right_wing", "right_wing");
        map.put("left_wing", "left_wing");
        map.put("bill", "beak");
        map.put("chin", "red_thing");
        map.put("root", "root");
        return map;
    }

    @Override
    protected LivingEntityRenderer makeLivingRenderer(Context context) {
        return new ChickenRenderer(context);
    }

    @Override
    protected void modifyLivingRenderer(LivingEntityRenderer renderer, Model modelBase, Object type, boolean baby) {
        ChickenRenderer chickenRenderer = (ChickenRenderer)renderer;
        ChickenModel chickenModel = (ChickenModel)modelBase;
        ChickenType chickenType = (ChickenType)type;
        chickenRenderer.setModel(chickenType, baby, chickenModel);
    }
}
