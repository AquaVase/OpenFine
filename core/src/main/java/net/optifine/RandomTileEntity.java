package net.optifine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.optifine.util.TileEntityUtils;

public class RandomTileEntity implements IRandomEntity {
    private BlockEntity tileEntity;
    private static final CompoundTag EMPTY_TAG = new CompoundTag();

    @Override
    public int getId() {
        return Config.getRandom(this.getSpawnPosition(), 0);
    }

    @Override
    public int getVehicleId() {
        return this.getId();
    }

    @Override
    public BlockPos getSpawnPosition() {
        return this.tileEntity.getBlockPos();
    }

    @Override
    public String getName() {
        return TileEntityUtils.getTileEntityName(this.tileEntity);
    }

    @Override
    public Biome getSpawnBiome() {
        return this.tileEntity.getLevel().getBiome(this.tileEntity.getBlockPos()).value();
    }

    @Override
    public int getHealth() {
        return -1;
    }

    @Override
    public int getMaxHealth() {
        return -1;
    }

    public BlockEntity getTileEntity() {
        return this.tileEntity;
    }

    public void setTileEntity(BlockEntity tileEntity) {
        this.tileEntity = tileEntity;
    }

    @Override
    public CompoundTag getNbtTag() {
        CompoundTag nbt = this.tileEntity.nbtTag;
        long timeMs = System.currentTimeMillis();
        if (nbt == null || this.tileEntity.nbtTagUpdateMs < timeMs - 1000L) {
            this.tileEntity.nbtTag = makeNbtTag(this.tileEntity);
            this.tileEntity.nbtTagUpdateMs = timeMs;
        }

        return nbt;
    }

    private static CompoundTag makeNbtTag(BlockEntity te) {
        Level world = te.getLevel();
        if (world == null) {
            return EMPTY_TAG;
        }

        RegistryAccess ra = world.registryAccess();
        return ra == null ? EMPTY_TAG : te.saveWithoutMetadata(ra);
    }

    @Override
    public DyeColor getColor() {
        return RandomEntityRule.getBlockEntityColor(this.tileEntity);
    }

    @Override
    public BlockState getBlockState() {
        return this.tileEntity.getBlockState();
    }

    @Override
    public String toString() {
        return this.tileEntity.toString();
    }
}
