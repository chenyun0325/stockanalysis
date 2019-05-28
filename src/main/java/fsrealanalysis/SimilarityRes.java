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

    private double s1;

    private double s2;

    private double s3;

    private double s4;

    private double s5;

    private double s6;

    private Map<String,Double> similarityMap = new HashMap<>();
}
