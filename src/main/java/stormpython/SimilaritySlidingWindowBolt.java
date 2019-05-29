package stormpython;

import com.google.common.base.Joiner;
import datacrawler.Constant;
import fsrealanalysis.FsData;
import fsrealanalysis.SimilarityRes;
import net.sf.json.JSONObject;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;
import org.apache.storm.Config;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stormpython.util.TupleHelpers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenyun on 2019/5/28.
 */

public class SimilaritySlidingWindowBolt extends BaseBasicBolt {

    static Logger log_error = LoggerFactory.getLogger("errorfile");
    /**
     * 上一个时间点数据
     */
    private Map<String, FsData> preFsDataMap = new HashMap();


    /**
     * 最近index价格差异
     */
    private Map<String, Double> preIndexPriceDiffMap = new HashMap();

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
     * 预警水位
     */
    private double priceWarnLevel;


    /**
     * 指数白名单列表
     */
    private List<String> indexList = Arrays.asList(Constant.stock_index_code.split(","));


    /**
     * 存储所有价格类型的窗口数据 key: code/code_index
     */
    private Map<String, SynchronizedDescriptiveStatistics> stock_window_map = new ConcurrentHashMap<>();


    /**
     * 量
     */
    private Map<String, SynchronizedDescriptiveStatistics> stock_volume_window_map = new ConcurrentHashMap<>();

    /**
     * 量比例
     */
    private int volumeLevel;

    /**
     * 时间窗口数据频次
     */
    private int emitFrequencyInSeconds = 10;


    public SimilaritySlidingWindowBolt(int windowSize, int offset1, int offset2) {
        this.windowSize = windowSize;
        this.offset1 = offset1;
        this.offset2 = offset2;
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {

        try {
            String code = input.getString(0);
            Object item = input.getValue(1);
            JSONObject item_json = JSONObject.fromObject(item);

            FsData curData = (FsData) JSONObject.toBean(item_json, FsData.class);

            FsData preData = preFsDataMap.put(code, curData);

            /**
             * code已经存在数据
             */
            if (preData != null) {

                /**
                 * 量
                 */
                double amount = curData.getAmount();


                SynchronizedDescriptiveStatistics stockVolumeWindow =
                        stock_volume_window_map.putIfAbsent(code, new SynchronizedDescriptiveStatistics(windowSize));
                stockVolumeWindow.addValue(amount);



                /**
                 * 价
                 */
                double priDiff = (curData.getPrice() - preData.getPrice()) / preData.getPrice();

                SynchronizedDescriptiveStatistics stockPriPreDiffWindow =
                        stock_window_map.putIfAbsent(code, new SynchronizedDescriptiveStatistics(windowSize));

                stockPriPreDiffWindow.addValue(priDiff);

                long dataCount = stockPriPreDiffWindow.getN();

                /**
                 * 指数白名单
                 */
                if (indexList.contains(code)) {
                    Double prePriceDiff = preIndexPriceDiffMap.put(code, priDiff);
                    System.out.println(prePriceDiff);
                } else {

                    Map<String, Double> similarityMap = new HashMap<>();

                    Map<String, Double[]> trendMap = new HashMap<>();

                    /**
                     * 指数比对相关----------------开始
                     */
                    for (int j = 0; j < indexList.size(); j++) {

                        String indexKey = indexList.get(j);

                        Double indexDiff = preIndexPriceDiffMap.getOrDefault(indexKey, 0d);
                        double priceIndexDiff = priDiff - indexDiff;
                        /**
                         * code_indexKey
                         */

                        /**
                         * 计算指数_价格差异
                         */

                        String codeIndexKey = Joiner.on("_").join(code, indexKey);
                        SynchronizedDescriptiveStatistics stockPriceIndexDiffWindow = stock_window_map
                                .putIfAbsent(codeIndexKey, new SynchronizedDescriptiveStatistics(windowSize));
                        stockPriceIndexDiffWindow.addValue(priceIndexDiff);


                        long priceIndexDiffCount = stockPriceIndexDiffWindow.getN();


                        /**
                         * N分钟价格差异累计涨幅(剔除指数) > rule_diff
                         */
                        if (priceIndexDiffCount > offset1) {
                            double offset1Sum = stockPriceIndexDiffWindow.getSumImpl().evaluate(
                                    stockPriceIndexDiffWindow.getValues(), offset1,
                                    (int) (priceIndexDiffCount - offset1));
                        }

                        if (priceIndexDiffCount > offset2) {
                            double offset2Sum = stockPriceIndexDiffWindow.getSumImpl().evaluate(
                                    stockPriceIndexDiffWindow.getValues(), offset2,
                                    (int) (priceIndexDiffCount - offset2));
                        }


                        /**
                         * N分钟价格差异累计涨幅 > rule
                         */
                        if (dataCount > offset1) {
                            double offset1Sum = stockPriPreDiffWindow.getSumImpl()
                                    .evaluate(stockPriPreDiffWindow.getValues(), offset1, (int) (dataCount - offset1));
                        }

                        if (dataCount > offset2) {
                            double offset2Sum = stockPriPreDiffWindow.getSumImpl()
                                    .evaluate(stockPriPreDiffWindow.getValues(), offset2, (int) (dataCount - offset2));
                        }



                        /**
                         * 计算相似度(不剔除指数影响)
                         */
                        SynchronizedDescriptiveStatistics indexPriPreDiffWindow = stock_window_map.putIfAbsent(indexKey,
                                new SynchronizedDescriptiveStatistics(windowSize));


                        calcSimilarityMap(similarityMap, stockPriPreDiffWindow, indexPriPreDiffWindow, offset1,
                                indexKey);

                        calcSimilarityMap(similarityMap, stockPriPreDiffWindow, indexPriPreDiffWindow, offset2,
                                indexKey);


                        /**
                         * TODO 趋势符号(不剔除指数影响)
                         */
                        SynchronizedDescriptiveStatistics indexVolumeWindow = stock_volume_window_map
                                .putIfAbsent(indexKey, new SynchronizedDescriptiveStatistics(windowSize));

                        calcTrendPvMap(trendMap, stockVolumeWindow, indexVolumeWindow, stockPriPreDiffWindow,
                                indexPriPreDiffWindow, offset1, indexKey);

                        calcTrendPvMap(trendMap, stockVolumeWindow, indexVolumeWindow, stockPriPreDiffWindow,
                                indexPriPreDiffWindow, offset2, indexKey);

                    }

                    SimilarityRes similarityRes =
                            SimilarityRes.builder().stock(code).similarityMap(similarityMap).trendMap(trendMap).build();

                    /**
                     * 窗口控制使用offset变相代替时间 而非使用时间
                     */
                    if (TupleHelpers.isTickTuple(input)) {
                        System.out.println("emit");
                    }
                    collector.emit(new Values(code, similarityRes));
                }


            }
            /**
             * 当前涨跌幅(相对昨天closePrice)
             */
            double priceCloseDiff = (curData.getPrice() - curData.getPre_close()) / curData.getPre_close();

            String codeCloseKey = Joiner.on("_").join(code, "close");
            SynchronizedDescriptiveStatistics stockPriCloseDiffWindow =
                    stock_window_map.putIfAbsent(codeCloseKey, new SynchronizedDescriptiveStatistics(windowSize));
            if (stockPriCloseDiffWindow != null) {
                stockPriCloseDiffWindow.addValue(priceCloseDiff);
            }



            //collector.emit(new Values(code));
        } catch (Exception e) {
            log_error.error("SimilaritySlidingWindowBolt error", e);
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("code", "similarity"));
    }


    @Override
    public Map<String, Object> getComponentConfiguration() {
        Map<String, Object> conf = new HashMap<String, Object>();
        conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, emitFrequencyInSeconds);
        return conf;
    }

    /**
     * 趋势符号计数---价
     * 
     * @param curPriceDiff
     * @param curV
     * @param volumeWind
     * @param map
     * @param stockPriceWind
     * @param indexPriceWind
     * @param offset
     * @param indexKey
     * @return
     */
    public Map<String, Double[]> calcTrendMap(Map<String, Double[]> map, double curPriceDiff, double curV,
            SynchronizedDescriptiveStatistics volumeWind, SynchronizedDescriptiveStatistics stockPriceWind,
            SynchronizedDescriptiveStatistics indexPriceWind, int offset, String indexKey) {

        long vCount = volumeWind.getN();
        double vMean = volumeWind.getMeanImpl().evaluate(volumeWind.getValues(), offset, (int) (vCount - offset));
        double vWeight = curV / vMean;

        long pCount = stockPriceWind.getN();
        long iCount = indexPriceWind.getN();
        long minCount = pCount < iCount ? pCount : iCount;

        if (minCount > offset) {

            double[] stockArraySub = Arrays.copyOfRange(stockPriceWind.getValues(), offset, (int) minCount);

            double[] indexArraySub = Arrays.copyOfRange(indexPriceWind.getValues(), offset, (int) minCount);

            Double[] trendCal = trendCal(indexArraySub, stockArraySub, vWeight, curPriceDiff);

            String indexOffset = Joiner.on("_").join(indexKey, offset);

            map.put(indexOffset, trendCal);
        }

        return map;
    }

    /**
     * 趋势符号计数----价量
     * 
     * @param map
     * @param volumeStockWind
     * @param volumeIndexWind
     * @param stockPriceWind
     * @param indexPriceWind
     * @param offset
     * @param indexKey
     * @return
     */
    public Map<String, Double[]> calcTrendPvMap(Map<String, Double[]> map,
            SynchronizedDescriptiveStatistics volumeStockWind, SynchronizedDescriptiveStatistics volumeIndexWind,
            SynchronizedDescriptiveStatistics stockPriceWind, SynchronizedDescriptiveStatistics indexPriceWind,
            int offset, String indexKey) {

        long pCount = stockPriceWind.getN();
        long iCount = indexPriceWind.getN();
        long minCount = pCount < iCount ? pCount : iCount;

        if (minCount > offset) {

            double[] stockArraySub = Arrays.copyOfRange(stockPriceWind.getValues(), offset, (int) minCount);

            double[] indexArraySub = Arrays.copyOfRange(indexPriceWind.getValues(), offset, (int) minCount);

            double[] stockVArraySub = Arrays.copyOfRange(volumeStockWind.getValues(), offset, (int) minCount);

            double[] indexVArraySub = Arrays.copyOfRange(volumeIndexWind.getValues(), offset, (int) minCount);

            Double[] trendCal = trendCalPv(indexArraySub, stockArraySub, indexVArraySub, stockVArraySub);

            String indexOffset = Joiner.on("_").join(indexKey, offset);

            map.put(indexOffset, trendCal);
        }

        return map;
    }


    /**
     * 趋势符号统计 -----区分不同意图
     * 
     * @param index
     * @param stock
     * @param vWeight
     * @param curPriceDiff
     * @return
     */
    public Double[] trendCal(double[] index, double[] stock, double vWeight, double curPriceDiff) {
        Double[] trendDiffCount = new Double[4];

        if (index.length == stock.length) {
            double a1, a2, a3, a4;
            for (int i = 0; i < index.length; i++) {

            }
        }

        return trendDiffCount;
    }

    /**
     * 趋势符号统计 -----区分不同意图(价量)
     * 
     * @param index
     * @param stock
     * @param vIndex
     * @param vStock
     * @return
     */
    public Double[] trendCalPv(double[] index, double[] stock, double[] vIndex, double[] vStock) {

        Double[] trendDiffCount = new Double[4];

        if (index.length == stock.length) {

            double a1 = 0, a2 = 0, a3 = 0, a4 = 0;

            double vAvg;

            double vSum = 0d;

            for (int i = 0; i < index.length; i++) {

                double v = vStock[i];

                vSum += v;

                vAvg = vSum / (i + 1);


                /**
                 * 逆势-----价格变化不相同(重点关注)
                 */
                if (index[i] * stock[i] < 0) {
                    /**
                     * 逆势上涨
                     */
                    if (stock[i] > 0) {
                        a1 = a1 + v / vAvg * stock[i];
                    } else {
                        /**
                         * 逆势打压
                         */
                        a2 = a2 + v / vAvg * stock[i];
                    }
                } else {

                    /**
                     * 同势---上涨
                     */

                    /**
                     * 强势---涨幅超过大盘或者抗跌
                     */
                    if (stock[i] - index[i] > 0) {
                        a3 = a3 + v / vAvg * (stock[i] - index[i]);
                    } else {
                        /**
                         * 弱势 ----涨不过大盘或者跌超过大盘
                          */
                        a4 = a4 + v / vAvg * (stock[i] - index[i]);
                    }

                }
            }
            trendDiffCount[0]=a1;
            trendDiffCount[1]=a2;
            trendDiffCount[2]=a3;
            trendDiffCount[3]=a4;
        }

        return trendDiffCount;
    }

    public Map<String, Double> calcSimilarityMap(Map<String, Double> map, SynchronizedDescriptiveStatistics a,
            SynchronizedDescriptiveStatistics b, int offset, String indexKey) {

        long dataCount = a.getN();
        long indexDataCount = b.getN();

        long minCount = dataCount < indexDataCount ? dataCount : indexDataCount;

        if (minCount > offset) {

            double[] stockArraySub = Arrays.copyOfRange(a.getValues(), offset, (int) minCount);

            double[] indexArraySub = Arrays.copyOfRange(b.getValues(), offset, (int) minCount);

            double similarityCal = similarityCal(stockArraySub, indexArraySub);

            String indexOffset = Joiner.on("_").join(indexKey, offset);

            map.put(indexOffset, similarityCal);
        }

        return map;
    }

    public double similarityCal(double[] a, double[] b) {
        double distance = 0;

        if (a.length == b.length) {
            for (int i = 0; i < a.length; i++) {
                double diff = Math.pow(a[i] - b[i], 2);
                distance += diff;
            }
            distance = Math.sqrt(distance);
        }

        return distance;
    }
}
