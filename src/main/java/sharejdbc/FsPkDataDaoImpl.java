package sharejdbc;

import com.google.common.collect.Lists;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import fsrealanalysis.FsData;

/**
 * Created by cy111966 on 2017/1/15.
 */
public class FsPkDataDaoImpl extends JdbcDaoSupport implements IfsPkDataDao {

  String insertSql =
      "insert ignore into fs_st_pk_data_v(timestamp,date,time,code,name,open,pre_close,price,high,low,bid,ask,volume,amount,b1_v,b1_p,b2_v,b2_p,b3_v,b3_p,b4_v,b4_p,b5_v,b5_p) values(:timestamp,:date,:time,:code,:name,:open,:pre_close,:price,:high,:low,:bid,:ask,:volume,:amount,:b1_v,:b1_p,:b2_v,:b2_p,:b3_v,:b3_p,:b4_v,:b4_p,:b5_v,:b5_p)";


  @Override
  public void save(FsData item) {
    SqlParameterSource[] param =
        SqlParameterSourceUtils.createBatch(Lists.newArrayList(item).toArray());
    JdbcTemplate template = getJdbcTemplate();
    NamedParameterJdbcTemplate nameParaTemplate = new NamedParameterJdbcTemplate(template);
    int[] res = nameParaTemplate.batchUpdate(insertSql, param);
  }

  @Override
  public void batchSave(List<FsData> itemList) {
    SqlParameterSource[] batchParams = SqlParameterSourceUtils.createBatch(itemList.toArray());
    JdbcTemplate template = getJdbcTemplate();
    NamedParameterJdbcTemplate nameParaTemplate = new NamedParameterJdbcTemplate(template);
    int[] res = nameParaTemplate.batchUpdate(insertSql, batchParams);
  }

  @Override
  public List<FsData> query(String code, String startDate, String endDate) {
    String sql = "select * from fs_st_pk_data_v where date >=? and date <=? and code = ?";
    return getJdbcTemplate()
        .query(sql, new Object[]{startDate, endDate, code}, new FsPkDataRowMapper());
  }

  public class FsPkDataRowMapper implements RowMapper<FsData> {

    @Override
    public FsData mapRow(ResultSet rs, int i) throws SQLException {
      FsData fsData = new FsData();
      fsData.setTimestamp(rs.getLong("timestamp"));
      fsData.setDate(rs.getString("date"));
      fsData.setTime(rs.getString("time"));
      fsData.setCode(rs.getString("code"));
      fsData.setName(rs.getString("name"));
      fsData.setOpen(rs.getDouble("open"));
      fsData.setPre_close(rs.getDouble("pre_close"));
      fsData.setPrice(rs.getDouble("price"));
      fsData.setHigh(rs.getDouble("high"));
      fsData.setLow(rs.getDouble("low"));
      fsData.setBid(rs.getDouble("bid"));
      fsData.setAsk(rs.getDouble("ask"));
      fsData.setVolume(rs.getLong("volume"));
      fsData.setAmount(rs.getDouble("amout"));
      fsData.setB1_v(rs.getInt("b1_v"));
      fsData.setB1_p(rs.getDouble("b1_p"));
      fsData.setB2_v(rs.getInt("b2_v"));
      fsData.setB2_p(rs.getDouble("b2_p"));
      fsData.setB3_v(rs.getInt("b3_v"));
      fsData.setB3_p(rs.getDouble("b3_p"));
      fsData.setB4_v(rs.getInt("b4_v"));
      fsData.setB4_p(rs.getDouble("b4_p"));
      fsData.setB5_v(rs.getInt("b5_v"));
      fsData.setB5_p(rs.getDouble("b5_p"));
      return fsData;
    }
  }
}
