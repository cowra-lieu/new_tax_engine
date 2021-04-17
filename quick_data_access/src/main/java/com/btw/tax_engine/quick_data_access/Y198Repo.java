package com.btw.tax_engine.quick_data_access;

import java.util.Date;

public interface Y198Repo {

    boolean checkRBDsFrom198(String tableNo, String carrier, char clazz, int bookingDate);

}
