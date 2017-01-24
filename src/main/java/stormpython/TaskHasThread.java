package stormpython;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import fsanalysis.FsModel;
import sharejdbc.IfsdataDao;

import static stormpython.FsPythonShellLoad.jsonToBean;

/**
 * Created by cy111966 on 2017/1/24.
 */
public class TaskHasThread implements Runnable, INode {
  private static final Logger errorLog = LoggerFactory.getLogger(TaskHasThread.class);
 private String cmdString;
  private int batchSize =500;
  private IfsdataDao fsDao;

  //超时处理
  private long timeout;
  private volatile Process process;
  private volatile FileChannel inputChannel;
  //任务处理完毕标识
  private volatile boolean flag=false;

  //工作线程
  private static Thread workThread;

  //间隔时间

  private long sleepTime;

  public TaskHasThread(int batchSize, String cmdString, IfsdataDao fsDao, long sleepTime) {
    this.batchSize = batchSize;
    this.cmdString = cmdString;
    this.fsDao = fsDao;
    this.sleepTime = sleepTime;
  }

  @Override
  public void run() {
    List<FsModel> itemList = new ArrayList<>();
    try {
      this.init();
      while (!Thread.currentThread().isInterrupted()){
        // TODO: 2017/1/24 如何控制频次
        if (!flag) {
          flag=true;
          ProcessBuilder builder = new ProcessBuilder(cmdString);
          process = builder.start();
          InputStream proInputStream = process.getInputStream();
          if (proInputStream instanceof FileInputStream) {
            inputChannel = ((FileInputStream) proInputStream).getChannel();
          }else {
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
          } catch (Exception e) {
            errorLog.error("batchSave error",e);
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
          flag = false;
          Thread.sleep(sleepTime);//控制调用频次
        }
      }
    }catch (Exception e){
     errorLog.error("",e);
    }finally {
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
    workThread = new Thread(this);
    workThread.start();
//    try {
//      workThread.join(timeout);//内部死循环所用无法定时调用
//    } catch (Exception e) {
//      e.printStackTrace();
//    }finally {
//      this.close();
//    }
  }

  @Override
  public void stopNode() {
    workThread.interrupt();
  }

  /***
   * io阻塞 ---定时任务外部调用
   */
  public void close(){
    try{
      process.destroy();
      if (inputChannel != null) {
        inputChannel.close();
      }
    } catch (IOException e) {
      errorLog.error("close error:",e);
    }
  }
}
