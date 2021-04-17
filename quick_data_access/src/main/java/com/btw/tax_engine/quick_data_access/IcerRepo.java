package com.btw.tax_engine.quick_data_access;

import java.util.Map;

public interface IcerRepo {

    double getRate(String key, String from, String to);

    int[] getIcerFlags();

}
