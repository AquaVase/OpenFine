package net.optifine.shaders;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.optifine.Config;
import net.optifine.config.MatchBlock;

public class BlockAlias {
    private int aliasBlockId;
    private int aliasMetadata;
    private MatchBlock[] matchBlocks;

    public BlockAlias(int aliasBlockId, int aliasMetadata, MatchBlock[] matchBlocks) {
        this.aliasBlockId = aliasBlockId;
        this.aliasMetadata = aliasMetadata;
        this.matchBlocks = matchBlocks;
    }

    public BlockAlias(int aliasBlockId, MatchBlock[] matchBlocks) {
        this.aliasBlockId = aliasBlockId;
        this.matchBlocks = matchBlocks;
    }

    public int getAliasBlockId() {
        return this.aliasBlockId;
    }

    public int getAliasMetadata() {
        return this.aliasMetadata;
    }

    public MatchBlock[] getMatchBlocks() {
        return this.matchBlocks;
    }

    public boolean matches(int id, int metadata) {
        for (int i = 0; i < this.matchBlocks.length; i++) {
            MatchBlock matchBlock = this.matchBlocks[i];
            if (matchBlock.matches(id, metadata)) {
                return true;
            }
        }

        return false;
    }

    public int[] getMatchBlockIds() {
        Set<Integer> blockIdSet = new HashSet<>();

        for (int i = 0; i < this.matchBlocks.length; i++) {
            MatchBlock matchBlock = this.matchBlocks[i];
            int blockId = matchBlock.getBlockId();
            blockIdSet.add(blockId);
        }

        Integer[] blockIdsArr = blockIdSet.toArray(new Integer[blockIdSet.size()]);
        return Config.toPrimitive(blockIdsArr);
    }

    public MatchBlock[] getMatchBlocks(int matchBlockId) {
        List<MatchBlock> listMatchBlock = new ArrayList<>();

        for (int i = 0; i < this.matchBlocks.length; i++) {
            MatchBlock mb = this.matchBlocks[i];
            if (mb.getBlockId() == matchBlockId) {
                listMatchBlock.add(mb);
            }
        }

        return listMatchBlock.toArray(new MatchBlock[listMatchBlock.size()]);
    }

    @Override
    public String toString() {
        return "block." + this.aliasBlockId + ":" + this.aliasMetadata + "=" + Config.arrayToString(this.matchBlocks);
    }
}
