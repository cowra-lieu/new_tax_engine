package com.btw.tax_engine.computing_core;

import com.btw.tax_engine.quick_data_access.AirportRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.btw.tax_engine.common.Const.*;
import static com.btw.tax_engine.common.SU.*;

@Service
public class AirportInfoServiceImpl implements AirportInfoService {

    private AirportRepo apcRepo;

    @Autowired
    public void setApcRepo(AirportRepo apcRepo) {
        this.apcRepo = apcRepo;
    }

    public String getCity(String apc) {
        String value = apcRepo.getRawValue(apc);
        return rnthsec(value, 2);
    }

    public String getNation(String apc) {
        String value = apcRepo.getRawValue(apc);
        return nthsec(value, AS_NATION);
    }

    public String getZone(String apc) {
        String value = apcRepo.getRawValue(apc);
        return nthsec(value, AS_ZONE);
    }

    public String getArea(String apc) {
        String value = apcRepo.getRawValue(apc);
        return nthsec(value, AS_AREA);
    }

    @Override
    public String getValue(String apc) {
        return apcRepo.getRawValue(apc);
    }
}
