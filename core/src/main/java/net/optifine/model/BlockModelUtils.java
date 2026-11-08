package net.optifine.model;

import com.mojang.math.Quadrant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidRotation;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.cuboid.CuboidFace.UVs;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.client.resources.model.sprite.Material.Baked;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.phys.AABB;
import net.optifine.Config;
import net.optifine.util.DummyRandomSource;
import org.joml.Vector3f;

public class BlockModelUtils {
    private static final float VERTEX_COORD_ACCURACY = 1.0E-6F;
    private static Minecraft mc = Minecraft.getInstance();

    public static BlockStateModelPart makeModelCube(TextureAtlasSprite sprite, int tintIndex) {
        QuadCollection.Builder qcb = new QuadCollection.Builder();
        Direction[] facings = Direction.VALUES;

        for (int i = 0; i < facings.length; i++) {
            Direction facing = facings[i];
            BakedQuad quad = makeBakedQuad(facing, sprite, tintIndex);
            qcb.addCulledFace(facing, quad);
        }

        Baked spriteMat = new Baked(sprite, false);
        QuadCollection qc = qcb.build();
        return new SimpleModelWrapper(qc, true, spriteMat);
    }

    public static BlockStateModelPart joinModelsCube(BlockStateModelPart modelBase, BlockStateModelPart modelAdd) {
        QuadCollection.Builder qcb = new QuadCollection.Builder();
        List<BakedQuad> generalQuads = new ArrayList<>();
        generalQuads.addAll(modelBase.getQuads(null));
        generalQuads.addAll(modelAdd.getQuads(null));
        addAllUnculled(qcb, generalQuads);
        Direction[] facings = Direction.VALUES;

        for (int i = 0; i < facings.length; i++) {
            Direction facing = facings[i];
            addAllCulled(qcb, facing, modelBase.getQuads(facing));
            addAllCulled(qcb, facing, modelAdd.getQuads(facing));
        }

        addAllUnculled(qcb, modelBase.getQuads(null));
        addAllUnculled(qcb, modelAdd.getQuads(null));
        QuadCollection qc = qcb.build();
        boolean ao = modelBase.useAmbientOcclusion();
        Baked particleMat = modelBase.particleMaterial();
        return new SimpleModelWrapper(qc, ao, particleMat);
    }

    public static void addAllCulled(QuadCollection.Builder qcb, Direction facing, List<BakedQuad> quads) {
        for (BakedQuad quad : quads) {
            qcb.addCulledFace(facing, quad);
        }
    }

    public static void addAllUnculled(QuadCollection.Builder qcb, List<BakedQuad> quads) {
        for (BakedQuad quad : quads) {
            qcb.addUnculledFace(quad);
        }
    }

    public static BakedQuad makeBakedQuad(Direction facing, TextureAtlasSprite sprite, int tintIndex) {
        Vector3f posFrom = new Vector3f(0.0F, 0.0F, 0.0F);
        Vector3f posTo = new Vector3f(16.0F, 16.0F, 16.0F);
        UVs uv = new UVs(0.0F, 0.0F, 16.0F, 16.0F);
        CuboidFace face = new CuboidFace(facing, tintIndex, "#" + facing.getSerializedName(), uv, Quadrant.R0);
        BlockModelRotation modelRotation = BlockModelRotation.IDENTITY;
        CuboidRotation partRotation = null;
        boolean shade = true;
        int lightEmission = 0;
        final ModelBakery.InternerImpl partCache = new ModelBakery.InternerImpl();
        Baked spriteMat = new Baked(sprite, false);
        ModelBaker modelBaker = new ModelBaker() {
            @Override
            public ResolvedModel getModel(Identifier location) {
                throw new IllegalStateException("Cube model can't have dependencies, but asked for " + location);
            }

            @Override
            public BlockStateModelPart missingBlockModelPart() {
                throw new IllegalStateException();
            }

            @Override
            public <T> T compute(ModelBaker.SharedOperationKey<T> key) {
                return key.compute(this);
            }

            @Override
            public MaterialBaker materials() {
                throw new IllegalStateException();
            }

            @Override
            public ModelBaker.Interner interner() {
                return partCache;
            }
        };
        return FaceBakery.bakeQuad(modelBaker, posFrom, posTo, face, spriteMat, facing, modelRotation, partRotation, shade, lightEmission);
    }

    public static BlockStateModelPart makeModel(BlockState blockState, final TextureAtlasSprite spriteOld, final TextureAtlasSprite spriteNew) {
        if (spriteOld != null && spriteNew != null) {
            ModelManager modelManager = Config.getModelManager();
            if (modelManager == null) {
                return null;
            } else {
                BlockStateModel stateModel = getBlockStateModel(blockState);
                if (stateModel != null && stateModel != modelManager.getMissingModels().block()) {
                    BlockStateModelPart model = getModel(stateModel);
                    BiFunction<List<BakedQuad>, Direction, List<BakedQuad>> quadsFunc = new BiFunction<List<BakedQuad>, Direction, List<BakedQuad>>() {
                        public List<BakedQuad> apply(List<BakedQuad> quads, Direction face) {
                            return BlockModelUtils.replaceTexture(quads, spriteOld, spriteNew);
                        }
                    };
                    return duplicateModel(model, quadsFunc);
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    public static SimpleModelWrapper duplicateModel(BlockStateModelPart model, BiFunction<List<BakedQuad>, Direction, List<BakedQuad>> quadsTransformer) {
        QuadCollection.Builder qcb = new QuadCollection.Builder();
        Direction[] faces = Direction.VALUES;

        for (int i = 0; i < faces.length; i++) {
            Direction face = faces[i];
            List<BakedQuad> quads = model.getQuads(face);
            List<BakedQuad> quads2 = quadsTransformer.apply(quads, face);
            addAllCulled(qcb, face, quads2);
        }

        List<BakedQuad> quadsGen = model.getQuads(null);
        List<BakedQuad> quadsGen2 = quadsTransformer.apply(quadsGen, null);
        addAllUnculled(qcb, quadsGen2);
        QuadCollection qc = qcb.build();
        return new SimpleModelWrapper(qc, model.useAmbientOcclusion(), model.particleMaterial());
    }

    private static List<BakedQuad> replaceTexture(List<BakedQuad> quads, TextureAtlasSprite spriteOld, TextureAtlasSprite spriteNew) {
        List<BakedQuad> quadsNew = new ArrayList<>();
        if (quads == null) {
            return quadsNew;
        }

        for (BakedQuad quad : quads) {
            if (quad.sprite() == spriteOld) {
                quad = BakedQuadRetextured.make(quad, spriteNew);
            }

            quadsNew.add(quad);
        }

        return quadsNew;
    }

    public static void snapVertexPosition(Vector3f pos) {
        pos.set(snapVertexCoord(pos.x()), snapVertexCoord(pos.y()), snapVertexCoord(pos.z()));
    }

    private static float snapVertexCoord(float x) {
        if (x > -1.0E-6F && x < 1.0E-6F) {
            return 0.0F;
        } else {
            return x > 0.999999F && x < 1.000001F ? 1.0F : x;
        }
    }

    public static AABB getOffsetBoundingBox(AABB aabb, OffsetType offsetType, BlockPos pos) {
        int x = pos.getX();
        int z = pos.getZ();
        long k = x * 3129871 ^ z * 116129781L;
        k = k * k * 42317861L + k * 11L;
        double dx = ((float)(k >> 16 & 15L) / 15.0F - 0.5) * 0.5;
        double dz = ((float)(k >> 24 & 15L) / 15.0F - 0.5) * 0.5;
        double dy = 0.0;
        if (offsetType == OffsetType.XYZ) {
            dy = ((float)(k >> 20 & 15L) / 15.0F - 1.0) * 0.2;
        }

        return aabb.move(dx, dy, dz);
    }

    public static BlockStateModel getBlockStateModel(BlockState state) {
        return mc.getModelManager().getBlockStateModelSet().get(state);
    }

    public static boolean isMissingModel(BlockStateModel model) {
        return model == null ? true : model == getMissingBlockStateModel();
    }

    public static BlockStateModel getMissingBlockStateModel() {
        return mc.getModelManager().getMissingModels().block();
    }

    public static BlockStateModelPart getMissingBlockModel() {
        return getModel(getMissingBlockStateModel());
    }

    public static BlockStateModelPart getModel(BlockState state) {
        BlockStateModel bsm = getBlockStateModel(state);
        return getModel(bsm);
    }

    public static BlockStateModelPart getModel(BlockStateModel bsm) {
        if (bsm instanceof SingleVariant sv) {
            return sv.getModel();
        } else {
            List<BlockStateModelPart> bmps = new ArrayList<>();
            bsm.collectParts(DummyRandomSource.INSTANCE, bmps);
            if (bmps.size() > 0) {
                return bmps.get(0);
            } else {
                return mc.getModelManager().getMissingModels().block() instanceof SingleVariant sv ? sv.getModel() : null;
            }
        }
    }
}
