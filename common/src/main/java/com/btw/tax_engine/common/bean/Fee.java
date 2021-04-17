package com.btw.tax_engine.common.bean;

import java.util.Objects;
import java.util.StringJoiner;

public class Fee {

    public String fare;
    public String ob;
    public String baggage;
    public String flight;
    public String ticket;
    public String merchandise;
    public String oc;
    public String od;

    public Fee() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fee fee = (Fee) o;
        return Objects.equals(fare, fee.fare) &&
                Objects.equals(ob, fee.ob) &&
                Objects.equals(baggage, fee.baggage) &&
                Objects.equals(flight, fee.flight) &&
                Objects.equals(ticket, fee.ticket) &&
                Objects.equals(merchandise, fee.merchandise) &&
                Objects.equals(oc, fee.oc) &&
                Objects.equals(od, fee.od);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fare, ob, baggage, flight, ticket, merchandise, oc, od);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Fee.class.getSimpleName() + "[", "]")
                .add("fare='" + fare + "'")
                .add("ob='" + ob + "'")
                .add("baggage='" + baggage + "'")
                .add("flight='" + flight + "'")
                .add("ticket='" + ticket + "'")
                .add("merchandise='" + merchandise + "'")
                .add("oc='" + oc + "'")
                .add("od='" + od + "'")
                .toString();
    }
}
