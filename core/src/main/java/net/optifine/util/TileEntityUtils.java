package net.optifine.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.optifine.reflect.Reflector;

public class TileEntityUtils {
    public static String getTileEntityName(BlockGetter blockAccess, BlockPos blockPos) {
        BlockEntity te = blockAccess.getBlockEntity(blockPos);
        return getTileEntityName(te);
    }

    public static String getTileEntityName(BlockEntity te) {
        if (!(te instanceof Nameable iwn)) {
            return null;
        } else {
            updateTileEntityName(te);
            return !iwn.hasCustomName() ? null : iwn.getCustomName().getString();
        }
    }

    public static void updateTileEntityName(BlockEntity te) {
        BlockPos pos = te.getBlockPos();
        Component name = getTileEntityRawName(te);
        if (name == null) {
            Component nameServer = getServerTileEntityRawName(pos);
            if (nameServer == null) {
                nameServer = Component.literal("");
            }

            setTileEntityRawName(te, nameServer);
        }
    }

    public static Component getServerTileEntityRawName(BlockPos blockPos) {
        BlockEntity tes = IntegratedServerUtils.getTileEntity(blockPos);
        return tes == null ? null : getTileEntityRawName(tes);
    }

    public static Component getTileEntityRawName(BlockEntity te) {
        if (te instanceof Nameable) {
            return ((Nameable)te).getCustomName();
        } else {
            return te instanceof BeaconBlockEntity ? (Component)Reflector.getFieldValue(te, Reflector.TileEntityBeacon_customName) : null;
        }
    }

    public static boolean setTileEntityRawName(BlockEntity te, Component name) {
        if (te instanceof BaseContainerBlockEntity) {
            Reflector.BaseContainerBlockEntity_customName.setValue(te, name);
            return true;
        } else if (te instanceof BannerBlockEntity) {
            Reflector.BannerBlockEntity_customName.setValue(te, name);
            return true;
        } else if (te instanceof EnchantingTableBlockEntity) {
            ((EnchantingTableBlockEntity)te).setCustomName(name);
            return true;
        } else if (te instanceof BeaconBlockEntity) {
            ((BeaconBlockEntity)te).setCustomName(name);
            return true;
        } else {
            return false;
        }
    }

    public static Identifier getTypeKey(BlockEntityType type) {
        return BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type);
    }
}
