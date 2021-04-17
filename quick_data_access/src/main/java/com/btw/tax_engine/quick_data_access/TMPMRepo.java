package com.btw.tax_engine.quick_data_access;

public interface TMPMRepo {

    double getTPM(String fromCode, String toCode);

    double getMPM(String fromCode, String toCode);

    double getGCM(String fromCode, String toCode);
}
