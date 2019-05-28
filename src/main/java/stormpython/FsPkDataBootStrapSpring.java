package stormpython;

import fsrealanalysis.FsData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sharejdbc.IfsPkDataDao;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cy111966 on 2017/1/19.
 */
public class FsPkDataBootStrapSpring implements Runnable, INode {
 private static final Logger errorLog = LoggerFactory.getLogger(FsPkDataBootStrapSpring.class);

  //必须 static ?
  private  FsPKQuene<FsData> fspkQuene;//数据来源

  private static Thread dataInputThread;

  private int batchSize;

  private IfsPkDataDao pkDao;

  public void setPkDao(IfsPkDataDao pkDao) {
    this.pkDao = pkDao;
  }

  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  @Override
  public void run() {
    List<FsData> itemList = new ArrayList<>();
    try {
      this.init();
      FsData item;
      while (!Thread.currentThread().isInterrupted()) {
        item = fspkQuene.getQuene().take();//阻塞
        itemList.add(item);
        int size = itemList.size();
        try {
          if (size % batchSize == 0) {
            pkDao.batchSave(itemList);
            itemList.clear();
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

    } catch (InterruptedException e1) {
      errorLog.error("FsPkDataBootStrap InterruptedException", e1);
    } catch (Exception e) {
      errorLog.error("FsPkDataBootStrap unexpect error", e);
    } finally {
      this.resourceRelease();
      pkDao.batchSave(itemList);
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
    System.out.println("______xx____"+fspkQuene);
    //新建task
    dataInputThread = new Thread(this,"data_db_inputThread_0");
    dataInputThread.start();
  }

  @Override
  public void stopNode() {
   dataInputThread.interrupt();
  }
}
