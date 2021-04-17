package com.btw.tax_engine.computing_core.yqyr.matcher;

import com.btw.tax_engine.common.bean.Itinerary;
import com.btw.tax_engine.common.bean.YqYrRule;

import java.util.Date;

public interface IRuleMatcher {

    int doPortionMatch(YqYrRule rule, Itinerary itinerary, int sectorIndex);

    int doSectorMatch(YqYrRule rule, Itinerary itinerary, int sectorIndex);

    boolean checkStopover(Date arrivalDate, Date departureDate, YqYrRule rule);
}
