package com.btw.tax_engine.common;

import java.time.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

public final class DEU {

    private static final int ONE_DAY_MILLIS = 86400000;
    private static final int ONE_HOUR_MILLIS = 3600000;
    private static final int TIME_ZONE = 8;

    public static long DATA_REFRESH_INTERVAL;


    private static final ZoneId ZID = ZoneId.systemDefault();

    private static final DateTimeFormatter DTF_y2M2d2 =
            DateTimeFormatter.ofPattern("yyMMdd").withZone(ZID);
    private static final DateTimeFormatter DTF_y4M2d2 =
            DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZID);
    private static final DateTimeFormatter DTF_y4M2d2_H2_m2 =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZID);

    public static Date parse_y4M2d2(String str) {
        return Date.from(LocalDate.parse(str, DTF_y4M2d2).atStartOfDay(ZID).toInstant());
    }

    public static Date parse_y4M2d2_H2_m2(String str) {
        return Date.from(LocalDateTime.parse(str, DTF_y4M2d2_H2_m2)
                .toInstant(OffsetDateTime.now().getOffset()));
    }

    public static boolean is_same_day(Date d1, Date d2) {
        long t1 = (d1.getTime() + ONE_HOUR_MILLIS * TIME_ZONE) / ONE_DAY_MILLIS;
        long t2 = (d2.getTime() + ONE_HOUR_MILLIS * TIME_ZONE) / ONE_DAY_MILLIS;
        return t1 == t2;
    }

    public static boolean moreThanOneDay(Date d1, Date d2) {
        return Math.abs(d1.getTime() - d2.getTime()) > ONE_DAY_MILLIS;
    }

    public static void truncDate(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        d.setTime(c.getTimeInMillis());
    }

    public static int i8(Date d) {
        return Integer.parseInt(DTF_y4M2d2.format(d.toInstant()));
    }

    public static int i6(Date d) {
        return Integer.parseInt(DTF_y2M2d2.format(d.toInstant()));
    }

    public static void diffD(Date d1, Date d2, long[] dmm_diff) {
        LocalDateTime dt1 = Instant.ofEpochMilli(d1.getTime()).atZone(ZID).toLocalDateTime();
        LocalDateTime dt2 = Instant.ofEpochMilli(d2.getTime()).atZone(ZID).toLocalDateTime();
        dmm_diff[0] = Math.abs(ChronoUnit.MINUTES.between(dt1, dt2));
        dmm_diff[1] = Math.abs(ChronoUnit.HOURS.between(dt1, dt2));
        dmm_diff[2] = Math.abs(ChronoUnit.DAYS.between(dt1, dt2));
        dmm_diff[3] = Math.abs(ChronoUnit.MONTHS.between(dt1, dt2));
    }

    public static Long monthsBetween(Date d1, Date d2) {
        LocalDateTime dt1 = Instant.ofEpochMilli(d1.getTime()).atZone(ZID).toLocalDateTime();
        LocalDateTime dt2 = Instant.ofEpochMilli(d2.getTime()).atZone(ZID).toLocalDateTime();
        return Math.abs(ChronoUnit.MONTHS.between(dt1, dt2));
    }

    public static long hours(Date d1, Date d2) {
        return Math.abs(d1.getTime() - d2.getTime()) / ONE_HOUR_MILLIS;
    }

    public static boolean between(String yyyyMMdd1, String yyyyMMdd2, int bookingDate) {
        return (Integer.parseInt(yyyyMMdd1) <= bookingDate) &&
                (bookingDate <= Integer.parseInt(yyyyMMdd2));
    }
}
