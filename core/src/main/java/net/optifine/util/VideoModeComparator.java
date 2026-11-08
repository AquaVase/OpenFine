package net.optifine.util;

import com.mojang.blaze3d.platform.VideoMode;
import java.util.Comparator;

public class VideoModeComparator implements Comparator<VideoMode> {
    public int compare(VideoMode vm1, VideoMode vm2) {
        if (vm1.getWidth() != vm2.getWidth()) {
            return vm1.getWidth() - vm2.getWidth();
        }

        if (vm1.getHeight() != vm2.getHeight()) {
            return vm1.getHeight() - vm2.getHeight();
        }

        if (vm1.getRefreshRate() != vm2.getRefreshRate()) {
            return vm1.getRefreshRate() - vm2.getRefreshRate();
        }

        int cols1 = vm1.getRedBits() + vm1.getGreenBits() + vm1.getBlueBits();
        int cols2 = vm2.getRedBits() + vm2.getGreenBits() + vm2.getBlueBits();
        return cols1 != cols2 ? cols1 - cols2 : 0;
    }
}
