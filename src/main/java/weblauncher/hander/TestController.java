package weblauncher.hander;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;

/**
 * Created by cy111966 on 2017/1/22.
 */
@Controller
public class TestController {

  @RequestMapping("/test.do")
  @ResponseBody
  public Map<String,String> testMap(){
    HashMap<String, String> map = new HashMap<>();
    map.put("x","1");
    map.put("xy","1fd");
    return map;
  }

  @RequestMapping("/query.do")
  public String query(){
    return "fsanalysis_query";
  }

  @RequestMapping("/test1.do")
  public String test(){
    return "test";
  }

  @RequestMapping("/hash.do")
  @ResponseBody
  public Integer keyHash(String key,int mod){
    int hash = newCompatHashingAlg(key);
    int i = hash%mod;
    return i;
  }

  private static int newCompatHashingAlg( String key ) {
    CRC32 checksum = new CRC32();
    checksum.update( key.getBytes() );
    int crc = (int) checksum.getValue();
    return (crc >> 16) & 0x7fff;
  }

}
