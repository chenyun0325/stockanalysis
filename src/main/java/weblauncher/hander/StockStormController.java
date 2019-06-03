package weblauncher.hander;

import datacrawler.Constant;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.BoltDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import stormpython.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;


/**
 * Created by cy111966 on 2017/1/22.
 */
@Controller
public class StockStormController {


  LocalCluster cluster = new LocalCluster();

  /**
   * localhost/ct2/start.do?topology=test&filter_mount=1000000&filter_per=100&slide_size=20&max_size=100&wind_size=10&price_dif_var=0.005&amount=1000000&price_dif_var1=0.001&amount1=1500000
   */
  @RequestMapping("/start.do")
  public void stormStart(StockStormQuery query, HttpServletResponse res) {
    try {
      TopologyBuilder builder = new TopologyBuilder();
      JdTdSlidingWindowBolt bolt = new JdTdSlidingWindowBolt(query.getFilter_mount(), query.getFilter_per(), query.getSlide_size());
      BoltDeclarer splitBolt = builder.setBolt("SplitBolt", bolt, 4);
      builder.setSpout("FsRealSpout", new StockbatchSpout(Constant.stock_all, "4"), 1);
      splitBolt.fieldsGrouping("FsRealSpout", new Fields("code"));
      builder.setBolt("slidBolt", new JDSlidingWindowBolt(query.getMax_size(), query.getWind_size(),
              query.getPrice_dif_var(), query.getAmount(),
              query.getPrice_dif_var1(),
              query.getAmount1()), 2)
          .fieldsGrouping("SplitBolt", new Fields("code"));
     // builder.setBolt("diffBlot",new DiffBolt()).fieldsGrouping("FsRealSpout","diff",new Fields("code"));

      String[] stockIndexArray = Constant.stock_index_code.split(",");
      List<String> stockIndexList = Arrays.asList(stockIndexArray);
      //builder.setBolt("diffBlot",new DiffBolt(),2).customGrouping("FsRealSpout","diff",new PriceDiffCustomStreamGrouping(new Fields("code"),stockIndexList));

      //builder.setBolt("",new SimilarityTrendFlagCountSWBolt(10,3,5)).globalGrouping("");

      builder.setBolt("similarityBolt",new SimilarityTrendFlagCountSWBolt(5,1,3,15),2).customGrouping("FsRealSpout","diff",new PriceDiffCustomStreamGrouping(new Fields("code"),stockIndexList));

      builder.setBolt("rankBolt",new GlobalRankBolt(5,1,3,10,15)).globalGrouping("similarityBolt");

      Config conf = new Config();
      conf.put(Config.TOPOLOGY_DEBUG, false);
      conf.put(Config.SUPERVISOR_WORKER_TIMEOUT_SECS, 1000);
      cluster.submitTopology(query.getTopology(), conf, builder.createTopology());
      res.getWriter().write("ok");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @RequestMapping("/stop.do")
  public void stormStop(String topology,HttpServletResponse res) {
    try {
      cluster.activate(topology);
      cluster.killTopology(topology);
      //cluster.shutdown();
      res.getWriter().write("ok");
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

}
