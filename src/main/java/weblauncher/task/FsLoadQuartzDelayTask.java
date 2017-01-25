package weblauncher.task;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import org.apache.commons.lang.time.DateUtils;

import java.io.File;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fsanalysis.DateUtil;
import sharejdbc.IfsdataDao;
import stormpython.TaskNoThread;

/**
 * Created by cy111966 on 2017/1/24.
 * 当日分时数据 处理
 */
public class FsLoadQuartzDelayTask implements ITask {

  private String stockList;

  private String shellFile;

  private int batchsize;

  private int dbBatchSize;

  private long timeout;

  private int period;

  private IfsdataDao fsdataDao;

  public void setPeriod(int period) {
    this.period = period;
  }

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

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  public void setStockList(String stockList) {
    this.stockList = stockList;
  }

  private final String split=",";

  private static List<TaskNoThread> taskThreads = new ArrayList<>();

  @Override
  public void start() {
    //控制调度频次
    Date now = new Date();
    String nowDate = DateUtil.convert2FdateStr(now);
    Date begin = DateUtils.addDays(now, -period);
    String beginDate = DateUtil.convert2FdateStr(begin);
    //脚本路径
    ProtectionDomain protectionDomain = FsLoadQuartzDelayTask.class.getProtectionDomain();
    URL codeLoc = protectionDomain.getCodeSource().getLocation();
    String dir = new File(codeLoc.getFile()).getAbsolutePath();
    String path = dir + "\\" + shellFile;

    if (stockList != null) {
      //删除数据
      String[] codes = stockList.split(this.split);
      for (String code : codes) {
        //int count = fsdataDao.batchDelete(code, beginDate, nowDate);
        //System.out.println(count);
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
            Lists.newArrayList("python", path, codeListStr, beginDate, nowDate);
        String cmdStr = Joiner.on(" ").join(params);
        System.out.println(cmdStr);
        TaskNoThread shellTask = new TaskNoThread(dbBatchSize, cmdStr, fsdataDao, timeout);
        shellTask.startNode();//直接执行task,线程阻塞
        taskThreads.add(shellTask);
      }
    }


  }

  @Override
  public void stop() {//定时任务外部调用解除io阻塞
    for (TaskNoThread taskThread : taskThreads) {
      taskThread.stopNode();
    }
  }
}
