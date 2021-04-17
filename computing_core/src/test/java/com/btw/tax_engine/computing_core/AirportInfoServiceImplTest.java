package com.btw.tax_engine.computing_core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("dev")
public class AirportInfoServiceImplTest {

    private AirportInfoService as;

    @Autowired
    public void setAs(AirportInfoService as) {
        this.as = as;
    }

    @Test
    public void getCity() {
        String cityCode = as.getCity("SEA");
        assertEquals("SEA", cityCode);
        cityCode = as.getCity("PVG");
        assertEquals("SHA", cityCode);
        cityCode = as.getCity("DXB");
        assertEquals("DXB", cityCode);
        cityCode = as.getCity("LON");
        assertEquals("LON", cityCode);
        cityCode = as.getCity("SHA");
        assertEquals("SHA", cityCode);
    }

    @Test
    public void getNation() {
        String countryCode = as.getNation("SEA");
        assertEquals("US", countryCode);
        countryCode = as.getNation("PVG");
        assertEquals("CN", countryCode);
        countryCode = as.getNation("DXB");
        assertEquals("AE", countryCode);
        countryCode = as.getNation("LON");
        assertEquals("GB", countryCode);
        countryCode = as.getNation("SHA");
        assertEquals("CN", countryCode);
        countryCode = as.getNation("AKL");
        assertEquals("NZ", countryCode);
    }

    @Test
    public void getZone() {
        String zoneCode = as.getZone("SEA");
        assertEquals("000", zoneCode);
        zoneCode = as.getZone("PVG");
        assertEquals("320", zoneCode);
        zoneCode = as.getZone("DXB");
        assertEquals("220", zoneCode);
    }

    @Test
    public void getArea() {
        String areaCode = as.getArea("SEA");
        assertEquals("1", areaCode);
        areaCode = as.getArea("PVG");
        assertEquals("3", areaCode);
        areaCode = as.getArea("DXB");
        assertEquals("2", areaCode);
        areaCode = as.getArea("FRA");
        assertEquals("2", areaCode);
        areaCode = as.getArea("AKL");
        assertEquals("3", areaCode);
        areaCode = as.getArea("SYD");
        assertEquals("3", areaCode);
        areaCode = as.getArea("LON");
        assertEquals("2", areaCode);
    }

}