package com.btw.tax_engine.common.bean;


import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

public class SectorTaxFeeItem extends TaxFeeItem  implements Comparable<SectorTaxFeeItem> {

    public String secNo;

    @JsonIgnore
    public TaxRule k;

    public SectorTaxFeeItem(TaxRule k, int secNo, String desc) {
        this.k = k;
        this.secNo = String.valueOf(secNo+1);
        this.desc = desc;
    }

    public SectorTaxFeeItem(String code, double amount, String curr, int seqNo) {
        this.code = code;
        this.amount = amount;
        this.curr = curr;
        this.secNo = String.valueOf(seqNo+1);
    }

    public SectorTaxFeeItem(String code, double amount, String curr, String sectors) {
        this.code = code;
        this.amount = amount;
        this.curr = curr;
        this.secNo = sectors;
    }

    @Override
    public int compareTo(SectorTaxFeeItem o) {
        return this.k.co - o.k.co;
    }

    @Override
    public String toString() {
        return "SectorTaxFeeItem{" +
                "secNo='" + secNo + '\'' +
                ", code='" + code + '\'' +
                ", amount=" + amount +
                ", curr='" + curr + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }

    public TaxRule getK() {
        return k;
    }

    public void setK(TaxRule k) {
        this.k = k;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SectorTaxFeeItem that = (SectorTaxFeeItem) o;
        return Objects.equals(secNo, that.secNo) &&
                Objects.equals(k, that.k);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), secNo, k);
    }


}
