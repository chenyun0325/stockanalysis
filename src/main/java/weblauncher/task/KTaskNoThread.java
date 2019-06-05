package weblauncher.task;

import fsanalysis.KModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sharejdbc.IKdataDao;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import static fsanalysis.KModel.jsonToBean;

/**
 * Created by cy111966 on 2017/1/24.
 * todo 返回脚本调用结果集
 */
public class KTaskNoThread implements Runnable, INode {

  private static final Logger errorLog = LoggerFactory.getLogger(KTaskNoThread.class);
  private String cmdString;
  private int batchSize = 500;
  private IKdataDao kDao;

  //超时处理
  private long timeout;
  private volatile Process process;
  private volatile FileChannel inputChannel;


  public KTaskNoThread(int batchSize, String cmdString, IKdataDao kDao, long timeout) {
    this.batchSize = batchSize;
    this.cmdString = cmdString;
    this.kDao = kDao;
    this.timeout = timeout;
  }

  @Override
  public void run() {
    List<KModel> itemList = new ArrayList<>();
    try {
      this.init();
      // ProcessBuilder builder = new ProcessBuilder(cmdString);
      Runtime rt = Runtime.getRuntime();
      process = rt.exec(cmdString);
      InputStream proInputStream = process.getInputStream();
      InputStream pcsErrorStream = process.getErrorStream();
      if (proInputStream instanceof FileInputStream) {
        inputChannel = ((FileInputStream) proInputStream).getChannel();
      } else {
        // throw new Exception("");
      }
      BufferedReader br = new BufferedReader(new InputStreamReader(proInputStream));
      String line;
      try {
        while ((line = br.readLine()) != null) {
          System.out.println(line);
          try {
            KModel kModel = jsonToBean(line);
            itemList.add(kModel);
          } catch (Exception e) {
            errorLog.error("tojson Error:", e);
          }
          int totalSize = itemList.size();
          if (totalSize % batchSize == 0) {
            kDao.batchSave(itemList);
            itemList.clear();
          }
        }
        //处理余量
        kDao.batchSave(itemList);
        BufferedReader brError = new BufferedReader(new InputStreamReader(pcsErrorStream));
        while ((line = brError.readLine()) != null) {
          System.out.println(line);
        }
      } catch (Exception e) {
        errorLog.error("batchSave error", e);
      }

      try {
        process.waitFor();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      br.close();
      int ret = process.exitValue();
      if (ret != 0) {

      }
      System.out.println(ret);
      System.out.println("执行完毕");

    } catch (Exception e) {
      errorLog.error("", e);
    } finally {
      this.resourceRelease();
    }

  }

  @Override
  public void init() {

  }

  @Override
  public void resourceRelease() {

  }

  @Override
  public void startNode() {

    this.run();
//    try {
//      Thread.currentThread().join(timeout);
//    } catch (Exception e) {
//      e.printStackTrace();
//    }finally {
//      this.close();
//    }
  }

  @Override
  public void stopNode() {//解除调用线程阻塞
    this.close();
  }

  /***
   * io阻塞 ---定时任务外部调用
   */
  public void close() {
    try {
      process.destroy();
      inputChannel.close();
    } catch (IOException e) {
      errorLog.error("close error:", e);
    }
  }
}
