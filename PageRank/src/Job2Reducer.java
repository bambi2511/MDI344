/***
 * Class Job2Reducer
 * Job2 Reducer class
 * @author sgarouachi
 */

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class Job2Reducer extends Reducer<Text, Text, Text, Text> {
	// Init dumping factor to 0.85
	private static final float damping = 0.85F;

	/**
	 * Job2 Reduce method Calculate the new page rank
	 */
	@Override
	public void reduce(Text page, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {
		// For each otherPage: 
        // - check control characters
        // - calculate pageRank share <rank> / count(<links>)
        // - add the share to sumShareOtherPageRanks
		
		System.out.println(" Input key :" + page + "; Input value : " + values.toString());
		
		Iterator<Text> valuesIterator = values.iterator();
		
		boolean rankedPage = false;
		
		if (valuesIterator.hasNext()){
			if (valuesIterator.next().toString().equals("!")) {
				rankedPage = true;
			}
		}

		
		if (rankedPage) {
			String parameter = "";
			float pageRank = 1 - damping;
			String pagesDest = "";
			
			while (valuesIterator.hasNext()) {
				parameter = valuesIterator.next().toString();
				if (!parameter.startsWith("|")) {
					String[] expression = parameter.split("\t");
					pageRank += damping * (Float.valueOf(expression[1]) / Float.valueOf(expression[2]));
				}
				else {
					pagesDest = parameter.substring(1);
				}
			}
			
			String outputString;
			outputString = String.format(java.util.Locale.US, "%.4f", pageRank);
			if (!pagesDest.isEmpty()) {
				outputString += "\t" + pagesDest;
			}
			
			System.out.println(" Output key :" + page + "; Output value : " + outputString);		
			context.write(page, new Text(outputString));
		}

		// Write to output
		// (page, rank \t outLinks)
		// context.write(page, new Text(String.format(java.util.Locale.US,
		// "%.4f", newRank) + links));

		// TODO if needed
		// throw new UnsupportedOperationException(
		// 		"Job2Reducer: reduce: Not implemented yet");
	}
}
