package com.btw.tax_engine.computing_core.tax;

import com.btw.tax_engine.common.bean.Itinerary;
import com.btw.tax_engine.common.bean.SectorTaxFeeItem;
import com.btw.tax_engine.common.bean.TaxFeeItem;
import com.btw.tax_engine.computing_core.TestConfig;
import com.btw.tax_engine.computing_core.TestUtil;
import com.btw.tax_engine.quick_data_access.CabinRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("dev")
public class TaxCalculatorImplTest {

    private static final Logger log = LoggerFactory.getLogger(TaxCalculatorImplTest.class);

    private TaxCalculator calculator;

    @Autowired
    public void setCalculator(TaxCalculator calculator) {
        this.calculator = calculator;
    }

    private CabinRepo cabinRepo;

    @Autowired
    public void setCabinDao(CabinRepo cabinRepo) {
        this.cabinRepo = cabinRepo;
    }

    @Test
    public void checkTypicalCases()
    {
        List<Itinerary> itis = new ArrayList<>();
        TestUtil.readManyFromTxt(itis, "/data/routes.txt", cabinRepo);
        assertTrue(itis.size() > 0);

        List<String[]> taxFeeList = new ArrayList<>();
        TestUtil.readTaxFees(taxFeeList, "/data/taxes.txt");
        assertTrue(taxFeeList.size() > 0);

        List<TaxFeeItem> feeList = new ArrayList<>();
        List<SectorTaxFeeItem> sectorFeeList = new ArrayList<>();

        int i = 0;
        for (Itinerary iti : itis)
        {
            System.out.println("---"+(++i));
            calculator.execute(iti, feeList, sectorFeeList);

            String feeRep = feeList.toString();
            String secFeeRep = sectorFeeList.toString();

            String[] expTaxSecTaxes = taxFeeList.get(i-1);
            assertEquals(feeRep, expTaxSecTaxes[0]);
            assertEquals(secFeeRep, expTaxSecTaxes[1]);

            System.out.println(feeRep);
            System.out.println(secFeeRep);

            feeList.clear();
            sectorFeeList.clear();
        }
    }

    static class QueryTask implements Runnable {
        private final TaxCalculator calculator;
        private final Itinerary itinerary;
        List<TaxFeeItem> feeList;
        List<SectorTaxFeeItem> sectorFeeList;
        private final int cycle;

        QueryTask(TaxCalculator calculator, Itinerary itinerary, int cycle) {
            this.calculator = calculator;
            this.itinerary = itinerary;
            this.feeList = new ArrayList<>();
            this.sectorFeeList = new ArrayList<>();
            this.cycle = cycle;
        }

        @Override
        public void run() {
            for (int i=0; i<this.cycle; i++) {
                calculator.execute(itinerary, feeList, sectorFeeList);
            }
        }
    }

}
