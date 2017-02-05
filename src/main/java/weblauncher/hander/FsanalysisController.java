package weblauncher.hander;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.SQLException;

import fsanalysis.FsAnalysis;
import fsanalysis.FsResDisplay;
import fsanalysis.Unit;

/**
 * Created by cy111966 on 2017/2/4.
 */
@Controller
public class FsanalysisController {


  /**
   * localhost/ct2/fsanalysis.do?stockcode=000040&beginDate=2016-11-07&beginTime=09:00:00&endDate=2017-01-16&endTime=15:00:00&period=1&unit=day&bVolume=120000&svolume=120000
   * @param query
   * @return
   * @throws SQLException
   */
  @RequestMapping("/fsanalysis.do")
  @ResponseBody
  public FsResDisplay fsanalysis_query(FsanalysisQuery query) throws SQLException {
    FsResDisplay fsResDisplay =
        FsAnalysis.fs_analysis_v(query.getStockcode(), query.getBeginDate(), query.getBeginTime(),
                                 query.getEndDate(), query.getEndTime(), query.getPeriod(),
                                 Unit.day, query.getbVolume(), query.getSvolume());
    return fsResDisplay;
  }
}
