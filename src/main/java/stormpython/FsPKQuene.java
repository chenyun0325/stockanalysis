package stormpython;

import net.sf.json.JSONObject;

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
    System.err.println(JSONObject.fromObject(item).toString());
    System.out.println("xxxxxxxxxxxxxxxxxx_yyyyyyyyyy");
    quene.add(item);
  }
  public BlockingQueue<T> getQuene(){
    return quene;
  }



}
