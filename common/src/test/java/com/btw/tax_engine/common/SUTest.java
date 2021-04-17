package com.btw.tax_engine.common;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class SUTest {

    private static final Logger log = LoggerFactory.getLogger(SUTest.class);

    @Test
    public void isBlank() {
        assertTrue(SU.blk(null));
        assertTrue(SU.blk(""));
        assertTrue(SU.blk(null));
        assertTrue(SU.blk("      "));
    }

    @Test
    public void getNthSegment() {
        String s = "3;320;CN;SHA";
        assertEquals("3", SU.nthsec(s, ';', 0));
        assertEquals("320", SU.nthsec(s, ';', 1));
        assertEquals("CN", SU.nthsec(s, ';', 2));
        assertEquals("SHA", SU.nthsec(s, ';', 3));

        s = ":";
        assertEquals("", SU.nthsec(s, 0));
        assertEquals("", SU.nthsec(s, 1));

        s = "1314:a:b";
        assertEquals("1314", SU.nthsec(s, ':', 0));
        assertEquals("a", SU.nthsec(s, ':', 1));
        assertEquals("b", SU.nthsec(s, ':', 2));

        assertEquals("1314", SU.nthsec(s, 0));
        assertEquals("a", SU.nthsec(s, 1));
        assertEquals("b", SU.nthsec(s, 2));

        s = "hello";
        assertEquals("hello", SU.nthsec(s, ':', 0));
        assertNull(SU.nthsec(s, ':', 1));
        assertNull(SU.nthsec(s, ':', 2));

        s = "3::320::SG:::SIN::";
        int i = 0;
        assertEquals("3", SU.nthsec(s, i++));
        assertEquals("", SU.nthsec(s, i++));
        assertEquals("320", SU.nthsec(s, i++));
        assertEquals("", SU.nthsec(s, i++));
        assertEquals("SG", SU.nthsec(s, i++));
        assertEquals("", SU.nthsec(s, i++));
        assertEquals("", SU.nthsec(s, i++));
        assertEquals("SIN", SU.nthsec(s, i++));
        assertEquals("", SU.nthsec(s, i++));
        assertEquals("", SU.nthsec(s, i));
    }

    @Test
    public void iterateNthSec() {
        Consumer<String> c= s1 -> log.info("{}", s1.length());
        String s = ":";
        SU.consumeNthSec(s, ':', c);
        s = "::";
        SU.consumeNthSec(s, ':', c);
        s = "";
        SU.consumeNthSec(s, ':', c);
        final int[] cs = new int[]{0};
        Consumer<String> c2 = s2 -> cs[0] = s2.length();
        s = "AA:BBDD:CCC";
        long t0 = System.currentTimeMillis();
        for (int i=0; i<10000000; i++) {
            SU.consumeNthSec(s, ':', c2);
        }
        log.info("cowra cost: {}ms", System.currentTimeMillis() - t0);
        log.info("cs[0]: {}", cs[0]);
        String[] ss;
        t0 = System.currentTimeMillis();
        for (int i=0; i<10000000; i++) {
            ss = s.split(":", 3);
            for (int j = 0; j<3; j++) {
                cs[0] = ss[j].length();
            }
        }
        log.info("cowra cost: {}ms", System.currentTimeMillis() - t0);
        log.info("cs[0]: {}", cs[0]);
    }
}
