package com.btw.tax_engine.common.bean;

import java.util.StringJoiner;

public class YqFeeItem {

    public String name;
    public String sectors;
    public double amount;
    public boolean conn_exemption;
    public char fee_application = '0';

    public YqFeeItem(String name, String sectors, double amount) {
        this.name = name;
        this.sectors = sectors;
        this.amount = amount;
    }

    public YqFeeItem(String name, String sectors, double amount, boolean conn_exemption) {
        this.name = name;
        this.sectors = sectors;
        this.amount = amount;
        this.conn_exemption = conn_exemption;
    }

    public YqFeeItem(String name, String sectors, double amount,
                     boolean conn_exemption, byte fee_application) {
        this.name = name;
        this.sectors = sectors;
        this.amount = amount;
        this.conn_exemption = conn_exemption;
        this.fee_application = (char)fee_application;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", YqFeeItem.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("sectors='" + sectors + "'")
                .add("amount=" + amount)
                .add("conn_exemption=" + conn_exemption)
                .add("fee_application=" + fee_application)
                .toString();
    }
}
