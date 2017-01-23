package weblauncher.hander;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

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

}
