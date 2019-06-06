package stormpython;

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

    private Fields fieldsExt = null;
    private Fields outFieldsExt = null;
    private List<Integer> targetTasksExt;

    public PriceDiffCustomStreamGrouping(Fields fields, List<String> whiteList) {
        super(fields);
        this.fieldsExt = fields;
        this.whiteList = whiteList;
    }

    @Override
    public void prepare(WorkerTopologyContext context, GlobalStreamId stream, List<Integer> targetTasks) {
        super.prepare(context,stream,targetTasks);
        if (this.fieldsExt != null) {
            this.outFieldsExt = context.getComponentOutputFields(stream);
        }
        this.targetTasksExt = targetTasks;
    }

    @Override
    public List<Integer> chooseTasks(int taskId, List<Object> values) {
        if (fieldsExt != null){
            List<Object> selectedFields = outFieldsExt.select(fieldsExt, values);

            /**
             * 白名单
             */
            if (selectedFields!=null&&whiteList.contains(selectedFields.get(0))){
                return targetTasksExt;
            }
        }
        return super.chooseTasks(taskId, values);
    }
}
