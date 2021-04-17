package com.btw.tax_engine.quick_data_access.impl;

import com.btw.tax_engine.common.DEU;
import com.btw.tax_engine.quick_data_access.CabinRepo;
import com.btw.tax_engine.quick_data_access.TestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("dev")
public class CabinRepoImplTest {

    private CabinRepo dao;

    @Autowired
    public void setDao(CabinRepo dao) {
        this.dao = dao;
    }

    @Test
    public void getCabin() {
        Date d = new Date();
        char cabin;
        for (int i=0; i<10; i++) {
            cabin = dao.getCabin("MU", 'P', DEU.i8(d));
            assertEquals('Y', cabin);

            cabin = dao.getCabin("MU", 'Q', DEU.i8(d));
            assertEquals('F', cabin);

            cabin = dao.getCabin("MU", 'U', DEU.i8(d));
            assertEquals('F', cabin);
        }
    }
}