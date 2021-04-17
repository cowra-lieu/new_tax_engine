package com.btw.tax_engine.common;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DEUTest {

    private static final Logger log = LoggerFactory.getLogger(DEUTest.class);

    @Test
    public void truncDate() {
        Date d = new Date();
        System.out.println(d.getTime());
        Calendar c0 = Calendar.getInstance();
        c0.setTime(d);

        DEU.truncDate(d);
        Calendar c = Calendar.getInstance();
        c.setTime(d);

        assertEquals(c.get(Calendar.YEAR), c0.get(Calendar.YEAR));
        assertEquals(c.get(Calendar.MONTH), c0.get(Calendar.MONTH));
        assertEquals(c.get(Calendar.DATE), c0.get(Calendar.DATE));
        assertTrue(c.get(Calendar.HOUR_OF_DAY) != c0.get(Calendar.HOUR_OF_DAY));
        assertTrue(c.get(Calendar.MINUTE) != c0.get(Calendar.MINUTE));
        assertTrue(c.get(Calendar.SECOND) != c0.get(Calendar.SECOND));

        assertEquals(c.get(Calendar.HOUR_OF_DAY), 0);
        assertEquals(c.get(Calendar.MINUTE), 0);
        assertEquals(c.get(Calendar.SECOND), 0);
    }

    @Test
    public void parse() {
        String s = "2019-10-18 10:10";
        Date d = DEU.parse_y4M2d2_H2_m2(s);
        log.debug("{}", d);
    }

}
