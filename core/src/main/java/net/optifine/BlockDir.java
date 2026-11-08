package net.optifine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public enum BlockDir {
    DOWN(Direction.DOWN),
    UP(Direction.UP),
    NORTH(Direction.NORTH),
    SOUTH(Direction.SOUTH),
    WEST(Direction.WEST),
    EAST(Direction.EAST),
    NORTH_WEST(Direction.NORTH, Direction.WEST),
    NORTH_EAST(Direction.NORTH, Direction.EAST),
    SOUTH_WEST(Direction.SOUTH, Direction.WEST),
    SOUTH_EAST(Direction.SOUTH, Direction.EAST),
    DOWN_NORTH(Direction.DOWN, Direction.NORTH),
    DOWN_SOUTH(Direction.DOWN, Direction.SOUTH),
    UP_NORTH(Direction.UP, Direction.NORTH),
    UP_SOUTH(Direction.UP, Direction.SOUTH),
    DOWN_WEST(Direction.DOWN, Direction.WEST),
    DOWN_EAST(Direction.DOWN, Direction.EAST),
    UP_WEST(Direction.UP, Direction.WEST),
    UP_EAST(Direction.UP, Direction.EAST);

    private Direction facing1;
    private Direction facing2;

    BlockDir(Direction facing1) {
        this.facing1 = facing1;
    }

    BlockDir(Direction facing1, Direction facing2) {
        this.facing1 = facing1;
        this.facing2 = facing2;
    }

    public Direction getFacing1() {
        return this.facing1;
    }

    public Direction getFacing2() {
        return this.facing2;
    }

    BlockPos offset(BlockPos pos) {
        pos = pos.relative(this.facing1, 1);
        if (this.facing2 != null) {
            pos = pos.relative(this.facing2, 1);
        }

        return pos;
    }

    public int getOffsetX() {
        int offset = this.facing1.getStepX();
        if (this.facing2 != null) {
            offset += this.facing2.getStepX();
        }

        return offset;
    }

    public int getOffsetY() {
        int offset = this.facing1.getStepY();
        if (this.facing2 != null) {
            offset += this.facing2.getStepY();
        }

        return offset;
    }

    public int getOffsetZ() {
        int offset = this.facing1.getStepZ();
        if (this.facing2 != null) {
            offset += this.facing2.getStepZ();
        }

        return offset;
    }

    public boolean isDouble() {
        return this.facing2 != null;
    }
}
