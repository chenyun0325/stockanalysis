package weblauncher.task;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.List;

import stormpython.ShellTaskNoRes;

/**
 * Created by cy111966 on 2017/1/25.
 */
public class ShellTaskNoResQuartzTask implements ITask {
  private static final Logger errorLog = LoggerFactory.getLogger(ShellTaskNoResQuartzTask.class);

  private String shellFile;
  private String args;

  public void setArgs(String args) {
    this.args = args;
  }

  public void setShellFile(String shellFile) {
    this.shellFile = shellFile;
  }

  @Override
  public void start() {
    try {
      ProtectionDomain protectionDomain = ShellTaskNoResQuartzTask.class.getProtectionDomain();
      URL codeLoc = protectionDomain.getCodeSource().getLocation();
      String dir = new File(codeLoc.getFile()).getAbsolutePath();
      String path = dir + "\\" + shellFile;
      List<String> params =
          Lists.newArrayList("python", path, args);
      String cmdStr = Joiner.on(" ").join(params);
      ShellTaskNoRes shellTask = new ShellTaskNoRes(cmdStr);
      shellTask.run();
    } catch (Exception e) {
      errorLog.error("ShellTaskNoResQuartzTask error:",e);
    }
  }

  @Override
  public void stop() {

  }
}
