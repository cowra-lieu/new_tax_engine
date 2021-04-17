package com.btw.tax_engine.computing_core;

import com.btw.tax_engine.common.Const;
import com.btw.tax_engine.common.DEU;
import com.btw.tax_engine.common.SU;
import com.btw.tax_engine.common.bean.*;
import com.btw.tax_engine.quick_data_access.AirportRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static com.btw.tax_engine.common.DEU.i8;

@Component
public class SpecialProcessor {

    private AirportRepo apRepo;
    private AirportInfoService as;
    private XchangeService xs;

    @Autowired
    public void setApRepo(AirportRepo apRepo) {
        this.apRepo = apRepo;
    }
    @Autowired
    public void setAs(AirportInfoService as) {
        this.as = as;
    }
    @Autowired
    public void setXs(XchangeService xs) {
        this.xs = xs;
    }

    public final void process(Itinerary iti, List<TaxFeeItem> feeList, List<SectorTaxFeeItem> sectorFeeList) {
//        case_arj(iti, feeList, sectorFeeList);
        case_cncn001(iti, feeList, sectorFeeList);
    }

    private void case_arj(Itinerary iti, List<TaxFeeItem> feeList, List<SectorTaxFeeItem> sectorFeeList) {
        double total = 0;
        boolean found = false;
        if (!iti.is_intl && iti.sectors.length<3) {
            for (int i=0; i<iti.sectors.length; i++) {
                Sector s = iti.sectors[i];
                if ("ARJ".equals(s.ptype)) {
                    int ts = i + 1;
                    for (SectorTaxFeeItem stfi : sectorFeeList) {
                        if ("CN".equals(stfi.k.nation) && "CN".equals(stfi.k.tax_Code)
                                && Integer.parseInt(stfi.secNo) == ts) {
                            total += stfi.amount;
                            stfi.amount = 0;
                            found = true;
                        }
                    }
                }
            }
            if (found) {
                for (TaxFeeItem tfi : feeList) {
                    if ("CN".equals(tfi.code)) {
                        tfi.amount -= total;
                        break;
                    }
                }
            }
        }
    }

    private void case_cncn001(Itinerary iti, List<TaxFeeItem> feeList, List<SectorTaxFeeItem> sectorFeeList) {
        double total = 0;
        if ( !iti.is_intl && "CN".equals(as.getNation(iti.sectors[0].from)) ) {
            int bdate = DEU.i8(iti.bdate);
            int sdate;
            Sector s;
            String raInfo;
            boolean raFound;
            double amount = 0;
            String curr = null;
            boolean isFlag = false;
            for (int i=0; i<iti.sectors.length; i++) {
                s = iti.sectors[i];
                if (!SU.blk(s.ptype)) {
                    raInfo = apRepo.getRAInfo(s.ptype);
                    raFound = false;
                    if (!SU.blk(raInfo)) {
                        String[] raInfos = raInfo.split(",");
                        for (String rinfo : raInfos) {
                            int d1 = Integer.parseInt(SU.nthsec(rinfo, ':', 0));
                            int d2 = Integer.parseInt(SU.nthsec(rinfo, ':', 1));
                            if (bdate >= d1 && bdate <= d2) {
                                sdate = DEU.i8(s.begin);
                                d1 = Integer.parseInt(SU.nthsec(rinfo, ':', 2));
                                d2 = Integer.parseInt(SU.nthsec(rinfo, ':', 3));
                                if (sdate >= d1 && sdate <= d2) {
                                    raFound = true;
                                    amount = Double.parseDouble(SU.nthsec(rinfo, ':', 4));
                                    curr = SU.nthsec(rinfo, ':', 5);
                                    break;
                                }
                            }
                        }
                        if (raFound) {
                            double exchange_amount = 0;
                            if (amount != 0) {
                                exchange_amount = xs.exchange(
                                        amount,
                                        curr,
                                        iti.scurr,
                                        Const.NC,
                                        Const.NC,
                                        i8(iti.bdate));
                            }
                            int ts = i + 1;
                            String ts_str = String.valueOf(ts);
                            boolean stFound = false;
                            for (SectorTaxFeeItem stfi : sectorFeeList) {
                                if (stfi.secNo.equals(ts_str) && isCNCN(stfi.k)) {
                                    total += exchange_amount - stfi.amount;
                                    stfi.amount = exchange_amount;
                                    stFound = true;
                                }
                            }
                            if (!stFound) {
                                total += exchange_amount;
                                SectorTaxFeeItem stfi = new SectorTaxFeeItem("CN", exchange_amount, curr, ts_str);
                                stfi.desc = apRepo.getTaxname("CNCN001:" + iti.lang);
                                sectorFeeList.add(stfi);
                                isFlag = true;
                            }
                        }
                    }
                }
            }
            if (isFlag) {
                Collections.sort(sectorFeeList);
            }
            for (TaxFeeItem tfi : feeList) {
                if ("CN".equals(tfi.code)) {
                    tfi.amount += total;
                    break;
                }
            }
        }
    }

    public final boolean isCNCN(TaxRule tr) {
        return tr.nation.equals("CN") && tr.tax_Code.equals("CN");
    }
}
