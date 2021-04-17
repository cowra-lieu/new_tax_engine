package com.btw.tax_engine.computing_core;

import com.btw.tax_engine.common.DEU;
import com.btw.tax_engine.common.bean.Itinerary;
import com.btw.tax_engine.common.bean.Sector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("dev")
public class AnalysisRouteServiceImplTest {

    private static final Logger log = LoggerFactory.getLogger(AnalysisRouteServiceImplTest.class);

    private AnalysisRouteService routeService;

    @Autowired
    public void setRouteService(AnalysisRouteService routeService) {
        this.routeService = routeService;
    }

    @Test
    public void withSameCity() {
        assertTrue(routeService.withSameCity("PVG", "SHA"));
        assertFalse(routeService.withSameCity("PVG", "AKL"));
        assertFalse(routeService.withSameCity("LON", "SIN"));
    }

    @Test
    public void withSameCountry() {
        assertTrue(routeService.withSameCountry("PVG", "SHA"));
        assertFalse(routeService.withSameCity("PVG", "AKL"));
        assertFalse(routeService.withSameCity("LON", "SIN"));
    }

    @Test
    public void getMaxTPM() {
        for (int i=0; i<10; i++) {
            double tpm = routeService.getTPM("SIN", "PVG");
            log.debug("from SIN to PVG is: {}km", tpm);
            assertTrue(tpm > 0);

            tpm = routeService.getTPM("SIN", "LAX");
            log.debug("from SIN to LAX is: {}km", tpm);
            assertTrue(tpm > 0);

            tpm = routeService.getTPM("SIN", "JFK");
            log.debug("from SIN to JFK is: {}km", tpm);
            assertTrue(tpm > 0);
        }
    }

    @Test
    public void findFurthestPoint() {
        // Prepare test data
        Itinerary itinerary = prepareTestData();

        // analyzeIntlDomAndOneWayRoundTrip is required before findFurthestPoint
        routeService.analyzeIntlDomAndOwRt(itinerary);

        routeService.findFurthestPoint(itinerary, false, false);
        log.debug("The furthest point is at the {}th(0 based) airport, which is in the {}th(1 based) sector",
                itinerary.journey_turnaround, itinerary.turnaroundNo);
        assertEquals(4, itinerary.journey_turnaround);
        assertEquals(3, itinerary.turnaroundNo);

        routeService.findFurthestPoint(itinerary, false, true);
        log.debug("The furthest stopover is at the {}th(0 based) airport, which is in the {}th(1 based) sector",
                itinerary.journey_turnaround, itinerary.turnaroundNo);
        assertEquals(4, itinerary.journey_turnaround);
        assertEquals(3, itinerary.turnaroundNo);
    }

    @Test
    public void analyzeTurnaround() {
        // Prepare test data
        // cas1
        Itinerary itinerary = prepareTestData();
        // analyzeIntlDomAndOneWayRoundTrip is required before analyzeTurnaround
        routeService.analyzeIntlDomAndOwRt(itinerary);
        routeService.analyzeTurnaround(itinerary);
        assertEquals(4, itinerary.journey_turnaround);
        assertEquals(3, itinerary.turnaroundNo);

        // case2
        itinerary = prepareRoundTripAndNoStopoverTestData();
        // analyzeIntlDomAndOneWayRoundTrip is required before analyzeTurnaround
        routeService.analyzeIntlDomAndOwRt(itinerary);
        routeService.analyzeTurnaround(itinerary);
        assertEquals(2, itinerary.journey_turnaround);
        assertEquals(2, itinerary.turnaroundNo);

        // case3
        itinerary = prepareRoundTripAndSurfaceSectorTestData();
        // analyzeIntlDomAndOneWayRoundTrip is required before analyzeTurnaround
        routeService.analyzeIntlDomAndOwRt(itinerary);
        routeService.analyzeTurnaround(itinerary);
        assertEquals(2, itinerary.journey_turnaround);
        assertEquals(2, itinerary.turnaroundNo);
    }

    private Itinerary prepareTestData() {
        Itinerary itinerary = new Itinerary();
        itinerary.is_intl = true;
        itinerary.sectors = new Sector[4];
        Sector s = new Sector("SIN", "PVG",
                DEU.parse_y4M2d2_H2_m2("2019-10-18 10:10"), DEU.parse_y4M2d2_H2_m2("2019-10-18 15:20"));
        itinerary.sectors[0] = (s);
        s = new Sector("PVG", "LAX",
                DEU.parse_y4M2d2_H2_m2("2019-10-19 18:00"), DEU.parse_y4M2d2_H2_m2("2019-10-19 21:00"));
        itinerary.sectors[1] = (s);
        s = new Sector("JFK", "PVG",
                DEU.parse_y4M2d2_H2_m2("2019-12-12 00:45"), DEU.parse_y4M2d2_H2_m2("2019-12-13 05:45"));
        itinerary.sectors[2] = (s);
        s = new Sector("PVG", "SIN",
                DEU.parse_y4M2d2_H2_m2("2019-12-13 09:20"), DEU.parse_y4M2d2_H2_m2("2019-12-13 15:20"));
        itinerary.sectors[3] = (s);
        return itinerary;
    }

    private Itinerary prepareRoundTripAndNoStopoverTestData() {
        Itinerary itinerary = new Itinerary();
        itinerary.is_intl = true;
        itinerary.sectors = new Sector[2];
        Sector s = new Sector("PEK", "SIN",
                DEU.parse_y4M2d2_H2_m2("2019-10-18 10:10"), DEU.parse_y4M2d2_H2_m2("2019-10-18 15:20"));
        itinerary.sectors[0] = (s);

        s = new Sector("SIN", "PVG",
                DEU.parse_y4M2d2_H2_m2("2019-10-19 10:00"), DEU.parse_y4M2d2_H2_m2("2019-10-19 21:00"));
        itinerary.sectors[1] = (s);
        return itinerary;
    }

    private Itinerary prepareRoundTripAndSurfaceSectorTestData() {
        Itinerary itinerary = new Itinerary();
        itinerary.is_intl = true;
        itinerary.sectors = new Sector[2];
        Sector s = new Sector("PVG", "YYZ",
                DEU.parse_y4M2d2_H2_m2("2019-10-18 10:10"), DEU.parse_y4M2d2_H2_m2("2019-10-18 15:20"));
        itinerary.sectors[0] = s;

        s = new Sector("JFK", "PVG",
                DEU.parse_y4M2d2_H2_m2("2019-10-19 10:00"), DEU.parse_y4M2d2_H2_m2("2019-10-19 21:00"));
        itinerary.sectors[1] = s;
        return itinerary;
    }

}