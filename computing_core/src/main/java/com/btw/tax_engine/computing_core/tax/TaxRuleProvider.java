package com.btw.tax_engine.computing_core.tax;

import com.btw.tax_engine.common.Const;
import com.btw.tax_engine.common.DAU;
import com.btw.tax_engine.common.bean.TaxRule;
import com.btw.tax_engine.common.bean.XFOption;
import com.btw.tax_engine.common.bean.XFRule;
import com.btw.tax_engine.quick_data_access.FTRepo;
import com.btw.tax_engine.quick_data_access.config.LettuceRedisConfig;
import com.opencsv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import static com.btw.tax_engine.common.SU.S;

@Service
public class TaxRuleProvider implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(TaxRuleProvider.class);

    private Environment env;
    private FTRepo ftRepo;

    private static final String X1_CT_DATA_FILE = Const.MSG_X1_CT + ".csv.gz";
    private static final String PFC_P_DATA_FILE = Const.MSG_PFC + "_p.csv.gz";
    private static final String PFC_C_DATA_FILE = Const.MSG_PFC + "_c.csv.gz";
    private final Map<String, LineParser> DATA_PARSER_MAP = new HashMap<>();

    private final Map<String, SortedSet<TaxRule>> x1MapA = new HashMap<>();
    private final Map<String, SortedSet<TaxRule>> x1MapB = new HashMap<>();

    private final Map<String, SortedSet<String>> ntMapA = new HashMap<>();
    private final Map<String, SortedSet<String>> ntMapB = new HashMap<>();

    private final Map<String, List<XFRule>> xfMapA = new LinkedHashMap<>();
    private final Map<String, List<XFRule>> xfMapB = new LinkedHashMap<>();

    private final List<XFOption> xfoListA = new ArrayList<>();
    private final List<XFOption> xfoListB = new ArrayList<>();

    private volatile boolean useXFA = true;

    public void switchTaxDataMap(boolean useRedisA) {
        if (useRedisA) {
            refresh4X1_CT(x1MapA, ntMapA);
            log.info("Tax Rule has been switched [from B to A] with {} entries and {} country-taxes entries.",
                    x1MapA.size(), ntMapA.size());
        } else {
            refresh4X1_CT(x1MapB, ntMapB);
            log.info("Tax Rule has been switched [from A to B] with {} entries and {} country-taxes entries.",
                    x1MapB.size(), ntMapB.size());
        }
        LettuceRedisConfig.tUseA = useRedisA;
    }

    public void switchXFDataMap() {
        if (useXFA) {
            refresh4PFC(xfMapB, xfoListB);
            useXFA = false;
            log.info("XF has been switched [from A to B] with {} XFs and {} XUOption.",
                    xfMapB.size(), xfoListB.size());
        } else {
            refresh4PFC(xfMapA, xfoListA);
            useXFA = true;
            log.info("XF has been switched [from B to A] with {} XFs and {} XUOption.",
                    xfMapA.size(), xfoListA.size());
        }
    }

    public void process_inc_data(List<TaxRule> delList, List<String> upList, boolean useRedisA) {
        if (LettuceRedisConfig.tUseA == useRedisA) {
            useRedisA = !useRedisA;
        }
        Map<String, SortedSet<TaxRule>> cx1Map = LettuceRedisConfig.tUseA? x1MapA : x1MapB;
        Map<String, SortedSet<TaxRule>> x1Map = useRedisA? x1MapA : x1MapB;
        Map<String, SortedSet<String>> ntMap = useRedisA? ntMapA : ntMapB;
        x1Map.clear();
        ntMap.clear();

        List<TaxRule> uTRList = upList.stream().map(s -> {
            TaxRule r = new TaxRule();
            fillTaxRule(r, Arrays.stream(s.split(",")).map(item -> item.substring(1, item.length()-1)).toArray(String[]::new));
            return r;}
        ).collect(Collectors.toList());

        cx1Map.forEach((k,v) -> {
            SortedSet<TaxRule> trSet = new TreeSet<>(v);
            for (TaxRule tr : delList) {
                if (k.equals(tr.key())) {
                    trSet.remove(tr);
                }
            }
            x1Map.put(k, trSet);
        });

        for (TaxRule tr: uTRList) {
            String k = tr.key();
            SortedSet<TaxRule> trSet;
            if (x1Map.containsKey(k)) {
                trSet = x1Map.get(k);
                trSet.remove(tr);
            } else {
                trSet = new TreeSet<>();
                x1Map.put(k, trSet);
            }
            trSet.add(tr);
        }

        x1Map.keySet().forEach(k -> {
            String n = k.substring(0, 2);
            if (!ntMap.containsKey(n)) {
                ntMap.put(n, new TreeSet<>());
            }
            ntMap.get(n).add(k.substring(2));
        });

        LettuceRedisConfig.tUseA = useRedisA;

    }

    public Map<String, SortedSet<TaxRule>> data() {
        return LettuceRedisConfig.tUseA ? x1MapA : x1MapB;
    }

    public Map<String, SortedSet<String>> ntMap() {
        return LettuceRedisConfig.tUseA ? ntMapA : ntMapB;
    }

    public Map<String, List<XFRule>> xf() {
        return useXFA ? xfMapA : xfMapB;
    }

    public List<XFOption> xfo() {
        return useXFA ? xfoListA : xfoListB;
    }

    @Override
    public void afterPropertiesSet() {
        DATA_PARSER_MAP.put(X1_CT_DATA_FILE, new LineParser4X1_CT());
        DATA_PARSER_MAP.put(PFC_P_DATA_FILE, new LineParser4PFC_P());
        DATA_PARSER_MAP.put(PFC_C_DATA_FILE, new LineParser4PFC_C());

        List<String> fileNameList = new ArrayList<>();
        String[] fileNames = {X1_CT_DATA_FILE, PFC_P_DATA_FILE, PFC_C_DATA_FILE};
        String localPath = env.getRequiredProperty("java.io.tmpdir");
        for (String fileName : fileNames) {
            if (Files.notExists(Paths.get(localPath, fileName))) {
                fileNameList.add(fileName);
            }
        }
        if (fileNameList.size() > 0) {
            ftRepo.download(fileNameList.toArray(new String[0]));
        }
        for (String fileName : fileNames) {
            Path p = Paths.get(localPath, fileName);
            log.info("read data from : {}", p);
            readDataFile(p, DATA_PARSER_MAP.get(fileName));
        }

        log.info("Tax Rule has been initialized with {} entries, {} country-taxes entries, {} XFs and {} XFOs",
                data().size(), ntMap().size(), xf().size(), xfo().size());
    }

    private void refresh4X1_CT(Map<String, SortedSet<TaxRule>> c1, Map<String, SortedSet<String>> c2) {
        c1.clear();
        c2.clear();

        ftRepo.download(X1_CT_DATA_FILE);

        String localPath = env.getRequiredProperty("java.io.tmpdir");
        readDataFile(Paths.get(localPath, X1_CT_DATA_FILE), new LineParser4X1_CT(c1, c2));
    }

    private void refresh4PFC(Map<String, List<XFRule>> c1, List<XFOption> c2) {
        c1.clear();
        c2.clear();

        ftRepo.download(new String[]{PFC_P_DATA_FILE, PFC_C_DATA_FILE});

        String localPath = env.getRequiredProperty("java.io.tmpdir");
        readDataFile(Paths.get(localPath, PFC_P_DATA_FILE), new LineParser4PFC_P(c1));
        readDataFile(Paths.get(localPath, PFC_C_DATA_FILE), new LineParser4PFC_C(c2));
    }

    private void readDataFile(Path dataFilePath, LineParser lineParser) {
        try(FileInputStream fis = new FileInputStream(dataFilePath.toFile());
            GZIPInputStream gis = new GZIPInputStream(fis);
            InputStreamReader isr = new InputStreamReader(gis);
            BufferedReader br = new BufferedReader(isr)) {

            CSVReader reader = new CSVReader(br);
            reader.skip(1);
            String[] rec;
            while ((rec = reader.readNext()) != null) {
                lineParser.parse(rec);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private interface LineParser {
        void parse(String[] rec);
    }

    private class LineParser4X1_CT implements LineParser {

        private final Map<String, SortedSet<TaxRule>> c1;
        private final Map<String, SortedSet<String>> c2;

        public LineParser4X1_CT() {
            if (LettuceRedisConfig.tUseA) {
                c1 = x1MapA;
                c2 = ntMapA;
            } else {
                c1 = x1MapB;
                c2 = ntMapB;
            }
        }

        public LineParser4X1_CT(Map<String, SortedSet<TaxRule>> c1, Map<String, SortedSet<String>> c2) {
            this.c1 = c1;
            this.c2 = c2;
        }

        @Override
        public void parse(String[] rec) {
            TaxRule taxRule = new TaxRule();
            fillTaxRule(taxRule, rec);

            // Fill x1MapA
            String key = taxRule.nation + taxRule.tax_Code + taxRule.tax_Type;
            if (!c1.containsKey(key)) {
                c1.put(key, new TreeSet<>());
            }
            c1.get(key).add(taxRule);

            // Fill ntMapA
            key = taxRule.nation;
            if (!c2.containsKey(key)) {
                c2.put(key, new TreeSet<>());
            }
            c2.get(key).add(taxRule.tax_Code + taxRule.tax_Type);
        }
    }

    private class LineParser4PFC_P implements LineParser {

        private final Map<String, List<XFRule>> c;

        public LineParser4PFC_P() {
            c = xfMapA;
        }

        public LineParser4PFC_P(Map<String, List<XFRule>> c) {
            this.c = c;
        }

        @Override
        public void parse(String[] rec) {
            XFRule xfRule = new XFRule();
            fillXFRule(xfRule, rec);
            if (!c.containsKey(xfRule.airport)) {
                c.put(xfRule.airport, new ArrayList<>());
            }
            c.get(xfRule.airport).add(xfRule);
        }
    }

    private class LineParser4PFC_C implements LineParser {

        private final List<XFOption> c;

        public LineParser4PFC_C() {
            c = xfoListA;
        }

        public LineParser4PFC_C(List<XFOption> c) {
            this.c = c;
        }

        @Override
        public void parse(String[] rec) {
            XFOption xfo = new XFOption();
            fillXFO(xfo, rec);
            c.add(xfo);
        }
    }

    private static void fillXFO(XFOption xfo, String[] rec) {
        int i = 0;
        xfo.colOption = S(rec[i++]);
        xfo.co = DAU.c(xfo.colOption);
        xfo.effDate = DAU.i(rec[i++]);
        xfo.disDate = DAU.i(rec[i]);

    }

    private static void fillXFRule(XFRule rule, String[] rec) {
        int i = 0;
        rule.airport = S(rec[i++]);
        rule.amount = DAU.d(rec[i++]);
        rule.currency =S(rec[i++]);
        rule.effdate = DAU.i(rec[i++]);
        rule.disdate = DAU.i(rec[i]);
    }

    private static void fillTaxRule(TaxRule tr, String[] rec) {
        int i = 0;
        tr.nation = S(rec[i++]); tr.tax_Code = S(rec[i++]); tr.tax_Type = S(rec[i++]);
        tr.tax_Point_Tag = S(rec[i++]); tr.seq_No = S(rec[i++]); tr.tax_Unit_Tg_Tag9 = S(rec[i++]);
        tr.calc_Order = S(rec[i++]); tr.sale_Dates_First = S(rec[i++]); tr.sale_Dates_Last = S(rec[i++]); tr.travel_Dates_Tag = S(rec[i++]);
        tr.travel_Dates_First = S(rec[i++]); tr.travel_Dates_Last = S(rec[i++]);
        tr.carrier_Appltable_No_190 = S(rec[i++]);
        tr.rtn_To_Orig = S(rec[i++]); tr.ptc_Table_169 = S(rec[i++]); tr.point_Of_Sale_Info = S(rec[i++]); tr.security_Table_No_183 = S(rec[i++]);

        tr.p_Of_Ticketing_Geo_Spec_Info = S(rec[i++]); tr.jrny_Geo_Spec_Indicator = S(rec[i++]);

        tr.jrny_Geo_Spec_Loc1_Info = S(rec[i++]); tr.jrny_Geo_Spec_Loc2_Info = S(rec[i++]);
        tr.jrny_Geo_Spec_Jo_In_Type = S(rec[i++]);tr.jrny_Geo_Spec_Jo_In_Info = S(rec[i++]);

        tr.jrny_Geo_Spec_Trvl_In_Loc_Type = S(rec[i++]); tr.jrny_Geo_Spec_Trvl_In_Loc_Info = S(rec[i++]);
        tr.tax_Point_Loc1_Nation_Domestic = S(rec[i++]); tr.tax_Point_Loc1_Transfer_Type = S(rec[i++]);
        tr.tax_Point_Loc1_Stopover_Tag = S(rec[i++]); tr.tax_Point_Loc1_Info = S(rec[i++]);

        tr.tax_Point_Loc2_Nation_Domestic = S(rec[i++]); tr.tax_Point_Loc2_Comparison = S(rec[i++]);
        tr.tax_Point_Loc2_Stopover_Tag = S(rec[i++]); tr.tax_Point_Loc2_Type = S(rec[i++]); tr.tax_Point_Loc2_Info = S(rec[i++]);

        tr.tax_Point_Loc3_Loc3Type = S(rec[i++]); tr.tax_Point_Loc3_Type = S(rec[i++]); tr.tax_Point_Loc3_Info = S(rec[i++]);

        tr.tax_Point_Qualif_Tags_Stop_Tag = S(rec[i++]); tr.tax_Point_Qualif_Tags_St_Unit = S(rec[i++]); tr.tax_Point_Qualif_Tags_Conn_Tag = S(rec[i++]);
        tr.service_And_Baggage_Table_168 = S(rec[i++]); tr.cxf_Or_Flt_Tbl_No_186 = S(rec[i++]); tr.sector_Detail_Table_No_167 = S(rec[i++]);

        tr.ticket_Value_Application = S(rec[i++]); tr.currency_Of_Sale = S(rec[i++]); tr.paid_By_Third_Party = S(rec[i++]);
        tr.tax_Calculation_Amount = S(rec[i++]); tr.tax_Calculation_Cur = S(rec[i++]); tr.tax_Calculation_Dec = S(rec[i++]); tr.tax_Calculation_Percent = S(rec[i++]);

        tr.tax_Calculation_Tax_App_To_Tag = S(rec[i++]); tr.tax_Calculation_Tax_Appl_Limit = S(rec[i++]);
        tr.tax_Round_Unit = S(rec[i++]); tr.tax_Round_Dir = S(rec[i++]);

        tr.tax_Processing_Appl_Tag = S(rec[i++]); tr.tax_Matching_Appl_Tag = S(rec[i++]);
        tr.use_Limit = S(rec[i++]); tr.ticket_Close = S(rec[i++]);

        tr.cxf_Or_Flt_Tbl_No_186_1 = S(rec[i]);

        tr.after_init();
    }

    @Autowired
    public void setEnv(Environment env) {
        this.env = env;
    }

    @Autowired
    public void setFtRepo(FTRepo ftRepo) {
        this.ftRepo = ftRepo;
    }


}
