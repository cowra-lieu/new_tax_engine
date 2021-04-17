package com.btw.tax_engine.quick_data_access.impl;

import com.btw.tax_engine.quick_data_access.IcerRepo;
import com.btw.tax_engine.quick_data_access.TestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("dev")
public class IcerDaoTest {

    private IcerRepo icerDao;

    @Autowired
    public void setIcerDao(IcerRepo icerDao) {
        this.icerDao = icerDao;
    }

    @Test
    public void getRate() {
        long t0 = System.currentTimeMillis();
        for (int i=0; i<10; i++) {
            double d = icerDao.getRate("ICER_2", "USD", "CNY");
            assertTrue(d != 1);
            double d2 = icerDao.getRate("ICER_2", "CNY", "USD");
            assertTrue(d2 != 1 && d != d2);
        }
        t0 = System.currentTimeMillis() - t0;
        System.out.println("real cost:" + t0 + "ms");
    }
}