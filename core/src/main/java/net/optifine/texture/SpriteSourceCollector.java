package net.optifine.texture;

import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.client.renderer.texture.atlas.SpriteSource.DiscardableLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource.Output;
import net.minecraft.resources.Identifier;

public class SpriteSourceCollector implements Output {
    private Set<Identifier> spriteNames;

    public SpriteSourceCollector(Set<Identifier> spriteNames) {
        this.spriteNames = spriteNames;
    }

    @Override
    public void add(Identifier locIn, DiscardableLoader supplierIn) {
        this.spriteNames.add(locIn);
    }

    @Override
    public void removeAll(Predicate<Identifier> checkIn) {
    }

    public Set<Identifier> getSpriteNames() {
        return this.spriteNames;
    }
}
