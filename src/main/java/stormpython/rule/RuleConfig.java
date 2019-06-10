package stormpython.rule;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by chenyun on 2019/6/6.
 */
@Data
@Builder
public class RuleConfig implements Serializable {

    private String stockCode;
    /**
     * 评估出合理压单托单金额
     */
    private double filter_mount;

    /**
     * 评估出合理比例
     */

    private double filter_per;

    /**
     * 评估出合理夹单金额
     */
    private double mount;
    /**
     * 价格波动
     */
    private double price_var;


    private int offset;


    /**
     * 某stock累计最少数据量
     */
    private int minCalcCount;
}
