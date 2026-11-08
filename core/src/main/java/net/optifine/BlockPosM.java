package net.optifine;

import com.google.common.collect.AbstractIterator;
import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class BlockPosM extends BlockPos {
    private int mx;
    private int my;
    private int mz;
    private int level;
    private BlockPosM[] facings;
    private boolean needsUpdate;

    public BlockPosM() {
        this(0, 0, 0, 0);
    }

    public BlockPosM(int x, int y, int z) {
        this(x, y, z, 0);
    }

    public BlockPosM(double xIn, double yIn, double zIn) {
        this(Mth.floor(xIn), Mth.floor(yIn), Mth.floor(zIn));
    }

    public BlockPosM(int x, int y, int z, int level) {
        super(0, 0, 0);
        this.mx = x;
        this.my = y;
        this.mz = z;
        this.level = level;
    }

    @Override
    public int getX() {
        return this.mx;
    }

    @Override
    public int getY() {
        return this.my;
    }

    @Override
    public int getZ() {
        return this.mz;
    }

    public void setXyz(int x, int y, int z) {
        this.mx = x;
        this.my = y;
        this.mz = z;
        this.needsUpdate = true;
    }

    public BlockPosM setXyz(double xIn, double yIn, double zIn) {
        this.setXyz(Mth.floor(xIn), Mth.floor(yIn), Mth.floor(zIn));
        return this;
    }

    @Override
    public BlockPos relative(Direction facing) {
        if (this.level <= 0) {
            return super.relative(facing, 1).immutable();
        }

        if (this.facings == null) {
            this.facings = new BlockPosM[Direction.VALUES.length];
        }

        if (this.needsUpdate) {
            this.update();
        }

        int index = facing.get3DDataValue();
        BlockPosM bpm = this.facings[index];
        if (bpm == null) {
            int nx = this.mx + facing.getStepX();
            int ny = this.my + facing.getStepY();
            int nz = this.mz + facing.getStepZ();
            bpm = new BlockPosM(nx, ny, nz, this.level - 1);
            this.facings[index] = bpm;
        }

        return bpm;
    }

    @Override
    public BlockPos relative(Direction facing, int n) {
        return n == 1 ? this.relative(facing) : super.relative(facing, n).immutable();
    }

    public void setWithOffset(BlockPos pos, Direction facing) {
        this.mx = pos.getX() + facing.getStepX();
        this.my = pos.getY() + facing.getStepY();
        this.mz = pos.getZ() + facing.getStepZ();
    }

    public BlockPos setWithOffset(BlockPos pos, Direction facing, Direction facing2) {
        this.mx = pos.getX() + facing.getStepX() + facing2.getStepX();
        this.my = pos.getY() + facing.getStepY() + facing2.getStepY();
        this.mz = pos.getZ() + facing.getStepZ() + facing2.getStepZ();
        return this;
    }

    private void update() {
        for (int i = 0; i < 6; i++) {
            BlockPosM bpm = this.facings[i];
            if (bpm != null) {
                Direction facing = Direction.VALUES[i];
                int nx = this.mx + facing.getStepX();
                int ny = this.my + facing.getStepY();
                int nz = this.mz + facing.getStepZ();
                bpm.setXyz(nx, ny, nz);
            }
        }

        this.needsUpdate = false;
    }

    @Override
    public BlockPos immutable() {
        return new BlockPos(this.mx, this.my, this.mz);
    }

    public static Iterable<BlockPosM> betweenClosedM(BlockPos from, BlockPos to) {
        final BlockPos posFrom = new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
        final BlockPos posTo = new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
        return new Iterable<BlockPosM>() {
            @Override
            public Iterator<BlockPosM> iterator() {
                return new AbstractIterator<BlockPosM>() {
                    private BlockPosM posM = null;

                    protected BlockPosM computeNext() {
                        if (this.posM == null) {
                            this.posM = new BlockPosM(posFrom.getX(), posFrom.getY(), posFrom.getZ(), 3);
                            return this.posM;
                        }

                        if (this.posM.equals(posTo)) {
                            return this.endOfData();
                        }

                        int bx = this.posM.getX();
                        int by = this.posM.getY();
                        int bz = this.posM.getZ();
                        if (bx < posTo.getX()) {
                            bx++;
                        } else if (bz < posTo.getZ()) {
                            bx = posFrom.getX();
                            bz++;
                        } else if (by < posTo.getY()) {
                            bx = posFrom.getX();
                            bz = posFrom.getZ();
                            by++;
                        }

                        this.posM.setXyz(bx, by, bz);
                        return this.posM;
                    }
                };
            }
        };
    }
}
