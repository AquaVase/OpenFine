package net.optifine.entity.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.optifine.Config;
import net.optifine.entity.model.anim.IRenderResolver;
import net.optifine.util.ArrayUtils;
import net.optifine.util.Either;

public abstract class ModelAdapter {
    private Either<EntityType, BlockEntityType> type;
    private String name;
    private String[] aliases;

    public ModelAdapter(EntityType entityType, String name) {
        this(Either.makeLeft(entityType), name);
    }

    public ModelAdapter(BlockEntityType tileEntityType, String name) {
        this(Either.makeRight(tileEntityType), name);
    }

    public ModelAdapter(Either<EntityType, BlockEntityType> type, String name) {
        this.type = type;
        this.name = name;
    }

    public Either<EntityType, BlockEntityType> getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    public String[] getAliases() {
        return this.aliases;
    }

    public void setAliases(String... aliases) {
        this.aliases = aliases;
    }

    public void setAlias(String alias) {
        this.aliases = new String[]{alias};
    }

    public abstract Model makeModel();

    public abstract ModelPart getModelRenderer(Model var1, String var2);

    public abstract String[] getModelRendererNames();

    public String[] getIgnoredPartNames() {
        return new String[0];
    }

    public void finalizeModel(Model model) {
    }

    public abstract IEntityRenderer makeEntityRender(Model var1, RendererCache var2, int var3);

    public boolean setTextureLocation(IEntityRenderer er, Identifier textureLocation) {
        return false;
    }

    public abstract IRenderResolver getRenderResolver();

    public ModelPart getModelRenderer(ModelPart root, String name, Supplier<ModelPart> supplier) {
        ModelPart part = this.getModelRenderer(root, name);
        return part != null ? part : supplier.get();
    }

    public ModelPart getModelRenderer(ModelPart root, String name) {
        if (root == null) {
            return null;
        } else {
            return Config.equals(root.getName(), name) ? root : root.getChildModelDeep(name);
        }
    }

    public ModelPart[] getModelRenderers(Model model) {
        String[] names = this.getModelRendererNames();
        List<ModelPart> list = new ArrayList<>();

        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            ModelPart mr = this.getModelRenderer(model, name);
            if (mr != null) {
                list.add(mr);
            }
        }

        return list.toArray(new ModelPart[list.size()]);
    }

    public boolean isPartOptional(String name) {
        return ArrayUtils.contains(this.getIgnoredPartNames(), name);
    }

    public static ModelPart bakeModelLayer(ModelLayerLocation loc) {
        return Minecraft.getInstance().getEntityRenderDispatcher().getContext().bakeLayer(loc);
    }

    public Context getBlockEntityContext() {
        BlockEntityRenderDispatcher dispatcher = Config.getMinecraft().getBlockEntityRenderDispatcher();
        return dispatcher.getContext();
    }

    public net.minecraft.client.renderer.entity.EntityRendererProvider.Context getEntityContext() {
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        return dispatcher.getContext();
    }

    public static String[] toArray(Set<String> set) {
        return set.toArray(new String[set.size()]);
    }

    public static String[] toArray(List<String> list) {
        return list.toArray(new String[list.size()]);
    }

    public static String[] toArray(String[] strs, Set<String> set) {
        Set<String> set2 = new LinkedHashSet<>();
        set2.addAll(Arrays.asList(strs));
        set2.addAll(set);
        return toArray(set2);
    }

    public boolean isEntity() {
        return this.type != null && this.type.getLeft().isPresent();
    }

    public boolean isBlockEntity() {
        return this.type != null && this.type.getRight().isPresent();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.type != null) {
            if (this.type.getLeft().isPresent()) {
                sb.append("entity: " + this.type.getLeft().get());
            }

            if (this.type.getRight().isPresent()) {
                sb.append("blockEntity: " + this.type.getLeft().get());
            }
        } else {
            sb.append("type: null");
        }

        sb.append(", name: " + this.name);
        if (this.aliases != null && this.aliases.length > 0) {
            sb.append(", aliases: " + ArrayUtils.arrayToString(this.aliases));
        }

        return sb.toString();
    }
}
