import java.io.IOException;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

//Only used for Unit Tests
public class Job3Reducer extends
		Reducer<FloatWritable, Text, FloatWritable, Text> {
	@Override
	public void reduce(FloatWritable key, Iterable<Text> values, Context context)
			throws InterruptedException, IOException {
		for (Text value : values) {
			context.write(key, value);
		}
	}
}