package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.wither.WitherBossModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.WitherBossRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class ModelAdapterWither extends ModelAdapterLiving {
    public ModelAdapterWither() {
        super(EntityTypes.WITHER, "wither", ModelLayers.WITHER);
    }

    public ModelAdapterWither(EntityType entityType, String name, ModelLayerLocation modelLayer) {
        super(entityType, name, modelLayer);
    }

    @Override
    protected Model makeModel(ModelPart root) {
        return new WitherBossModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("body1", "shoulders");
        map.put("body2", "ribcage");
        map.put("body3", "tail");
        map.put("head1", "center_head");
        map.put("head2", "right_head");
        map.put("head3", "left_head");
        map.put("root", "root");
        return map;
    }

    @Override
    protected LivingEntityRenderer makeLivingRenderer(Context context) {
        return new WitherBossRenderer(context);
    }
}
