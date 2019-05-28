package sharejdbc;

import fsanalysis.StockBasics;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by cy111966 on 2017/1/25.
 */
public class StockBaseDataDaoImpl extends JdbcDaoSupport implements IStockBaseDataDao {


  @Override
  public List<StockBasics> queryAll() {
    String querySqlAll ="select * from stock_basics ";
    return getJdbcTemplate().query(querySqlAll,new StockBasicRowMapper());
  }

  @Override
  public List<String> queryAllCode() {
    String querySqlCodeAll ="select code from stock_basics ";
    return getJdbcTemplate().query(querySqlCodeAll,new StockBasicCodeRowMapper());
  }

  @Override
  public StockBasics queryByCode(String code) {
    String querySql ="select * from stock_basics where code = ?";
    return getJdbcTemplate().query(querySql,new Object[]{code},new StockBasicRowMapper()).get(0);
  }

  public class StockBasicRowMapper implements RowMapper<StockBasics>{

    @Override
    public StockBasics mapRow(ResultSet rs, int i) throws SQLException {
      StockBasics item = new StockBasics();
      item.setCode(rs.getString(1));
      item.setName(rs.getString(2));
      item.setIndustry(rs.getString(3));
      item.setArea(rs.getString(4));
      item.setPb(rs.getDouble(5));
      item.setOutstanding(rs.getDouble(6));
      item.setTotals(rs.getDouble(7));
      item.setTotalassets(rs.getDouble(8));
      item.setLiquidassets(rs.getDouble(9));
      item.setFixedassets(rs.getDouble(10));
      item.setReserved(rs.getDouble(11));
      item.setReservedpershare(rs.getDouble(12));
      item.setEsp(rs.getDouble(13));
      item.setBvps(rs.getDouble(14));
      item.setPb(rs.getDouble(15));
      item.setTimetomarket(rs.getLong(16));
      item.setUndp(rs.getDouble(17));
      item.setPerundp(rs.getDouble(18));
      item.setRev(rs.getDouble(19));
      item.setProfit(rs.getDouble(20));
      item.setGpr(rs.getDouble(21));
      item.setNpr(rs.getDouble(22));
      item.setHolders(rs.getLong(23));
      return item;
    }
  }

  public class StockBasicCodeRowMapper implements RowMapper<String>{

    @Override
    public String mapRow(ResultSet rs, int i) throws SQLException {
      return rs.getString("code");
    }
  }
}
