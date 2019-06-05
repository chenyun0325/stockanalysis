package stormpython.testStorm;

import org.apache.storm.spout.ShellSpout;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;

import java.util.Map;

/*
This is basically just defining some JVM things for Storm, such as
the output fields, or passing around configuration. Then it invokes the
sentencespout.py using Python.
*/
public class SentenceSpout extends ShellSpout implements IRichSpout {
    // Invoke the python spout
    public SentenceSpout(String codelist) {
        //super("python", "fsrealspout_test.py");
        super("python", "fsrealspout_test.py",codelist);
    }

    // Declare that we emit a 'sentence' field
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("code","row"));
    }

    // No real configuration going on
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}