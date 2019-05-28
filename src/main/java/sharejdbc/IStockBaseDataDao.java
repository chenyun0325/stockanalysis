package sharejdbc;

import fsanalysis.StockBasics;

import java.util.List;

/**
 * Created by cy111966 on 2017/1/25.
 */
public interface IStockBaseDataDao {


  List<StockBasics> queryAll();

  List<String> queryAllCode();

  StockBasics queryByCode(String code);

}
