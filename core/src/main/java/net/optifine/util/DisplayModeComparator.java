package net.optifine.util;

import com.mojang.blaze3d.platform.VideoMode;
import java.util.Comparator;

public class DisplayModeComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
        VideoMode dm1 = (VideoMode)o1;
        VideoMode dm2 = (VideoMode)o2;
        if (dm1.getWidth() != dm2.getWidth()) {
            return dm1.getWidth() - dm2.getWidth();
        } else if (dm1.getHeight() != dm2.getHeight()) {
            return dm1.getHeight() - dm2.getHeight();
        } else {
            int bits1 = dm1.getRedBits() + dm1.getGreenBits() + dm1.getBlueBits();
            int bits2 = dm2.getRedBits() + dm2.getGreenBits() + dm2.getBlueBits();
            if (bits1 != bits2) {
                return bits1 - bits2;
            } else {
                return dm1.getRefreshRate() != dm2.getRefreshRate() ? dm1.getRefreshRate() - dm2.getRefreshRate() : 0;
            }
        }
    }
}
