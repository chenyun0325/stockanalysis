package weblauncher;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import java.net.URL;
import java.security.ProtectionDomain;

import weblauncher.hander.HelloServlet;

/**
 * Created by cy111966 on 2017/1/22.
 */
public class LauncherJar {

  public static final int PORT = 80;
  public static final String CONTEXT = "/";
  private static final String DEFAULT_WEBAPP_PATH = "src/main/webapp";
  /**
   * 创建用于开发运行调试的Jetty Server, 以src/main/webapp为Web应用目录.
   */
  public static Server createServerInSource(int port, String contextPath) {
    Server server = new Server();
    // 设置在JVM退出时关闭Jetty的钩子。
    server.setStopAtShutdown(true);

    //这是http的连接器
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(port);
    // 解决Windows下重复启动Jetty居然不报告端口冲突的问题.
    connector.setReuseAddress(false);
    server.setConnectors(new Connector[] {connector });

    ServletContextHandler servletHandler =
        new ServletContextHandler(ServletContextHandler.SESSIONS);
    servletHandler.setContextPath("/ct1");
    servletHandler.addServlet(new ServletHolder(new HelloServlet("test")),"/it/*");
    //webapp
    WebAppContext webContext = new WebAppContext();
    webContext.setContextPath("/ct2");
    ProtectionDomain protectionDomain = LauncherJar.class.getProtectionDomain();
    URL loc = protectionDomain.getCodeSource().getLocation();
    System.out.println(loc.toExternalForm());
    webContext.setWar(loc.toExternalForm());
//    webContext.setDescriptor("src/main/webapp/WEB-INF/web.xml");
//    webContext.setResourceBase(DEFAULT_WEBAPP_PATH);
//    webContext.setClassLoader(Thread.currentThread().getContextClassLoader());
    ContextHandlerCollection contexts = new ContextHandlerCollection();
    contexts.setHandlers(new Handler[]{webContext,servletHandler});
    server.setHandler(contexts);
    return server;
  }

  /**
   * 启动jetty服务
   * @param port
   * @param context
   */
  public void startJetty(int port,String context){
    final Server server = LauncherJar.createServerInSource(PORT, CONTEXT);
    try {
      server.stop();
      server.start();
      server.join();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }
  public static void main(String[] args) {
    new LauncherJar().startJetty(80, "");
  }

}
