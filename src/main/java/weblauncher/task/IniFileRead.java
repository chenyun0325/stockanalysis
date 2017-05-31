package weblauncher.task;


import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenyun on 2017/5/26.
 */
public class IniFileRead implements ITask {


    private static final Logger errorLog = LoggerFactory.getLogger(IniFileRead.class);

    private String fileName;

    private String charSet = "utf8";

    private static Map<String,List<String>> selectionMap = new HashMap<>();//static 存储到方法区---多个实例共享

    private static IniFileRead instance;

    private String bkNames;


    private final String split = ",";

    private final String keySplit = "=";

    private final String zxlr = "D7DD";

    private final String blocks = "BLOCK_NAME_MAP_TABLE";

    private final String BLOCK_STOCK_CONTEXT = "BLOCK_STOCK_CONTEXT";

    private Map<String, String> block_name_maps = new HashMap<>();

    private Map<String, String> block_context_maps = new HashMap<>();

    private final String allBlock="all";

    private IniFileRead() {
    }

    public static IniFileRead getInstance(){

        if (instance==null) {
            synchronized (IniFileRead.class){
                if (instance ==null) {
                    instance = new IniFileRead();
                }
            }
        }
        return instance;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getBkNames() {
        return bkNames;
    }

    public void setBkNames(String bkNames) {
        this.bkNames = bkNames;
    }

    public void setCharSet(String charSet) {
        this.charSet = charSet;
    }

    public List<String> getSelectionItems(String selection){

       return selectionMap.get(selection);
    }

    @Override
    public void start()  {
        File iniFile = new File(fileName);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(iniFile),charSet));
            String line;
            String curSelection = "";
            while ((line = reader.readLine()) != null) {//循环读取
                line = line.trim();
                if ("".equals(line)) continue;
                if (line.startsWith("[")&&line.endsWith("]")) {
                    curSelection = line.substring(1,line.length()-1);
                   List<String> selItems = new ArrayList<>();
                   selectionMap.put(curSelection,selItems);
                }else {
                    List<String> selItems = selectionMap.get(curSelection);
                    if (selItems!=null) {
                        selItems.add(line);
                    }
                }
            }
            System.out.println("--------------------");
        } catch (Exception e) {
            errorLog.error(e.getMessage());
        }finally {
            if (reader!=null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    errorLog.error(e.getMessage());
                }
            }
        }

    }

    @Override
    public void stop() {

    }

    public Map<String, List<String>> getSelectionMap() {
        return selectionMap;
    }


    /**
     *
     * @param bkNames
     * @return
     */
    public List<String> getCodesByBlocks(String bkNames){

        List<String> block_name_map_table = getSelectionItems(blocks);
        for (String block_name_map : block_name_map_table) {
            String[] keyValue = block_name_map.split(keySplit);
            block_name_maps.put(keyValue[1], keyValue[0]);
            block_name_maps.put("最近浏览", zxlr);
        }
        ;
        List<String> selectionItems = getSelectionItems(BLOCK_STOCK_CONTEXT);
        for (String selectionItem : selectionItems) {
            String[] keyValue = selectionItem.split(keySplit);
            block_context_maps.put(keyValue[0], keyValue[1]);
        }

        String[] bkNameArray = bkNames.split(this.split);

        if (allBlock.equals(bkNames)) {
            Lists.newArrayList(block_name_maps.keySet()).toArray(bkNameArray);
        }
        StringBuffer stockCodeBuffer = new StringBuffer();
        for (String bkName : bkNameArray) {
            String bkCode = block_name_maps.get(bkName);
            String bkContext = block_context_maps.get(bkCode);
            stockCodeBuffer.append(bkContext);
        }
        String stockCodeStr = stockCodeBuffer.toString();

        String[] codesOrignals = stockCodeStr.split(this.split);

        List<String> codes = new ArrayList<>();
        for (String codeOrignal : codesOrignals) {
            String[] keyValue = codeOrignal.split(":");
            codes.add(keyValue[1]);
        }
        return codes;
    }
}
