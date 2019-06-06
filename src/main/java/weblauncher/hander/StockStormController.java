package weblauncher.hander;

import com.google.common.collect.Lists;
import datacrawler.Constant;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.BoltDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import stormpython.*;
import stormpython.rule.RuleConfig;

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
   * localhost/ct2/start.do?topology=testStorm&filter_mount=1000000&filter_per=100&slide_size=20&max_size=100&wind_size=10&price_dif_var=0.005&amount=1000000&price_dif_var1=0.001&amount1=1500000
   */
  @RequestMapping("/start.do")
  public void stormStart(StockStormQuery query, HttpServletResponse res) {
    try {
      TopologyBuilder builder = new TopologyBuilder();
      RuleConfig ruleConfig = RuleConfig.builder()
              .mount(query.getAmount()).price_var(query.getPrice_dif_var())
              .filter_mount(query.getFilter_mount())
              .filter_per(query.getFilter_per())
              .minCalcCount(query.getSlide_size()).offset(0).build();
      RuleConfig ruleConfig1 = RuleConfig.builder()
              .mount(query.getAmount1()).price_var(query.getPrice_dif_var1())
              .filter_mount(query.getFilter_mount())
              .filter_per(query.getFilter_per())
              .minCalcCount(query.getSlide_size()).offset(5).build();

      YdTdJdWindowBolt ydTdJdWindowBolt = new YdTdJdWindowBolt(Lists.newArrayList(ruleConfig,ruleConfig1), query.getMax_size());

      BoltDeclarer ytjCalcBolt = builder.setBolt("ytjCalcBolt", ydTdJdWindowBolt, 4);
      builder.setSpout("FsRealSpout", new StockbatchSpout(Constant.stock_all, "4"), 1);
      ytjCalcBolt.fieldsGrouping("FsRealSpout", new Fields("code"));

      String[] stockIndexArray = Constant.stock_index_code.split(",");
      List<String> stockIndexList = Arrays.asList(stockIndexArray);

      builder.setBolt("similarityBolt",new SimilarityTrendFlagCountSWBolt(10,2,5,15),2).customGrouping("FsRealSpout","diff",new PriceDiffCustomStreamGrouping(new Fields("code"),stockIndexList));
      builder.setBolt("rankBolt",new GlobalRankBolt(10,2,5,30,15)).globalGrouping("similarityBolt");

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
