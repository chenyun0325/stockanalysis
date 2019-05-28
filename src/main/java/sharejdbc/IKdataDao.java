package sharejdbc;

import fsanalysis.KModel;

import java.util.List;

/**
 * Created by cy111966 on 2017/1/25.
 */
public interface IKdataDao {

  void save(KModel item);

  int batchDelete(String code,String ktype,String startDate,String endDate);

  void batchSave(List<KModel> itemList);

  List<KModel> query(String code,String ktype,String startDate,String endDate);

}
