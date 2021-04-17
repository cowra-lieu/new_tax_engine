package com.btw.tax_engine.common.bean;

import java.io.Serializable;
import java.util.Objects;

public class TaxFeeItem implements Serializable {

    public String code;
    public String desc;
    public double amount;
    public String curr;

    public TaxFeeItem(String code, double amount, String curr, String desc) {
        this.code = code;
        this.amount = amount;
        this.curr = curr;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "TaxFeeItem{" +
                "code='" + code + '\'' +
                ", amount=" + amount +
                ", curr='" + curr + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }

    public TaxFeeItem() {}

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaxFeeItem that = (TaxFeeItem) o;
        return Double.compare(that.amount, amount) == 0 &&
                Objects.equals(code, that.code) &&
                Objects.equals(desc, that.desc) &&
                Objects.equals(curr, that.curr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, desc, amount, curr);
    }
}
