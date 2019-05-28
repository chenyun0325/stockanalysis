package stormpython;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import datacrawler.Constant;
import fsrealanalysis.FsData;
import fsrealanalysis.SimilarityRes;
import net.sf.json.JSONObject;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private Map<String , FsData> preFsDataMap = new HashMap();


    /**
     * 最近index价格差异
     */
    private Map<String , Double> preIndexPriceDiffMap = new HashMap();

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
    private double warnLevel;


    /**
     * 指数白名单列表
     */
    private List<String> indexList = Arrays.asList(Constant.stock_index_code.split(","));


    /**
     * 存储所有类型的窗口数据
     * key: code/code_index
     */
    private Map<String, SynchronizedDescriptiveStatistics> stock_window_map = new ConcurrentHashMap<>();


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
            if (preData !=null) {

                double priDiff = (curData.getPrice() - preData.getPrice()) / preData.getPrice();

                SynchronizedDescriptiveStatistics stockPriPreDiffWindow = stock_window_map.putIfAbsent(code, new SynchronizedDescriptiveStatistics(windowSize));

                stockPriPreDiffWindow.addValue(priDiff);

                long dataCount = stockPriPreDiffWindow.getN();

                /**
                 * 指数白名单
                 */
                if (indexList.contains(code)) {
                    Double prePriceDiff = preIndexPriceDiffMap.put(code, priDiff);
                    System.out.println(prePriceDiff);
                }else {

                    Map<String,Double> similarityMap = new HashMap<>();

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
                        SynchronizedDescriptiveStatistics stockPriceIndexDiffWindow = stock_window_map.putIfAbsent(codeIndexKey, new SynchronizedDescriptiveStatistics(windowSize));
                        stockPriceIndexDiffWindow.addValue(priceIndexDiff);

                        /**
                         * N分钟价格差异累计涨幅 > rule
                         */
                        if (dataCount>offset1){
                            double offset1Sum = stockPriPreDiffWindow.getSumImpl().evaluate(stockPriPreDiffWindow.getValues(), offset1, (int) (dataCount - offset1));
                        }

                        if (dataCount>offset2){
                            double offset2Sum = stockPriPreDiffWindow.getSumImpl().evaluate(stockPriPreDiffWindow.getValues(), offset2, (int) (dataCount - offset2));
                        }


                        /**
                         * 计算相似度
                         */
                        SynchronizedDescriptiveStatistics indexPriPreDiffWindow = stock_window_map.putIfAbsent(indexKey, new SynchronizedDescriptiveStatistics(windowSize));


                        calcSimilarityMap(similarityMap,stockPriceIndexDiffWindow,indexPriPreDiffWindow,offset1,indexKey);

                        calcSimilarityMap(similarityMap,stockPriceIndexDiffWindow,indexPriPreDiffWindow,offset2,indexKey);

                    }

                    SimilarityRes similarityRes = SimilarityRes.builder().stock(code).similarityMap(similarityMap).build();

                    collector.emit(new Values(code,similarityRes));
                }


            }
            /**
             * 当前涨跌幅
             */
            double priceCloseDiff = (curData.getPrice() - curData.getPre_close()) / curData.getPre_close();

            String codeCloseKey = Joiner.on("_").join(code, "close");
            SynchronizedDescriptiveStatistics stockPriCloseDiffWindow = stock_window_map.putIfAbsent(codeCloseKey, new SynchronizedDescriptiveStatistics(windowSize));
            stockPriCloseDiffWindow.addValue(priceCloseDiff);


            /**
             * TODO 趋势符号计数
             */


            collector.emit(new Values(code));
        }catch (Exception e){
         log_error.error("",e);
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("code","similarity"));
    }



    public Map<String,Double> calcSimilarityMap(Map<String,Double> map,SynchronizedDescriptiveStatistics a, SynchronizedDescriptiveStatistics b, int offset,String indexKey){

        long dataCount = a.getN();
        long indexDataCount = b.getN();

        if (dataCount>offset&&indexDataCount>offset){

            double[] stockArraySub = Arrays.copyOfRange(a.getValues(), offset, (int) dataCount);

            double[] indexArraySub = Arrays.copyOfRange(b.getValues(), offset, (int) indexDataCount);

            double similarityCal =  similarityCal(stockArraySub,indexArraySub);

            String indexOffset = Joiner.on("_").join(indexKey, offset);

            map.put(indexOffset,similarityCal);
        }

        return map;
    }
    public double similarityCal(double[] a,double[] b){
        double distance = 0;

        if (a.length==b.length){
            for (int i = 0; i < a.length; i++) {
                double diff = Math.pow(a[i]-b[i],2);
                distance+=diff;
            }
            distance = Math.sqrt(distance);
        }

        return distance;
    }
}
