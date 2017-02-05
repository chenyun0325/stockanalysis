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

import fsanalysis.KModel;

/**
 * Created by cy111966 on 2017/1/25.
 */
public class KdataDaoImpl extends JdbcDaoSupport implements IKdataDao {

  String insertSql ="insert ignore into k_st_his_data(code,ktype,date,open,high,close,low,volume,price_change,p_change,ma5,ma10,ma20,v_ma5,v_ma10,v_ma20,turnover)"
                    + "values(:code,:ktype,:date,:open,:high,:close,:low,:volume,:price_change,:p_change,:ma5,:ma10,:ma20,:v_ma5,:v_ma10,:v_ma20,:turnover)";

  String delSql = "delete from k_st_his_data where date >=? and date <=? and code = ? and ktype = ?";
  @Override
  public void save(KModel item) {
    SqlParameterSource[] param =
        SqlParameterSourceUtils.createBatch(Lists.newArrayList(item).toArray());
    JdbcTemplate template = getJdbcTemplate();
    NamedParameterJdbcTemplate nameParaTemplate = new NamedParameterJdbcTemplate(template);
    int[] res = nameParaTemplate.batchUpdate(insertSql, param);
  }

  @Override
  public int batchDelete(String code, String ktype, String startDate, String endDate) {
    int count = getJdbcTemplate().update(delSql, new Object[]{ startDate, endDate,code,ktype});
    return count;
  }

  @Override
  public void batchSave(List<KModel> itemList) {
    SqlParameterSource[] batchParams = SqlParameterSourceUtils.createBatch(itemList.toArray());
    JdbcTemplate template = getJdbcTemplate();
    NamedParameterJdbcTemplate nameParaTemplate = new NamedParameterJdbcTemplate(template);
    int[] res = nameParaTemplate.batchUpdate(insertSql, batchParams);
  }

  @Override
  public List<KModel> query(String code, String ktype, String startDate, String endDate) {
    String sql = "select * from k_st_his_data where date >=? and date <=? and code = ? and ktype = ?";
    return getJdbcTemplate().query(sql,new Object[]{startDate,endDate,code,ktype},new KDataRowMapper());
  }

  public class KDataRowMapper implements RowMapper<KModel>{

    @Override
    public KModel mapRow(ResultSet rs, int i) throws SQLException {
      KModel kModel = new KModel();
      kModel.setCode(rs.getString("code"));
      kModel.setDate(rs.getString("date"));
      kModel.setKtype(rs.getString("ktype"));
      kModel.setOpen(rs.getDouble("open"));
      kModel.setHigh(rs.getDouble("high"));
      kModel.setClose(rs.getDouble("close"));
      kModel.setLow(rs.getDouble("low"));
      kModel.setVolume(rs.getDouble("volume"));
      kModel.setPrice_change(rs.getDouble("price_change"));
      kModel.setP_change(rs.getDouble("p_change"));
      kModel.setMa5(rs.getDouble("ma5"));
      kModel.setMa10(rs.getDouble("ma10"));
      kModel.setMa20(rs.getDouble("ma20"));
      kModel.setV_ma5(rs.getDouble("v_ma5"));
      kModel.setV_ma10(rs.getDouble("v_ma10"));
      kModel.setV_ma20(rs.getDouble("v_ma20"));
      kModel.setTurnover(rs.getDouble("turnover"));
      return kModel;
    }
  }
}
