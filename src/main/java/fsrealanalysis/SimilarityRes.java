package fsrealanalysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenyun on 2019/5/28.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimilarityRes implements Serializable {

    private String stock;

    /**
     * key:index_offset
     */
    private Map<String,Double> similarityMap = new HashMap<>();


    private Map<String,List<Double>> trendMap = new HashMap<>();


    /**
     * value:v1_v2_vN组成时间窗口
     */
    private Map<String,String> similarityWindMap = new HashMap<>();


    private Map<String,List<String>> trendWindMap = new HashMap<>();
}
