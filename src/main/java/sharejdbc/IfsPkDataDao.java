package sharejdbc;

import java.util.List;

import fsrealanalysis.FsData;

/**
 * Created by cy111966 on 2017/1/15.
 */
public interface IfsPkDataDao {

  void save(FsData item);

  void batchSave(List<FsData> itemList);

  List<FsData> query(String code,String startDate,String endDate);

}
