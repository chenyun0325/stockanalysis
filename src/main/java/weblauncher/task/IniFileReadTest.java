package weblauncher.task;

import java.io.File;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Map;

/**
 * Created by chenyun on 2017/5/26.
 */
public class IniFileReadTest {

    public static void main(String[] args) {
        ProtectionDomain protectionDomain = IniFileReadTest.class.getProtectionDomain();
        URL codeLoc = protectionDomain.getCodeSource().getLocation();
        String dir = new File(codeLoc.getFile()).getAbsolutePath();
        String path = dir + System.getProperty("file.separator") + "StockBlock2.ini";
        System.out.println(path);
        IniFileRead instance = IniFileRead.getInstance().getInstance();
        instance.setFileName(path);
        instance.start();
        List<String> block_name_map_table = instance.getSelectionItems("BLOCK_NAME_MAP_TABLE");
        for (String s : block_name_map_table) {
            System.out.println(s);
        }
        Map<String, List<String>> selectionMap = instance.getSelectionMap();
        List<String> all = instance.getCodesByBlocks("all");
        System.out.println(all);


    }
}
