package stormtest;

import datacrawler.Constant;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenyun on 2019/5/28.
 */
public class MathTest {
    public static void main(String[] args) {
        List<String> indexList = Arrays.asList(Constant.stock_index_code.split(","));
        SynchronizedDescriptiveStatistics stat = new SynchronizedDescriptiveStatistics(3);
        stat.addValue(1d);
        stat.addValue(2d);
        stat.addValue(3d);
        double[] values = stat.getValues();
        double element = stat.getElement(0);
        long n = stat.getN();
        double evaluate1 = stat.getSumImpl().evaluate(values);
        int startIndex = 1;
        double evaluate = stat.getSumImpl().evaluate(values, startIndex, (int) (n - startIndex));
        stat.addValue(4d);
        double[] values1 = stat.getValues();

        String[] stockIndexArray = Constant.stock_all.split(",");
        System.out.println(stockIndexArray);
    }
}
