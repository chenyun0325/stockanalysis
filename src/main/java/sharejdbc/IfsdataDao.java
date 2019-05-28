package sharejdbc;

import fsanalysis.FsModel;

import java.util.List;

/**
 * Created by cy111966 on 2017/1/15.
 */
public interface IfsdataDao {

  void save(FsModel item);

  int batchDelete(String code,String startDate,String endDate);

  void batchSave(List<FsModel> itemList);

  List<FsModel> query(String code,String startDate,String endDate);

}
