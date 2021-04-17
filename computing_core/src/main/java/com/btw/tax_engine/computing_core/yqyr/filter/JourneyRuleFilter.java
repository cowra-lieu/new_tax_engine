package com.btw.tax_engine.computing_core.yqyr.filter;

import com.btw.tax_engine.common.DEU;
import com.btw.tax_engine.common.bean.Itinerary;
import com.btw.tax_engine.common.bean.Sector;
import com.btw.tax_engine.common.bean.YqYrRule;
import com.btw.tax_engine.computing_core.AnalysisRouteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.btw.tax_engine.common.Const.*;

@Service
public class JourneyRuleFilter {

    private static final Logger log = LoggerFactory.getLogger(JourneyRuleFilter.class);

    private AnalysisRouteService service;

    @Autowired
    public void setService(AnalysisRouteService service) {
        this.service = service;
    }

    public boolean doFilter(Itinerary itinerary, YqYrRule rule) {
        boolean result = false;
        Sector[] sectors = itinerary.sectors;
        int slen = sectors.length;
        String originCode = sectors[0].from;
        String destinationCode = itinerary.sectors[slen-1].to;

        if (rule.jrny_geo_spec_indicator == NO_EXIST) {
            result = whenGeoSpecIndicatorIsBlank(
                        rule.jrny_geo_spec_loc1,
                        rule.jrny_geo_spec_loc1_value,
                        rule.jrny_geo_spec_loc2,
                        rule.jrny_geo_spec_loc2_value,
                        originCode,
                        destinationCode,
                        itinerary);
        } else if (rule.jrny_geo_spec_indicator == REQUIRE_LOC1_IS_JOURNEY_ORIGIN) {
            result = whenGeoSpecIndicatorIsA(
                        rule.jrny_geo_spec_loc1,
                        rule.jrny_geo_spec_loc1_value,
                        rule.jrny_geo_spec_loc2,
                        rule.jrny_geo_spec_loc2_value,
                        originCode,
                        destinationCode,
                        itinerary);
        }
        if (result && (rule.jrny_geo_spec_via_loc != NO_EXIST) ) {
            if (slen > 1) {
                result = checkViaLoc(itinerary, rule.jrny_geo_spec_via_loc,
                        rule.jrny_geo_spec_via_loc_value);
            } else {
                result = false;
            }
        }
        if (result && rule.jrny_geo_spec_trvl_w_w_l != NO_EXIST) {
            result = checkWholeWithin(itinerary, rule.jrny_geo_spec_trvl_w_w_l,
                    rule.jrny_geo_spec_trvl_w_w_l_v);
        }
        return result;
    }

    private boolean whenGeoSpecIndicatorIsBlank(byte loc1,
                                                String loc1Value,
                                                byte loc2,
                                                String loc2Value,
                                                String originCode,
                                                String destinationCode,
                                                Itinerary itinerary) {
        boolean result = false;
        if (loc1 == NO_EXIST && loc2 == NO_EXIST) {
            result = true;
        } else if (loc1 != NO_EXIST) {
            if (itinerary.is_ow) {
                result = whenOW(loc1, loc2, loc1Value, loc2Value,
                        originCode, destinationCode, itinerary.sectors[0].mcxr,
                        itinerary.bdate);
            } else {
                result = whenRT(loc1, loc2, loc1Value, loc2Value,
                        originCode, itinerary);
            }
        }
        return result;
    }

    private boolean whenOW(byte loc1, byte loc2, String loc1Value, String loc2Value,
                                         String originCode, String destinationCode,
                           String carrier, Date bookingDate) {
        boolean result;
        int book = DEU.i8(bookingDate);
        if (loc2 == NO_EXIST) {
            log.debug("[OW] INDICATOR: blank, LOC2: blank");
            result = service.checkLocation(loc1, loc1Value, originCode, PJL, carrier, book) ||
                    service.checkLocation(loc1, loc1Value, destinationCode, PJL, carrier, book);
        } else {
            log.debug("[OW] INDICATOR: blank, LOC2: not blank");
            result = (service.checkLocation(loc1, loc1Value, originCode, PJL, carrier, book) &&
                    service.checkLocation(loc2, loc2Value, destinationCode, PJL, carrier, book))
                    ||
                    (service.checkLocation(loc1, loc1Value, destinationCode, PJL, carrier, book) &&
                            service.checkLocation(loc2, loc2Value, originCode, PJL, carrier, book));
        }
        return result;
    }

    private boolean whenRT(byte loc1, byte loc2, String loc1Value, String loc2Value,
                                         String originCode, Itinerary itinerary) {
        boolean result;
        String carrier = itinerary.sectors[0].mcxr;
        int book = DEU.i8(itinerary.bdate);
        if (loc2 == NO_EXIST) {
            result = service.checkLocation(loc1, loc1Value, originCode, PJL, carrier, book);
            if (!result) {
                service.analyzeTurnaround(itinerary);
                result = service.checkLocation(loc1, loc1Value,
                        itinerary.journey_turnaround_code, PJL, carrier, book);
            }
        } else {
            if (service.checkLocation(loc1, loc1Value, originCode, PJL, carrier, book)) {
                service.analyzeTurnaround(itinerary);
                result = service.checkLocation(loc2, loc2Value,
                        itinerary.journey_turnaround_code, PJL, carrier, book);
            } else {
                result = service.checkLocation(loc2, loc2Value, originCode, PJL, carrier, book);
                if (result) {
                    service.analyzeTurnaround(itinerary);
                    result = service.checkLocation(loc1, loc1Value,
                            itinerary.journey_turnaround_code, PJL, carrier, book);
                }
            }
        }
        return result;
    }

    private boolean whenGeoSpecIndicatorIsA(byte loc1, String loc1Value,
                                                   byte loc2, String loc2Value,
                                                   String originCode,
                                                   String destinationCode,
                                                   Itinerary itinerary) {
        boolean result = false;
        String carrier = itinerary.sectors[0].mcxr;
        int book = DEU.i8(itinerary.bdate);
        if (loc1 == NO_EXIST) {
            log.debug("LOC1: blank");
            result = true;
        } else if (service.checkLocation(loc1, loc1Value, originCode, PJL, carrier, book)) {
            if (loc2 == NO_EXIST) {
                result = true;
            } else {
                if (itinerary.is_ow) {
                    log.debug("[OW], LOC1: not blank, LOC2: not blank");
                    result = service.checkLocation(loc2, loc2Value, destinationCode, PJL, carrier, book);
                } else {
                    log.debug("[RT], LOC1: not blank, LOC2: not blank");
                    service.analyzeTurnaround(itinerary);
                    result = service.checkLocation(loc2, loc2Value,
                            itinerary.journey_turnaround_code, PJL, carrier, book);
                }
            }
        }
        return result;
    }

    private boolean checkViaLoc(Itinerary itinerary, byte jrny_geo_spec_via_loc,
                                String jrny_geo_spec_via_loc_value) {
        boolean result = false;
        String carrier = itinerary.sectors[0].mcxr;
        int book = DEU.i8(itinerary.bdate);

        if (!itinerary.hasCheckedTurnaround) {
            service.analyzeTurnaround(itinerary);
        }
        String[] aps = itinerary.getAPs();
        int tai = itinerary.journey_turnaround;
        String apc;
        for (int n=1; n<aps.length-1; n++) {
            if (itinerary.journey_turnaround != n) {
                apc = aps[n];
                if ((n & 1) == 1) {
                    if ((tai - 1 != n) || !aps[tai].equals(aps[n])) {
                        result = service.checkLocation(jrny_geo_spec_via_loc,
                                jrny_geo_spec_via_loc_value, apc, PJL, carrier, book);
                    }
                } else {
                    if ((tai + 1 != n) || !aps[tai].equals(aps[n])) {
                        result = service.checkLocation(jrny_geo_spec_via_loc,
                                jrny_geo_spec_via_loc_value, apc, PJL, carrier, book);
                    }
                }
                if (result) {
                    break;
                }
            }
        }

        log.debug("check via loc");
        return result;
    }

    private boolean checkWholeWithin(Itinerary itinerary, byte jrny_geo_spec_trvl_w_w_l,
                                     String jrny_geo_spec_trvl_w_w_l_v) {
        boolean result = true;
        Sector[] sectors = itinerary.sectors;
        String carrier = itinerary.sectors[0].mcxr;
        int book = DEU.i8(itinerary.bdate);
        for (Sector sector : sectors) {
            result = service.checkLocation(jrny_geo_spec_trvl_w_w_l,
                    jrny_geo_spec_trvl_w_w_l_v, sector.from, PJL, carrier, book);

            if (result) {
                result = service.checkLocation(jrny_geo_spec_trvl_w_w_l,
                        jrny_geo_spec_trvl_w_w_l_v, sector.to, PJL, carrier, book);
            }

            if (!result) {
                break;
            }
        }
        log.debug("checked whole within");
        return result;
    }

}