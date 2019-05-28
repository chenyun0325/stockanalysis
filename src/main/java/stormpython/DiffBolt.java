package stormpython;

import fsrealanalysis.FsData;
import net.sf.json.JSONObject;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

/**
 * Created by chenyun on 2019/4/26.
 */
public class DiffBolt extends BaseBasicBolt {
    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        try {
            String code = input.getString(0);
            Object item = input.getValue(1);
            JSONObject item_json = JSONObject.fromObject(item);
            FsData fsdata = (FsData) JSONObject.toBean(item_json, FsData.class);
            System.err.println(code);
            System.out.println(fsdata);
            System.out.println("mm:"+code);
            System.out.println("name:"+Thread.currentThread().getName()+",code:"+code);
            collector.emit(new Values(code));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("code"));
    }
}
