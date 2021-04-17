package com.btw.tax_engine.computing_core.tax;

import com.btw.tax_engine.common.Const;
import com.btw.tax_engine.common.DAU;
import com.btw.tax_engine.common.SU;
import com.btw.tax_engine.common.bean.*;
import com.btw.tax_engine.computing_core.AnalysisRouteService;
import com.btw.tax_engine.computing_core.SpecialProcessor;
import com.btw.tax_engine.computing_core.XchangeService;
import com.btw.tax_engine.computing_core.tax.filter.EasyFilter0;
import com.btw.tax_engine.computing_core.tax.filter.EasyFilter1;
import com.btw.tax_engine.computing_core.tax.filter.SubTableFilter;
import com.btw.tax_engine.computing_core.tax.filter.Taxpoint23Filter;
import com.btw.tax_engine.quick_data_access.AirportRepo;
import com.btw.tax_engine.quick_data_access.T168Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.btw.tax_engine.common.Const.*;
import static com.btw.tax_engine.common.DEU.*;
import static com.btw.tax_engine.common.SU.*;

@Service
public class TaxCalculatorImpl implements TaxCalculator {

    private TaxRuleProvider taxRuleProvider;

    private AnalysisRouteService rs;
    private XchangeService xs;

    private AirportRepo airportRepo;
    private T168Repo t168Repo;

    private EasyFilter1 easyFilter1;
    private EasyFilter0 easyFilter0;
    private SubTableFilter subTableFilter;
    private Taxpoint23Filter taxpoint23Filter;

    private SpecialProcessor sp;

    @Autowired
    public void setRs(AnalysisRouteService rs) {
        this.rs = rs;
    }
    @Autowired
    public void setXs(XchangeService xs) {
        this.xs = xs;
    }

    @Autowired
    public void setAirportRepo(AirportRepo airportRepo) {
        this.airportRepo = airportRepo;
    }
    @Autowired
    public void setT168Repo(T168Repo t168Repo) {
        this.t168Repo = t168Repo;
    }

    @Autowired
    public void setTaxRuleProvider(TaxRuleProvider taxRuleProvider) {
        this.taxRuleProvider = taxRuleProvider;
    }

    @Autowired
    public void setEasyFilter1(EasyFilter1 easyFilter1) {
        this.easyFilter1 = easyFilter1;
    }
    @Autowired
    public void setEasyFilter0(EasyFilter0 easyFilter0) {
        this.easyFilter0 = easyFilter0;
    }
    @Autowired
    public void setSubTableFilter(SubTableFilter subTableFilter) {
        this.subTableFilter = subTableFilter;
    }
    @Autowired
    public void setTaxpoint23Filter(Taxpoint23Filter taxpoint23Filter) {
        this.taxpoint23Filter = taxpoint23Filter;
    }

    @Autowired
    public void setSp(SpecialProcessor sp) {
        this.sp = sp;
    }


    @Override
    public void execute(final Itinerary iti, List<TaxFeeItem> feeList, List<SectorTaxFeeItem> sectorFeeList) {

        Map<String, SortedSet<TaxRule>> tidRuleListMap = taxRuleProvider.data();
        Map<String, SortedSet<String>> nationTaxesMap = taxRuleProvider.ntMap();

        Mofp inbound = new Mofp();
        Mofp outbound = new Mofp();
        EasyInfo ei = new EasyInfo();
        SubFilterInfo sfi = new SubFilterInfo();
        Sector[] sectors = iti.sectors;
        ei.abd = i6(iti.bdate);
        ei.afb = sectors[0].begin != null ? i6(sectors[0].begin) : ei.abd;
        int sc = sectors.length;
        int last_api = (sc >> 1) - 1;
        Sector sec;

        Map<Integer, List<SectorTaxFeeItem>> si_sectorfees = new LinkedHashMap<>();

        rs.analyzeIntlDomAndOwRt(iti);

        for (int si = 0; si < sc; si++) {
            sec = sectors[si];
            fill_mofp(inbound, sec);
            ei.asb = sectors[si].begin != null ? i6(sec.begin) : ei.abd;
            for (int i=0; i<2; i++) {

                fill_ei_sfi(inbound, outbound, ei, sfi, sectors, last_api, sec, si, i);

                for (String tid :  nationTaxesMap.get(ei.apn)) {
//                    if (!"JP104".equals(tid)) {
//                        continue;
//                    }
                    for (TaxRule k : tidRuleListMap.get(ei.apn+tid)) {

                        if (check(k, ei) && easyFilter0.check(k, iti) && easyFilter1.check(k, ei)) {

                            if (subTableFilter.check(k, iti, sfi, inbound, outbound)) {

                                if (taxpoint23Filter.check(k, iti, sfi.api, ei.atg)) {

                                    if (!si_sectorfees.containsKey(si)) {
                                        si_sectorfees.put(si, new ArrayList<>());
                                    }
                                    si_sectorfees.get(si).add(
                                            new SectorTaxFeeItem(k, si,
                                                    airportRepo.getTaxname(ei.apn+k.tax_Code+k.tax_Type+
                                                            ':'+iti.lang))
                                    );
                                    break;

                                }
                            }
                        }
                    }
                }
            }
        }

        BaseFeeIno bfi = new BaseFeeIno();
        Map<String, Integer> tid_count = new HashMap<>();
        Map<String, Double> code_max = new HashMap<>();
        Map<String, TaxFeeItem> code_fee_map = new LinkedHashMap<>();

        si_sectorfees.forEach((si, sectorfees) ->
                applyExchangeRate(sectorfees, iti, bfi, tid_count, code_max, code_fee_map));

        code_max.forEach((code, max_value) ->
                addValueToMap(code_fee_map, code.substring(0,2), code.substring(2), max_value,
                        bfi.scurr, iti.lang));

        if (!Const.INF.equals(iti.passenger.type)) {
            pfc_process(si_sectorfees, iti, code_fee_map);
        }

        code_fee_map.values().forEach( tfi -> tfi.amount = xs.round(tfi.amount, tfi.curr));

        code_fee_map.forEach((k,v)->{
            if (v.amount > 0 || k.equalsIgnoreCase("CNCN")) {
                feeList.add(v);
            }
        });

        if (sectorFeeList != null) {
            si_sectorfees.forEach((k, v) -> v.forEach(s -> {
                if (s.code != null && (s.amount > 0 || sp.isCNCN(s.k)) ) {
                    sectorFeeList.add(s);
                }
            })
            );
        }

        sp.process(iti, feeList, sectorFeeList);

    }

    private static final Set<String> US_SET = new HashSet<>();
    static {
        US_SET.add("US");
        US_SET.add("AS");
        US_SET.add("GU");
        US_SET.add("MP");
        US_SET.add("PR");
        US_SET.add("VI");
    }

    private void pfc_process(Map<Integer, List<SectorTaxFeeItem>> si_sectorfees, Itinerary iti,
                             Map<String, TaxFeeItem> code_fee) {

        Map<String, List<XFRule>> apc_xfs = taxRuleProvider.xf();
        List<XFOption> xfos = taxRuleProvider.xfo();
        char co = NC;
        int bdate8 = i8(iti.bdate);
        for (XFOption xfo : xfos) {
            if (xfo.disDate >= bdate8 && xfo.effDate <= bdate8) {
                co = xfo.co;
                break;
            }
        }

        List<XFRule> xfRuleList;
        Sector[] sectors = iti.sectors;
        String apc;
        String apn;
        double xf_amount = 0;
        int xf_limit = 0;
        boolean us = "USD".equals(iti.scurr);

        int sno = 0;
        if (co == '1' && !us) {
            sno = sectors.length - 1;
            while (sno >= 0) {
                apc = sectors[sno].from;
                apn = nthsec(airportRepo.getRawValue(apc), NATION);
                if (US_SET.contains(apn)) {
                    xfRuleList = apc_xfs.get(apc);
                    xf_amount += getXf_amount(si_sectorfees, iti, xfRuleList, bdate8, sno, apn);
                    break;
                }
                sno -= 1;
            }
        } else if (co == '2' || us) {
            rs.analyzeTurnaround(iti);
            while (sno < sectors.length && xf_limit < 2) {
                apc = sectors[sno].from;
                apn = nthsec(airportRepo.getRawValue(apc), NATION);
                if (US_SET.contains(apn)) {
                    xfRuleList = apc_xfs.get(apc);
                    xf_amount += getXf_amount(si_sectorfees, iti, xfRuleList, bdate8, sno, apn);
                    xf_limit += 1;
                }
                sno += 1;
                if (iti.rtn_to_orig && sno >= iti.turnaroundNo) {
                    break;
                }
            }
            if (iti.rtn_to_orig) {
                xf_limit = 0;
                sno = sectors.length - 1;
                while (xf_limit < 2 && sno >= iti.turnaroundNo && sno>-1) {
                    apc = sectors[sno].from;
                    apn = nthsec(airportRepo.getRawValue(apc), NATION);
                    if (US_SET.contains(apn)) {
                        xfRuleList = apc_xfs.get(apc);
                        xf_amount += getXf_amount(si_sectorfees, iti, xfRuleList, bdate8, sno, apn);
                        xf_limit += 1;
                    }
                    sno -= 1;
                }
            }
        }

        if (xf_amount > 0) {
            code_fee.put("XF", new TaxFeeItem("XF", xf_amount, iti.scurr,
                    airportRepo.getTaxname("USXF001:"+iti.lang)));
        }
    }

    private double getXf_amount(Map<Integer, List<SectorTaxFeeItem>> si_sectorfees,
                                Itinerary iti, List<XFRule> xfRuleList,
                                int bdate8, int forward_sno, String apn) {
        XFRule xfr = getXFRule(xfRuleList, bdate8);
        if (!si_sectorfees.containsKey(forward_sno)) {
            si_sectorfees.put(forward_sno, new ArrayList<>());
        }
        SectorTaxFeeItem tmp = new SectorTaxFeeItem("XF",
                xs.exchange(xfr.amount, xfr.currency, iti.scurr, NC, NC, bdate8),
                iti.scurr, forward_sno);
        tmp.desc = airportRepo.getTaxname(apn+"XF001:"+iti.lang);
        si_sectorfees.get(forward_sno).add(tmp);
        return tmp.amount;
    }

    private XFRule getXFRule(List<XFRule> xfrs, int bdate6) {
        XFRule result = null;
        for (XFRule r : xfrs) {
            if (r.effdate <= bdate6 && bdate6 <= r.disdate) {
                result = r;
                break;
            }
        }
        return result;
    }

    private void applyExchangeRate(List<SectorTaxFeeItem> trList, Itinerary iti, BaseFeeIno bfi,
                                   Map<String, Integer> tid_count, Map<String, Double> code_max,
                                   Map<String, TaxFeeItem> code_fee_map) {

        Collections.sort(trList);
        double exchange_amount;
        String t168No, t168Codes, t168Code;
        TaxRule temp;
        String n_c;
        for (int i=0; i<trList.size(); i++) {
            SectorTaxFeeItem sectorTaxFeeItem = trList.get(i);
            bfi.k = sectorTaxFeeItem.k;
            if (bfi.k.taxAmount > 0 || bfi.k.taxPercent > 0 || sp.isCNCN(sectorTaxFeeItem.k)) {

                if (bfi.k.taxPercent > 0) {
                    bfi.baseFeeAmount = 0;
                    bfi.baseFeeCurr = iti.scurr;

                    t168No = bfi.k.service_And_Baggage_Table_168;
                    if (!EIGHT_ZERO.equals(t168No)) {
                        t168Codes = t168Repo.getRawValue(t168No);
                        for (int j = 0; j < Integer.MAX_VALUE; j++) {
                            t168Code = nthsec(t168Codes, ',', j);
                            if (t168Code == null) {
                                break;
                            }
                            for (int m = 0; m<i; m++) {
                                if ((temp = trList.get(m).k).tax_Code.equals(t168Code)) {
                                    bfi.baseFeeAmount += temp.taxAmount;
                                    bfi.baseFeeCurr = temp.tax_Calculation_Cur;
                                }
                            }
                        }
                    }
                    if ('A' == DAU.c(bfi.k.tax_Calculation_Tax_App_To_Tag) && 'X' == DAU.c(bfi.k.tax_Unit_Tg_Tag9)) {
                        bfi.baseFeeAmount += iti.yqyrFee + DAU.d(iti.fee.fare);
                    }

                    bfi.baseFeeAmount = bfi.baseFeeAmount * bfi.k.taxPercent;
                    bfi.k.taxAmount = bfi.baseFeeAmount;
                    bfi.k.tax_Calculation_Cur = bfi.baseFeeCurr;
                }

                exchange_amount = xs.exchange(
                        bfi.k.taxAmount,
                        bfi.k.tax_Calculation_Cur,
                        iti.scurr,
                        DAU.c(bfi.k.tax_Round_Unit),
                        DAU.c(bfi.k.tax_Round_Dir),
                        i8(iti.bdate));


                sectorTaxFeeItem.code = bfi.k.tax_Code;
                sectorTaxFeeItem.amount = exchange_amount;
                sectorTaxFeeItem.curr = iti.scurr;

                String tid = bfi.k.tax_Code + bfi.k.tax_Type;
                int count;
                switch (bfi.k.limit) {
                    case Const.NC:
                        addValueToMap(code_fee_map, bfi.k.nation, bfi.k.tax_Code, exchange_amount, iti.scurr, iti.lang);
                        break;
                    case '1':
                        if (!tid_count.containsKey(tid)) {
                            tid_count.put(tid, 1);
                            addValueToMap(code_fee_map, bfi.k.nation, bfi.k.tax_Code, exchange_amount, iti.scurr, iti.lang);
                        }
                        break;
                    case '2':
                    case '4':
                        count= getTidCount(tid_count, tid);
                        if (count < 2) {
                            tid_count.put(tid, count+1);
                            addValueToMap(code_fee_map, bfi.k.nation, bfi.k.tax_Code, exchange_amount, iti.scurr, iti.lang);
                        }
                        break;
                    case '5':
                        count= getTidCount(tid_count, tid);
                        if (count < 4) {
                            tid_count.put(tid, count+1);
                            addValueToMap(code_fee_map, bfi.k.nation, bfi.k.tax_Code, exchange_amount, iti.scurr, iti.lang);
                        }
                        break;
                    case '3':
                        n_c = bfi.k.nation + bfi.k.tax_Code;
                        if (exchange_amount > getMaxAmount(code_max, n_c)) {
                            code_max.put(n_c, exchange_amount);
                        }
                        break;
                }
            }
        }

    }

    private void addValueToMap(Map<String, TaxFeeItem> code_fee, String nation, String code, double amount,
                               String curr, String lang) {
        String n_c = nation + code;
        if (code_fee.containsKey(n_c)) {
            code_fee.get(n_c).amount += amount;
        } else {
            code_fee.put(n_c, new TaxFeeItem(code, amount, curr,
                    airportRepo.getTaxname(n_c+"001:"+lang)));
        }
    }

    private int getTidCount(Map<String, Integer> tid_count, String tid) {
        int result  = 0;
        if (!tid_count.containsKey(tid)) {
            tid_count.put(tid, 0);
        } else {
            result = tid_count.get(tid);
        }
        return result;
    }

    private double getMaxAmount(Map<String, Double> code_max, String  nationAndcode) {
        double result = 0;
        if (!code_max.containsKey(nationAndcode)) {
            result = code_max.get(nationAndcode);
        }
        return result;
    }

    private void fill_ei_sfi(Mofp inbound, Mofp outbound, EasyInfo ei, SubFilterInfo sfi, Sector[] sectors,
                             int last_api, Sector sec, int si, int i) {
        sfi.apc = i == 0 ? sec.from : sec.to;
        sfi.apInfo = airportRepo.getRawValue(sfi.apc);
        sfi.si = si;
        sfi.api = (si << 1) + i;

        ei.apn = nthsec(sfi.apInfo, NATION);
        ei.atg = decide_tax_point_tag(i, si, sectors, outbound);
        ei.att = tax_transfer_type((sfi.api != 0) && (sfi.api != last_api), inbound, outbound);
    }

    private char tax_transfer_type(boolean inMiddle, Mofp inbound, Mofp outbound) {
        char result = NC;
        if (inMiddle) {
            if (!inbound.m.equals(outbound.m)) {
                result = 'A';
            } else {
                if (!inbound.f.equals(outbound.f)) {
                    result = 'B';
                } else {
                    if (inbound.p.equals(outbound.p) && !"CHG".equals(inbound.p)) {
                        result = 'D';
                    } else {
                        result = 'C';
                    }
                }
            }
        }
        return result;
    }

    private char decide_tax_point_tag(int i, int si, Sector[] sectors, Mofp outbound) {
        char result = 'D';
        outbound.clear();
        if (i==0) {
            if (si > 0) {
                fill_mofp(outbound, sectors[si-1]);
            }
        } else {
            result = 'A';
            if (si < sectors.length - 1) {
                fill_mofp(outbound, sectors[si+1]);
            }
        }
        return result;
    }

    private void fill_mofp(Mofp outbound, Sector sector) {
        outbound.m = SU.blk(sector.mcxr) ? DEFAULT_CXR : sector.mcxr;
        outbound.o = SU.blk(sector.ocxr) ? DEFAULT_CXR : sector.ocxr;
        outbound.f = SU.blk(sector.mfno) ? "1000" : sector.mfno;
        outbound.p = SU.blk(sector.ptype) ? "E90" :sector.ptype;
    }

    public boolean check(TaxRule k, EasyInfo ei) {
        return  ei.abd >= k.sdf
                && (null == k.ticket_Value_Application)
                && (null == k.paid_By_Third_Party)
                && (null == k.tax_Matching_Appl_Tag || !k.tax_Code.equals("CA"));
    }

    private static class BaseFeeIno {
        double baseFeeAmount;
        String baseFeeCurr;
        String scurr;
        TaxRule k;
    }

    public static class Mofp {
        public String m;
        public String o;
        public String f;
        public String p;

        void clear() {
            m = "MU";
            o = "MU";
            f = "1000";
            p = "E90";
        }
    }

    public static class EasyInfo {
        public String apn;
        public int abd;
        public int afb;
        public int asb;
        public char att;
        public char atg;
    }

    public static class SubFilterInfo {
        public String apInfo;
        public String apc;
        public int si;
        public int api;
    }

}
