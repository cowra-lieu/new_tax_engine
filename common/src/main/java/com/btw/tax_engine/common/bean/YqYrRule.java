package com.btw.tax_engine.common.bean;

import com.btw.tax_engine.common.DAU;

import java.util.Objects;

public class YqYrRule implements Comparable<YqYrRule> {

    public String cxr_code;
    public String sub_code;
    public int seq_no;
    public long travel_eff;
    public long travel_disc;
    public long ticket_first;
    public long ticket_last;

    public String rtn_to_orig;
    public String psgr;

    public String point_of_sale_geographic_l;

    public String point_of_sale_code;
    public String point_of_sale_code_value;

    public byte jrny_geo_spec_indicator;
    public byte jrny_geo_spec_loc1;
    public String jrny_geo_spec_loc1_value;
    public byte jrny_geo_spec_loc2;
    public String jrny_geo_spec_loc2_value;
    public byte jrny_geo_spec_via_loc;
    public String jrny_geo_spec_via_loc_value;
    public byte jrny_geo_spec_trvl_w_w_l;
    public String jrny_geo_spec_trvl_w_w_l_v;

    public byte sector_prt_geo_spec;
    public byte sector_prt_from_to;
    public byte sector_prt_loc1;
    public String sector_prt_loc1_value;
    public byte sector_prt_loc2;
    public String sector_prt_loc2_value;
    public byte sector_prt_via_geo;
    public String sector_prt_via_geo_value;
    public byte sector_prt_via_stp_cnx;
    public byte sector_prt_via_cnx_exempt;
    public byte sector_prt_intl_dom;

    public String rbd_tbl_no_198;
    public char cabin;
    public byte rbd;
    public byte rbd2;
    public byte rbd3;

    public String eqp;

    public int sector_prt_via_exc_stop_time;
    public byte sector_prt_via_exc_stop_t_u;

    public int service_fee_amount;
    public String service_fee_cur;
    public byte service_fee_dec;
    public byte service_fee_tax;
    public byte service_fee_application;

    public int lineno;
    public byte use_limit;
    public long travel_d;

    public boolean rto;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YqYrRule yqYrRule = (YqYrRule) o;
        return cxr_code.equals(yqYrRule.cxr_code) &&
                sub_code.equals(yqYrRule.sub_code) &&
                seq_no == yqYrRule.seq_no;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cxr_code, sub_code, seq_no);
    }

    public YqYrRule() {}

    public YqYrRule(String[] fs) {
        int i = 0;
        this.cxr_code = fs[i++];
        this.sub_code = fs[i++];
        this.seq_no = DAU.getI(fs[i]);
    }

    @Override
    public int compareTo(YqYrRule o) {
        int r = this.sub_code.compareTo(o.sub_code);
        if (r == 0) {
            r = this.cxr_code.compareTo(o.cxr_code);
            if (r == 0) {
                r = this.seq_no - o.seq_no;
            }
        }
        return r;
    }
}
