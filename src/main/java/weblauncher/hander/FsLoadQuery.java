package weblauncher.hander;

/**
 * Created by cy111966 on 2017/1/23.
 */
public class FsLoadQuery {
  private String stockList;
  private String beginDate;
  private String endDate;
  private String shellFile;
  private Integer batchSize;

  public Integer getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(Integer batchSize) {
    this.batchSize = batchSize;
  }

  public String getBeginDate() {
    return beginDate;
  }

  public void setBeginDate(String beginDate) {
    this.beginDate = beginDate;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  public String getShellFile() {
    return shellFile;
  }

  public void setShellFile(String shellFile) {
    this.shellFile = shellFile;
  }

  public String getStockList() {
    return stockList;
  }

  public void setStockList(String stockList) {
    this.stockList = stockList;
  }
}
