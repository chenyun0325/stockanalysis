package stormpython;


import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by cy111966 on 2017/1/19.
 */
public class FsPkQueue<T> implements Serializable{

  private BlockingQueue<T> quene;

  private static FsPkQueue instance;

  private FsPkQueue(){
    if (quene == null) {
      quene = new LinkedBlockingQueue<>();
    }
  }
  public static FsPkQueue getInstance(){
    if (instance == null) {
      synchronized (FsPkQueue.class){
        if (instance == null) {
          instance = new FsPkQueue();
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
