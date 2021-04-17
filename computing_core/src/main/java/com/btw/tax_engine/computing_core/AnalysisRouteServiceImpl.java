package com.btw.tax_engine.computing_core;

import com.btw.tax_engine.common.DEU;
import com.btw.tax_engine.common.bean.Itinerary;
import com.btw.tax_engine.common.bean.Sector;
import com.btw.tax_engine.common.bean.TaxRule;
import com.btw.tax_engine.quick_data_access.AirportCustomRepo;
import com.btw.tax_engine.quick_data_access.CabinRepo;
import com.btw.tax_engine.quick_data_access.TMPMRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.btw.tax_engine.common.Const.*;
import static com.btw.tax_engine.common.DEU.i8;
import static com.btw.tax_engine.common.SU.nthsec;
import static com.btw.tax_engine.common.SU.rnthsec;

@Service
public class AnalysisRouteServiceImpl implements AnalysisRouteService {


    private AirportInfoService as;
    private TMPMRepo tpmDao;
    private AirportCustomRepo airportCustRepo;
    private CabinRepo cabinRepo;

    /**
     * Check if provided airports with same city
     * @param airportCode1 an airport code
     * @param airportCode2 another airport code
     * @return true if two airports with same city, otherwise false
     */
    @Override
    public boolean withSameCity(String airportCode1, String airportCode2) {
        return as.getCity(airportCode1).equals(as.getCity(airportCode2));
    }

    /**
     * Check if provided airports with same country
     * @param airportCode1 an airport code
     * @param airportCode2 another airport code
     * @return true if two airports with same country, otherwise false
     */
    @Override
    public boolean withSameCountry(String airportCode1, String airportCode2) {
        String c1 = as.getNation(airportCode1);
        String c2 = as.getNation(airportCode2);
        if ("XU".equals(c1)) {
            c1 = "RU";
        }
        if ("XU".equals(c2)) {
            c2 = "RU";
        }
        return c1.equals(c2);
    }

    /**
     * Analyze the segments of specified itinerary.
     * If itinerary is international, the is_intl field of itinerary will be set as true, otherwise false.
     * If itinerary is one way, the is_ow field of itinerary will be set as true, otherwise false.
     * @param itinerary the reference to an Itinerary object
     */
    @Override
    public void analyzeIntlDomAndOwRt(Itinerary itinerary) {
        itinerary.is_intl = false;
        itinerary.is_ow = true;
        Sector[] sectors = itinerary.sectors;
        String origAirportCode  = sectors[0].from;
        String destAirportCode = sectors[sectors.length-1].to;
        for (Sector s : sectors) {
            s.departure_to_origin_is_dom = withSameCountry(origAirportCode, s.from);
            s.arrival_to_origin_is_dom = withSameCountry(origAirportCode, s.to);
            if (!s.departure_to_origin_is_dom || !s.arrival_to_origin_is_dom) {
                itinerary.is_intl = true;
            }
            s.isDom = withSameCountry(s.from, s.to);
            if (s.cabin == NC) {
                s.cabin = cabinRepo.getCabin(s.mcxr, s.clazz, i8(new Date()));
            }
        }
        if (!itinerary.is_intl) {
            if (withSameCity(origAirportCode, destAirportCode)) {
                itinerary.is_ow = false;
                itinerary.rtn_to_orig = true;
            }
        } else if (withSameCountry(origAirportCode, destAirportCode)) {
            itinerary.is_ow = false;
            itinerary.rtn_to_orig = true;
        }
        itinerary.atpco_bdate = itinerary.bdate.getTime();
        itinerary.tdate = itinerary.sectors[0].begin.getTime();
    }

    /**
     * Analyze the turnaround of specified itinerary.
     * After analyze the international or domestic and one way or round trip of the itinerary,
     * it's possible to find the turnaround.
     *
     * @param itinerary the reference to an Itinerary object
     */
    @Override
    public void analyzeTurnaround(Itinerary itinerary) {
        if (!itinerary.hasCheckedTurnaround && !itinerary.is_ow) {
            Sector[] sectors = itinerary.sectors;
            Sector currentSector, nextSector;
            boolean without_stopover = true;
            for (int i=0; i<sectors.length-1; i++) {
                currentSector = sectors[i];
                nextSector = sectors[i+1];
                if (!withSameCity(currentSector.to, nextSector.from)) {
                    // here is a surface sector
                    currentSector.arrival_is_stopover = true;
                    nextSector.departure_is_stopover = true;
                    without_stopover = false;
                } else if (DEU.moreThanOneDay(currentSector.end, nextSector.begin)) {
                    // here is a timeout
                    currentSector.arrival_is_stopover = true;
                    without_stopover = false;
                }
            }
            if (without_stopover) {
                if (itinerary.is_symmetry()) {
                    itinerary.turnaroundNo = (byte)(sectors.length >> 1) + 1;
                    itinerary.journey_turnaround = (byte)((itinerary.turnaroundNo << 1) - 2);
                } else {
                    analyzeFurthestStopoverOrTicketedPoint(itinerary, false);
                }
            } else {
                analyzeFurthestStopoverOrTicketedPoint(itinerary, true);
            }
            if (itinerary.journey_turnaround >= 0) {
                itinerary.journey_turnaround_code = itinerary.getAPs()[itinerary.journey_turnaround];
            }
            itinerary.hasCheckedTurnaround = true;
        }
    }

    /**
     * According to the specified parameters, analyze the furthest stopover or ticketed point.
     *
     * @param itinerary the reference to an Itinerary object
     * @param stopoverExist indicates if there is stopover in itinerary
     */
    @Override
    public void analyzeFurthestStopoverOrTicketedPoint(Itinerary itinerary, boolean stopoverExist) {
        if (itinerary.is_intl) {
            if (stopoverExist) {
                if (!findFurthestPoint(itinerary, false, true)) {
                    if (!findFurthestPoint(itinerary, true, true)) {
                        findFurthestPoint(itinerary, false, false);
                    }
                }
            } else {
                findFurthestPoint(itinerary, false, false);
            }
        } else {
            if (stopoverExist) {
                if (!findFurthestPoint(itinerary, true, true)) {
                    findFurthestPoint(itinerary, true, false);
                }
            } else {
                findFurthestPoint(itinerary, true, false);
            }
        }
    }

    /**
     * According to the specified parameters, find the furthest stopover or ticketed point.
     * If a competent stopover or ticketed point had been found, the corresponding index will be
     * set to the journey_turnaround and turnaroundNo fields of itinerary.
     *
     * The journey_turnaround is 0-based sequence number of the turnaround airport.
     * The turnaroundNo is 1-based sequence number of the turnaround segment.
     *
     * If the turnaround airport is a departure airport, the turnaround segment will be previous one.
     *
     * @param itinerary the reference to an Itinerary object
     * @param domLine indicates if current itinerary is domestic
     * @param requireStopover indicates if the turnaround should be a stopover
     * @return true, if a competent stopover or ticketed point has been found; otherwise false.
     */
    @Override
    public boolean findFurthestPoint(Itinerary itinerary, boolean domLine, boolean requireStopover) {
        boolean result = false;
        Sector[] sectors = itinerary.sectors;
        int segSize = sectors.length;
        int endSegIndex = segSize - 1;
        String originAirport = sectors[0].from;
        double maxTPM = 0, tpm, tpm0, tpm1;
        int furthestIndex = -1;
        String tmpCode;
        boolean dept;
        Sector sector;
        for (int i=0; i<segSize; i++) {
            sector = sectors[i];
            tpm = 0; tpm0 = 0; tpm1 = 0; tmpCode = null;
            dept = false;
            if (i == 0) {
                // only cares about the arrival point in first sector
                if ( (!requireStopover || sector.arrival_is_stopover)
                        && (domLine == sector.arrival_to_origin_is_dom)) {
                    tpm = getTPM(originAirport, sector.to);
                    tmpCode = sector.to;
                }
            } else if (i == endSegIndex) {
                // Only cares about the departure point in last sector,
                // as the departure may be the end point of a surface sector
                if ( (!requireStopover || sector.departure_is_stopover)
                        && (domLine == sector.departure_to_origin_is_dom)
                        && !sectors[i-1].to.equals(sector.from)) {
                    tpm = getTPM(originAirport, sector.from);
                    tmpCode = sector.from;
                    dept = true;
                }
            } else {
                // Both departure and arrival point need to be checked in other sectors,
                // as the departure may be the end point of a surface sector
                if ( (!requireStopover || sector.departure_is_stopover)
                        && (domLine == sector.departure_to_origin_is_dom)
                        && !sectors[i-1].to.equals(sector.from)) {
                    tpm0 = getTPM(originAirport, sector.from);
                }
                if ( (!requireStopover || sector.arrival_is_stopover)
                        && (domLine == sector.arrival_to_origin_is_dom)) {
                    tpm1 = getTPM(originAirport, sector.to);
                }
                if (tpm0 >= tpm1) {
                    dept = true;
                    tpm = tpm0;
                    tmpCode = sector.from;
                } else {
                    tpm = tpm1;
                    tmpCode = sector.to;
                }
            }
            if (tpm > maxTPM) {
                maxTPM = tpm;
                furthestIndex = dept ? (i << 1) : (i << 1) + 1 ;
                itinerary.journey_turnaround_code = tmpCode;
            }
        }
        if (maxTPM > 0) {
            if ((furthestIndex & 1) == 1) {
                furthestIndex += 1;
            }
            itinerary.journey_turnaround = furthestIndex;
            itinerary.turnaroundNo = (furthestIndex >> 1) + 1;
            result = true;
        }
        return result;
    }

    /**
     * Check if specified airport code can match the specified rule loc
     *
     * @param locType specify loc type which may be
     *                         P(airport), C(city), N(nation), Z(zone), A(area), U(user define)
     * @param loc a specific loc related with the above locType
     * @param code an airport code
     * @return true, if the airport can match the specified loc, otherwise false.
     */
    @Override
    @Cacheable(value = "checkLocationCache", key = "#p0+#p1+#p2+#p3+#p4+#p5")
    public boolean checkLocation(byte locType, String loc, String code,
                                 String partition, String carrier, int bdate) {
        boolean result = false;
        if (LOC_TYPE_AIRPORT == locType) {
            result = code.equals(loc);
        } else if (LOC_TYPE_CITY == locType) {
            result = loc.equals(as.getCity(code));
        } else if (LOC_TYPE_NATION == locType) {
            result = loc.equals(as.getNation(code));
        } else if (LOC_TYPE_ZONE == locType) {
            result = loc.equals(as.getZone(code));
        } else if (LOC_TYPE_AREA == locType) {
            result = loc.equals(as.getArea(code));
        } else if (LOC_TYPE_USER_DEFINE == locType) {
            switch (partition) {
                case "PJL":
                    result = airportCustRepo.isEffectivePJL(carrier, code, loc, bdate);
                    break;
                case "PSL":
                    result = airportCustRepo.isEffectivePSL(carrier, code, loc, bdate);
                    break;
                case "PSV":
                    result = airportCustRepo.isEffectivePSV(carrier, code, loc, bdate);
                    break;
            }
        }
        return result;
    }

    /**
     * Get the TPM which is based on the specified departure airport code and arrival airport code.
     * @param fromCode the departure airport code
     * @param toCode the arrival airport code
     * @return a float number represents the TPM
     */
    @Override
    public double getTPM(String fromCode, String toCode) {
        double result = tpmDao.getTPM(fromCode, toCode);
        if (result == 0) {
//            log.warn("No TPM found for {}-{}", fromCode, toCode);
            result = tpmDao.getMPM(fromCode, toCode);
            if (result == 0) {
//                log.warn("No TPM and MPM found for {}-{}", fromCode, toCode);
                result = tpmDao.getGCM(fromCode, toCode);
            }
        }
        return result;
    }

    @Override
    public boolean airport_area_matching(String code, String atype, String avalue) {
        boolean result = false;
        if ("P".equals(atype)) {
            result = code.equals(avalue);
        } else {
            //a:sa:z:sz:n:s:ss:city:scity:u50
            String aInfo = as.getValue(code);
            switch (atype) {
                case "C":
                    result = nthsec(aInfo, 7, 10).equals(avalue)
                            || nthsec(aInfo, 8, 10).equals(avalue);
                    break;
                case "S":
                    result = nthsec(aInfo, 5, 10).equals(avalue)
                            || nthsec(aInfo, 6, 10).equals(avalue);
                    break;
                case "N":
                    result = nthsec(aInfo, 4, 10).equals(avalue);
                    break;
                case "Z":
                    result = nthsec(aInfo, 2, 10).equals(avalue)
                            || nthsec(aInfo, 3, 10).equals(avalue);
                    break;
                case "A":
                    result = nthsec(aInfo, 0, 10).equals(avalue)
                            || nthsec(aInfo, 1, 10).equals(avalue);
                    break;
            }
        }
        return result;
    }

    @Override
    public boolean us_special_process(TaxRule k, Itinerary itinerary) {
        boolean result = false;
        if ( k.nation.equals(US) && k.tax_Code.equals(US) &&
                (k.tax_Type.equals("005") || k.tax_Type.equals("006")) &&
            !itinerary.spoint.equals(US)){
            Sector[] ss = itinerary.sectors;
            String originCode = ss[0].from;
            String destCode = ss[ss.length-1].to;
            String oriInfo = as.getValue(originCode);
            //a:sa:z:sz:n:s:ss:city:scity:u50
            if (rnthsec(oriInfo, 0).length() == 0 &&
                    (rnthsec(as.getValue(destCode), 0)).length() == 0) {
                if (!itinerary.rtn_to_orig) {
                    result = true;
                }
            }
        } else {
            result = true;
        }
        return result;
    }

    @Autowired
    public void setAs(AirportInfoService as) {
        this.as = as;
    }

    @Autowired
    public void setCabinRepo(CabinRepo cabinRepo) {
        this.cabinRepo = cabinRepo;
    }

    @Autowired
    public void setTpmDao(TMPMRepo tpmDao) {
        this.tpmDao = tpmDao;
    }

    @Autowired
    public void setAirportCustRepo(AirportCustomRepo airportCustRepo) {
        this.airportCustRepo = airportCustRepo;
    }
}
