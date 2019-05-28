package sharejdbc;

import fsanalysis.FsModel;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import stormpython.FsPkDataBootStrapSpring;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cy111966 on 2017/1/15.
 */
public class ShareJDBCTest {

  public static void main(String[] args) {
    try {
      ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:spring.xml");
      IfsdataDao fsdao = ctx.getBean("fsDao", IfsdataDao.class);
      List<FsModel> fsList = new ArrayList<>();
      for (int i=350;i<450;i++){
        FsModel item = new FsModel();
        item.setCode("stock"+i);
        item.setChange("change"+i);
        item.setVolume(i);
        item.setAmount(i);
        fsList.add(item);
      }
      fsdao.batchSave(fsList);
      FsPkDataBootStrapSpring fsPkDbThread = ctx.getBean("fsPkDbThread", FsPkDataBootStrapSpring.class);
      fsPkDbThread.stopNode();
//      FsModel item = new FsModel();
//      item.setCode("stock"+1);
//      item.setChange("change"+1);
//      item.setPrice(1);
//      item.setVolume(2);
//      item.setAmount(3);
//      fsdao.save(item);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

}
