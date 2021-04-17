package com.btw.tax_engine.common.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;
import java.util.StringJoiner;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    public String bcc;
    public String bcn;
    public String estr;

    public User() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(bcc, user.bcc) &&
                Objects.equals(bcn, user.bcn) &&
                Objects.equals(estr, user.estr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bcc, bcn, estr);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", User.class.getSimpleName() + "[", "]")
                .add("bcc='" + bcc + "'")
                .add("bcn='" + bcn + "'")
                .add("estr='" + estr + "'")
                .toString();
    }
}
