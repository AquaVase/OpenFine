package net.optifine;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;

public class RandomEntity implements IRandomEntity {
    private Entity entity;

    @Override
    public int getId() {
        return getId(this.entity);
    }

    @Override
    public int getVehicleId() {
        Entity vehicle = this.entity.getRootVehicle();
        return getId(vehicle);
    }

    private static int getId(Entity entityIn) {
        UUID uuid = entityIn.getUUID();
        long uuidLow = uuid.getLeastSignificantBits();
        return (int)(uuidLow & 2147483647L);
    }

    @Override
    public BlockPos getSpawnPosition() {
        return this.entity.getEntityData().spawnPosition;
    }

    @Override
    public Biome getSpawnBiome() {
        return this.entity.getEntityData().spawnBiome;
    }

    @Override
    public String getName() {
        return this.entity.hasCustomName() ? this.entity.getCustomName().getString() : null;
    }

    @Override
    public int getHealth() {
        return !(this.entity instanceof LivingEntity el) ? 0 : (int)el.getHealth();
    }

    @Override
    public int getMaxHealth() {
        return !(this.entity instanceof LivingEntity el) ? 0 : (int)el.getMaxHealth();
    }

    public Entity getEntity() {
        return this.entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    @Override
    public CompoundTag getNbtTag() {
        SynchedEntityData edm = this.entity.getEntityData();
        CompoundTag nbt = edm.nbtTag;
        long timeMs = System.currentTimeMillis();
        if (nbt == null || edm.nbtTagUpdateMs < timeMs - 1000L) {
            nbt = new CompoundTag();
            TagValueOutput tvo = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, this.entity.registryAccess());
            this.entity.saveWithoutId(tvo);
            if (this.entity instanceof TamableAnimal et) {
                nbt.putBoolean("Sitting", et.isInSittingPose());
            }

            edm.nbtTag = nbt;
            edm.nbtTagUpdateMs = timeMs;
        }

        return nbt;
    }

    @Override
    public DyeColor getColor() {
        return RandomEntityRule.getEntityColor(this.entity);
    }

    @Override
    public BlockState getBlockState() {
        if (this.entity instanceof ItemEntity ie && ie.getItem().getItem() instanceof BlockItem bi) {
            return bi.getBlock().defaultBlockState();
        } else {
            SynchedEntityData edm = this.entity.getEntityData();
            BlockState bs = edm.blockStateOn;
            long timeMs = System.currentTimeMillis();
            if (bs == null || edm.blockStateOnUpdateMs < timeMs - 50L) {
                BlockPos pos = this.entity.blockPosition();
                bs = this.entity.level().getBlockState(pos);
                if (bs.isAir()) {
                    bs = this.entity.level().getBlockState(pos.below());
                }

                edm.blockStateOn = bs;
                edm.blockStateOnUpdateMs = timeMs;
            }

            return bs;
        }
    }

    @Override
    public String toString() {
        return this.entity.toString();
    }
}
