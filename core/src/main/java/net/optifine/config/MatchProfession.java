package net.optifine.config;

import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.optifine.Config;

public class MatchProfession {
    private VillagerProfession profession;
    private int[] levels;

    public MatchProfession(VillagerProfession profession) {
        this(profession, null);
    }

    public MatchProfession(VillagerProfession profession, int level) {
        this(profession, new int[]{level});
    }

    public MatchProfession(VillagerProfession profession, int[] levels) {
        this.profession = profession;
        this.levels = levels;
    }

    public boolean matches(VillagerProfession prof, int lev) {
        return this.profession != prof ? false : this.levels == null || Config.equalsOne(lev, this.levels);
    }

    private boolean hasLevel(int lev) {
        return this.levels == null ? false : Config.equalsOne(lev, this.levels);
    }

    public boolean addLevel(int lev) {
        if (this.levels == null) {
            this.levels = new int[]{lev};
            return true;
        }

        if (this.hasLevel(lev)) {
            return false;
        }

        this.levels = Config.addIntToArray(this.levels, lev);
        return true;
    }

    public VillagerProfession getProfession() {
        return this.profession;
    }

    public int[] getLevels() {
        return this.levels;
    }

    public static boolean matchesOne(VillagerProfession prof, int level, MatchProfession[] mps) {
        if (mps == null) {
            return false;
        }

        for (int i = 0; i < mps.length; i++) {
            MatchProfession mp = mps[i];
            if (mp.matches(prof, level)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return this.levels == null ? this.profession + "" : this.profession + ":" + Config.arrayToString(this.levels);
    }
}
