package fsrealanalysis;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenyun on 2019/5/28.
 */
@Builder
@Data
@NoArgsConstructor
public class SimilarityRes implements Serializable {

    private String stock;

    /**
     * key:index_offset
     */
    private Map<String,Double> similarityMap = new HashMap<>();


    private Map<String,Double[]> trendMap = new HashMap<>();
}
