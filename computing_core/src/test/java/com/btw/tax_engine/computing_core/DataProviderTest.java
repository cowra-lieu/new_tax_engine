package com.btw.tax_engine.computing_core;

import com.btw.tax_engine.common.DEU;
import com.btw.tax_engine.common.bean.Itinerary;
import com.btw.tax_engine.common.bean.Passenger;
import com.btw.tax_engine.common.bean.Sector;
import com.btw.tax_engine.quick_data_access.CabinRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("dev")
public class DataProviderTest {

    private static final Logger log = LoggerFactory.getLogger(DataProviderTest.class);

    private CabinRepo cabinDao;

    @Autowired
    public void setCabinDao(CabinRepo cabinDao) {
        this.cabinDao = cabinDao;
    }

    private Itinerary readOneFromTxt(File file) {
        Itinerary itinerary = new Itinerary();
        if (file.exists()) {
            try {
                FileReader fileReader = new FileReader(file);
                BufferedReader br = new BufferedReader(fileReader);
                String lineContent;
                int i = 0;
                String[] sectorArray = null;
                String[] departureDateArray = null;
                String[] arrivalDateArray = null;
                String[] classArray = null;
                String[] marketingCarrierArray = null;
                String passengerType = "ADT";
                String saleCurrency = "CNY";
                while((lineContent = br.readLine())!=null){
                    if (i == 0) {
                        sectorArray = lineContent.trim().split(";");
                    } else if (i == 1) {
                        departureDateArray = lineContent.trim().split(";");
                    } else if (i == 2) {
                        arrivalDateArray = lineContent.trim().split(";");
                    } else if (i == 3) {
                        classArray = lineContent.trim().split(";");
                    } else if (i == 4) {
                        marketingCarrierArray = lineContent.trim().split(";");
                    } else if (i == 5) {
                        passengerType = lineContent.trim();
                    } else if (i == 6) {
                        saleCurrency = lineContent.trim();
                    } else {
                        break;
                    }
                    i++;
                }
                if (sectorArray != null) {
                    fillItinerary(itinerary, sectorArray, departureDateArray, arrivalDateArray,
                            classArray, marketingCarrierArray, passengerType, saleCurrency);
                }
                br.close();
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return itinerary;
    }

    private void fillItinerary(Itinerary itinerary,
                                      String[] sectorArray,
                                      String[] departureDateArray,
                                      String[] arrivalDateArray,
                                      String[] classArray,
                                      String[] marketingCarrierArray,
                                      String passengerType,
                                      String saleCurrency) {

        String[] airportCodeArray;
        itinerary.sectors = new Sector[sectorArray.length];
        for (int i=0; i<sectorArray.length; i++) {
            Sector s = new Sector(i);

            s.begin = (DEU.parse_y4M2d2_H2_m2(departureDateArray[i]));
            s.end = (DEU.parse_y4M2d2_H2_m2(arrivalDateArray[i]));
            airportCodeArray = sectorArray[i].split("-");
            s.from = (airportCodeArray[0]);
            s.to = (airportCodeArray[1]);
            s.clazz = (classArray[i].charAt(0));
            s.mcxr = (marketingCarrierArray[i]);
            s.cabin = cabinDao.getCabin(s.mcxr, s.clazz, DEU.i8(new Date()));

            itinerary.sectors[i] = s;
        }

        Passenger p = new Passenger();
        itinerary.passenger = p;
        p.type = passengerType;

        itinerary.scurr = saleCurrency;
    }

    @Test
    public void testSplit() {
        String s = "3::320::SG:::SIN::";
        String[] ss = s.split(":", 10);
        assertEquals(10, ss.length);

        char c = 'D';
        int i = 68;
        assertEquals(c, i);
    }

    @Test
    public void testMath() {
        double d = 100.12345;
        System.out.println(Math.floor(d * 10000) / 10000);
        System.out.println(Math.ceil(d * 10000) / 10000);
        System.out.println(Math.round(d * 10000) / 10000.0);
        System.out.println("--------------------------------------------");

        System.out.println(Math.floor(d / 0.0001) * 0.0001);
        System.out.println(Math.ceil(d / 0.0001) * 0.0001);
        System.out.println(Math.round(d / 0.0001) * 0.0001);
        System.out.println("--------------------------------------------");

        System.out.println(Math.floor(d * 0.1) / 0.1);
        System.out.println(Math.ceil(d * 0.1) / 0.1);
        System.out.println(Math.round(d * 0.1) / 0.1);
        System.out.println("--------------------------------------------");
        System.out.println(Math.floor(d * 0.01) / 0.01);
        System.out.println(Math.ceil(d * 0.01) / 0.01);
        System.out.println(Math.round(d * 0.01) / 0.01);
        System.out.println("--------------------------------------------");
        System.out.println(Math.floor(d * 0.001) / 0.001);
        System.out.println(Math.ceil(d * 0.001) / 0.001);
        System.out.println(Math.round(d * 0.001) / 0.001);

        String s = ".1";
        System.out.println(Double.parseDouble(s));
        s = ".01";
        System.out.println(Double.parseDouble(s));
        s = ".001";
        System.out.println(Double.parseDouble(s));
        s = "1";
        System.out.println(Double.parseDouble(s));

        System.out.println("===============================================");

        double a = 12.345;

        System.out.println(Math.floor(a * Math.pow(10, -1)) / Math.pow(10, 1));
        System.out.println(Math.floor(a * Math.pow(10, 0)) / Math.pow(10, 0));
        System.out.println(Math.floor(a * Math.pow(10, 1)) / Math.pow(10, 1));
        System.out.println(Math.floor(a * Math.pow(10, 2)) / Math.pow(10, 2));
        System.out.println(Math.floor(a * Math.pow(10, 3)) / Math.pow(10, 3));
    }

    @Test
    public void doBin() {
        int i = 1;
        i <<= 1;
        i |= 1;
        i <<= 1;
        i |= 1;
        assertEquals(i, 0b111);
    }

    @Test
    public void doFilter() {
        Map<String, List<Integer>> s_ilist = new LinkedHashMap<>();
        List<Integer> aList = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> bList = Arrays.asList(6, 7, 8, 9, 10);
        List<Integer> cList = Arrays.asList(11, 12, 13, 14, 15);
        List<Integer> dList = Arrays.asList(16, 17, 18, 19, 20);
        List<Integer> eList = Arrays.asList(21, 22, 23, 24, 25);
        List<Integer> fList = Arrays.asList(26, 27, 28, 29, 30);
        List<Integer> gList = Arrays.asList(32, 33, 34, 35, 36);

        s_ilist.put("a", aList);
        s_ilist.put("b", bList);
        s_ilist.put("c", cList);
        s_ilist.put("d", dList);
        s_ilist.put("e", eList);
        s_ilist.put("f", fList);
        s_ilist.put("g", gList);

        Predicate<Integer> p1 = i -> {
            boolean result = (i == 2);
            if (!result && i > 2) {
                int uplimit = i >> 1;
                result = true;
                for (int n=2; n<uplimit; n++) {
                    if (i % n == 0) {
                        result = false;
                        break;
                    }
                }
            }
            return result;
        };

        log.info("----------------------------------------------------------------------");

        Map<String, Integer> str_int = Stream.of("a", "c", "d", "f", "g")
                .collect(LinkedHashMap::new,
                        (m, s) -> m.put(s, s_ilist.get(s).stream().filter(p1).findFirst().orElse(null)),
                        LinkedHashMap::putAll);

        str_int.forEach((k, v) -> log.debug("k:{}, v:{}", k, v));

        log.info("----------------------------------------------------------------------");

        str_int = str_int.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        str_int.forEach((k, v) -> log.debug("k:{}, v:{}", k, v));
    }

    @Test
    public void doFilter2() {
        Map<Integer, String> HOSTING = new HashMap<>();
        HOSTING.put(1, "linode.com");
        HOSTING.put(2, "heroku.com");
        HOSTING.put(3, "digitalocean.com");
        HOSTING.put(4, "aws.amazon.com");

        String str = HOSTING.entrySet().stream()
                .filter(e -> 0 == (1 & e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.joining());
        log.info("collect map to string: {}", str);
    }

    @Test
    public void doCounting() {

        Map<String, Long> result = Stream.of("apple", "banana", "apple", "banana", "apple", "papaya")
                .collect(Collectors.groupingBy(String::toString,
                        Collectors.counting()));
        result.forEach((k,v) -> log.debug("k:{}, v:{}", k, v));

        log.info("-------------------------------------------------");

        result = Stream.of("apple", "banana", "apple", "banana", "apple", "papaya")
                .collect(Collectors.groupingBy(Function.identity(),
                        Collectors.counting()));
        result.forEach((k,v) -> log.debug("k:{}, v:{}", k, v));

        log.info("-------------------------------------------------");

        result = Stream.of("apple", "banana", "apple", "banana", "apple", "papaya")
                .collect(Collectors.groupingBy(Function.identity(),
                        LinkedHashMap::new, Collectors.counting()));
        result.forEach((k,v) -> log.debug("k:{}, v:{}", k, v));

        log.info("-------------------------------------------------");

        List<Item> items = Arrays.asList(
                new Item("apple", 10, 9.99),
                new Item("banana", 20, 19.99),
                new Item("orang", 10, 29.99),
                new Item("watermelon", 10, 29.99),
                new Item("papaya", 20, 9.99),
                new Item("apple", 10, 9.99),
                new Item("banana", 10, 19.99),
                new Item("apple", 20, 9.99));

        Map<String, Integer> si = items.stream().collect(
            Collectors.groupingBy(Item::getName, LinkedHashMap::new,
                    Collectors.summingInt(Item::getQuantity))
        );
        si.forEach((k, v) -> log.debug("k: {}, v: {}", k, v));

        log.info("-------------------------------------------------");

        Map<String, Double> sd = items.stream().collect(
                Collectors.groupingBy(Item::getName, LinkedHashMap::new,
                        Collectors.summingDouble(item -> item.getQuantity() * item.getPrice()))
        );
        sd.forEach((k, v) -> log.debug("k: {}, v: {}", k, Math.round(v*100)/100.0));
    }

    private static class Item {
        public String name;
        public int quantity;
        public double price;

        public Item(String name, int quantity, double price) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }

        public String getName() {
            return name;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getPrice() {
            return price;
        }

    }

}
