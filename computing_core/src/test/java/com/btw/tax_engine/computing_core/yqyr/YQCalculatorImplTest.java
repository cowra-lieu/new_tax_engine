package com.btw.tax_engine.computing_core.yqyr;

import com.btw.tax_engine.common.bean.YqFeeItem;
import com.btw.tax_engine.common.bean.Itinerary;
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
public class YQCalculatorImplTest {

    private static final Logger log = LoggerFactory.getLogger(YQCalculatorImplTest.class);

    private YQCalculator calculator;

    @Autowired
    public void setCalculator(YQCalculator calculator) {
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

        List<String[]> expYqYrFeeList = new ArrayList<>();
        TestUtil.readYqYrFees(expYqYrFeeList, "/data/yqyrs.txt");
        assertTrue(expYqYrFeeList.size() > 0);

        List<YqFeeItem> yqFeeList = new ArrayList<>();
        List<YqFeeItem> yrFeeList = new ArrayList<>();

        int i = 0;
        for (Itinerary iti : itis)
        {
            System.out.println("---"+(++i));
            calculator.execute(iti, yqFeeList, "YQ");
            calculator.execute(iti, yrFeeList, "YR");

            String yqsRep = yqFeeList.toString();
            String yrsRep = yrFeeList.toString();

            String[] expYqYr = expYqYrFeeList.get(i-1);
            assertEquals(yqsRep, expYqYr[0]);
            assertEquals(yrsRep, expYqYr[1]);

            System.out.println(yqsRep);
            System.out.println(yrsRep);

            yqFeeList.clear();
            yrFeeList.clear();
        }
    }

    static class QueryTask implements Runnable {
        private final YQCalculator calculator;
        private final Itinerary itinerary;
        private final List<YqFeeItem> feeList;
        private final int cycle;

        QueryTask(YQCalculator calculator, Itinerary itinerary, int cycle) {
            this.calculator = calculator;
            this.itinerary = itinerary;
            this.feeList = new ArrayList<>();
            this.cycle = cycle;
        }

        @Override
        public void run() {
            for (int i=0; i<this.cycle; i++) {
                calculator.execute(itinerary, feeList, "YQ");
                calculator.execute(itinerary, feeList, "YR");
                feeList.clear();
            }
        }
    }

}