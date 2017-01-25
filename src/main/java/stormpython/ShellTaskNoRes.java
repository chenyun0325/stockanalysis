package stormpython;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by cy111966 on 2017/1/25.
 */
public class ShellTaskNoRes implements Runnable {

  private static final Logger errorLog = LoggerFactory.getLogger(ShellTaskNoRes.class);
  private String cmd;

  public ShellTaskNoRes(String cmd) {
    this.cmd = cmd;
  }

  @Override
  public void run() {
    try {
      Runtime rt = Runtime.getRuntime();
      Process pcs = rt.exec(cmd);
      InputStream pcsInputStream = pcs.getInputStream();
      InputStream pcsErrorStream = pcs.getErrorStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(pcsInputStream));
      String line;
      while ((line = br.readLine()) != null) {
        System.out.println(line);

      }
      BufferedReader brError = new BufferedReader(new InputStreamReader(pcsErrorStream));
      while ((line = brError.readLine()) != null) {
        System.out.println(line);
      }
      try {
        pcs.waitFor();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      br.close();
      int ret = pcs.exitValue();
      if (ret != 0) {
        System.out.println("error");
      }
      System.out.println(ret);
      System.out.println("执行完毕");
    } catch (Exception e) {
      errorLog.error("shellTask Error:",e);
    }
  }
}
