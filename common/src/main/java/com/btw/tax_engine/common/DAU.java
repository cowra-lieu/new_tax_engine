package com.btw.tax_engine.common;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.btw.tax_engine.common.Const.*;

public final class DAU {

    public static byte getB(String v, boolean firstChar) {
        if (SU.blk(v)) {
            return Const.NO_EXIST;
        }
        return firstChar ? (byte) v.charAt(0) : (byte) Integer.parseInt(v);
    }

    public static int getI(String v) {
        if (SU.blk(v)) {
            return Const.NO_EXIST;
        }
        return Integer.parseInt(v);
    }

    public static String getS(String v) {
        if (EIGHT_ZERO.equals(v) || (v.length() == 0)) {
            return null;
        }
        return v;
    }

    public static double d(String v) {
        if (SU.blk(v)) {
            return 0.0;
        }
        return Double.parseDouble(v);
    }

    public static double d(BigDecimal v) {
        if (null == v) {
            return 0.0;
        }
        return v.doubleValue();
    }

    public static int i(String v) {
        if (SU.blk(v)) {
            return 0;
        }
        return Integer.parseInt(v);
    }

    public static char c(String v) {
        if (SU.blk(v)) {
            return NC;
        }
        return v.charAt(0);
    }

    public static double round(double v, int decimal_len) {
        double ad = POWMAP.get(decimal_len);
        return Math.round(v * ad) / ad;
    }

    public static double cut(double v, int decimal_len) {
        double ad = POWMAP.get(decimal_len);
        return Math.floor(v * ad) / ad;
    }

    public static final Map<Integer, Double> POWMAP = new HashMap<>();
    static {
        POWMAP.put(0, 1.0);
        POWMAP.put(1, 10.0);
        POWMAP.put(2, 100.0);
        POWMAP.put(3, 1000.0);
        POWMAP.put(4, 10000.0);
        POWMAP.put(5, 100000.0);
        POWMAP.put(6, 1000000.0);
        POWMAP.put(7, 10000000.0);
        POWMAP.put(8, 100000000.0);
        POWMAP.put(9, 1000000000.0);
    }

    public static int getImmediateIndex(int bdate, final int[] icerFlags) {
        int min = Integer.MAX_VALUE;
        int result = 0;
        int dist;
        for (int i=0; i<icerFlags.length; i++) {
            dist = Math.abs(bdate - icerFlags[i]);
            if (dist == 0) {
                result = i;
                break;
            } else if (dist < min) {
                result = i;
                min = dist;
            }
        }
        return result;
    }

}
