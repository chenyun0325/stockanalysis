package stormpython.rule;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by chenyun on 2019/6/6.
 */
@Data
@Builder
public class WarnRuleConfig implements Serializable {

    /**
     * 预警水位
     */
    private double priceWarnLevel;


    /**
     * 量比例
     */
    private int volumeLevel;


    /**
     * 窗口计算偏移量
     */
    private int offset;


    /**
     * topN
     */
    private int topN;
}
