import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by cy111966 on 2016/12/21.
 */
public class MainTest {

  public static void main(String[] args) {

    String path = System.getProperty("file.separator");
    System.out.println(path);
    FileWriter fw = null;
    BufferedWriter bfw = null;
    try {
      fw = new FileWriter("/Users/chenyun/stockData/xyz.tttttt", true);
      bfw = new BufferedWriter(fw);
      Long time = System.currentTimeMillis();
      bfw.write(time.toString());
      bfw.flush();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        bfw.close();
        fw.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
