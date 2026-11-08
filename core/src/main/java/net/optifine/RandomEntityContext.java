package net.optifine;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.Identifier;
import net.optifine.config.ConnectedParser;
import net.optifine.entity.model.CustomEntityModels;
import net.optifine.entity.model.IEntityRenderer;
import net.optifine.entity.model.RendererCache;

public interface RandomEntityContext<T> {
    String getName();

    String[] getResourceKeys();

    String getResourceName();

    T makeResource(String var1, Identifier var2, int var3);

    default String getResourceNameCapital() {
        return this.getResourceName().substring(0, 1).toUpperCase() + this.getResourceName().substring(1);
    }

    default String getResourceNamePlural() {
        return this.getResourceName() + "s";
    }

    default ConnectedParser getConnectedParser() {
        return new ConnectedParser(this.getName());
    }

    class Models implements RandomEntityContext<IEntityRenderer> {
        private RendererCache rendererCache = new RendererCache();

        @Override
        public String getName() {
            return "CustomEntityModels";
        }

        @Override
        public String[] getResourceKeys() {
            return new String[]{"models"};
        }

        @Override
        public String getResourceName() {
            return "model";
        }

        public IEntityRenderer makeResource(String name, Identifier locBase, int index) {
            Identifier loc = index <= 1 ? locBase : RandomEntities.getLocationIndexed(locBase, index);
            if (loc == null) {
                Config.warn("Invalid path: " + locBase.getPath());
                return null;
            }

            IEntityRenderer renderer = CustomEntityModels.parseEntityRender(name, loc, this.rendererCache, index);
            if (renderer == null) {
                Config.warn("Model not found: " + loc.getPath());
                return null;
            }

            if (renderer instanceof EntityRenderer) {
                this.rendererCache.put(renderer.getType().getLeft().get(), index, (EntityRenderer)renderer);
            } else if (renderer instanceof BlockEntityRenderer) {
                this.rendererCache.put(renderer.getType().getRight().get(), index, (BlockEntityRenderer)renderer);
            }

            return renderer;
        }

        public RendererCache getRendererCache() {
            return this.rendererCache;
        }
    }

    class Textures implements RandomEntityContext<Identifier> {
        private boolean legacy;

        public Textures(boolean legacy) {
            this.legacy = legacy;
        }

        @Override
        public String getName() {
            return "RandomEntities";
        }

        @Override
        public String[] getResourceKeys() {
            return new String[]{"textures", "skins"};
        }

        @Override
        public String getResourceName() {
            return "texture";
        }

        public Identifier makeResource(String name, Identifier locBase, int index) {
            if (index <= 1) {
                return locBase;
            } else {
                Identifier locOf = RandomEntities.getLocationRandom(locBase, this.legacy);
                if (locOf == null) {
                    Config.warn("Invalid path: " + locBase.getPath());
                    return null;
                } else {
                    Identifier locNew = RandomEntities.getLocationIndexed(locOf, index);
                    if (locNew == null) {
                        Config.warn("Invalid path: " + locBase.getPath());
                        return null;
                    } else if (!Config.hasResource(locNew)) {
                        Config.warn("Texture not found: " + locNew.getPath());
                        return null;
                    } else {
                        return locNew;
                    }
                }
            }
        }

        public boolean isLegacy() {
            return this.legacy;
        }
    }
}
