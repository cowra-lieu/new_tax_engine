package com.btw.tax_engine.computing_core;

public interface XchangeService {

    double exchange(double oriAmount, String oriCurrency, String saleCurrency,
                    char round_unit, char round_direction, int bdate8);

    double round(double v, String curr);
}
