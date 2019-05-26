package datacrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by cy111966 on 2016/11/15.
 */
public class HtmlLoad {
  static Logger log = LoggerFactory.getLogger("logfile");
  static Logger log_error = LoggerFactory.getLogger("errorfile");
public static final String dir ="/Users/chenyun/stockData/holders";
  public static void main(String[] args) throws InterruptedException {
    String[] codes = Constant.stock_all.split(",");
    String path="http://basic.10jqka.com.cn/32/${code}/holder.html";
    for (String code : codes) {
      String url = path.replace("${code}",code);
      System.out.println(url);
      load(url,dir,code);
      Thread.sleep(1000);
    }
  }


  /**
   * 下载文件
   * @param url
   * @param outpath
   * @param name
   */
  public static void load(String url,String outpath,String name){
    try {
      Document doc = Jsoup.connect(url).timeout(10000).userAgent("Mozilla").get();
      System.out.println(doc.html());
      String fullName = outpath+"/"+name+".html";
      System.out.println(fullName);
      FileOutputStream fos = new FileOutputStream(fullName);
      OutputStreamWriter osw = new OutputStreamWriter(fos,"utf-8");
      osw.write(doc.html());
      osw.close();
    } catch (IOException e) {
      log_error.error("load",e);
      log.error("url:{},dir:{},code:{}", Arrays.asList(url,outpath,name).toArray(new String[3]));

    }

  }

}
