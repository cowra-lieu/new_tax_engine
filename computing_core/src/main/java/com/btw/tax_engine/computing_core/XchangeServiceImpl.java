package com.btw.tax_engine.computing_core;

import com.btw.tax_engine.common.DAU;
import com.btw.tax_engine.quick_data_access.CurrRepo;
import com.btw.tax_engine.quick_data_access.IcerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.btw.tax_engine.common.Const.ICER_KEY_LIST;
import static com.btw.tax_engine.common.Const.NC;
import static com.btw.tax_engine.common.DAU.getImmediateIndex;
import static com.btw.tax_engine.common.SU.*;

@Service
public class XchangeServiceImpl implements XchangeService {

    private CurrRepo currRepo;
    private IcerRepo icerRepo;

    private final static Map<Character, Double> AccuracyMap = new HashMap<>();
    static {
        AccuracyMap.put('M', 0.000001);
        AccuracyMap.put('U', 0.00001);
        AccuracyMap.put('E', 0.0001);
        AccuracyMap.put('S', 0.001);
        AccuracyMap.put('H', 0.01);
        AccuracyMap.put('T', 0.1);
        AccuracyMap.put('0', 1.0);
        AccuracyMap.put('1', 10.0);
        AccuracyMap.put('2', 100.0);
        AccuracyMap.put('3', 1000.0);
        AccuracyMap.put('4', 10000.0);
    }

    @Autowired
    public void setCurrRepo(CurrRepo currRepo) {
        this.currRepo = currRepo;
    }

    @Autowired
    public void setIcerRepo(IcerRepo icerRepo) {
        this.icerRepo = icerRepo;
    }

    @Override
    public double exchange(double oriAmount, String oriCurrency, String saleCurrency,
                           char round_unit, char round_direction, int bdate8) {

        String currInfo = currRepo.getRawValue(saleCurrency);
        double t_accuracy = Double.parseDouble(nthsec(currInfo, 0));
        int t_decimal_len = Integer.parseInt(nthsec(currInfo, 1));
        char t_round_direction = DAU.c(rnthsec(currInfo, 0));

        String oriAmountStr = String.valueOf(oriAmount);
        int decimalPointPos = oriAmountStr.indexOf('.');
        int decimalPartLen = decimalPointPos < 0 ? 0 : oriAmountStr.length() - decimalPointPos - 1;
        boolean sameCurr = saleCurrency.equals(oriCurrency);
        if ( sameCurr && decimalPartLen <= t_decimal_len) {
            return oriAmount;
        }
        double exchangeAmount;
        if (sameCurr || blk(oriCurrency)) {
            exchangeAmount = oriAmount;
        } else {
            exchangeAmount = icerRepo.getRate(getIcerKey(bdate8), oriCurrency, saleCurrency) * oriAmount;
        }
        boolean use_standard_round = false;
        if (NC == round_unit || NC == round_direction) {
            use_standard_round = true;
        } else {
            if ('N' == round_unit) {
                use_standard_round = true;
            } else {
                t_accuracy = AccuracyMap.getOrDefault(round_unit, 1.0);
                if ('S' == round_direction) {
                    use_standard_round = true;
                } else if ('U' == round_direction) {
                    exchangeAmount = Math.ceil(exchangeAmount * t_accuracy) / t_accuracy;
                } else if ('D' == round_direction) {
                    exchangeAmount = Math.floor(exchangeAmount * t_accuracy) / t_accuracy;
                } else if ('N' == round_direction) {
                    exchangeAmount = Math.round(exchangeAmount * t_accuracy) / t_accuracy;
                }
            }
        }
        if (use_standard_round) {
            return accuracy_adjust(exchangeAmount, t_accuracy, t_decimal_len, t_round_direction);
        }
        return exchangeAmount;
    }

    @Override
    public double round(double v, String curr) {
        String currInfo = currRepo.getRawValue(curr);
        int t_decimal_len = Integer.parseInt(nthsec(currInfo, 1));
        return DAU.round(v, t_decimal_len);
    }

    private String getIcerKey(int bdate) {
        int[] icerFlags = icerRepo.getIcerFlags();
        int index = getImmediateIndex(bdate, icerFlags);
        return (String)ICER_KEY_LIST.get(index);
    }

    private double accuracy_adjust(double exchangeAmount, double t_accuracy, int t_decimal_len,
                                   char t_round_direction) {
        double result;
        double truncated = DAU.cut(exchangeAmount, 1 + t_decimal_len);
        if ('N' == t_round_direction) {
            result = Math.round(truncated / t_accuracy) * t_accuracy;
        } else if ('D' == t_round_direction) {
            result = Math.floor(truncated / t_accuracy) * t_accuracy;
        } else {
            result = Math.ceil(truncated / t_accuracy) * t_accuracy;
        }
        return DAU.cut(result, t_decimal_len);
    }

}
