package sharejdbc;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import fsanalysis.DateUtil;
import fsanalysis.FsModel;

/**
 * Created by cy111966 on 2017/1/15.
 */
public class FsDataDaoImpl extends JdbcDaoSupport implements IfsdataDao {

  String insertSql = "insert into fs_st_his_data_v(code,date,time,price,`change`,volume,amount,type)values(?,?,?,?,?,?,?,?)";

  String delSql = "delete from fs_st_his_data_v where code =? and date>=? and date<=? ";

  @Override
  public void save(final FsModel item) {
//    getJdbcTemplate().update(insertSql, item.getCode(), item.getDate(), item.getTime(), item.getPrice(),
//                item.getChange(), item.getVolume(), item.getAmount(), item.getType());
    getJdbcTemplate().update(insertSql, new PreparedStatementSetter() {
      @Override
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, item.getCode());
        ps.setString(2, item.getDate());
        ps.setString(3, item.getTime());
        ps.setDouble(4, item.getPrice());
        ps.setString(5, item.getChange());
        ps.setLong(6, item.getVolume());
        ps.setLong(7, item.getAmount());
        ps.setString(8, item.getType());
        System.out.println(ps.toString());
      }
    });
  }

  @Override
  public int batchDelete(String code, String startDate, String endDate) {
    int count = getJdbcTemplate().update(delSql, new Object[]{code, startDate, endDate});
    return count;
  }

  @Override
  public void batchSave(final List<FsModel> itemList) {
    getJdbcTemplate().batchUpdate(insertSql, new BatchPreparedStatementSetter() {
      @Override
      public void setValues(PreparedStatement ps, int i) throws SQLException {
        ps.setString(1, itemList.get(i).getCode());
        ps.setString(2, itemList.get(i).getDate());
        ps.setString(3, itemList.get(i).getTime());
        ps.setDouble(4, itemList.get(i).getPrice());
        ps.setString(5, itemList.get(i).getChange());
        ps.setLong(6, itemList.get(i).getVolume());
        ps.setLong(7, itemList.get(i).getAmount());
        ps.setString(8, itemList.get(i).getType());
        System.err.println(ps.toString());
      }

      @Override
      public int getBatchSize() {
        return itemList.size();
      }
    });
  }

  @Override
  public List<FsModel> query(String code, String startDate, String endDate) {
    String sql = "select * from fs_st_his_data_v where date >=? and date <=? and code = ?";
    return getJdbcTemplate()
        .query(sql, new Object[]{startDate, endDate, code}, new FsDataRowMapper());
  }

  public class FsDataRowMapper implements RowMapper<FsModel> {

    @Override
    public FsModel mapRow(ResultSet rs, int i) throws SQLException {
      String code = rs.getString(2);
      String date = rs.getString(3);
      String time = rs.getString(4);
      double price = rs.getDouble(5);
      String change = rs.getString(6);
      long volume = rs.getLong(7);
      long amount = rs.getLong(8);
      String type = rs.getString(9);
      String dateTime = date + " " + time;
      long time_long = DateUtil.convert2long(dateTime, DateUtil.TIME_FORMAT);
      FsModel model = new FsModel();
      model.setCode(code);
      model.setDate(date);
      model.setTime(time);
      model.setPrice(price);
      model.setChange(change);
      model.setVolume(volume);
      model.setAmount(amount);
      model.setType(type);
      model.setDateTime(time_long);
      return model;
    }
  }
}
