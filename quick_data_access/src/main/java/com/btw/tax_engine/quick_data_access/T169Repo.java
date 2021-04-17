package com.btw.tax_engine.quick_data_access;

public interface T169Repo {

    String getRawValue(String key);

    String c2ss(String city);
    String c2n(String city);
    String n2a(String country);
    String ss2a(String substate);
    String s2a(String state);
    String z2a(String state);
}
