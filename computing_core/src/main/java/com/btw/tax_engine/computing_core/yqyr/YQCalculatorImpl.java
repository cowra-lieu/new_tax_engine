package com.btw.tax_engine.computing_core.yqyr;

import com.btw.tax_engine.common.DAU;
import com.btw.tax_engine.common.DEU;
import com.btw.tax_engine.common.bean.Itinerary;
import com.btw.tax_engine.common.bean.Sector;
import com.btw.tax_engine.common.bean.YqFeeItem;
import com.btw.tax_engine.common.bean.YqYrRule;
import com.btw.tax_engine.computing_core.AnalysisRouteService;
import com.btw.tax_engine.computing_core.XchangeService;
import com.btw.tax_engine.computing_core.yqyr.filter.EasyRuleFilter;
import com.btw.tax_engine.computing_core.yqyr.filter.JourneyRuleFilter;
import com.btw.tax_engine.computing_core.yqyr.matcher.IRuleMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.btw.tax_engine.common.Const.*;
import static com.btw.tax_engine.common.DEU.i8;
import static com.btw.tax_engine.common.SU.blk;

@Service
public class YQCalculatorImpl implements YQCalculator {

    private static final Logger log = LoggerFactory.getLogger(YQCalculatorImpl.class);

    private EasyRuleFilter easyRuleFilter;
    private JourneyRuleFilter journeyRuleFilter;
    private IRuleMatcher sectorPortionRuleMatcher;

    private AnalysisRouteService routeService;
    private XchangeService xs;

    private YqRuleProvider yqRuleProvider;

    @Autowired
    public void setEasyRuleFilter(EasyRuleFilter easyRuleFilter) {
        this.easyRuleFilter = easyRuleFilter;
    }

    @Autowired
    public void setJourneyRuleFilter(JourneyRuleFilter journeyRuleFilter) {
        this.journeyRuleFilter = journeyRuleFilter;
    }

    @Autowired
    public void setSectorPortionRuleMatcher(IRuleMatcher sectorPortionRuleMatcher) {
        this.sectorPortionRuleMatcher = sectorPortionRuleMatcher;
    }

    @Autowired
    public void setYqRuleProvider(YqRuleProvider yqRuleProvider) {
        this.yqRuleProvider = yqRuleProvider;
    }

    @Autowired
    public void setRouteService(AnalysisRouteService routeService) {
        this.routeService = routeService;
    }

    @Autowired
    public void setXs(XchangeService xs) {
        this.xs = xs;
    }

    public void execute(Itinerary itinerary, List<YqFeeItem> feeList, String feeName) {
        int sectorIndex = 0;
        int nextSectorIndex;
        boolean hasAppliedRule;
        int sectorSize = itinerary.sectors.length;

        routeService.analyzeIntlDomAndOwRt(itinerary);

        String[] per = new String[]{"", "", ""};
        double[] max = new double[3];
        Map<String, List<YqYrRule>> ruleListMap = yqRuleProvider.data(feeName);
        do {
            hasAppliedRule = false;
            Iterator<Map.Entry<String, List<YqYrRule>>> iter = ruleListMap.entrySet().iterator();

            while (!hasAppliedRule && iter.hasNext() && sectorIndex < sectorSize) {
                Map.Entry<String, List<YqYrRule>> me = iter.next();
                String cxr = me.getKey();
                if (cxr.equals(itinerary.sectors[sectorIndex].mcxr)) {
                    List<YqYrRule> ruleList = me.getValue();

                    for (YqYrRule rule : ruleList) {
                        if (this.easyRuleFilter.doFilter(itinerary, rule) &&
                                this.journeyRuleFilter.doFilter(itinerary, rule)) {

                            if (rule.sector_prt_geo_spec == MATCH_PER_PORTION) {
                                nextSectorIndex = sectorPortionRuleMatcher.doPortionMatch(rule, itinerary, sectorIndex);
                            } else {
                                nextSectorIndex = sectorPortionRuleMatcher.doSectorMatch(rule, itinerary, sectorIndex);
                            }
                            if (nextSectorIndex != sectorIndex) {
                                makeFeeItem(per, max, itinerary, feeName, rule,
                                        sectorIndex, nextSectorIndex - 1, feeList);
                                hasAppliedRule = true;
                                log.debug("S[{}-{}] match rule:{}-{}-{}", sectorIndex, nextSectorIndex - 1,
                                        rule.seq_no, rule.lineno, rule.service_fee_amount);
                                sectorIndex = nextSectorIndex;
                                break;
                            }
                        }
                    }   // End of for

                }// End of if

            }   // End of while

            if (!hasAppliedRule) {
                sectorIndex += 1;
            }
        } while (sectorIndex < sectorSize);

        for (int i=0; i<3; i++) {
            if (max[i]>0) {
                feeList.add(new YqFeeItem(feeName, per[i], max[i],
                        false,
                        i < 2 ? FEE_APPLICATION_PER_DIRECTION : FEE_APPLICATION_PER_JOURNEY));
            }
        }

    }

    /**
     * Convert a matched rule to FeeItem(s) that will be inserted into a list container.
     * @param per an String array with fixed length of 3. Each element may be more than one sector index joined by ';'.
     *            per[0] is the sectors indexes per outbound, per[1] is the sectors indexes per inbound
     *            and per[2] is the sector indexes per journey.
     * @param max an double array with fixed length of 3.
     *            max[0] is the max amount per outbound, per[1] is the max amount per inbound
     *            and max[2] is the max amount per journey.
     * @param itinerary as its name
     * @param feeName as it name
     * @param rule refer to a matched YqYrRule.
     * @param from the start index of sector(s) that match the above rule.
     * @param to the end index of sector(s) that match the above rule.
     * @param feeList refer to a list container of FeeItem which is the target object we want to make and fill.
     */
    private void makeFeeItem(String[] per, double[] max, Itinerary itinerary, String feeName,
                             YqYrRule rule, int from, int to, List<YqFeeItem> feeList) {

        double feeAmount = xs.exchange(rule.service_fee_amount / DAU.POWMAP.get(rule.service_fee_dec & 0xFF),
                rule.service_fee_cur, itinerary.scurr, NC, NC, i8(itinerary.bdate));

        if (rule.service_fee_application == FEE_APPLICATION_PER_JOURNEY) {
            if (feeAmount > max[2]) {
                max[2] = feeAmount;
            }
            if (from != to) {
                per[2] += blk(per[2]) ? ((1+from)+"-"+(1+to)) : (";" + (1+from)+"-"+(1+to));
            } else {
                per[2] += blk(per[2]) ? (1+from) : (";" + (1+from));
            }
        } else if (rule.service_fee_application == FEE_APPLICATION_PER_DIRECTION) {
            if (itinerary.turnaroundNo < 0 || to < itinerary.turnaroundNo-1) {
                if (feeAmount > max[0]) {
                    max[0] = feeAmount;
                }
                if (from != to) {
                    per[0] += blk(per[0]) ? ((1+from)+"-"+(1+to)) : (";" + (1+from)+"-"+(1+to));
                } else {
                    per[0] += blk(per[0]) ? (1+from) : (";" + (1+from));
                }
            } else {
                if (feeAmount > max[1]) {
                    max[1] = feeAmount;
                }
                if (from != to) {
                    per[1] += blk(per[1]) ? ((1+from)+"-"+(1+to)) : (";" + (1+from)+"-"+(1+to));
                } else {
                    per[1] += blk(per[1]) ? (1+from) : (";" + (1+from));
                }
            }
        } else {
            if (to > from) {
                if (rule.sector_prt_via_cnx_exempt == NO_EXIST) {
                    feeList.add(new YqFeeItem(feeName, (1+from)+"-"+(1+to), feeAmount * (from - to + 1)));
                } else {
                    doConnExemption(feeName, rule, itinerary, from, to, feeAmount, feeList);
                }
            } else {
                feeList.add(new YqFeeItem(feeName, String.valueOf((1+from)), feeAmount));
            }
        }
    }

    private void doConnExemption(String feeName, YqYrRule rule, Itinerary itinerary,
                                 int from, int to, double feeAmount, List<YqFeeItem> feeList) {
        int exemptionLen = 0;
        Sector[] sectors = itinerary.sectors;
        for (int i=from; i<to; i++) {
            if (isConn(rule, sectors, i, itinerary.bdate)) {
                exemptionLen += 1;
            } else {
                if (exemptionLen > 0) {
                    feeList.add(new YqFeeItem(feeName, (1+(i-exemptionLen))+"-"+(1+i), feeAmount, true));
                    exemptionLen = 0;
                }
                feeList.add(new YqFeeItem(feeName, String.valueOf((1+i)), feeAmount));
            }
        }
        if (exemptionLen > 0) {
            feeList.add(new YqFeeItem(feeName, (1+(to-exemptionLen))+"-"+(1+to), feeAmount, true));
        }
    }

    private boolean isConn(YqYrRule rule, Sector[] sectors, int sectorIndex, Date bookingDate) {
        boolean result = true;
        Sector current = sectors[sectorIndex];
        Sector next = sectors[sectorIndex+1];
        if (!routeService.withSameCity(current.to, next.from)) {
            result = false;
        } else if (sectorPortionRuleMatcher.checkStopover(current.end, next.begin, rule)) {
            result = false;
        } else if (rule.sector_prt_via_geo != NO_EXIST) {
            result = routeService.checkLocation(rule.sector_prt_via_geo, rule.sector_prt_via_geo_value,
                    current.to, PSV, current.mcxr, DEU.i8(bookingDate));
        }
        return result;
    }

}
