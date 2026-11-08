package net.optifine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import net.optifine.render.RenderUtils;
import net.optifine.util.PropertiesOrdered;
import net.optifine.util.TextureUtils;

public class EmissiveTextures {
    private static String suffixEmissive = null;
    private static String suffixEmissivePng = null;
    private static boolean active = false;
    private static boolean render = false;
    private static boolean hasEmissive = false;
    private static boolean renderEmissive = false;
    private static final String SUFFIX_PNG = ".png";
    private static final Identifier LOCATION_TEXTURE_EMPTY = TextureUtils.LOCATION_TEXTURE_EMPTY;
    private static final Identifier LOCATION_SPRITE_EMPTY = TextureUtils.LOCATION_SPRITE_EMPTY;
    private static TextureManager textureManager;
    private static int countRecursive = 0;

    public static boolean isActive() {
        return active;
    }

    public static String getSuffixEmissive() {
        return suffixEmissive;
    }

    public static void beginRender() {
        if (render) {
            countRecursive++;
        } else {
            render = true;
            hasEmissive = false;
        }
    }

    public static Identifier getEmissiveTexture(Identifier locationIn) {
        if (!render) {
            return locationIn;
        }

        AbstractTexture texture = textureManager.getTexture(locationIn);
        if (texture instanceof TextureAtlas) {
            return locationIn;
        }

        Identifier locationEmissive = null;
        if (texture instanceof SimpleTexture) {
            locationEmissive = ((SimpleTexture)texture).locationEmissive;
        }

        if (!renderEmissive) {
            if (locationEmissive != null) {
                hasEmissive = true;
            }

            return locationIn;
        } else {
            if (locationEmissive == null) {
                locationEmissive = LOCATION_TEXTURE_EMPTY;
            }

            return locationEmissive;
        }
    }

    public static TextureAtlasSprite getEmissiveSprite(TextureAtlasSprite sprite) {
        if (!render) {
            return sprite;
        }

        TextureAtlasSprite spriteEmissive = sprite.spriteEmissive;
        if (!renderEmissive) {
            if (spriteEmissive != null) {
                hasEmissive = true;
            }

            return sprite;
        } else {
            if (spriteEmissive == null) {
                spriteEmissive = sprite.getTextureAtlas().getSprite(LOCATION_SPRITE_EMPTY);
            }

            return spriteEmissive;
        }
    }

    public static BakedQuad getEmissiveQuad(BakedQuad quad) {
        if (!render) {
            return quad;
        }

        BakedQuad quadEmissive = quad.getQuadEmissive();
        if (!renderEmissive) {
            if (quadEmissive != null) {
                hasEmissive = true;
            }

            return quad;
        } else {
            return quadEmissive;
        }
    }

    public static boolean hasEmissive() {
        return countRecursive > 0 ? false : hasEmissive;
    }

    public static void beginRenderEmissive() {
        renderEmissive = true;
    }

    public static boolean isRenderEmissive() {
        return renderEmissive;
    }

    public static void endRenderEmissive() {
        RenderUtils.flushRenderBuffers();
        renderEmissive = false;
    }

    public static void endRender() {
        if (countRecursive > 0) {
            countRecursive--;
        } else {
            render = false;
            hasEmissive = false;
        }
    }

    public static void update() {
        textureManager = Minecraft.getInstance().getTextureManager();
        active = false;
        suffixEmissive = null;
        suffixEmissivePng = null;
        if (Config.isEmissiveTextures()) {
            try {
                String fileName = "optifine/emissive.properties";
                Identifier loc = new Identifier(fileName);
                InputStream in = Config.getResourceStream(loc);
                if (in == null) {
                    return;
                }

                dbg("Loading " + fileName);
                Properties props = new PropertiesOrdered();
                props.load(in);
                in.close();
                suffixEmissive = props.getProperty("suffix.emissive");
                if (suffixEmissive != null) {
                    suffixEmissivePng = suffixEmissive + ".png";
                }

                active = suffixEmissive != null;
            } catch (FileNotFoundException e) {
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateIcons(TextureAtlas textureMap, Set<Identifier> locations) {
        if (active) {
            for (Identifier loc : locations) {
                checkEmissive(textureMap, loc);
            }
        }
    }

    private static void checkEmissive(TextureAtlas textureMap, Identifier locSprite) {
        String suffixEm = getSuffixEmissive();
        if (suffixEm != null) {
            if (!locSprite.getPath().endsWith(suffixEm)) {
                Identifier locSpriteEm = new Identifier(locSprite.getNamespace(), locSprite.getPath() + suffixEm);
                Identifier locPngEm = textureMap.getSpritePath(locSpriteEm);
                if (Config.hasResource(locPngEm)) {
                    TextureAtlasSprite sprite = textureMap.registerSprite(locSprite);
                    TextureAtlasSprite spriteEmissive = textureMap.registerSprite(locSpriteEm);
                    spriteEmissive.isSpriteEmissive = true;
                    sprite.spriteEmissive = spriteEmissive;
                    textureMap.registerSprite(LOCATION_SPRITE_EMPTY);
                }
            }
        }
    }

    public static void refreshIcons(TextureAtlas textureMap) {
        for (TextureAtlasSprite sprite : textureMap.getRegisteredSprites()) {
            refreshIcon(sprite, textureMap);
        }
    }

    private static void refreshIcon(TextureAtlasSprite sprite, TextureAtlas textureMap) {
        if (sprite.spriteEmissive != null) {
            TextureAtlasSprite spriteNew = textureMap.getUploadedSprite(sprite.getName());
            if (spriteNew != null) {
                TextureAtlasSprite spriteEmissiveNew = textureMap.getUploadedSprite(sprite.spriteEmissive.getName());
                if (spriteEmissiveNew != null) {
                    spriteEmissiveNew.isSpriteEmissive = true;
                    spriteNew.spriteEmissive = spriteEmissiveNew;
                }
            }
        }
    }

    private static void dbg(String str) {
        Config.dbg("EmissiveTextures: " + str);
    }

    private static void warn(String str) {
        Config.warn("EmissiveTextures: " + str);
    }

    public static boolean isEmissive(Identifier loc) {
        return suffixEmissivePng == null ? false : loc.getPath().endsWith(suffixEmissivePng);
    }

    public static void loadTexture(Identifier loc, ReloadableTexture tex) {
        if (loc != null && tex != null) {
            tex.isEmissive = false;
            tex.locationEmissive = null;
            if (suffixEmissivePng != null) {
                String path = loc.getPath();
                if (path.endsWith(".png")) {
                    if (path.endsWith(suffixEmissivePng)) {
                        tex.isEmissive = true;
                    } else {
                        String pathEmPng = path.substring(0, path.length() - ".png".length()) + suffixEmissivePng;
                        Identifier locEmPng = new Identifier(loc.getNamespace(), pathEmPng);
                        if (Config.hasResource(locEmPng)) {
                            tex.locationEmissive = locEmPng;
                        }
                    }
                }
            }
        }
    }
}
