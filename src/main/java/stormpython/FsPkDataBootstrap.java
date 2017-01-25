package stormpython;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fsrealanalysis.FsData;

/**
 * Created by cy111966 on 2017/1/19.
 */
public class FsPkDataBootStrap implements Runnable, INode {
 private static final Logger errorLog = LoggerFactory.getLogger(FsPkDataBootStrap.class);

  private static FsPKQuene<FsData> fspkQuene;//数据来源

  private static volatile FsPkDataBootStrap instance;

  private static Thread dataInputThread;

  public static FsPkDataBootStrap getInstance(){
    if (instance == null) {
      synchronized (FsPkDataBootStrap.class){
        if (instance == null) {
          instance = new FsPkDataBootStrap();
        }
      }
    }
    return instance;
  }
  @Override
  public void run() {
    try {
      this.init();
      FsData item;
      while (!Thread.currentThread().isInterrupted()) {
        item = fspkQuene.getQuene().take();//阻塞
      }

    } catch (InterruptedException e1) {
      errorLog.error("FsPkDataBootStrap InterruptedException", e1);
    } catch (Exception e) {
      errorLog.error("FsPkDataBootStrap unexpect error", e);
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
    fspkQuene = FsPKQuene.getInstance();
    //新建task
    dataInputThread = new Thread(this,"data_db_inputThread_0");
    dataInputThread.start();
  }

  @Override
  public void stopNode() {
   dataInputThread.interrupt();
  }
}
