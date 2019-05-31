package stormpython;

import com.google.common.base.Joiner;
import datacrawler.Constant;
import fsrealanalysis.SimilarityRes;
import net.sf.json.JSONObject;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Created by chenyun on 2019/5/29.
 *
 * storm TopN
 *
 * 1.全局排序---考虑衰减系数
 * 2.滚动窗口(聚合计算---sum(count))
 *   时间戳合并
 *   或
 *   窗口数据合并
 *
 */
public class RankBolt extends BaseBasicBolt {

    /**
     * 指数白名单列表
     */
    private List<String> indexList = Arrays.asList(Constant.stock_index_code.split(","));


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
    private Map<String,AtomicLong> stockStatCount = new ConcurrentHashMap<>();


    /**
     * 最新的map
     */
    private Map<String,SimilarityRes> lastSimilarityMap = new ConcurrentHashMap<>();

    /**
     * 累计map
     * key:index_offset
     * value:剔除非offset指标
     */
    private Map<String,SimilarityRes> sumSimilarityMap = new ConcurrentHashMap<>();


    /**
     * 时间窗口数据
     */
    private Map<String,LinkedList<SimilarityRes>> similarityMapWindow = new ConcurrentHashMap<>();


    public RankBolt(int windowSize, int offset1, int offset2,int topN) {
        this.windowSize = windowSize;
        this.offset1 = offset1;
        this.offset2 = offset2;
        this.topN = topN;
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {

        try {
            String code = input.getString(0);
            Object item = input.getValue(1);
            JSONObject item_json = JSONObject.fromObject(item);
            SimilarityRes similarityRes = (SimilarityRes) JSONObject.toBean(item_json, SimilarityRes.class);
            lastSimilarityMap.put(code,similarityRes);
            long count = stockCountCac(stockStatCount, code);

            LinkedList<SimilarityRes> similarityResWindow = calStockList(similarityMapWindow, code, similarityRes, windowSize);


            SimilarityRes similarityOffset;
            /**
             * 累计计算
             */
            if (count%(windowSize-offset1)==0) {
                similarityOffset = similarityResWindow.get(offset1);
                String key1 = Joiner.on("_").join(code, offset1);
                sumSimilarityMap.put(key1,sumMerger(similarityOffset,similarityRes,offset1));
            }else if (count%(windowSize-offset2) == 0){
                similarityOffset = similarityResWindow.get(offset2);
                String key2 = Joiner.on("_").join(code, offset1);
                sumSimilarityMap.put(key2,sumMerger(similarityOffset,similarityRes,offset2));
            }

            if (lastSimilarityMap.size()>100) {

                Map<String,List<String>> resList = new HashMap<>();

                for (String index : indexList) {
                    String keyOffset1 = Joiner.on("_").join(index, offset1);
                    String keyOffset2 = Joiner.on("_").join(index, offset2);
                    List<String> keyOffset1ListAscending = sortSimilarityTopN(lastSimilarityMap, keyOffset1, topN, true);
                    String keyOffset1Asceding = Joiner.on("_").join(keyOffset1,true);
                    List<String> keyOffset1ListDescending = sortSimilarityTopN(lastSimilarityMap, keyOffset1, topN, false);
                    String keyOffset1Descending = Joiner.on("_").join(keyOffset1,false);
                    List<String> keyOffset2ListAscending = sortSimilarityTopN(lastSimilarityMap, keyOffset2, topN, true);
                    String keyOffset2Asceding = Joiner.on("_").join(keyOffset2,true);
                    List<String> keyOffset2ListDescending = sortSimilarityTopN(lastSimilarityMap, keyOffset2, topN, false);
                    String keyOffset2Descending = Joiner.on("_").join(keyOffset2,false);
                    resList.put(keyOffset1Asceding,keyOffset1ListAscending);
                    resList.put(keyOffset1Descending,keyOffset1ListDescending);
                    resList.put(keyOffset2Asceding,keyOffset2ListAscending);
                    resList.put(keyOffset2Descending,keyOffset2ListDescending);
                }
            }

        } catch (Exception e) {

        }
    }

    /**
     * 指标数据合并
     * @param o1
     * @param o2
     * @param offset
     * @return
     */
    public SimilarityRes sumMerger(SimilarityRes o1,SimilarityRes o2,int offset){

        Set<String> keySet = o1.getSimilarityMap().keySet().stream().filter(i -> i.contains("_" + offset)).collect(Collectors.toSet());
         Map<String,Double> similarityMap = new HashMap<>();

         Map<String,Double[]> trendMap = new HashMap<>();

        for (String key : keySet) {
            double sum = o1.getSimilarityMap().get(key) + o2.getSimilarityMap().get(key);
            similarityMap.put(key,sum);
            Double[] o1TrendMap = o1.getTrendMap().get(key);
            Double[] o2TrendMap = o2.getTrendMap().get(key);
            int length = o1TrendMap.length;
            Double[] trendMapM = new Double[length];
            for (int i = 0; i < length; i++) {
                trendMapM[i] = o1TrendMap[i]+o2TrendMap[i];
            }
            trendMap.put(key,trendMapM);
        }
        return SimilarityRes.builder().stock(o1.getStock()).trendMap(trendMap)
                .similarityMap(similarityMap).build();
    }
    /**
     * 相似度排序
     * @param lastSimilarityMap
     * @param sortKey
     * @param topN
     * @param ascending descending
     * @return
     */
    public List<String> sortSimilarityTopN(Map<String,SimilarityRes> lastSimilarityMap,String sortKey,int topN,boolean ascending){

        List<Map.Entry<String,SimilarityRes>> list = new ArrayList<>(lastSimilarityMap.entrySet());

        Collections.sort(list, (o1, o2) -> {
            if (ascending){
                return o2.getValue().getSimilarityMap().get(sortKey).compareTo(o1.getValue().getSimilarityMap().get(sortKey));
            }else {
                return o1.getValue().getSimilarityMap().get(sortKey).compareTo(o2.getValue().getSimilarityMap().get(sortKey));

            }
        });
       return list.subList(0,topN).stream().map(i->i.getKey()).collect(Collectors.toList());
    }

    public long stockCountCac(Map<String,AtomicLong> map,String stock){
        AtomicLong count = map.putIfAbsent(stock, new AtomicLong(0));
        if (count == null){
            count = new AtomicLong(0);
        }
        return count.incrementAndGet();
    }

    public LinkedList<SimilarityRes> calStockList(Map<String,LinkedList<SimilarityRes>> similarityMapWindow,String key,SimilarityRes value,int maxSize){
        LinkedList<SimilarityRes> stockList = new LinkedList<>();
        LinkedList<SimilarityRes> linkedList = similarityMapWindow.putIfAbsent(key, stockList);
        if (linkedList == null){
            linkedList = stockList;
        }
        if (linkedList.size()>maxSize){
            linkedList.removeFirst();
        }
        linkedList.add(value);
        return linkedList;
    }
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }
}
