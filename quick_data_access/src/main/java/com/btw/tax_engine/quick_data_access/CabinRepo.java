package com.btw.tax_engine.quick_data_access;

public interface CabinRepo {

    char getCabin(String carrierCode, char primeCode, int date);

}
