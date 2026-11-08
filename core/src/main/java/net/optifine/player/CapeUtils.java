package net.optifine.player;

import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.optifine.Config;
import net.optifine.http.FileDownloadThread;
import net.optifine.http.IFileDownloadListener;

public class CapeUtils {
    private static final Pattern PATTERN_USERNAME = Pattern.compile("[a-zA-Z0-9_]+");
    private static final Map CHECKED_USERNAMES = CacheBuilder.newBuilder().maximumSize(1000L).build().asMap();

    public static void downloadCape(final AbstractClientPlayer player) {
        String username = player.getNameClear();
        if (username != null && !username.isEmpty() && !username.contains("\u0000") && PATTERN_USERNAME.matcher(username).matches()) {
            String ofCapeUrl = "http://s.optifine.net/capes/" + username + ".png";
            final Identifier capeLocation = new Identifier("capeof/" + username);
            final TextureManager textureManager = Minecraft.getInstance().getTextureManager();
            if (textureManager.hasTexture(capeLocation)
                && textureManager.getTexture(capeLocation) instanceof DynamicTexture capeTexture
                && capeTexture.isCapeTexture()) {
                player.setLocationOfCape(capeLocation);
                player.setElytraOfCape(capeTexture.isElytraCapeTexture());
                return;
            }

            if (CHECKED_USERNAMES.put(username, username) != null) {
                return;
            }

            final IFileDownloadListener fdl = new IFileDownloadListener() {
                @Override
                public void fileDownloadFinished(String url, byte[] bytes, Throwable exception) {
                    try {
                        if (exception != null) {
                            return;
                        }

                        NativeImage rawImage = NativeImage.read(bytes);
                        NativeImage capeImage = CapeUtils.parseCape(rawImage);
                        DynamicTexture capeTexture = new DynamicTexture(capeLocation::toString, capeImage);
                        capeTexture.setCapeTexture(true);
                        capeTexture.setElytraCapeTexture(CapeUtils.isElytraCape(rawImage, capeImage));
                        textureManager.register(capeLocation, capeTexture);
                        player.setLocationOfCape(capeLocation);
                        player.setElytraOfCape(capeTexture.isElytraCapeTexture());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            IFileDownloadListener fdlAsync = new IFileDownloadListener() {
                @Override
                public void fileDownloadFinished(String url, byte[] bytes, Throwable exception) {
                    Minecraft minecraft = Minecraft.getInstance();
                    minecraft.execute(() -> fdl.fileDownloadFinished(url, bytes, exception));
                }
            };
            FileDownloadThread frt = new FileDownloadThread(ofCapeUrl, fdlAsync);
            frt.start();
        }
    }

    public static NativeImage parseCape(NativeImage img) {
        int imageWidth = 64;
        int imageHeight = 32;
        NativeImage srcImg = img;
        int srcWidth = srcImg.getWidth();

        for (int srcHeight = srcImg.getHeight(); imageWidth < srcWidth || imageHeight < srcHeight; imageHeight *= 2) {
            imageWidth *= 2;
        }

        NativeImage imgNew = new NativeImage(imageWidth, imageHeight, true);
        imgNew.copyFrom(srcImg);
        srcImg.close();
        return imgNew;
    }

    public static boolean isElytraCape(NativeImage imageRaw, NativeImage imageFixed) {
        return imageRaw.getWidth() > imageFixed.getHeight();
    }

    public static void reloadCape(AbstractClientPlayer player) {
        String username = player.getNameClear();
        Identifier capeLocation = new Identifier("capeof/" + username);
        TextureManager textureManager = Config.getTextureManager();
        if (textureManager.hasTexture(capeLocation)) {
            AbstractTexture capeTexture = textureManager.getTexture(capeLocation);
            capeTexture.close();
            textureManager.release(capeLocation);
        }

        player.setLocationOfCape(null);
        player.setElytraOfCape(false);
        CHECKED_USERNAMES.remove(username);
        downloadCape(player);
    }
}
