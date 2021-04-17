package com.btw.tax_engine.common;

import java.util.function.Consumer;

public final class SU {

    public static boolean blk(String str) {
        return null == str ||
                str.length() == 0 ||
                str.replaceAll("\\s+", "").length() == 0;
    }

    public static void consumeNthSec(String str, char spacer, Consumer<String> consumer){
        if (str != null) {
            int n = 0;
            String sec;
            while (n < Integer.MAX_VALUE) {
                sec = nthsec(str, spacer, n++);
                if (sec == null) {
                    break;
                }
                consumer.accept(sec);
            }
        }
    }

    public static String s(String str) {
        if (str == null) {
            return "";
        }
        return str;
    }

    public static String S(String s) {
        if (s.length() == 0) {
            return null;
        }
        return s;
    }

    public static boolean eq(String psgRegion, String specRegion) {
        boolean result = false;
        if (psgRegion == null && specRegion == null) {
            result = true;
        } else if (psgRegion != null) {
            result = psgRegion.equals(specRegion);
        }
        return result;
    }

    private static String b_nthsec(String s, char sep, int n) {
        int last_end = s.length();
        int last_begin;
        int i = 0;
        do {
            last_begin = s.lastIndexOf(sep, last_end-1);
            if (n == i++) {
                return s.substring(last_begin+1, last_end);
            }
            last_end = last_begin;
        } while (last_begin >= 0);
        return null;
    }

    private static String f_nthsec(String s, char sep, int n) {
        int begin = 0;
        int end;
        int j = 0;
        int len = s.length();
        do {
            end = s.indexOf(sep, begin);
            if (end < 0) {
                end = len;
            }
            if (n == j++) {
                return s.substring(begin, end);
            }
            begin = end + 1;
        } while (begin <= len);
        return null;
    }

    public static String nthsec(String s, int n, int total) {
        return nthsec(s, ':', n, total);
    }

    public static String nthsec(String s, int n) {
        return nthsec(s, ':', n);
    }

    public static String rnthsec(String s, int n) {
        return rnthsec(s, ':', n);
    }

    public static String nthsec(String s, char sep, int n, int total) {
        if (n+1 > total >> 1) {
            return b_nthsec(s, sep, total-1-n);
        } else {
            return f_nthsec(s, sep, n);
        }
    }

    public static String nthsec(String s, char sep, int n) {
        return f_nthsec(s, sep, n);
    }

    public static String rnthsec(String s, char sep, int n) {
        return b_nthsec(s, sep, n);
    }

}
