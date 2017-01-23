package weblauncher.hander;

/**
 * Created by cy111966 on 2017/1/22.
 */
public class StockStormQuery {

  private Double filter_mount;
  private Double filter_per;
  private Integer slide_size;
  private Integer max_size;
  private Integer wind_size;
  private Double price_dif_var;
  private Double amount;
  private Double price_dif_var1;
  private Double amount1;
  private String topology;

  public String getTopology() {
    return topology;
  }

  public void setTopology(String topology) {
    this.topology = topology;
  }

  public Double getAmount1() {
    return amount1;
  }

  public void setAmount1(Double amount1) {
    this.amount1 = amount1;
  }

  public Double getAmount() {
    return amount;
  }

  public void setAmount(Double amount) {
    this.amount = amount;
  }

  public Double getFilter_mount() {
    return filter_mount;
  }

  public void setFilter_mount(Double filter_mount) {
    this.filter_mount = filter_mount;
  }

  public Double getFilter_per() {
    return filter_per;
  }

  public void setFilter_per(Double filter_per) {
    this.filter_per = filter_per;
  }

  public Integer getMax_size() {
    return max_size;
  }

  public void setMax_size(Integer max_size) {
    this.max_size = max_size;
  }

  public Double getPrice_dif_var1() {
    return price_dif_var1;
  }

  public void setPrice_dif_var1(Double price_dif_var1) {
    this.price_dif_var1 = price_dif_var1;
  }

  public Double getPrice_dif_var() {
    return price_dif_var;
  }

  public void setPrice_dif_var(Double price_dif_var) {
    this.price_dif_var = price_dif_var;
  }

  public Integer getSlide_size() {
    return slide_size;
  }

  public void setSlide_size(Integer slide_size) {
    this.slide_size = slide_size;
  }

  public Integer getWind_size() {
    return wind_size;
  }

  public void setWind_size(Integer wind_size) {
    this.wind_size = wind_size;
  }
}
