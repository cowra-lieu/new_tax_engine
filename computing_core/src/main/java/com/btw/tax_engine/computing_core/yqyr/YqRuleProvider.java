package com.btw.tax_engine.computing_core.yqyr;

import com.btw.tax_engine.common.Const;
import com.btw.tax_engine.common.DAU;
import com.btw.tax_engine.common.DEU;
import com.btw.tax_engine.common.SU;
import com.btw.tax_engine.common.bean.YqYrRule;
import com.btw.tax_engine.common.exception.DataRefreshException;
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
public class YqRuleProvider implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(YqRuleProvider.class);

    private final Map<String, Byte> USE_LIMIT_MAP = new HashMap<>();

    private Environment env;

    private final Map<String, List<YqYrRule>> yqListA = new LinkedHashMap<>();
    private final Map<String, List<YqYrRule>> yqListB = new LinkedHashMap<>();
    private final Map<String, List<YqYrRule>> yrListA = new LinkedHashMap<>();
    private final Map<String, List<YqYrRule>> yrListB = new LinkedHashMap<>();

    private FTRepo ftRepo;

    public void switchYQDataMap(boolean useRedisA) {
        try {
            if (useRedisA) {
                switchList(yqListA, yrListA, true);
            } else {
                switchList(yqListB, yrListB, false);
            }
        } catch (Exception e) {
            throw new DataRefreshException(e);
        }
    }

    private void switchList(Map<String, List<YqYrRule>> yqList, Map<String, List<YqYrRule>> yrList, boolean useRedisA) {
        yqList.clear();
        yrList.clear();

        ftRepo.download(DATA_FILE_NAME);
        String localPath = env.getRequiredProperty("java.io.tmpdir");
        Path dataFilePath = Paths.get(localPath, DATA_FILE_NAME);
        log.info("read data from: {}", dataFilePath);

        readDataFile(dataFilePath, yqList, yrList);
        LettuceRedisConfig.yUseA = useRedisA;

        log.info("YQ/YR Rule have been switched {}, included:",
                useRedisA ? "[from B to A]" : "[from A to B]");

        log_data_details(yqList, yrList);
    }

    private void log_data_details(Map<String, List<YqYrRule>> yqList, Map<String, List<YqYrRule>> yrList) {
        Iterator<Map.Entry<String, List<YqYrRule>>> iter = yqList.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, List<YqYrRule>> me = iter.next();
            log.info("YQ - {}: {}", me.getKey(), me.getValue().size());
        }
        iter = yrList.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, List<YqYrRule>> me = iter.next();
            log.info("YR - {}: {}", me.getKey(), me.getValue().size());
        }
    }

    public Map<String, List<YqYrRule>> data(String feeName) {
        if ("YQ".equals(feeName)) {
            return LettuceRedisConfig.yUseA ? yqListA : yqListB;
        } else {
            return LettuceRedisConfig.yUseA ? yrListA : yrListB;
        }
    }

    public void process_inc_data(Map<String,List<YqYrRule>> delListMap, List<String> upList, boolean useRedisA) {
        if (LettuceRedisConfig.yUseA == useRedisA) {
            useRedisA = !useRedisA;
        }

        Map<String, List<YqYrRule>> cyqListMap = LettuceRedisConfig.yUseA ? yqListA : yqListB;
        Map<String, List<YqYrRule>> cyrListMap = LettuceRedisConfig.yUseA ? yrListA : yrListB;

        Map<String,List<YqYrRule>> uYqYrListMap = upList.stream().map(s -> {
                        YqYrRule r = new YqYrRule();
                        fillRule(r, Arrays.stream(s.split(","))
                                        .map(item -> item.substring(1, item.length()-1))
                                        .toArray(String[]::new));
                        return r;}
                    ).collect(Collectors.groupingBy(y -> y.cxr_code));

        SortedSet<YqYrRule> newRuleSet = new TreeSet<>();

        Map.Entry<String, List<YqYrRule>> me;
        String cxr;
        List<YqYrRule> cyList;
        List<YqYrRule> tmpList;
        List<YqYrRule> nList;

        Map<String, List<YqYrRule>> yqListMap = useRedisA? yqListA : yqListB;
        Map<String, List<YqYrRule>> yrListMap = useRedisA? yrListA : yrListB;

        yqListMap.clear();
        yrListMap.clear();

        for (Map.Entry<String, List<YqYrRule>> stringListEntry : cyqListMap.entrySet()) {
            me = stringListEntry;
            cxr = me.getKey();
            cyList = me.getValue();

            nList = new ArrayList<>(cyList.size());
            yqListMap.put(cxr, nList);

            if (delListMap.containsKey(cxr)) {
                tmpList = delListMap.get(cxr);
                for (YqYrRule yq : cyList) {
                    if (!tmpList.contains(yq)) {
                        newRuleSet.add(yq);
                    }
                }
            } else {
                newRuleSet.addAll(cyList);
            }

            if (uYqYrListMap.containsKey(cxr)) {
                tmpList = uYqYrListMap.get(cxr);
                for (YqYrRule yq : tmpList) {
                    if (yq.sub_code.equals("F")) {
                        newRuleSet.remove(yq);
                        newRuleSet.add(yq);
                    }
                }
            }

            nList.addAll(newRuleSet);
            newRuleSet.clear();
        }

        for (Map.Entry<String, List<YqYrRule>> stringListEntry : cyrListMap.entrySet()) {
            me = stringListEntry;
            cxr = me.getKey();
            cyList = me.getValue();

            nList = new ArrayList<>(cyList.size());
            yrListMap.put(cxr, nList);

            if (delListMap.containsKey(cxr)) {
                tmpList = delListMap.get(cxr);
                for (YqYrRule yq : cyList) {
                    if (!tmpList.contains(yq)) {
                        newRuleSet.add(yq);
                    }
                }
            } else {
                newRuleSet.addAll(cyList);
            }

            if (uYqYrListMap.containsKey(cxr)) {
                tmpList = uYqYrListMap.get(cxr);
                for (YqYrRule yr : tmpList) {
                    if (yr.sub_code.equals("I")) {
                        newRuleSet.remove(yr);
                        newRuleSet.add(yr);
                    }
                }
            }

            nList.addAll(newRuleSet);
            newRuleSet.clear();
        }

        LettuceRedisConfig.yUseA = useRedisA;
    }

    private static final String DATA_FILE_NAME = Const.MSG_S1_CT + ".csv.gz";

    @Override
    public void afterPropertiesSet() {
        USE_LIMIT_MAP.put("E", Const.USE_LIMIT_E);
        USE_LIMIT_MAP.put("EC", Const.USE_LIMIT_EC);
        USE_LIMIT_MAP.put("ER", Const.USE_LIMIT_ER);

        String localPath = env.getRequiredProperty("java.io.tmpdir");
        Path dataFilePath = Paths.get(localPath, DATA_FILE_NAME);
        if (Files.notExists(dataFilePath)) {
            ftRepo.download(DATA_FILE_NAME);
        }

        Map<String, List<YqYrRule>> yqList = data("YQ");
        Map<String, List<YqYrRule>> yrList = data("YR");
        readDataFile(dataFilePath, yqList, yrList);
        log.info("YQ/YR Rule have been initialized from '{}', included:",
                dataFilePath.toAbsolutePath());

        log_data_details(yqList, yrList);
    }

    private void readDataFile(Path dataFilePath, Map<String, List<YqYrRule>> c1, Map<String, List<YqYrRule>> c2) {
        try(FileInputStream fis = new FileInputStream(dataFilePath.toFile());
            GZIPInputStream gis = new GZIPInputStream(fis);
            InputStreamReader isr = new InputStreamReader(gis);
            BufferedReader br = new BufferedReader(isr)) {

            CSVReader reader = new CSVReader(br);
            reader.skip(1);
            String[] rec;
            while ((rec = reader.readNext()) != null) {
                YqYrRule yRule = new YqYrRule();
                fillRule(yRule, rec);
                if ("F".equals(rec[0])) {
                    c1.computeIfAbsent(yRule.cxr_code, k -> new ArrayList<>());
                    c1.get(yRule.cxr_code).add(yRule);
                } else {
                    c2.computeIfAbsent(yRule.cxr_code, k -> new ArrayList<>());
                    c2.get(yRule.cxr_code).add(yRule);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillRule(YqYrRule r, String[] rec) {
        int i = 1;
        r.sub_code = rec[0];
        r.cxr_code = S(rec[i++]);
        r.seq_no = DAU.getI(rec[i++]);

        r.travel_eff = getTime(rec[i++]);
        r.travel_disc = getTime(rec[i++]);
        r.ticket_first = getTime(rec[i++]);
        r.ticket_last = getTime(rec[i++]);

        r.rtn_to_orig = S(rec[i++]);
        r.rto = !SU.blk(r.rtn_to_orig) && r.rtn_to_orig.charAt(0) == 'Y';

        r.psgr = S(rec[i++]);
        r.point_of_sale_geographic_l = S(rec[i++]);

        r.point_of_sale_code = S(rec[i++]);
        r.point_of_sale_code_value = S(rec[i++]);

        r.jrny_geo_spec_indicator = DAU.getB(rec[i++], true);
        r.jrny_geo_spec_loc1 = DAU.getB(rec[i++], true);
        r.jrny_geo_spec_loc1_value = S(rec[i++]);
        r.jrny_geo_spec_loc2 = DAU.getB(rec[i++], true);
        r.jrny_geo_spec_loc2_value = S(rec[i++]);

        r.jrny_geo_spec_via_loc = DAU.getB(rec[i++], true);
        r.jrny_geo_spec_via_loc_value = S(rec[i++]);
        r.jrny_geo_spec_trvl_w_w_l = DAU.getB(rec[i++], true);
        r.jrny_geo_spec_trvl_w_w_l_v = S(rec[i++]);

        r.sector_prt_geo_spec = DAU.getB(rec[i++], true);
        r.sector_prt_from_to = DAU.getB(rec[i++], true);
        r.sector_prt_loc1 = DAU.getB(rec[i++], true);
        r.sector_prt_loc1_value = S(rec[i++]);
        r.sector_prt_loc2 = DAU.getB(rec[i++], true);
        r.sector_prt_loc2_value = S(rec[i++]);

        r.sector_prt_via_geo = DAU.getB(rec[i++], true);
        r.sector_prt_via_geo_value = S(rec[i++]);
        r.sector_prt_via_stp_cnx = DAU.getB(rec[i++], true);
        r.sector_prt_via_cnx_exempt = DAU.getB(rec[i++], true);
        r.sector_prt_intl_dom = DAU.getB(rec[i++], true);

        r.rbd_tbl_no_198 = DAU.getS(rec[i++]);
        r.cabin = DAU.c(rec[i++]);

        r.rbd = DAU.getB(rec[i++], true);
        r.rbd2 = DAU.getB(rec[i++], true);
        r.rbd3 = DAU.getB(rec[i++], true);

        r.eqp = S(rec[i++]);
        r.sector_prt_via_exc_stop_time = DAU.getI(rec[i++]);
        r.sector_prt_via_exc_stop_t_u = DAU.getB(rec[i++], true);

        r.service_fee_amount = DAU.getI(rec[i++]);
        r.service_fee_cur = S(rec[i++]);
        r.service_fee_dec = DAU.getB(rec[i++], false);
        r.service_fee_tax = DAU.getB(rec[i++], true);
        r.service_fee_application = DAU.getB(rec[i++], true);

        r.lineno = DAU.getI(rec[i++]);
        r.use_limit = USE_LIMIT_MAP.get(rec[i++]);
        r.travel_d = getTime(rec[i]);
    }

    private long getTime(String date) {
        if (SU.blk(date)) {
            return 0;
        }
        String dateStr = "999999".equals(date) ? "20991231" : "20" + date;
        return DEU.parse_y4M2d2(dateStr).getTime();
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
