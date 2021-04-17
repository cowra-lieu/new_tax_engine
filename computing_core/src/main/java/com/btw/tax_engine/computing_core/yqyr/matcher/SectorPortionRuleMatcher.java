package com.btw.tax_engine.computing_core.yqyr.matcher;

import com.btw.tax_engine.common.DEU;
import com.btw.tax_engine.common.bean.Itinerary;
import com.btw.tax_engine.common.bean.Sector;
import com.btw.tax_engine.common.bean.YqYrRule;
import com.btw.tax_engine.computing_core.AnalysisRouteService;
import com.btw.tax_engine.quick_data_access.Y198Repo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.btw.tax_engine.common.Const.*;

@Service
public class SectorPortionRuleMatcher implements IRuleMatcher {

    private static final Logger log = LoggerFactory.getLogger(SectorPortionRuleMatcher.class);

    private static final Map<Byte, Integer>TimeMilliSecondsMap  = new HashMap<>();
    static {
        TimeMilliSecondsMap.put((byte)'N', 60000);
        TimeMilliSecondsMap.put((byte)'H', 3600000);
        TimeMilliSecondsMap.put((byte)'D', 86400000);
    }

    private AnalysisRouteService routeService;
    private Y198Repo subDataDao;

    @Autowired
    public void setRouteService(AnalysisRouteService routeService) {
        this.routeService = routeService;
    }

    @Autowired
    public void setSubDataDao(Y198Repo subDataDao) {
        this.subDataDao = subDataDao;
    }

    @Override
    public int doPortionMatch(YqYrRule rule, Itinerary itinerary, int sectorIndex) {
        int result = sectorIndex;
        if (!itinerary.hasCheckedTurnaround) {
            routeService.analyzeTurnaround(itinerary);
        }
        int portion_end = detect_portion_end(sectorIndex, itinerary);
        boolean matchSuccess = false;
        while (sectorIndex <= portion_end) {
            if (matchSuccess = matchPortionAgainstRule(rule, itinerary, sectorIndex, portion_end)) {
                break;
            } else {
                portion_end -= 1;
            }
        }
        if (matchSuccess) {
            log.debug("[Portion Match] <{}-{}> - {} - {} - {}", sectorIndex, portion_end,
                    rule.seq_no, rule.lineno, rule.service_fee_amount);
            result = portion_end + 1;
        }
        return result;
    }

    @Override
    public int doSectorMatch(YqYrRule rule, Itinerary itinerary, int sectorIndex) {
        int result = sectorIndex;
        if (
            itinerary.sectors[sectorIndex].mcxr.equals(rule.cxr_code)
                &&
            (rule.cabin == NC || checkCabin(rule.cabin, itinerary.sectors, sectorIndex, sectorIndex))
                &&
            (
                rule.sector_prt_intl_dom == NO_EXIST
                    ||
                checkIntlDom(rule.sector_prt_intl_dom, itinerary.sectors, sectorIndex, sectorIndex)
            )
                &&
            (rule.eqp == null || checkEquipment(rule.eqp, itinerary.sectors, sectorIndex, sectorIndex))
                &&
            (
                (rule.rbd == NO_EXIST && rule.rbd2 == NO_EXIST && rule.rbd3 == NO_EXIST)
                    ||
                checkRBDFields(rule.rbd, rule.rbd2, rule.rbd3, itinerary.sectors, sectorIndex, sectorIndex)
            )
                &&
            (rule.rbd_tbl_no_198 == null || checkRBD198(rule.rbd_tbl_no_198, itinerary, sectorIndex, sectorIndex))
                &&
            (checkLoc1Loc2(rule, itinerary.sectors, sectorIndex, sectorIndex, itinerary.bdate))
        ) {
            log.debug("[Sector Match] <{}> - {} - {} - {}", result,
                    rule.seq_no, rule.lineno, rule.service_fee_amount);
            result += 1;
        }
        return result;
    }

    private boolean matchPortionAgainstRule(YqYrRule rule, Itinerary itinerary,
                                            int sectorIndex, int portion_end) {
        boolean result = true;

        if (
            (rule.sector_prt_via_geo != NO_EXIST || rule.sector_prt_via_stp_cnx != NO_EXIST)
                &&
            rule.sector_prt_loc1 == NO_EXIST
                &&
            rule.sector_prt_loc2 == NO_EXIST
        ) {

            int span = portion_end - sectorIndex;
            if (span < 1) {
                result = false;
            } else if (span > 1) {
                portion_end = sectorIndex + 1;
            }
        }

        if ( result
                &&
            (result = (rule.cabin == NC
                    || checkCabin(rule.cabin, itinerary.sectors, sectorIndex, portion_end)))
                &&
            (result = (rule.sector_prt_intl_dom == NO_EXIST
                    || checkIntlDom(rule.sector_prt_intl_dom, itinerary.sectors, sectorIndex, portion_end)))
                &&
            (result = (rule.eqp == null || checkEquipment(rule.eqp, itinerary.sectors, sectorIndex, portion_end)))
                &&
            (result = ((rule.rbd == NO_EXIST && rule.rbd2 == NO_EXIST && rule.rbd3 == NO_EXIST)
                    || checkRBDFields(rule.rbd, rule.rbd2, rule.rbd3, itinerary.sectors, sectorIndex, portion_end)))
                &&
            (result = (rule.rbd_tbl_no_198 == null
                    || checkRBD198(rule.rbd_tbl_no_198, itinerary, sectorIndex, portion_end)))
                &&
            (result = checkLoc1Loc2(rule, itinerary.sectors, sectorIndex, portion_end, itinerary.bdate))
        ) {

            if (rule.sector_prt_via_geo == NO_EXIST) {
                if (rule.sector_prt_via_stp_cnx != NO_EXIST) {
                    result = allViaIsStpCnx(rule, itinerary, sectorIndex, portion_end);
                }
            } else {
                if (rule.sector_prt_via_stp_cnx != NO_EXIST) {
                    result = allSpecifiedViaIsStpCnx(rule, itinerary, sectorIndex, portion_end);
                } else {
                    result = hasSpecifiedViaLoc(rule, itinerary, sectorIndex, portion_end);
                }
            }

        }
        return result;
    }

    private boolean allViaIsStpCnx(YqYrRule rule, Itinerary itinerary, int sectorIndex, int portion_end) {
        boolean result = true;
        Sector[] sectors = itinerary.sectors;
        Sector sector;
        for (int i=sectorIndex; i<portion_end; i++) {
            sector = sectors[i];
            result = (rule.sector_prt_via_stp_cnx == VIA_STOPOVER) ==
                    (checkStopover(sector.end, sectors[i + 1].begin, rule));
            if (!result) {
                break;
            }
        }
        return result;
    }

    private boolean allSpecifiedViaIsStpCnx(YqYrRule rule, Itinerary itinerary, int sectorIndex, int portion_end) {
        boolean result = true;
        boolean foundViaLoc = false;
        Sector[] sectors = itinerary.sectors;
        Sector sector;
        byte locType = rule.sector_prt_via_geo;
        String locValue = rule.sector_prt_via_geo_value;
        int book = DEU.i8(itinerary.bdate);
        for (int i=sectorIndex; i<portion_end; i++) {
            sector = sectors[i];
            if (routeService.checkLocation(locType, locValue, sector.to
                    , PSV, sector.mcxr, book )) {
                foundViaLoc = true;
                result = (rule.sector_prt_via_stp_cnx == VIA_STOPOVER) ==
                        (checkStopover(sector.end, sectors[i + 1].begin, rule));
                if (!result) {
                    break;
                }
            }
        }
        return result && foundViaLoc;
    }

    private boolean hasSpecifiedViaLoc(YqYrRule rule, Itinerary itinerary, int sectorIndex, int portion_end) {
        boolean result = false;
        Sector[] sectors = itinerary.sectors;
        Sector sector;
        byte locType = rule.sector_prt_via_geo;
        String locValue = rule.sector_prt_via_geo_value;
        int book = DEU.i8(itinerary.bdate);
        for (int i=sectorIndex; i<portion_end; i++) {
            sector = sectors[i];
            result = routeService.checkLocation(locType, locValue, sector.to
                    , PSV, sector.mcxr, book);
            if (result) {
                break;
            }
        }
        return result;
    }

    @Override
    public boolean checkStopover(Date arrivalDate, Date departureDate, YqYrRule rule) {
        boolean result;
        if (rule.sector_prt_via_exc_stop_t_u == NO_EXIST) {
            result = DEU.moreThanOneDay(arrivalDate, departureDate);
        } else {
            int timeout;
            if (rule.sector_prt_via_exc_stop_t_u == SECTOR_PORTION_DAY_UNIT
                    && 0 == rule.sector_prt_via_exc_stop_time) {
                result = !DEU.is_same_day(arrivalDate, departureDate);
            } else if (rule.sector_prt_via_exc_stop_t_u == SECTOR_PORTION_MONTH_UNIT) {
                result = DEU.monthsBetween(arrivalDate, departureDate) > rule.sector_prt_via_exc_stop_time;
            } else {
                timeout = rule.sector_prt_via_exc_stop_time * TimeMilliSecondsMap.get(rule.sector_prt_via_exc_stop_t_u);
                result = (departureDate.getTime() - arrivalDate.getTime()) > timeout;
            }
        }
        return result;
    }

    private int detect_portion_end(int sectorIndex, Itinerary itinerary) {
        int result = sectorIndex;
        Sector[] sectors = itinerary.sectors;
        int lastSectorIndex = sectors.length - 1;
        String baseCarrier = sectors[sectorIndex].mcxr;
        Sector nextSector;
        int next;
        while (result < lastSectorIndex) {
            next = result + 1;
            nextSector = sectors[next];
            if ( (next << 1) != itinerary.journey_turnaround  &&
                    baseCarrier.equals(nextSector.mcxr) &&
                    routeService.withSameCity(sectors[result].to, nextSector.from)) {
                result = next;
            } else {
                break;
            }
        }
        return result;
    }

    private boolean checkLoc1Loc2(YqYrRule rule, Sector[] sectors, int startSector, int endSector,
                                  Date bookingDate) {
        boolean result = true;
        String from = sectors[startSector].from;
        String to = sectors[endSector].to;
        String fromCarrier = sectors[startSector].mcxr;
        String toCarrier = sectors[endSector].mcxr;
        int book = DEU.i8(bookingDate);
        if (rule.sector_prt_from_to == NO_EXIST) {
            if (rule.sector_prt_loc1 != NO_EXIST) {
                if (rule.sector_prt_loc2 == NO_EXIST) {
                    result = routeService.checkLocation(rule.sector_prt_loc1,
                            rule.sector_prt_loc1_value, from, PSL, fromCarrier, book) ||
                            routeService.checkLocation(rule.sector_prt_loc1,
                                    rule.sector_prt_loc1_value, to, PSL, toCarrier, book);
                } else {
                    result = (
                            routeService.checkLocation(rule.sector_prt_loc1,
                                    rule.sector_prt_loc1_value, from, PSL, fromCarrier, book) &&
                                    routeService.checkLocation(rule.sector_prt_loc2,
                                    rule.sector_prt_loc2_value, to, PSL, toCarrier, book)
                            ) ||
                            (   !from.equals(to) &&
                                    routeService.checkLocation(rule.sector_prt_loc1,
                                    rule.sector_prt_loc1_value, to, PSL, toCarrier, book) &&
                                    routeService.checkLocation(rule.sector_prt_loc2,
                                    rule.sector_prt_loc2_value, from, PSL, fromCarrier, book));
                }
            }
        } else if (rule.sector_prt_from_to == SECTOR_PORTION_FROM_LOC1) {
            result = routeService.checkLocation(rule.sector_prt_loc1,
                    rule.sector_prt_loc1_value, from, PSL, fromCarrier, book);
            if (result) {
                if (rule.sector_prt_loc2 != NO_EXIST) {
                    result = routeService.checkLocation(rule.sector_prt_loc2,
                            rule.sector_prt_loc2_value, to, PSL, toCarrier, book);
                }
            }
        } else if (rule.sector_prt_from_to == SECTOR_PORTION_TO_LOC1) {
            result = routeService.checkLocation(rule.sector_prt_loc1,
                    rule.sector_prt_loc1_value, to, PSL, toCarrier, book);
            if (result) {
                if (rule.sector_prt_loc2 != NO_EXIST) {
                    result = routeService.checkLocation(rule.sector_prt_loc2,
                            rule.sector_prt_loc2_value, from, PSL, fromCarrier, book);
                }
            }
        }
        return result;
    }

    private boolean checkIntlDom(byte intlOrDom, Sector[] sectors, int from, int to) {
        boolean result = true;
        Sector sector;
        for (int i=from; i<=to; i++) {
            sector = sectors[i];
            if (intlOrDom == IS_DOM) {
                result = routeService.withSameCountry(sector.from, sector.to);
            } else {
                result = !routeService.withSameCountry(sector.from, sector.to);
            }
            if (!result) {
                break;
            }
        }
        return result;
    }

    private boolean checkCabin(char cabin, Sector[] sectors, int from, int to) {
        boolean result = true;
        Sector sector;
        for (int i=from; i<=to; i++) {
            sector = sectors[i];
            if (sector.cabin != cabin) {
                result = false;
                break;
            }
        }
        return result;
    }

    private boolean checkEquipment(String eqp, Sector[] sectors, int from, int to) {
        boolean result = true;
        Sector sector;
        for (int i=from; i<=to; i++) {
            sector = sectors[i];
            if (eqp.equals(sector.ptype)) {
                result = false;
                break;
            }
        }
        return result;
    }

    private boolean checkRBDFields(byte rbd, byte rbd2, byte rbd3,
                                   Sector[] sectors, int from, int to) {
        boolean result = true;
        Sector sector;
        for (int i=from; i<=to; i++) {
            sector = sectors[i];
            if ((rbd != sector.clazz) && (rbd2 != sector.clazz) && (rbd3 != sector.clazz)) {
                result = false;
                break;
            }
        }
        return result;
    }

    private boolean checkRBD198(String tableNo, Itinerary itinerary, int from, int to) {
        boolean result = true;
        int bookingDate = DEU.i8(itinerary.bdate);
        Sector[] sectors = itinerary.sectors;
        Sector sector;
        for (int i=from; i<=to; i++) {
            sector = sectors[i];
            result = subDataDao.checkRBDsFrom198(tableNo, sector.mcxr, sector.clazz, bookingDate);
            if (!result) {
                break;
            }
        }
        return result;
    }
}
