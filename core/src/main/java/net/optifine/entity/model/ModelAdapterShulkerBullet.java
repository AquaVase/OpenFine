package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.projectile.ShulkerBulletModel;
import net.minecraft.client.renderer.entity.ShulkerBulletRenderer;
import net.minecraft.world.entity.EntityTypes;
import net.optifine.reflect.Reflector;

public class ModelAdapterShulkerBullet extends ModelAdapterEntity {
    public ModelAdapterShulkerBullet() {
        super(EntityTypes.SHULKER_BULLET, "shulker_bullet", ModelLayers.SHULKER_BULLET);
    }

    @Override
    public Model makeModel(ModelPart root) {
        return new ShulkerBulletModel(root);
    }

    @Override
    public Map<String, String> makeMapParts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("bullet", "main");
        map.put("root", "root");
        return map;
    }

    @Override
    public IEntityRenderer makeEntityRender(Model modelBase, RendererCache rendererCache, int index) {
        ShulkerBulletRenderer render = new ShulkerBulletRenderer(this.getContext());
        this.setModel(render, Reflector.RenderShulkerBullet_model, "ShulkerBulletRenderer.model", modelBase);
        return render;
    }
}
