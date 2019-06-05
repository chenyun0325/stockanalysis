package stormpython;


import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by cy111966 on 2017/1/19.
 */
public class FsPKQuene<T> implements Serializable{

  private BlockingQueue<T> quene;

  private static FsPKQuene instance;

  private FsPKQuene(){
    if (quene == null) {
      quene = new LinkedBlockingQueue<>();
    }
  }
  public static FsPKQuene getInstance(){
    if (instance == null) {
      synchronized (FsPKQuene.class){
        if (instance == null) {
          instance = new FsPKQuene();
        }
      }
    }
    return instance;
  }

  public void put(T item){
    quene.add(item);
  }
  public BlockingQueue<T> getQuene(){
    return quene;
  }



}
