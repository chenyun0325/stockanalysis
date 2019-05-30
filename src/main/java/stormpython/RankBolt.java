package stormpython;

import datacrawler.Constant;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;

import java.util.Arrays;
import java.util.List;

/**
 * Created by chenyun on 2019/5/29.
 *
 * storm TopN
 *
 * TODO
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
     * 窗口计算偏移量1
     */
    private int offset1;

    /**
     * 窗口计算偏移量1
     */
    private int offset2;


    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        try {

        } catch (Exception e) {

        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }
}
