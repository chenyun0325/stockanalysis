package stormpython;

import fsanalysis.FsModel;
import net.sf.json.JSONObject;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import sharejdbc.IfsdataDao;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cy111966 on 2016/12/27.
 */
public class FsPythonShellLoad {

  public static void main(String[] args){
    try {
      //todo 路径作为参数传递进来
      ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:spring.xml");
      IfsdataDao fsdao = ctx.getBean("fsDao", IfsdataDao.class);
      int batchSize =100;
      fsdao.batchDelete("","","");//删除数据
      String path = FsPythonShellLoad.class.getResource("").getPath();
      String absolutePath = new File("").getAbsolutePath();
      System.out.println(absolutePath);
      System.out.println(path);
      String s ="python "+ path + "fsdata_load.py"+" 000798 2016-12-25 2016-12-27";
      System.out.println(s);
      String urlPath = "D:/alibaba-workspace/ideaWorkspace/stockanalysis/src/main/resources/fsdata_load.py";
      String cmd="python fsdata_load.py 000798 2016-12-25 2016-12-27";
      System.out.println(cmd);
      Runtime rt = Runtime.getRuntime();
      String command="python "+urlPath+" 000798 2016-12-25 2016-12-27";
      Process pcs = rt.exec(command);
      InputStream pcsInputStream = pcs.getInputStream();
      InputStream pcsErrorStream = pcs.getErrorStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(pcsInputStream));
      List<FsModel> itemList = new ArrayList<>();
      String line;
      while ((line =br.readLine())!=null){
        System.out.println(line);
        FsModel fsModel = jsonToBean(line);
        itemList.add(fsModel);
        int totalSize = itemList.size();
        if (totalSize % batchSize == 0) {
          fsdao.batchSave(itemList);
          itemList.clear();
        }
      }
      //处理余量
      fsdao.batchSave(itemList);
      BufferedReader brError = new BufferedReader(new InputStreamReader(pcsErrorStream));
      while ((line =brError.readLine())!=null){
        System.out.println(line);
      }
      try {
        pcs.waitFor();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      br.close();
      int ret = pcs.exitValue();
      System.out.println(ret);
      System.out.println("执行完毕");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static FsModel jsonToBean(String json){
    JSONObject jsObj = JSONObject.fromObject(json);
    FsModel item = (FsModel) JSONObject.toBean(jsObj, FsModel.class);
    return item;
  }
}
