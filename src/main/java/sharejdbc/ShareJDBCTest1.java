package sharejdbc;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

import fsanalysis.FsModel;
import fsrealanalysis.FsData;
import stormpython.FsPkDataBootStrapSpring;

/**
 * Created by cy111966 on 2017/1/15.
 */
public class ShareJDBCTest1 {

  public static void main(String[] args) {
    try {
      ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:spring.xml");
      IfsPkDataDao fsdao = ctx.getBean("fsPkDao", IfsPkDataDao.class);
      List<FsData> fsList = new ArrayList<>();
      for (int i=3500;i<4500;i++){
        FsData item = new FsData();
        item.setName("xxxxxx"+i);
        item.setB1_v(1);
        fsList.add(item);
      }
      fsdao.batchSave(fsList);
//      FsPkDataBootStrapSpring fsPkDbThread = ctx.getBean("fsPkDbThread", FsPkDataBootStrapSpring.class);
//      fsPkDbThread.stopNode();
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
