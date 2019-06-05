package weblauncher.hander;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import fsanalysis.FsModel;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import sharejdbc.IfsdataDao;

import javax.annotation.Resource;
import java.io.*;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import static stormpython.testStorm.FsPythonShellLoad.jsonToBean;

/**
 * Created by cy111966 on 2017/1/23.
 */
@Controller
public class FsPythonShellLoadController {

  @Resource
  private IfsdataDao fsDao;

  private final int batchSize = 200;

  private final String split = ",";

  /**
   * http://localhost:8080/ct2/fsload.do?stockList=600379&beginDate=2017-03-20&endDate=2017-07-22&shellFile=fsdata_load.py&batchSize=500
   * @param query
   */
  @RequestMapping("/fsload.do")
  public void fsload(FsLoadQuery query) {
    try {
      String stockList = query.getStockList();
      String beginDate = query.getBeginDate();
      String endDate = query.getEndDate();
      String shellFile = query.getShellFile();
      if (stockList != null) {
        String[] splitList = stockList.split(this.split);
        for (String code : splitList) {
          int count = fsDao.batchDelete(code, beginDate, endDate);
          System.out.println(count);
        }
      }
      ProtectionDomain protectionDomain = FsPythonShellLoadController.class.getProtectionDomain();
      URL codeLoc = protectionDomain.getCodeSource().getLocation();
      String dir = new File(codeLoc.getFile()).getAbsolutePath();
      String path = dir + System.getProperty("file.separator") + shellFile;
      List<String> params =
          Lists.newArrayList("python", path, stockList, beginDate, endDate);
      String cmd = Joiner.on(" ").join(params);
      System.out.println(cmd);

      Runtime rt = Runtime.getRuntime();
      Process pcs = rt.exec(cmd);
      InputStream pcsInputStream = pcs.getInputStream();
      InputStream pcsErrorStream = pcs.getErrorStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(pcsInputStream));
      List<FsModel> itemList = new ArrayList<>();
      String line;
      while ((line = br.readLine()) != null) {
        System.out.println(line);
        FsModel fsModel = jsonToBean(line);
        itemList.add(fsModel);
        int totalSize = itemList.size();
        if (totalSize % batchSize == 0) {
          fsDao.batchSave(itemList);
          itemList.clear();
        }
      }
      //处理余量
      fsDao.batchSave(itemList);
      BufferedReader brError = new BufferedReader(new InputStreamReader(pcsErrorStream));
      while ((line = brError.readLine()) != null) {
        System.out.println(line);
      }
      try {
        pcs.waitFor();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      br.close();
      int ret = pcs.exitValue();
      System.out.println(ret);
      System.out.println("执行完毕");
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

}
