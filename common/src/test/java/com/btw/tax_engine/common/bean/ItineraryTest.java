package com.btw.tax_engine.common.bean;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class ItineraryTest {

    @Test
    public void is_symmetry() {
        // Prepare test data
        Itinerary itinerary = new Itinerary();
        itinerary.is_intl = true;

        Sector s = new Sector();
        s.from = "PVG";
        s.to = "BKK";
        itinerary.sectors = new Sector[1];
        itinerary.sectors[0] = s;

        assertFalse(itinerary.is_symmetry());
    }
}