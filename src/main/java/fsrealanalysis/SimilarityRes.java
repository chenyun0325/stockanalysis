package fsrealanalysis;

import lombok.Builder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenyun on 2019/5/28.
 */
@Builder
public class SimilarityRes implements Serializable {

    private String stock;


    /**
     * key:index_start_end
     */
    private Map<String,Double> similarityMap = new HashMap<>();

    private Map<String,Double[]> trendMap = new HashMap<>();
}
