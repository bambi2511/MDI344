/***
 * Class Job2Mapper
 * Job2 Mapper class
 * @author sgarouachi
 */

import java.io.IOException;
import java.util.TreeSet;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class Job2Mapper extends Mapper<LongWritable, Text, Text, Text> {

	/**
	 * Job2 Map method Generates 3 outputs: Mark existing page: (pageI, !) Used
	 * to calculate the new rank (rank pageI depends on the rank of the inLink):
	 * (pageI, inLink \t rank \t totalLink) Original links of the page for the
	 * reduce output: (pageI, |pageJ,pageK...)
	 */
	@Override
	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		
		// System.out.println(" Input key :" + key + "; Input value : " + value);
		
		String[] parameters = value.toString().split("\t");
		
		String pageSource = parameters[0];
		String pageRank = parameters[1];
		
		String pagesDest = "";
		if (parameters.length > 2) {
			pagesDest = parameters[2];
		}
		
		int pagesDestNb = pagesDest.split(",").length;
		
		context.write(new Text(pageSource), new Text("!"));
		// System.out.println(" Output key :" + pageSource + "; Output value : " + "!");
		
		if (parameters.length > 2) {
			for (String pageDest: pagesDest.split(",")) {
				context.write(new Text(pageDest), new Text(pageSource + "\t" + pageRank + "\t" + pagesDestNb));
				// System.out.println(" Output key :" + pageDest + "; Output value : " + pageSource + "\t" + pageRank + "\t" + pagesDestNb);
			}
		
			// Les cas de tests proposes sont imbeciles:
			// L ordre lexicographique est utilise pour une etape, pas pour l autre
			// Est ce que le but de ce TP est de nous faire perdre notre temps ou de nous apprendre quelque chose ?
		
			TreeSet<String> orderedpagesDest = new TreeSet<>();
		
			for (String pageDest: pagesDest.split(",")) {
				orderedpagesDest.add(pageDest);
			}
			String[] orderedpagesDestString = orderedpagesDest.toArray(new String[orderedpagesDest.size()]);

		
		    //context.write(new Text(pageSource), new Text("|" +pagesDest));
		    context.write(new Text(pageSource), new Text("|" + String.join(",", orderedpagesDestString)));
		    // System.out.println(" Output key :" + pageSource + "; Output value : " + "|" + pagesDest);
		}

		// TODO if needed
		// throw new UnsupportedOperationException("Job2Mapper: map: Not implemented yet");
	}
}
