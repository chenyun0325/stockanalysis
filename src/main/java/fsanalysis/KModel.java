package fsanalysis;

import net.sf.json.JSONObject;

/**
 * Created by cy111966 on 2017/1/25.
 */
public class KModel {

  private String code;
  private String date;
  private String ktype;
  private double open;
  private double high;
  private double close;
  private double low;
  private double volume;
  private double price_change;
  private double p_change;
  private double ma5;
  private double ma10;
  private double ma20;
  private double v_ma5;
  private double v_ma10;
  private double v_ma20;
  private double turnover;

  public double getClose() {
    return close;
  }

  public void setClose(double close) {
    this.close = close;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public double getHigh() {
    return high;
  }

  public void setHigh(double high) {
    this.high = high;
  }

  public String getKtype() {
    return ktype;
  }

  public void setKtype(String ktype) {
    this.ktype = ktype;
  }

  public double getLow() {
    return low;
  }

  public void setLow(double low) {
    this.low = low;
  }

  public double getMa10() {
    return ma10;
  }

  public void setMa10(double ma10) {
    this.ma10 = ma10;
  }

  public double getMa20() {
    return ma20;
  }

  public void setMa20(double ma20) {
    this.ma20 = ma20;
  }

  public double getMa5() {
    return ma5;
  }

  public void setMa5(double ma5) {
    this.ma5 = ma5;
  }

  public double getOpen() {
    return open;
  }

  public void setOpen(double open) {
    this.open = open;
  }

  public double getP_change() {
    return p_change;
  }

  public void setP_change(double p_change) {
    this.p_change = p_change;
  }

  public double getPrice_change() {
    return price_change;
  }

  public void setPrice_change(double price_change) {
    this.price_change = price_change;
  }

  public double getTurnover() {
    return turnover;
  }

  public void setTurnover(double turnover) {
    this.turnover = turnover;
  }

  public double getV_ma10() {
    return v_ma10;
  }

  public void setV_ma10(double v_ma10) {
    this.v_ma10 = v_ma10;
  }

  public double getV_ma20() {
    return v_ma20;
  }

  public void setV_ma20(double v_ma20) {
    this.v_ma20 = v_ma20;
  }

  public double getV_ma5() {
    return v_ma5;
  }

  public void setV_ma5(double v_ma5) {
    this.v_ma5 = v_ma5;
  }

  public double getVolume() {
    return volume;
  }

  public void setVolume(double volume) {
    this.volume = volume;
  }

  public static KModel jsonToBean(String json){
    JSONObject jsObj = JSONObject.fromObject(json);
    KModel item = (KModel) JSONObject.toBean(jsObj, KModel.class);
    return item;
  }
}
