package net.optifine.texture;

import net.optifine.util.IntArray;

public class BlenderSplit implements IBlender {
    private int startHigh;
    private boolean discreteHigh;

    public BlenderSplit(int startHigh, boolean discreteHigh) {
        this.startHigh = startHigh;
        this.discreteHigh = discreteHigh;
    }

    @Override
    public int blend(int v1, int v2, int v3, int v4) {
        if (v1 == v2 && v2 == v3 && v3 == v4) {
            return v1;
        }

        boolean low1 = v1 < this.startHigh;
        boolean low2 = v2 < this.startHigh;
        boolean low3 = v3 < this.startHigh;
        boolean low4 = v4 < this.startHigh;
        if (low1 != low2 || low2 != low3 || low3 != low4) {
            IntArray lows = new IntArray(4);
            IntArray highs = new IntArray(4);
            this.separate(v1, lows, highs);
            this.separate(v2, lows, highs);
            this.separate(v3, lows, highs);
            this.separate(v4, lows, highs);
            if (highs.getPosition() > lows.getPosition()) {
                return this.discreteHigh ? highs.get(0) : this.getAverage(highs);
            } else {
                return this.getAverage(lows);
            }
        } else {
            return !low1 && this.discreteHigh ? v1 : (v1 + v2 + v3 + v4) / 4;
        }
    }

    private void separate(int val, IntArray low, IntArray high) {
        if (val < this.startHigh) {
            low.put(val);
        } else {
            high.put(val);
        }
    }

    private int getAverage(IntArray arr) {
        int count = arr.getLimit();
        switch (count) {
            case 2:
                return (arr.get(0) + arr.get(1)) / 2;
            case 3:
                return (arr.get(0) + arr.get(1) + arr.get(2)) / 3;
            default:
                int val = 0;

                for (int i = 0; i < count; i++) {
                    val += arr.get(i);
                }

                return val / count;
        }
    }

    @Override
    public String toString() {
        return "BlenderSplit: " + this.startHigh + ", " + this.discreteHigh;
    }

    public static void main(String[] args) {
        BlenderSplit bs = new BlenderSplit(230, true);
        System.out.println(bs + "");
        int v1 = bs.blend(10, 20, 30, 40);
        System.out.println(v1 + " =? 25");
        int v2 = bs.blend(10, 20, 30, 230);
        System.out.println(v2 + " =? 20");
        int v3 = bs.blend(10, 20, 240, 230);
        System.out.println(v3 + " =? 15");
        int v4 = bs.blend(10, 250, 240, 230);
        System.out.println(v4 + " =? 250");
        int v5 = bs.blend(245, 250, 240, 230);
        System.out.println(v5 + " =? 245");
        int v6 = bs.blend(10, 10, 10, 10);
        System.out.println(v6 + " =? 10");
        BlenderSplit bs2 = new BlenderSplit(65, false);
        System.out.println(bs2 + "");
        int v10 = bs2.blend(10, 20, 30, 40);
        System.out.println(v10 + " =? 25");
        int v11 = bs2.blend(10, 20, 30, 70);
        System.out.println(v11 + " =? 20");
        int v12 = bs2.blend(10, 90, 20, 70);
        System.out.println(v12 + " =? 15");
        int v13 = bs2.blend(110, 90, 20, 70);
        System.out.println(v13 + " =? 90");
        int v14 = bs2.blend(110, 90, 130, 70);
        System.out.println(v14 + " =? 100");
    }
}
