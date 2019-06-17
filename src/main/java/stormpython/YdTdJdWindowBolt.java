package stormpython;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fsanalysis.DateUtil;
import fsrealanalysis.FsData;
import fsrealanalysis.FsIndexRes;
import fsrealanalysis.SingleFsDataProcess;
import fsrealanalysis.SlidingWindowPriceRes;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stormpython.rule.RuleConfig;
import stormpython.rule.RuleConfigConstant;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static datacrawler.Constant.stock_index_code;

/**
 * Created by chenyun on 16/2/2. 定时发射数据 http://www.2cto.com/net/201605/512041.html
 */
public class YdTdJdWindowBolt extends BaseBasicBolt {

    static Logger log_error = LoggerFactory.getLogger("errorfile");

    /**
     * todo 第一key：业务规则组合,第二key：股票代码
     */
    static Map<String, Map<String, List<FsIndexRes>>> code_map_multi = new ConcurrentHashMap<>();

    static Map<String, Map<String, List<SlidingWindowPriceRes>>> jd_map_multi = new ConcurrentHashMap<>();

    /**
     * key:业务规则,value:股票集合
     */
    static Map<String, Set<String>> ruleStockSetMap = new ConcurrentHashMap<>();

    Map<String, String> code_hash_map = new ConcurrentHashMap<>();// 存储上一个股票的分时hash,防止同一个数据进入多次

    SingleFsDataProcess process = new SingleFsDataProcess();// 分时数据处理函数

    private int windSize;// 窗口大小

    Map<String, SynchronizedDescriptiveStatistics> stats_map = new ConcurrentHashMap<>();


    StandardDeviation std = new StandardDeviation();

    private static String yd_td_file = "/Users/chenyun/stockData/holders/ydTd";
    private static String jd_file = "/Users/chenyun/stockData/holders/jd";

    private static String rule_stock_file = "/Users/chenyun/stockData/holders/ruleStock";

    private List<String> indexList = Lists.newArrayList(stock_index_code.split(","));

    private static FsPkQueue fsPkQueue = FsPkQueue.getInstance();


    private List<RuleConfig> ruleConfigs = new ArrayList<>();

    static {
        // 设置执行时间
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);// 每天
        // 定制每天的15:02:00执行，
        calendar.set(year, month, day, 15, 2, 00);
        Date date = calendar.getTime();
        // 第一次执行定时任务的时间加一天，以便此任务在下个时间点执行。如果不加一天，任务会立即执行。
        if (date.before(new Date())) {
            Calendar taskStartDate = Calendar.getInstance();
            taskStartDate.setTime(date);
            taskStartDate.add(Calendar.DAY_OF_MONTH, 1);
            date = taskStartDate.getTime();
        }
        // 定点执行任务
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                code_map_multi.clear();
                jd_map_multi.clear();
                ruleStockSetMap.clear();
            }
        };

        Timer timer = new Timer();
        timer.schedule(timerTask, date, 24 * 60 * 60 * 1000);

        new Thread("data_out") {
            @Override
            public void run() {
                while (true) {
                    try {
                        String dateStr = DateUtil.convert2dateStr(new Date()) + ".txt";

                        for (String tdYdRuleKey : code_map_multi.keySet()) {
                            String fileName = Joiner.on("_").join(yd_td_file, tdYdRuleKey, dateStr);
                            printRes(fileName, code_map_multi.get(tdYdRuleKey));
                        }
                        for (String jdRuleKey : jd_map_multi.keySet()) {
                            String fileName = Joiner.on("_").join(jd_file, jdRuleKey, dateStr);
                            printResThread(fileName, jd_map_multi.get(jdRuleKey), false);
                        }
                        for (String ruleStockKey : ruleStockSetMap.keySet()) {
                            String fileName = Joiner.on("_").join(rule_stock_file, ruleStockKey, dateStr);
                            String json = JSONArray.fromObject(ruleStockSetMap.get(ruleStockKey)).toString();

                            printJsonResThread(fileName, json, false);

                        }
                        Thread.sleep(10000);
                    } catch (Exception e) {
                        log_error.error("data_out error:", e);
                        continue;
                    }
                }
            }
        }.start();
    }

    public YdTdJdWindowBolt(List<RuleConfig> ruleConfigs, int wind_size) {
        this.ruleConfigs = ruleConfigs;
        this.windSize = wind_size;
    }

    public void execute(Tuple input, BasicOutputCollector collector) {
        try {

            String code = input.getString(0);

            if (!indexList.contains(code)) {

                Object item = input.getValue(1);
                JSONObject item_json = JSONObject.fromObject(item);
                FsData fsdata = (FsData) JSONObject.toBean(item_json, FsData.class);
                // 添加异步db数据存储
                // fsPkQueue.put(fsdata);
                /**
                 * 数据去重
                 */
                String time_stamp_hash = fsdata.getDate() + " " + fsdata.getTime();

                String preHash = code_hash_map.put(code, time_stamp_hash);

                if (!time_stamp_hash.equals(preHash)) {

                    FsIndexRes indexRes = this.process.process(fsdata);

                    boolean b_ge_s = indexRes.isB_ge_s();
                    double per = indexRes.getPer();
                    double a_all_m = indexRes.getA_all_m();// 可以利用平均值
                    double b_all_m = indexRes.getB_all_m();
                    double price_dif = indexRes.getPrice_dif();// 涨跌幅度

                    /**
                     * 停牌or涨跌停
                     */
                    if (indexRes.isZdtFlag()) {
                        if (price_dif > 0) {
                            ruleStockData(ruleStockSetMap, RuleConfigConstant.ztRuleKey, code);
                        } else {
                            ruleStockData(ruleStockSetMap, RuleConfigConstant.dtRuleKey, code);
                        }
                    } else if (indexRes.isTpFlag()) {
                        ruleStockData(ruleStockSetMap, RuleConfigConstant.tpRuleKey, code);
                    } else {
                        /**
                         * 多业务规则
                         */
                        for (RuleConfig ruleConfig : ruleConfigs) {

                            double filter_per = ruleConfig.getFilter_per();
                            double filter_mount = ruleConfig.getFilter_mount();
                            String ruleKey = Joiner.on("_").join(filter_mount, filter_per, b_ge_s);
                            double compare_mount = b_ge_s ? b_all_m : a_all_m;

                            if (per > filter_per && compare_mount > filter_mount) {

                                indexYdTdCalcStore(code_map_multi, ruleKey, code, indexRes);

                            }

                        }
                    }


                    /**
                     * 托压单比例
                     */
                    double tydPer = indexRes.getPer();

                    if (!Double.isInfinite(tydPer)) {

                        SynchronizedDescriptiveStatistics stats = windowData(stats_map, code, price_dif, windSize);

                        long n = stats.getN();// 管道中的数量

                        for (RuleConfig ruleConfig : ruleConfigs) {

                            double amount = ruleConfig.getMount();
                            double price_dif_var = ruleConfig.getPrice_var();
                            int offset = ruleConfig.getOffset();
                            int minCalcCount = ruleConfig.getMinCalcCount();

                            String varRuleKey = Joiner.on("_").join(amount, offset, price_dif_var);

                            if (n > offset && n > minCalcCount) {

                                double[] var_trans = new double[(int) (n - offset)];
                                double offsetSum =
                                        stats.getSumImpl().evaluate(stats.getValues(), offset, (int) (n - offset));

                                for (int i = offset; i < n; i++) {
                                    var_trans[i - offset] = stats.getElement(i) / offsetSum;
                                }
                                double var_p = std.evaluate(var_trans);

                                // 夹单条件:a1_p>0.9&b1_p>0.9&jd_per=1;

                                if (a_all_m > amount && b_all_m > amount && var_p < price_dif_var) {
                                    SlidingWindowPriceRes varItem = new SlidingWindowPriceRes();
                                    varItem.setVar_p(var_p);
                                    varItem.setDatas(var_trans);
                                    indexJDCalcStore(jd_map_multi, varRuleKey, code, varItem);
                                }
                            }

                        }

                    } else {
                        System.out.println(code + ":zt/dt");
                    }
                }

            }
        } catch (Exception e) {
            log_error.error("unexcept error:", e);
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("code", "index", "price_var"));
    }

    public SynchronizedDescriptiveStatistics windowData(Map<String, SynchronizedDescriptiveStatistics> map, String key,
            double data, int windowSize) {
        SynchronizedDescriptiveStatistics dataWindow = new SynchronizedDescriptiveStatistics(windowSize);

        SynchronizedDescriptiveStatistics preWindow = map.putIfAbsent(key, dataWindow);
        if (preWindow == null) {
            preWindow = dataWindow;
        }
        preWindow.addValue(data);
        return preWindow;
    }

    public Map<String, Set<String>> ruleStockData(Map<String, Set<String>> ruleStockSetMap, String ruleKey,
            String code) {

        HashSet<String> stockSet = Sets.newHashSet();
        Set<String> preStockSet = ruleStockSetMap.putIfAbsent(ruleKey, stockSet);
        if (preStockSet == null) {
            preStockSet = stockSet;
        }
        preStockSet.add(code);
        return ruleStockSetMap;
    }

    /**
     * 多业务规则-->计算结果
     * 
     * @param code_map_multi
     * @param ruleKey
     * @param code
     * @param item
     * @return
     */
    public Map<String, Map<String, List<FsIndexRes>>> indexYdTdCalcStore(
            Map<String, Map<String, List<FsIndexRes>>> code_map_multi, String ruleKey, String code, FsIndexRes item) {

        Map<String, List<FsIndexRes>> ruleStockMap = new HashMap<>();

        Map<String, List<FsIndexRes>> preRuleStockMap = code_map_multi.putIfAbsent(ruleKey, ruleStockMap);

        if (preRuleStockMap == null) {
            preRuleStockMap = ruleStockMap;
        }
        List codeIndexList = new ArrayList<FsIndexRes>();
        List preCodeIndexList = preRuleStockMap.putIfAbsent(code, codeIndexList);
        if (preCodeIndexList == null) {
            preCodeIndexList = codeIndexList;
        }
        preCodeIndexList.add(item);

        return code_map_multi;
    }

    /**
     * jd分析
     * 
     * @param jd_map_multi
     * @param ruleKey
     * @param code
     * @param item
     * @return
     */
    public Map<String, Map<String, List<SlidingWindowPriceRes>>> indexJDCalcStore(
            Map<String, Map<String, List<SlidingWindowPriceRes>>> jd_map_multi, String ruleKey, String code,
            SlidingWindowPriceRes item) {

        Map<String, List<SlidingWindowPriceRes>> ruleStockMap = new HashMap<>();

        Map<String, List<SlidingWindowPriceRes>> preRuleStockMap = jd_map_multi.putIfAbsent(ruleKey, ruleStockMap);

        if (preRuleStockMap == null) {
            preRuleStockMap = ruleStockMap;
        }
        List codeIndexList = new ArrayList<SlidingWindowPriceRes>();
        List preCodeIndexList = preRuleStockMap.putIfAbsent(code, codeIndexList);
        if (preCodeIndexList == null) {
            preCodeIndexList = codeIndexList;
        }
        preCodeIndexList.add(item);

        return jd_map_multi;
    }



    public static void printRes(String fileName, Map<String, List<FsIndexRes>> maps) {
        FileWriter fw = null;
        BufferedWriter bfw = null;
        try {
            fw = new FileWriter(fileName, false);
            bfw = new BufferedWriter(fw);
            long time = System.currentTimeMillis();
            String timeStr = DateUtil.convert2dateStr(time);
            for (String code : maps.keySet()) {
                List<FsIndexRes> list = maps.get(code);
                String json = JSONArray.fromObject(list).toString();
                String item = code + ":" + "@" + "time:" + timeStr + "@" + "content:" + json;
                bfw.write(item);
                bfw.newLine();
            }
            bfw.flush();

        } catch (IOException e) {
            log_error.error("create file error", e);
        } finally {
            try {
                bfw.close();
                fw.close();
            } catch (IOException e) {
                log_error.error("close file error:", e);
            }
        }
    }

    public static void printResThread(String fileName, Map<String, List<SlidingWindowPriceRes>> maps,
            boolean appendFlag) {
        FileWriter fw = null;
        BufferedWriter bfw = null;
        try {
            fw = new FileWriter(fileName, appendFlag);
            bfw = new BufferedWriter(fw);
            long time = System.currentTimeMillis();
            for (String code : maps.keySet()) {
                List<SlidingWindowPriceRes> list = maps.get(code);
                String json = JSONArray.fromObject(list).toString();
                String item = code + ":" + "@" + "time:" + time + "@" + "content:" + json;
                bfw.write(item);
                bfw.newLine();
            }
            bfw.flush();

        } catch (IOException e) {
            log_error.error("create file error", e);
        } finally {
            try {
                bfw.close();
                fw.close();
            } catch (IOException e) {
                log_error.error("close file error:", e);
            }
        }
    }

    public static void printJsonResThread(String fileName, String json, boolean appendFlag) {
        FileWriter fw = null;
        BufferedWriter bfw = null;
        try {
            fw = new FileWriter(fileName, appendFlag);
            bfw = new BufferedWriter(fw);
            bfw.write(json);
            bfw.flush();

        } catch (IOException e) {
            log_error.error("create file error", e);
        } finally {
            try {
                bfw.close();
                fw.close();
            } catch (IOException e) {
                log_error.error("close file error:", e);
            }
        }
    }
}
