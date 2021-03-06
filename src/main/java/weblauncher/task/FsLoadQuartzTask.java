package weblauncher.task;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.io.File;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fsanalysis.DateUtil;
import sharejdbc.IfsdataDao;
import stormpython.TaskHasThread;

/**
 * Created by cy111966 on 2017/1/24.
 * 当日分时数据 处理
 */
public class FsLoadQuartzTask implements ITask {

  private String stockList;

  private String shellFile;

  private int batchsize;

  private int dbBatchSize;

  private long sleepTime;

  private IfsdataDao fsdataDao;

  public void setBatchsize(int batchsize) {
    this.batchsize = batchsize;
  }

  public void setDbBatchSize(int dbBatchSize) {
    this.dbBatchSize = dbBatchSize;
  }

  public void setFsdataDao(IfsdataDao fsdataDao) {
    this.fsdataDao = fsdataDao;
  }

  public void setShellFile(String shellFile) {
    this.shellFile = shellFile;
  }

  public void setSleepTime(long sleepTime) {
    this.sleepTime = sleepTime;
  }

  public void setStockList(String stockList) {
    this.stockList = stockList;
  }

  private final String split=",";

  private static List<TaskHasThread> taskThreads = new ArrayList<>();

  @Override
  public void start() {
    //控制调度频次
    String nowDate = DateUtil.convert2dateStr(new Date());
    //脚本路径
    ProtectionDomain protectionDomain = FsLoadQuartzTask.class.getProtectionDomain();
    URL codeLoc = protectionDomain.getCodeSource().getLocation();
    String dir = new File(codeLoc.getFile()).getAbsolutePath();
    String path = dir + System.getProperty("file.separator") + shellFile;

    if (stockList != null) {
      //删除数据
      String[] codes = stockList.split(this.split);
      for (String code : codes) {
        int count = fsdataDao.batchDelete(code, nowDate, nowDate);
        System.out.println(count);
      }
      int total = codes.length;
      int batch = total/batchsize;
      for (int i = 0; i <= batch; i++) {//新建n个线程
        int start = i * batchsize;
        int end = (i + 1) * batchsize;
        if (end > total) {
          end = total;
        }
        List<String> codeslist = new ArrayList<String>();
        for (int j = start; j < end; j++) {
          codeslist.add(codes[j]);
        }
        String codeListStr = Joiner.on(",").join(codeslist);
        codeslist.clear();
        List<String> params =
            Lists.newArrayList("python", path, codeListStr, nowDate, nowDate);
        String cmdStr = Joiner.on(" ").join(params);
        System.out.println(cmdStr);
        TaskHasThread shellTask = new TaskHasThread(dbBatchSize, cmdStr, fsdataDao, sleepTime);
        shellTask.startNode();//新建线程执行任务
        taskThreads.add(shellTask);
      }
    }


  }

  @Override
  public void stop() {
    for (TaskHasThread taskThread : taskThreads) {
      taskThread.stopNode();
    }
  }
}
