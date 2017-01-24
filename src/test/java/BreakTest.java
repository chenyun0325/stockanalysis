/**
 * Created by cy111966 on 2017/1/24.
 */
public class BreakTest {

  public static void main(String[] args) {
    try {
      while (!Thread.currentThread().isInterrupted()) {
        try {
          int i =1/0;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

}
