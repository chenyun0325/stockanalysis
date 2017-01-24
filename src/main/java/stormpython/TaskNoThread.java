package stormpython;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import fsanalysis.FsModel;
import sharejdbc.IfsdataDao;

import static stormpython.FsPythonShellLoad.jsonToBean;

/**
 * Created by cy111966 on 2017/1/24.
 */
public class TaskNoThread implements Runnable, INode {

  private static final Logger errorLog = LoggerFactory.getLogger(TaskNoThread.class);
  private String cmdString;
  private int batchSize = 500;
  private IfsdataDao fsDao;

  //超时处理
  private long timeout;
  private volatile Process process;
  private volatile FileChannel inputChannel;


  public TaskNoThread(int batchSize, String cmdString, IfsdataDao fsDao, long timeout) {
    this.batchSize = batchSize;
    this.cmdString = cmdString;
    this.fsDao = fsDao;
    this.timeout = timeout;
  }

  @Override
  public void run() {
    List<FsModel> itemList = new ArrayList<>();
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
            FsModel fsModel = jsonToBean(line);
            itemList.add(fsModel);
          } catch (Exception e) {
            errorLog.error("tojson Error:", e);
          }
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
