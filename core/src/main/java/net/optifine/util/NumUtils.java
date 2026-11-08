package net.optifine.util;

public class NumUtils {
    private static final int[] POWERS_OF_10 = new int[]{1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};

    public static int limit(int val, int min, int max) {
        if (val < min) {
            return min;
        } else {
            return val > max ? max : val;
        }
    }

    public static float limit(float val, float min, float max) {
        if (val < min) {
            return min;
        } else {
            return val > max ? max : val;
        }
    }

    public static double limit(double val, double min, double max) {
        if (val < min) {
            return min;
        } else {
            return val > max ? max : val;
        }
    }

    public static int mod(int x, int y) {
        int result = x % y;
        if (result < 0) {
            result += y;
        }

        return result;
    }

    public static float round(float val, int places) {
        int mul = POWERS_OF_10[places];
        int intVal = Math.round(val * mul);
        return (float)intVal / mul;
    }
}
