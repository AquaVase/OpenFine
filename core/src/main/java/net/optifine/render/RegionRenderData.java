package net.optifine.render;

import net.optifine.util.PairInt;

public class RegionRenderData {
    private RegionRenderer renderer;
    private PairInt position;

    public RegionRenderData(RegionRenderer renderer, PairInt position) {
        this.renderer = renderer;
        this.position = position;
    }

    public RegionRenderer getRenderer() {
        return this.renderer;
    }

    public PairInt getPosition() {
        return this.position;
    }

    @Override
    public String toString() {
        return this.renderer + " " + this.position;
    }
}
