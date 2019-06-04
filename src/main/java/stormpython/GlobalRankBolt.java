package stormpython;

import com.google.common.base.Joiner;
import datacrawler.Constant;
import fsanalysis.DateUtil;
import fsrealanalysis.SimilarityRes;
import net.sf.json.JSONObject;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Created by chenyun on 2019/5/29.
 *
 * storm TopN
 *
 * 1.全局排序---考虑衰减系数 2.滚动窗口(聚合计算---sum(count)) 时间戳合并 或 窗口数据合并
 *
 */
public class GlobalRankBolt extends BaseBasicBolt {


    static Logger log_error = LoggerFactory.getLogger("errorfile");

    private String rk_sim_file_thead = "/Users/chenyun/stockData/holders/rank/rank_sim_thead";
    private String rk_sim_sum_file_thead = "/Users/chenyun/stockData/holders/rank/rank_sim_sum_thead";

    private String rk_trend_file_thead = "/Users/chenyun/stockData/holders/rank/rank_trend_thead";

    private String rk_trend_sum_file_thead = "/Users/chenyun/stockData/holders/rank/rank_trend_sum_thead";
    /**
     * 指数白名单列表
     */
    private static List<String> indexList = Arrays.asList(Constant.stock_index_code.split(","));


    /**
     * 窗口大小
     */
    private int windowSize;

    /**
     * 窗口计算偏移量1
     */
    private int offset1;

    /**
     * 窗口计算偏移量1
     */
    private int offset2;


    /**
     * topN
     */
    private int topN;

    /**
     * 计算上游发送数据
     */
    private Map<String, AtomicLong> stockStatCount = new ConcurrentHashMap<>();


    /**
     * 最新的map
     */
    private static Map<String, SimilarityRes> lastSimilarityMap = new ConcurrentHashMap<>();

    /**
     * 累计map key:index_offset value:剔除非offset指标
     */
    private static Map<String, SimilarityRes> sumSimilarityMap = new ConcurrentHashMap<>();

    /**
     * 累计map窗口计算 key:index_offset value:剔除非offset指标
     */
    private static Map<String, SimilarityRes> sumSimilarityWindMap = new ConcurrentHashMap<>();


    /**
     * 时间窗口数据
     */
    private static Map<String, LinkedList<SimilarityRes>> similarityMapWindow = new ConcurrentHashMap<>();


    private int emitFrequencyInSeconds = 10;


    private int errorCount = 100;

    public GlobalRankBolt(int windowSize, int offset1, int offset2, int topN, int frequencyInSeconds) {
        this.windowSize = windowSize;
        this.offset1 = offset1;
        this.offset2 = offset2;
        this.topN = topN;
        this.emitFrequencyInSeconds = frequencyInSeconds;
        /**
         * 数据输出线程
         */
        new Thread("rank_data_out") {
            @Override
            public void run() {
                while (true) {
                    try {

                        Thread.sleep(emitFrequencyInSeconds * 1000);

                        if (lastSimilarityMap.size() >= Constant.stockSize-errorCount) {
                            rankOutPutFile(lastSimilarityMap, indexList, offset1, offset2, topN, rk_sim_file_thead,true);
                            rankOutPutFile(lastSimilarityMap, indexList, offset1, offset2, topN, rk_trend_file_thead,false);
                        }
                        if (sumSimilarityMap.size() >= 2*(Constant.stockSize-errorCount)) {
                            rankOutPutFile(sumSimilarityMap, indexList, offset1, offset2, topN, rk_sim_sum_file_thead,true);
                            rankOutPutFile(sumSimilarityMap, indexList, offset1, offset2, topN, rk_trend_sum_file_thead,false);
                        }

                    } catch (Exception e) {
                        log_error.error("data_out error:", e);
                        e.printStackTrace();
                        continue;
                    }
                }
            }
        }.start();

    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {


        try {
            String code = input.getString(0);
            Object item = input.getValue(1);
            JSONObject item_json = JSONObject.fromObject(item);
            SimilarityRes similarityRes = (SimilarityRes) JSONObject.toBean(item_json, SimilarityRes.class);
            lastSimilarityMap.put(code, similarityRes);
            long count = stockCountCac(stockStatCount, code);

            LinkedList<SimilarityRes> similarityResWindow =
                    calStockList(similarityMapWindow, code, similarityRes, windowSize);


            SimilarityRes similarityOffset;
            /**
             * 累计计算
             */
            if (count % windowSize == offset1) {
                similarityOffset = similarityResWindow.get(offset1 - 1);
                String key1 = Joiner.on("_").join(code, offset1);
                calcSumAndWindSum(similarityRes, count, similarityOffset, key1,windowSize,sumSimilarityMap,sumSimilarityWindMap,offset1);
            } else if (count % windowSize == offset2) {
                similarityOffset = similarityResWindow.get(offset2 - 1);
                String key2 = Joiner.on("_").join(code, offset2);
                calcSumAndWindSum(similarityRes, count, similarityOffset, key2,windowSize,sumSimilarityMap,sumSimilarityWindMap,offset2);
            }


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(input);
        }
    }

    private void calcSumAndWindSum(SimilarityRes similarityRes, long count, SimilarityRes similarityOffset, String key, int windowSize, Map<String, SimilarityRes> sumSimilarityMap, Map<String, SimilarityRes> sumSimilarityWindMap, int offset) {

        if (count > windowSize) {

            SimilarityRes merger = sumMerger(similarityOffset, similarityRes, offset,false);

            SimilarityRes preMerger = sumSimilarityMap.put(key, merger);
            if (preMerger == null){
                preMerger = merger;
            }
            preMerger = sumSimilarityWindMap.get(key)== null?preMerger:sumSimilarityWindMap.get(key);

            sumSimilarityWindMap.put(key,sumMerger(preMerger,similarityRes,offset,true));

        }
    }

    /**
     * 指标数据合并
     * 
     * @param o1 offset之前数据
     * @param o2 最新数据
     * @param offset
     * @param windOp 窗口拼接操作
     * @return
     */
    public SimilarityRes sumMerger(SimilarityRes o1, SimilarityRes o2, int offset,boolean windOp) {


        Set<String> keySet = o1.getSimilarityMap().keySet().stream().filter(i -> i.contains("_" + offset))
                .collect(Collectors.toSet());
        Map<String, Double> similarityMap = new HashMap<>();

        Map<String, List<Double>> trendMap = new HashMap<>();

        Map<String,String> similarityWindMap = new HashMap<>();

        Map<String,List<String>> trendWindMap = new HashMap<>();

        for (String key : keySet) {
            double sum = o1.getSimilarityMap().get(key) + o2.getSimilarityMap().get(key);
            similarityMap.put(key, sum);
            if (windOp) {
                Map<String, String> o1SimilarityWindMap = o1.getSimilarityWindMap();
                if (o1SimilarityWindMap != null && o1SimilarityWindMap.get(key) != null) {
                    similarityWindMap.put(key, Joiner.on("_").join(o1SimilarityWindMap.get(key), o2.getSimilarityMap().get(key)));
                }
            }else {
                similarityWindMap.put(key, Joiner.on("_").join(o1.getSimilarityMap().get(key), o2.getSimilarityMap().get(key)));
            }
            List<Double> o1IndexList = o1.getTrendMap().get(key);
            List<Double> o2IndexList = o2.getTrendMap().get(key);

            int length = o1IndexList.size();
            List<Double> trendMapMList = new ArrayList<>();
            List<String> trendMapWindMList = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                trendMapMList.add(o1IndexList.get(i)+o2IndexList.get(i));
               if (windOp) {
                   Map<String, List<String>> o1TrendWindMap = o1.getTrendWindMap();
                   if (o1TrendWindMap != null && o1TrendWindMap.get(key) != null && o1TrendWindMap.get(key).size() > i) {
                       trendMapWindMList.add(Joiner.on("_").join(o1TrendWindMap.get(key).get(i), o2IndexList.get(i)));
                   }
               }else {
                   trendMapWindMList.add(Joiner.on("_").join(o1IndexList.get(i),o2IndexList.get(i)));
               }
            }
            trendMap.put(key, trendMapMList);
            trendWindMap.put(key,trendMapWindMList);
        }
        return SimilarityRes.builder().stock(o1.getStock()).trendMap(trendMap).similarityMap(similarityMap)
                .trendWindMap(trendWindMap)
                .similarityWindMap(similarityWindMap).build();
    }

    /**
     * 相似度排序
     * 
     * @param lastSimilarityMap
     * @param sortKey
     * @param topN
     * @param ascending descending
     * @return
     */
    public static List<Map.Entry<String, SimilarityRes>> sortSimilarityTopN(
            Map<String, SimilarityRes> lastSimilarityMap, String sortKey, int topN, boolean ascending) {

        List<Map.Entry<String, SimilarityRes>> list = new ArrayList<>(lastSimilarityMap.entrySet());

        list = list.stream().filter(i -> i.getValue().getSimilarityMap().get(sortKey) != null)
                .collect(Collectors.toList());

        Collections.sort(list, (o1, o2) -> {
            if (ascending) {
                return o2.getValue().getSimilarityMap().get(sortKey)
                        .compareTo(o1.getValue().getSimilarityMap().get(sortKey));
            } else {
                return o1.getValue().getSimilarityMap().get(sortKey)
                        .compareTo(o2.getValue().getSimilarityMap().get(sortKey));

            }
        });
        list = list.size() > topN ? list.subList(0, topN) : list;
        List<Map.Entry<String, SimilarityRes>> topNRes = list.stream().collect(Collectors.toList());
        return topNRes;
    }


    /**
     * 趋势符号计数排序
     * @param lastSimilarityMap
     * @param sortKey
     * @param indexPos
     * @param topN
     * @param ascending
     * @return
     */
    public static List<Map.Entry<String, SimilarityRes>> sortTrendCountTopN(
            Map<String, SimilarityRes> lastSimilarityMap, String sortKey, int indexPos, int topN, boolean ascending) {

        List<Map.Entry<String, SimilarityRes>> list = new ArrayList<>(lastSimilarityMap.entrySet());

        list = list.stream().filter(i -> {
            List<Double> countList = i.getValue().getTrendMap().get(sortKey);
            return countList != null&& !countList.isEmpty()
         &&countList.size()>indexPos&& countList.get(indexPos)!=null;
        }).collect(Collectors.toList());

        Collections.sort(list, (o1, o2) -> {
            if (ascending) {
                return o2.getValue().getTrendMap().get(sortKey).get(indexPos)
                        .compareTo(o1.getValue().getTrendMap().get(sortKey).get(indexPos));
            } else {
                return o1.getValue().getTrendMap().get(sortKey).get(indexPos)
                        .compareTo(o2.getValue().getTrendMap().get(sortKey).get(indexPos));

            }
        });
        list = list.size() > topN ? list.subList(0, topN) : list;
        List<Map.Entry<String, SimilarityRes>> topNRes = list.stream().collect(Collectors.toList());
        return topNRes;
    }

    public long stockCountCac(Map<String, AtomicLong> map, String stock) {
        AtomicLong count = map.putIfAbsent(stock, new AtomicLong(0));
        if (count == null) {
            count = new AtomicLong(0);
        }
        return count.incrementAndGet();
    }

    public LinkedList<SimilarityRes> calStockList(Map<String, LinkedList<SimilarityRes>> similarityMapWindow,
            String key, SimilarityRes value, int maxSize) {
        LinkedList<SimilarityRes> stockList = new LinkedList<>();
        LinkedList<SimilarityRes> linkedList = similarityMapWindow.putIfAbsent(key, stockList);
        if (linkedList == null) {
            linkedList = stockList;
        }
        if (linkedList.size() > maxSize) {
            linkedList.removeFirst();
        }
        linkedList.add(value);
        return linkedList;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }


    /**
     * 排序结果输出到文件
     * @param map
     * @param indexList
     * @param offset1
     * @param offset2
     * @param topN
     * @param rk_file_thead
     * @param isSimSort
     */
    public void rankOutPutFile(Map<String, SimilarityRes> map, List<String> indexList, int offset1, int offset2,
                               int topN, String rk_file_thead,boolean isSimSort) {
        Map<String, List<Map.Entry<String, SimilarityRes>>> resList = new HashMap<>();
        String dateStr = DateUtil.convert2dateStr(new Date());
        for (String index : indexList) {
            String keyOffset1 = Joiner.on("_").join(index, offset1);
            String keyOffset2 = Joiner.on("_").join(index, offset2);
            if (isSimSort) {
                List<Map.Entry<String, SimilarityRes>> keyOffset1ListAscending =
                        sortSimilarityTopN(map, keyOffset1, topN, true);
                String keyOffset1Asceding = Joiner.on("_").join(keyOffset1, true);
                List<Map.Entry<String, SimilarityRes>> keyOffset1ListDescending =
                        sortSimilarityTopN(map, keyOffset1, topN, false);
                String keyOffset1Descending = Joiner.on("_").join(keyOffset1, false);
                List<Map.Entry<String, SimilarityRes>> keyOffset2ListAscending =
                        sortSimilarityTopN(map, keyOffset2, topN, true);
                String keyOffset2Asceding = Joiner.on("_").join(keyOffset2, true);
                List<Map.Entry<String, SimilarityRes>> keyOffset2ListDescending =
                        sortSimilarityTopN(map, keyOffset2, topN, false);
                String keyOffset2Descending = Joiner.on("_").join(keyOffset2, false);
                resList.put(keyOffset1Asceding, keyOffset1ListAscending);
                resList.put(keyOffset1Descending, keyOffset1ListDescending);
                resList.put(keyOffset2Asceding, keyOffset2ListAscending);
                resList.put(keyOffset2Descending, keyOffset2ListDescending);
                String file1 = rk_file_thead + "_" + keyOffset1Asceding + "_" + dateStr + ".txt";
                printResThead(file1, keyOffset1ListAscending, false);
                file1 = rk_file_thead + "_" + keyOffset1Descending + "_" + dateStr + ".txt";
                printResThead(file1, keyOffset1ListDescending, false);
                file1 = rk_file_thead + "_" + keyOffset2Asceding + "_" + dateStr + ".txt";
                printResThead(file1, keyOffset2ListAscending, false);
                file1 = rk_file_thead + "_" + keyOffset2Descending + "_" + dateStr + ".txt";
                printResThead(file1, keyOffset2ListDescending, false);
            }else {
                /**
                 * 趋势符号在List位置
                 */
                for (int i =0;i<=3;i++){

                    List<Map.Entry<String, SimilarityRes>> keyOffset1ListAscending=sortTrendCountTopN(map,keyOffset1,i,topN,true);
                    Object[] joinArrayA = {i,true,dateStr,".txt"};
                    Object[] joinArrayD = {i,false,dateStr,".txt"};

                    printResThead(Joiner.on("_").join(rk_file_thead,keyOffset1, joinArrayA),keyOffset1ListAscending,false);

                    List<Map.Entry<String, SimilarityRes>> keyOffset1ListDescending = sortTrendCountTopN(map,keyOffset1,i,topN,false);

                    printResThead(Joiner.on("_").join(rk_file_thead,keyOffset1, joinArrayD),keyOffset1ListDescending,false);

                    List<Map.Entry<String, SimilarityRes>> keyOffset2ListAscending = sortTrendCountTopN(map,keyOffset2,i,topN,true);

                    printResThead(Joiner.on("_").join(rk_file_thead,keyOffset2, joinArrayA),keyOffset2ListAscending,false);

                    List<Map.Entry<String, SimilarityRes>> keyOffset2ListDescending =sortTrendCountTopN(map,keyOffset2,i,topN,false);

                    printResThead(Joiner.on("_").join(rk_file_thead,keyOffset2, joinArrayD),keyOffset2ListDescending,false);

                }
            }

        }
    }

    public void printResThead(String fileName, List<Map.Entry<String, SimilarityRes>> topNRes, boolean appendFlag) {
        FileWriter fw = null;
        BufferedWriter bfw = null;
        try {
            fw = new FileWriter(fileName, appendFlag);
            bfw = new BufferedWriter(fw);
            long time = System.currentTimeMillis();
            for (Map.Entry<String, SimilarityRes> entry : topNRes) {
                String stockCode = entry.getKey();
                SimilarityRes sortValue = entry.getValue();
                String json = JSONObject.fromObject(sortValue).toString();
                String item = stockCode + ":" + "@" + "time:" + time + "@" + "content:" + json;
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
}
