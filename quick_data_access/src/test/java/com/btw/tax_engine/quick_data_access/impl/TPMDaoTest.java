package com.btw.tax_engine.quick_data_access.impl;

import com.btw.tax_engine.quick_data_access.TMPMRepo;
import com.btw.tax_engine.quick_data_access.TestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("dev")
public class TPMDaoTest {

    private TMPMRepo tpmDao;

    @Autowired
    public void setTpmDao(TMPMRepo tpmDao) {
        this.tpmDao = tpmDao;
    }

    @Test
    public void getTPM() {
        assertTrue(tpmDao.getTPM("AAA", "PPT") > 0);
        assertEquals(tpmDao.getTPM("AAA", "ACE"), 0, 0.0000001);
    }

    @Test
    public void getMPM() {
        assertTrue(tpmDao.getMPM("AAA", "ACE") > 0);
        assertEquals(tpmDao.getMPM("AAA", "ZZZ"), 0, 0.0000001);
    }
}