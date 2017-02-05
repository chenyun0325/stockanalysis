package fsanalysis;

/**
 * Created by cy111966 on 2016/11/25.
 */
public class FsRes {
  private String code;//股票代码
  private String begin;//开始时间
  private long begin_l;
  private String end;//结束时间
  private long end_l;
  private long buy;//买量
  private long sale;//卖量
  private long diff_v;//买卖差
  private long amount_b;//买入金额
  private long amount_s;//卖出金额
  private long amount_diff;//买卖金额差
  private long amount_var;//增量资金

  //累计量计算
  private String begin_c;//开始时间
  private long begin_l_c;
  private String end_c;//结束时间
  private long end_l_c;
  private long buy_c;//买量
  private long sale_c;//卖量
  private long diff_v_c;//买卖差
  private long amount_b_c;//买入金额
  private long amount_s_c;//卖出金额
  private long amount_diff_c;//买卖金额差
  private long amount_var_c;//增量资金

  public long getAmount_b_c() {
    return amount_b_c;
  }

  public void setAmount_b_c(long amount_b_c) {
    this.amount_b_c = amount_b_c;
  }

  public long getAmount_diff_c() {
    return amount_diff_c;
  }

  public void setAmount_diff_c(long amount_diff_c) {
    this.amount_diff_c = amount_diff_c;
  }

  public long getAmount_s_c() {
    return amount_s_c;
  }

  public void setAmount_s_c(long amount_s_c) {
    this.amount_s_c = amount_s_c;
  }

  public long getAmount_var_c() {
    return amount_var_c;
  }

  public void setAmount_var_c(long amount_var_c) {
    this.amount_var_c = amount_var_c;
  }

  public String getBegin_c() {
    return begin_c;
  }

  public void setBegin_c(String begin_c) {
    this.begin_c = begin_c;
  }

  public long getBegin_l_c() {
    return begin_l_c;
  }

  public void setBegin_l_c(long begin_l_c) {
    this.begin_l_c = begin_l_c;
  }

  public long getBuy_c() {
    return buy_c;
  }

  public void setBuy_c(long buy_c) {
    this.buy_c = buy_c;
  }

  public long getDiff_v_c() {
    return diff_v_c;
  }

  public void setDiff_v_c(long diff_v_c) {
    this.diff_v_c = diff_v_c;
  }

  public String getEnd_c() {
    return end_c;
  }

  public void setEnd_c(String end_c) {
    this.end_c = end_c;
  }

  public long getEnd_l_c() {
    return end_l_c;
  }

  public void setEnd_l_c(long end_l_c) {
    this.end_l_c = end_l_c;
  }

  public long getSale_c() {
    return sale_c;
  }

  public void setSale_c(long sale_c) {
    this.sale_c = sale_c;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public long getBegin_l() {
    return begin_l;
  }

  public void setBegin_l(long begin_l) {
    this.begin_l = begin_l;
  }

  public long getEnd_l() {
    return end_l;
  }

  public void setEnd_l(long end_l) {
    this.end_l = end_l;
  }

  public long getAmount_b() {
    return amount_b;
  }

  public void setAmount_b(long amount_b) {
    this.amount_b = amount_b;
  }

  public long getAmount_diff() {
    return amount_diff;
  }

  public void setAmount_diff(long amount_diff) {
    this.amount_diff = amount_diff;
  }

  public long getAmount_s() {
    return amount_s;
  }

  public void setAmount_s(long amount_s) {
    this.amount_s = amount_s;
  }

  public long getAmount_var() {
    return amount_var;
  }

  public void setAmount_var(long amount_var) {
    this.amount_var = amount_var;
  }

  public String getBegin() {
    return begin;
  }

  public void setBegin(String begin) {
    this.begin = begin;
  }

  public long getBuy() {
    return buy;
  }

  public void setBuy(long buy) {
    this.buy = buy;
  }

  public long getDiff_v() {
    return diff_v;
  }

  public void setDiff_v(long diff_v) {
    this.diff_v = diff_v;
  }

  public String getEnd() {
    return end;
  }

  public void setEnd(String end) {
    this.end = end;
  }

  public long getSale() {
    return sale;
  }

  public void setSale(long sale) {
    this.sale = sale;
  }

  @Override
  public String toString() {
    return code+","+begin+","+end+","+buy+","+sale+","+diff_v+","+amount_b+","+amount_s+","+amount_diff+","+amount_var;
  }
}
