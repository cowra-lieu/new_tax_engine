package com.btw.tax_engine.common;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class DAUTest {

    private static final Logger log = LoggerFactory.getLogger(DAUTest.class);

    @Test
    public void cut() {

        double d1 = 0.1234;
        double d2 = 0.6789;

        log.debug("raw value: {}", d1 + d2);

        log.debug("0 decimal: {}", DAU.cut(d1 + d2, 0));
        log.debug("1 decimal: {}", DAU.cut(d1 + d2, 1));
        log.debug("2 decimal: {}", DAU.cut(d1 + d2, 2));
        log.debug("3 decimal: {}", DAU.cut(d1 + d2, 3));
        log.debug("4 decimal: {}", DAU.cut(d1 + d2, 4));
        log.debug("5 decimal: {}", DAU.cut(d1 + d2, 5));

    }

    @Test
    public void round() {

        double d1 = 0.1234;
        double d2 = 0.6789;

        log.debug("raw value: {}", d1 + d2);

        log.debug("{}", 8023.0 / 10000);

        log.debug("0 decimal: {}", DAU.round(d1 + d2, 0));
        log.debug("1 decimal: {}", DAU.round(d1 + d2, 1));
        log.debug("2 decimal: {}", DAU.round(d1 + d2, 2));
        log.debug("3 decimal: {}", DAU.round(d1 + d2, 3));
        log.debug("4 decimal: {}", DAU.round(d1 + d2, 4));
        log.debug("5 decimal: {}", DAU.round(d1 + d2, 5));

    }

    @Test
    public void getImmediateIndex() {
        int[] icer_flags = {20200716, 20200717, 20200718};
        int bdate = 20200505;
        assertEquals(0, DAU.getImmediateIndex(bdate, icer_flags));
        bdate = 20200715;
        assertEquals(0, DAU.getImmediateIndex(bdate, icer_flags));
        bdate = 20200716;
        assertEquals(0, DAU.getImmediateIndex(bdate, icer_flags));
        bdate = 20200717;
        assertEquals(1, DAU.getImmediateIndex(bdate, icer_flags));
        bdate = 20200718;
        assertEquals(2, DAU.getImmediateIndex(bdate, icer_flags));
        bdate = 20200719;
        assertEquals(2, DAU.getImmediateIndex(bdate, icer_flags));
        bdate = 20200801;
        assertEquals(2, DAU.getImmediateIndex(bdate, icer_flags));
    }
}