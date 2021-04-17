package com.btw.tax_engine.common.bean;

import com.btw.tax_engine.common.Const;

import java.io.Serializable;
import java.util.Objects;

import static com.btw.tax_engine.common.DAU.*;

public class TaxRule implements Serializable, Comparable<TaxRule> {

    public String nation;
    public String tax_Code;
    public String tax_Type;
//    public String tax_Remittanceid;
    public String tax_Point_Tag;
//    public String percent_Flattag;
//    public String filler_13;
    public String seq_No;
//    public String tax_Unit_Tg_Tag1;
//    public String tax_Unit_Tg_Tag2;
//    public String tax_Unit_Tg_Tag3;
//    public String tax_Unit_Tg_Tag4;
//    public String tax_Unit_Tg_Tag5;
//    public String tax_Unit_Tg_Tag6;
//    public String tax_Unit_Tg_Tag7;
//    public String tax_Unit_Tg_Tag8;
    public String tax_Unit_Tg_Tag9;
//    public String tax_Unit_Tg_Tag10;
//    public String filler_34;
    public String calc_Order;
    public String sale_Dates_First;
    public String sale_Dates_Last;
    public String travel_Dates_Tag;
    public String travel_Dates_First;
    public String travel_Dates_Last;
//    public String tax_Carrier;
    public String carrier_Appltable_No_190;
    public String rtn_To_Orig;
    public String ptc_Table_169;
//    public String ticketed_Point_Tag;
//    public String point_Of_Sale_Type;
    public String point_Of_Sale_Info;
    public String security_Table_No_183;
//    public String filler_99;
//    public String p_Of_Ticketing_Geo_Spec_Type;
    public String p_Of_Ticketing_Geo_Spec_Info;
//    public String p_Of_Delivery_Geo_Spec_Type;
//    public String p_Of_Delivery_Geo_Spec_Info;
//    public String filler_118;
    public String jrny_Geo_Spec_Indicator;
//    public String jrny_Geo_Spec_Loc1_Type;
    public String jrny_Geo_Spec_Loc1_Info;
//    public String jrny_Geo_Spec_Loc2_Type;
    public String jrny_Geo_Spec_Loc2_Info;
    public String jrny_Geo_Spec_Jo_In_Type;
    public String jrny_Geo_Spec_Jo_In_Info;
    public String jrny_Geo_Spec_Trvl_In_Loc_Type;
    public String jrny_Geo_Spec_Trvl_In_Loc_Info;
    public String tax_Point_Loc1_Nation_Domestic;
    public String tax_Point_Loc1_Transfer_Type;
    public String tax_Point_Loc1_Stopover_Tag;
//    public String tax_Point_Loc1_Type;
    public String tax_Point_Loc1_Info;
    public String tax_Point_Loc2_Nation_Domestic;
    public String tax_Point_Loc2_Comparison;
    public String tax_Point_Loc2_Stopover_Tag;
    public String tax_Point_Loc2_Type;
    public String tax_Point_Loc2_Info;
    public String tax_Point_Loc3_Loc3Type;
    public String tax_Point_Loc3_Type;
    public String tax_Point_Loc3_Info;
//    public String filler190;
    public String tax_Point_Qualif_Tags_Stop_Tag;
    public String tax_Point_Qualif_Tags_St_Unit;
    public String tax_Point_Qualif_Tags_Conn_Tag;
//    public String service_And_Baggage_App_Tag;
    public String service_And_Baggage_Table_168;
    public String cxf_Or_Flt_Tbl_No_186;
//    public String sector_Detail_Application_Tag;
    public String sector_Detail_Table_No_167;
//    public String filler_253;
    public String ticket_Value_Application;
//    public String ticket_Value_Minimum;
//    public String ticket_Value_Maximum;
//    public String ticket_Value_Currency;
//    public String x1_Decimal;
    public String currency_Of_Sale;
    public String paid_By_Third_Party;
//    public String ratd_Date;
//    public String filler298;
//    public String historyic_Sale_Effective;
//    public String historic_Sale_Discount;
//    public String historic_Travel_Effective;
//    public String historic_Travel_Discount;
    public String tax_Calculation_Amount;
    public String tax_Calculation_Cur;
    public String tax_Calculation_Dec;
    public String tax_Calculation_Percent;
//    public String tax_Calculation_Minimum_Tax;
//    public String tax_Calculation_Maximum_Tax;
//    public String tax_Calculation_Min_Max_Curr;
//    public String tax_Calculation_Min_Max_Deci;
//    public String tax_Calculation_Vat_Inclusive;
    public String tax_Calculation_Tax_App_To_Tag;
    public String tax_Calculation_Tax_Appl_Limit;
//    public String tax_Calculation_Remit_Appl_Tag;
//    public String filler383;
    public String tax_Round_Unit;
    public String tax_Round_Dir;
//    public String alternate_Tax_Rule_Ref_Tag;
//    public String txt_Tbl_No_164;
    public String tax_Processing_Appl_Tag;
    public String tax_Matching_Appl_Tag;
//    public String output_Type_Indicator;
//    public String exempt_Tag;
    public String use_Limit;
    public String ticket_Close;
    public String cxf_Or_Flt_Tbl_No_186_1;

    public int sdf;
    public int sdl;
    public int tc;
    public int tdf;
    public int tdl;
    public int co;

    public char tpt;
    public boolean rto;
    public char tdt;
    public char loc1TT;
    public char eul;
    public char limit;

    public double taxAmount;
    public double taxPercent;

    public void after_init() {
        if (tax_Calculation_Dec != null) {
            int dec = i(tax_Calculation_Dec);
            this.taxAmount = round(d(tax_Calculation_Amount)/POWMAP.get(dec), dec);
        }
        this.taxPercent = d(tax_Calculation_Percent) / 1000000;

        this.sdf = i(sale_Dates_First);
        this.sdl = i(sale_Dates_Last);
        this.tc = i(ticket_Close);
        this.tpt = c(tax_Point_Tag);
        this.rto = c(rtn_To_Orig) == 'Y';
        this.loc1TT = c(tax_Point_Loc1_Transfer_Type);
        this.tdf = i(travel_Dates_First);
        this.tdl = i(travel_Dates_Last);
        this.co = i(calc_Order);

        if (use_Limit == null || use_Limit.length() != 1) {
            this.eul = Const.NC;
        } else {
            this.eul = c(use_Limit);
        }

        if (travel_Dates_Tag == null || travel_Dates_Tag.length() != 1) {
            this.tdt = Const.NC;
        } else {
            this.tdt = c(travel_Dates_Tag);
        }

        if (tax_Calculation_Tax_Appl_Limit == null || tax_Calculation_Tax_Appl_Limit.length() != 1) {
            this.limit = Const.NC;
        } else {
            this.limit = c(tax_Calculation_Tax_Appl_Limit);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaxRule taxRule = (TaxRule) o;
        return Objects.equals(nation, taxRule.nation) &&
                Objects.equals(tax_Code, taxRule.tax_Code) &&
                Objects.equals(tax_Type, taxRule.tax_Type) &&
                Objects.equals(seq_No, taxRule.seq_No) &&
                Objects.equals(sale_Dates_First, taxRule.sale_Dates_First) &&
                Objects.equals(sale_Dates_Last, taxRule.sale_Dates_Last) &&
                Objects.equals(travel_Dates_First, taxRule.travel_Dates_First) &&
                Objects.equals(travel_Dates_Last, taxRule.travel_Dates_Last);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nation, tax_Code, tax_Type, seq_No,
                sale_Dates_First, sale_Dates_Last, travel_Dates_First, travel_Dates_Last);
    }

    public TaxRule() {}

    public TaxRule(String[] fs) {
        int i = 0;
        this.nation = fs[i++];
        tax_Code = fs[i++];
        tax_Type = fs[i++];
        seq_No = fs[i++];
        sale_Dates_First = fs[i++];
        sale_Dates_Last = fs[i++];
        travel_Dates_First = fs[i++];
        travel_Dates_Last = fs[i];
    }

    public String key() {
        return this.nation + this.tax_Code + this.tax_Type;
    }

    @Override
    public int compareTo(TaxRule o) {
        // NATION, TAX_CODE, TAX_TYPE, SEQ_NO, SALE_DATES_FIRST desc
        int r = seq_No.compareTo(o.seq_No);
        if (r == 0) {
            r = o.sale_Dates_First.compareTo(sale_Dates_First);
        }
        return r;
    }

    //    public void setTax_Calculation_Amount(String tca) {
//        this.tax_Calculation_Amount = tca;
//        if (tax_Calculation_Dec != null) {
//            int dec = i(tax_Calculation_Dec);
//            this.taxAmount = round(d(tca)/POWMAP.get(dec), dec);
//        }
//    }
//
//    public void setTax_Calculation_Dec(String tcd) {
//        this.tax_Calculation_Dec = tcd;
//        if (tax_Calculation_Amount != null) {
//            int dec = i(tcd);
//            this.taxAmount = round(d(tax_Calculation_Amount)/POWMAP.get(dec), dec);
//        }
//    }
//
//    public void setTax_Calculation_Percent(String tcp) {
//        this.tax_Calculation_Percent = tcp;
//        this.taxPercent = d(tcp) / 1000000;
//    }
//
//    public void setSale_Dates_First(String sale_Dates_First) {
//        this.sale_Dates_First = sale_Dates_First;
//        this.sdf = i(sale_Dates_First);
//    }
//
//    public void setSale_Dates_Last(String sale_Dates_Last) {
//        this.sale_Dates_Last = sale_Dates_Last;
//        this.sdl = i(sale_Dates_Last);
//    }
//
//    public void setTicket_Close(String ticket_Close) {
//        this.ticket_Close = ticket_Close;
//        this.tc = i(ticket_Close);
//    }
//
//    public void setTax_Point_Tag(String tax_Point_Tag) {
//        this.tax_Point_Tag = tax_Point_Tag;
//        this.tpt = c(tax_Point_Tag);
//    }
//
//    public void setRtn_To_Orig(String rtn_To_Orig) {
//        this.rtn_To_Orig = rtn_To_Orig;
//        this.rto = c(rtn_To_Orig) == 'Y';
//    }
//
//    public void setTax_Point_Loc1_Transfer_Type(String tax_Point_Loc1_Transfer_Type) {
//        this.tax_Point_Loc1_Transfer_Type = tax_Point_Loc1_Transfer_Type;
//        this.loc1TT = c(tax_Point_Loc1_Transfer_Type);
//    }
//
//    public void setUse_Limit(String use_Limit) {
//        this.use_Limit = use_Limit;
//        if (use_Limit == null || use_Limit.length() != 1) {
//            this.eul = Const.NC;
//        } else {
//            this.eul = c(use_Limit);
//        }
//    }
//
//    public void setTravel_Dates_Tag(String travel_Dates_Tag) {
//        this.travel_Dates_Tag = travel_Dates_Tag;
//        if (travel_Dates_Tag == null || travel_Dates_Tag.length() != 1) {
//            this.tdt = Const.NC;
//        } else {
//            this.tdt = c(travel_Dates_Tag);
//        }
//    }
//
//    public void setTax_Calculation_Tax_Appl_Limit(String tax_Calculation_Tax_Appl_Limit) {
//        this.tax_Calculation_Tax_Appl_Limit = tax_Calculation_Tax_Appl_Limit;
//        if (tax_Calculation_Tax_Appl_Limit == null || tax_Calculation_Tax_Appl_Limit.length() != 1) {
//            this.limit = Const.NC;
//        } else {
//            this.limit = c(tax_Calculation_Tax_Appl_Limit);
//        }
//    }
//
//    public void setTravel_Dates_First(String travel_Dates_First) {
//        this.travel_Dates_First = travel_Dates_First;
//        this.tdf = i(travel_Dates_First);
//    }
//
//    public void setTravel_Dates_Last(String travel_Dates_Last) {
//        this.travel_Dates_Last = travel_Dates_Last;
//        this.tdl = i(travel_Dates_Last);
//    }
//
//    public void setCalc_Order(String calc_Order) {
//        this.calc_Order = calc_Order;
//        this.co = i(calc_Order);
//    }

}
