package weblauncher.hander;

import fsanalysis.Unit;

/**
 * Created by cy111966 on 2017/2/4.
 */
public class FsanalysisQuery {

  String stockcode;
  String beginDate;
  String beginTime;
  String endDate;
  String endTime;
  int period;
  Unit unit;
  long bVolume;
  long sVolume;

  long zl_volume_all;

  public long getZl_volume_all() {
    return zl_volume_all;
  }

  public void setZl_volume_all(long zl_volume_all) {
    this.zl_volume_all = zl_volume_all;
  }

  public Unit getUnit() {
    return unit;
  }

  public void setUnit(Unit unit) {
    this.unit = unit;
  }

  public String getBeginDate() {
    return beginDate;
  }

  public void setBeginDate(String beginDate) {
    this.beginDate = beginDate;
  }

  public String getBeginTime() {
    return beginTime;
  }

  public void setBeginTime(String beginTime) {
    this.beginTime = beginTime;
  }

  public long getbVolume() {
    return bVolume;
  }

  public void setbVolume(long bVolume) {
    this.bVolume = bVolume;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  public int getPeriod() {
    return period;
  }

  public void setPeriod(int period) {
    this.period = period;
  }

  public String getStockcode() {
    return stockcode;
  }

  public void setStockcode(String stockcode) {
    this.stockcode = stockcode;
  }

  public long getsVolume() {
    return sVolume;
  }

  public void setsVolume(long sVolume) {
    this.sVolume = sVolume;
  }
}
