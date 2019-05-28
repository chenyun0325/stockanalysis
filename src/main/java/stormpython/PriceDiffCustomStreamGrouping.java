package stormpython;

import net.sf.json.JSONArray;
import org.apache.storm.generated.GlobalStreamId;
import org.apache.storm.grouping.CustomStreamGrouping;
import org.apache.storm.grouping.PartialKeyGrouping;
import org.apache.storm.task.WorkerTopologyContext;
import org.apache.storm.tuple.Fields;

import java.util.List;

/**
 * Created by chenyun on 2019/5/27.
 */
public class PriceDiffCustomStreamGrouping extends PartialKeyGrouping implements CustomStreamGrouping {

    private List<String> whiteList;

    private Fields fieldsx = null;
    private Fields outFieldsx = null;
    private List<Integer> targetTasksx;

    public PriceDiffCustomStreamGrouping(Fields fields, List<String> whiteList) {
        super(fields);
        this.fieldsx = fields;
        this.whiteList = whiteList;
    }

    @Override
    public void prepare(WorkerTopologyContext context, GlobalStreamId stream, List<Integer> targetTasks) {
        super.prepare(context,stream,targetTasks);
        if (this.fieldsx != null) {
            this.outFieldsx = context.getComponentOutputFields(stream);
        }
        this.targetTasksx = targetTasks;
    }

    @Override
    public List<Integer> chooseTasks(int taskId, List<Object> values) {
        if (fieldsx!= null){
            List<Object> selectedFields = outFieldsx.select(fieldsx, values);

            System.out.println("diffField:"+selectedFields+"json:"+ JSONArray.fromObject(selectedFields).toString());
            /**
             * 白名单
             */
            if (selectedFields!=null&&whiteList.contains(selectedFields.get(0))){
                System.out.println("white,"+whiteList+","+selectedFields);
                return targetTasksx;
            }
        }
        return super.chooseTasks(taskId, values);
    }
}
