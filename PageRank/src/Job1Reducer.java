/***
 * Class Job1Reducer
 * Job1 Reducer class
 * @author sgarouachi
 */

import java.io.IOException;
import java.util.TreeSet;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class Job1Reducer extends Reducer<Text, Text, Text, Text> {

	/**
	 * Job1 Reduce method (page, 1.0 \t outLinks)
	 * Remove redundant links & sort them Asc
	 */
	@Override
	public void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {
		
		//System.out.println(" Input key :" + key + "; Input value : " + values.toString());
		
		TreeSet<String> sortedList = new TreeSet<>();
		String value = "1.0";
		
		for(Text val: values) {
			sortedList.add((val.toString()));
		}
		
		if (!sortedList.isEmpty()) {
			value = value + "\t" + String.join(",", sortedList);
		}
		
		//System.out.println(" Output key :" + key + "; Output value : " + value);
		
		context.write(key, new Text(value));
		
		// TODO if needed
		// Remove redundant outLinks
		// Sort outLinks by Asc
		// Append default page rank + outLinks
		// throw new UnsupportedOperationException("Job1Reducer: reduce: Not implemented yet");
	}
}
