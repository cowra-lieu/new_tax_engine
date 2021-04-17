package com.btw.tax_engine.computing_core.tax.filter;

import com.btw.tax_engine.common.DEU;
import com.btw.tax_engine.common.bean.Itinerary;
import com.btw.tax_engine.common.bean.Sector;
import com.btw.tax_engine.common.bean.TaxRule;
import com.btw.tax_engine.computing_core.AirportInfoService;
import com.btw.tax_engine.computing_core.AnalysisRouteService;
import com.btw.tax_engine.quick_data_access.TAirportCustRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.btw.tax_engine.common.Const.NC;

@Service
public class Taxpoint23Filter {

    private AirportInfoService as;
    private AnalysisRouteService routeService;
    private TAirportCustRepo airportCustRepo;

    @Autowired
    public void setAs(AirportInfoService as) {
        this.as = as;
    }

    @Autowired
    public void setRouteService(AnalysisRouteService routeService) {
        this.routeService = routeService;
    }

    @Autowired
    public void setAirportCustRepo(TAirportCustRepo airportCustRepo) {
        this.airportCustRepo = airportCustRepo;
    }

    public boolean check(TaxRule k, Itinerary itinerary, int apIndex, char apTag) {
        boolean result;
        String[] aps = itinerary.getAPs();
        int tNoEnd = aps.length - 1;
        result = checkLoc1StopoverTag(itinerary, k, apIndex, tNoEnd);
        if (result) {
            String[] t1PfPsSfSs = new String[4];
            result = checkNationDomestic(itinerary, k, t1PfPsSfSs, aps, apTag, apIndex, tNoEnd);
            if (result) {
                String tp2 = getTP2(itinerary, aps, apTag, apIndex, tNoEnd, k, t1PfPsSfSs);
                result = checkLoc2(tp2, k);
                if (result) {
                    result = checkTp2Domestic(tp2, aps[apIndex], k);
                    if (result) {
                        result = checkTp2Comparison(k, tp2, apTag, t1PfPsSfSs);
                        if (result) {
                            result = checkTp3(k, apTag, t1PfPsSfSs);
                        }
                    }
                }
            }
        }
        return result;
    }

    private boolean checkTp3(TaxRule k, char tPos, String[] t1PfPsSfSs) {
        boolean result = true;
        String tp3Value = k.tax_Point_Loc3_Info;
        String tp3Type = k.tax_Point_Loc3_Type;
        if (tp3Value != null) {
            String tp3 = null;
            if ("N".equals(k.tax_Point_Loc3_Loc3Type)) {
                tp3 = tPos == 'A' ? t1PfPsSfSs[1] : t1PfPsSfSs[3];
            } else if ("P".equals(k.tax_Point_Loc3_Loc3Type)) {
                tp3 = tPos == 'A' ? t1PfPsSfSs[2] : t1PfPsSfSs[0];
            }
            if ("U".equals(tp3Type)) {
                if (!airportCustRepo.isMemberOfPT3(tp3Value + tp3)) {
                    result = false;
                }
            } else if (!routeService.airport_area_matching(tp3, tp3Type, tp3Value)) {
                result = false;
            }
        }
        return result;
    }

    private boolean checkTp2Comparison(TaxRule k, String tp2, char tPos, String[] t1PfPsSfSs) {
        boolean result = true;
        String tp2_comparison = k.tax_Point_Loc2_Comparison;
        if (null != tp2_comparison) {
            char v = tp2_comparison.charAt(0);
            String tp2C = as.getCity(tp2);
            switch (v) {
                case 'X':
                    if (tPos == 'A') {
                        if (tp2C.equals(as.getCity(t1PfPsSfSs[2]))) {
                            result = false;
                        }
                    } else {
                        if (tp2C.equals(as.getCity(t1PfPsSfSs[0]))) {
                            result = false;
                        }
                    }
                    break;
                case 'Y':
                    if (tPos == 'A') {
                        if (tp2C.equals(as.getCity(t1PfPsSfSs[3]))) {
                            result = false;
                        }
                    } else {
                        if (tp2C.equals(as.getCity(t1PfPsSfSs[1]))) {
                            result = false;
                        }
                    }
                    break;
                case 'V':
                    if (tPos == 'A') {
                        if (!tp2C.equals(as.getCity(t1PfPsSfSs[2])) || tp2.equals(t1PfPsSfSs[2])) {
                            result = false;
                        }
                    } else {
                        if (!tp2C.equals(as.getCity(t1PfPsSfSs[0])) || tp2.equals(t1PfPsSfSs[0])) {
                            result = false;
                        }
                    }
                    break;
                case 'W':
                    if (tPos == 'A') {
                        if (!tp2C.equals(as.getCity(t1PfPsSfSs[3])) || tp2.equals(t1PfPsSfSs[3])) {
                            result = false;
                        }
                    } else {
                        if (!tp2C.equals(as.getCity(t1PfPsSfSs[1])) || tp2.equals(t1PfPsSfSs[1])) {
                            result = false;
                        }
                    }
                    break;
            }
        }
        return result;
    }

    private boolean checkTp2Domestic(String tp2, String ap, TaxRule k) {
        boolean result = true;
        if (k.tax_Point_Loc2_Nation_Domestic != null) {
            if (k.tax_Point_Loc2_Nation_Domestic.charAt(0) != checkID(tp2, ap)) {
                result = false;
            }
        }
        return result;
    }

    private boolean checkLoc2(String tp2, TaxRule k) {
        boolean result = true;
        if (k.tax_Point_Loc2_Info != null) {
            if (k.tax_Point_Loc2_Type.charAt(0) == 'U') {
                if (!airportCustRepo.isMemberOfPT2(k.tax_Point_Loc2_Info + tp2)) {
                    result = false;
                }
            } else if (!routeService.airport_area_matching(tp2,
                    k.tax_Point_Loc2_Type, k.tax_Point_Loc2_Info)) {
                result = false;
            }
        }
        return result;
    }
    private String getTP2(Itinerary itinerary, String[] aps, char tPos, int tNo, int tNoEnd,
                          TaxRule k, String[] t1PfPsSfSs) {
        String tp2;
        if (tPos == 'A') {
            if (k.tax_Point_Loc2_Stopover_Tag == null) {
                tp2 = t1PfPsSfSs[0];
            } else {
                char k_v = k.tax_Point_Loc2_Stopover_Tag.charAt(0);
                if (k_v == 'S') {
                    tp2 = t1PfPsSfSs[1];
                } else {
                    tp2 = judgeT2fbpOrT2ffbp(k_v, itinerary, aps, tNo, tNoEnd, tPos);
                }
            }
        } else {
            if (k.tax_Point_Loc2_Stopover_Tag == null) {
                tp2 = t1PfPsSfSs[2];
            } else {
                char k_v = k.tax_Point_Loc2_Stopover_Tag.charAt(0);
                if (k_v == 'S') {
                    tp2 = t1PfPsSfSs[3];
                } else {
                    tp2 = judgeT2fbpOrT2ffbp(k_v, itinerary, aps, tNo, tNoEnd, tPos);
                }
            }
        }
        return tp2;
    }

    private String judgeT2fbpOrT2ffbp(char loc2STag, Itinerary iti, String[] aps, int tNo, int tNoEnd, char tPos) {
        if (!iti.hasCheckedTurnaround) {
            routeService.analyzeTurnaround(iti);
        }
        int t_turnaround_no = iti.journey_turnaround < 0 ? tNoEnd : iti.journey_turnaround;
        int t2fbp = 0;
        int t2ffbp = 0;
        if (tPos == 'A') {
            if (tNo > 0 && tNo <= t_turnaround_no) {
                t2fbp = 1;
            } else if (tNo > t_turnaround_no) {
                t2fbp = t_turnaround_no;
            }
            if (tNo > 0 && tNo < tNoEnd) {
                t2ffbp = tNoEnd;
            } else if (tNo == tNoEnd) {
                t2ffbp = t_turnaround_no;
            }
        } else {
            if (tNo < t_turnaround_no) {
                t2fbp = t_turnaround_no;
            } else if (tNo < tNoEnd) {
                t2fbp = tNoEnd;
            }
            if (tNo > 0 && tNo < tNoEnd) {
                t2ffbp = tNoEnd;
            } else if (tNo == 0) {
                t2ffbp = t_turnaround_no;
            }
        }
        return 'F' == loc2STag ? aps[t2fbp] : ('T' == loc2STag ? aps[t2ffbp] : null);
    }

    private boolean checkLoc1StopoverTag(Itinerary itinerary, TaxRule k, int tNo, int tNoEnd) {
        boolean result = true;
        String tp1_stopover_tag = k.tax_Point_Loc1_Stopover_Tag;
        if (tp1_stopover_tag != null) {
            char k_v = tp1_stopover_tag.charAt(0);
            if (isStopover(itinerary, k, tNo, tNoEnd)) {
                if (k_v == 'C') {
                    result = false;
                }
            } else if (k_v == 'S') {
                result = false;
            }
        }
        return result;
    }

    private boolean checkNationDomestic(Itinerary itinerary, TaxRule k, String[] t1PfPsSfSs,
                                        String[] aps, char tPos, int tNo, int tNoEnd) {
        boolean result = true;
        int startNo = (tPos == 'A') ? tNo - 1 : tNo - 2;
        if (startNo >= 0) {
            t1PfPsSfSs[0] = aps[startNo];
            do {
                if (isStopover(itinerary, k, startNo, tNoEnd)) {
                    t1PfPsSfSs[1] = aps[startNo];
                    break;
                }
                startNo -= 2;
            } while (startNo >=0);
        }
        // ----------------------------------------------------------------------------
        startNo = (tPos == 'A') ? tNo + 2 : tNo + 1;
        if (startNo <= tNoEnd) {
            t1PfPsSfSs[2] = aps[startNo];
            do {
                if (isStopover(itinerary, k, startNo, tNoEnd)) {
                    t1PfPsSfSs[3] = aps[startNo];
                    break;
                }
                startNo += 2;
            } while (startNo <= tNoEnd);
        }
        String tp1_nation_domestic = k.tax_Point_Loc1_Nation_Domestic;
        if (tp1_nation_domestic != null) {
            char k_v = tp1_nation_domestic.charAt(0);
            if (k_v == 'J') {
                k_v = 'I';
            } else if (k_v == 'E') {
                k_v = 'D';
            }
            String code = aps[tNo];
            if (tPos == 'A') {
                if ( (checkID(t1PfPsSfSs[3], code) != k_v) && (checkID(t1PfPsSfSs[2], code) != k_v) ) {
                    result = false;
                }
            } else {
                if ( (checkID(t1PfPsSfSs[1], code) != k_v) && (checkID(t1PfPsSfSs[0], code) != k_v) )  {
                    result = false;
                }
            }
        }
        return result;
    }

    private char checkID(String code1, String code2) {
        char result = 'I';
        if (routeService.withSameCountry(code1, code2)) {
            result = 'D';
        }
        return result;
    }

    private boolean isStopover(Itinerary itinerary, TaxRule k, int tNo, int tNoEnd) {
        boolean result = true;
        if (itinerary.sectors[0].begin != null) {
            if (tNo != 0 && tNo < tNoEnd) {
                // check if the sector containing this tNo is X
                if (isX(itinerary, k, (tNo-1) >> 1)) {
                    result = false;
                }
            }
        }
        return result;
    }

    private boolean isX(Itinerary itinerary, TaxRule k, int si) {
        boolean result = false;
        if (null != k.tax_Point_Qualif_Tags_Conn_Tag &&
                checkStopoverConnection(k.tax_Point_Qualif_Tags_Conn_Tag, itinerary, si)) {
            return false;
        }
        long diffTime = 0;
        char su = NC;
        Date thisEnd = itinerary.sectors[si].end;
        Date nextFrom = itinerary.sectors[si+1].begin;
        if (k.tax_Point_Qualif_Tags_St_Unit != null) {
            long[] mhdM = new long[4];
            DEU.diffD(thisEnd, nextFrom, mhdM);
            su = k.tax_Point_Qualif_Tags_St_Unit.charAt(0);
            switch (su) {
                case 'D':
                    diffTime = mhdM[2];
                    break;
                case 'N':
                    diffTime = mhdM[0];
                    break;
                case 'M':
                    diffTime = mhdM[3];
                    break;
                case 'H':
                    diffTime = mhdM[1];
                    break;
                default:
                    if (null == k.tax_Point_Qualif_Tags_Stop_Tag) {
                        diffTime = mhdM[1];
                    }
            }
        }
        if (null == k.tax_Point_Qualif_Tags_Stop_Tag) {
            result = DEU.hours(thisEnd, nextFrom) <= 24;
        } else {
            char stopover_time = k.tax_Point_Qualif_Tags_Stop_Tag.charAt(0);
            if ('A' != stopover_time && 'D' != stopover_time) {
                long st = Long.parseLong(k.tax_Point_Qualif_Tags_Stop_Tag);
                if (st == 0 && su == 'D') {
                    result = DEU.is_same_day(thisEnd, nextFrom);
                } else if (diffTime <= st) {
                    result = true;
                }
            }
        }

        return result;
    }

    private boolean checkStopoverConnection(String connTag, Itinerary itinerary, int si) {
        boolean result = false;
        Sector[] ss = itinerary.sectors;
        Sector ts = ss[si];
        Sector ns = ss[si+1];
        String thisTo = ts.to;
        String nextFrom = ns.from;
        boolean td = ts.isDom;
        boolean nd = ns.isDom;
        if (connTag.indexOf('B') >= 0) {
            if (!itinerary.hasCheckedTurnaround) {
                routeService.analyzeTurnaround(itinerary);
            }
            if (si + 1 == itinerary.turnaroundNo - 1) {
                result = true;
            }
        }
        if (!result) {
            if (connTag.indexOf('H') >= 0 && (td && !nd && thisTo.equals(nextFrom))) {
                result = true;
            } else if (connTag.indexOf('I') >= 0 && (!td && nd && thisTo.equals(nextFrom))) {
                result = true;
            } else if (connTag.indexOf('J') >= 0 && (!td && !nd && thisTo.equals(nextFrom))) {
                result = true;
            } else if (connTag.indexOf('E') >= 0 && (!thisTo.equals(nextFrom))) {
                result = true;
            } else if (connTag.indexOf('G') >= 0 && (!thisTo.equals(nextFrom) && routeService.withSameCity(thisTo, nextFrom))) {
                result = true;
            } else if (connTag.indexOf('F') >= 0 && (!ts.mcxr.equals(ns.mcxr))) {
                result = true;
            }
        }
        return result;
    }

}
