/***
 * Class PageRankDriver
 * Driver class
 * @author sgarouachi
 */

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.PropertyConfigurator;

public class PageRankDriver extends Configured implements Tool {
	// Init number format
    private static NumberFormat nf = new DecimalFormat("00");
    
    // Init Number of iterations
    private static int nIter = 5;

    public static void main(String[] args) throws Exception {
    	// Exit the program based on the success of the program
        System.exit(ToolRunner.run(new Configuration(), new PageRankDriver(), args));
    }

    @Override
    public int run(String[] args) throws Exception {
    	// Configure log4j
    	PropertyConfigurator.configure("config/log4j.properties");
    	
    	// Init paths
    	String inputPath = "data/input";
    	String iterOutputPath = "data/ranking";
    	String outputPath = "data/output";
    	
    	// Delete existing directory
    	FileUtils.deleteDirectory(new File(iterOutputPath));
    	FileUtils.deleteDirectory(new File(outputPath));
    	
    	// Start 1st job 
        boolean isCompleted = runXmlParsing(inputPath, iterOutputPath+"/iter00");
        // Check if done?
        if (!isCompleted) return 1;
        
        // Execute 2nd job for nIter times
        String lastResultPath = null;
        for (int runs = 0; runs < nIter; runs++) {
            String inPath = iterOutputPath+"/iter" + nf.format(runs);
            lastResultPath = iterOutputPath+"/iter" + nf.format(runs + 1);
            // Check if done?
            isCompleted = runRankCalculation(inPath, lastResultPath);
            if (!isCompleted) return 1;
        }
        
        // Start 3rd job
        isCompleted = runRankOrdering(lastResultPath, outputPath);
        // Check if done?
        if (!isCompleted) return 1;
        
        // Done!
        return 0;
    }

    /**
     * Configure the 1st job
     * Parse the XML to Page with Default ranks 1.0 and outLinks 
     * 
     * @param inputPath
     * @param outputPath
     * @return Boolean
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    public boolean runXmlParsing(String inputPath, String outputPath) throws IOException, ClassNotFoundException, InterruptedException {
        // Init configuration
    	Configuration conf = new Configuration();
        conf.set(XmlInputFormat.START_TAG_KEY, "<page>");
        conf.set(XmlInputFormat.END_TAG_KEY, "</page>");
        
        // Init job
        Job xmlParser = Job.getInstance(conf, "xmlParser");
        xmlParser.setJarByClass(PageRankDriver.class);

        // Input path
        FileInputFormat.addInputPath(xmlParser, new Path(inputPath));
        xmlParser.setInputFormatClass(XmlInputFormat.class);
        
        // Mapper
        xmlParser.setMapperClass(Job1Mapper.class);
        
        // Reducer
        xmlParser.setReducerClass(Job1Reducer.class);
        
        //Reducer output : key/value
        xmlParser.setOutputKeyClass(Text.class);
        xmlParser.setOutputValueClass(Text.class);
        
        // Output path 
        FileOutputFormat.setOutputPath(xmlParser, new Path(outputPath));
        xmlParser.setOutputFormatClass(TextOutputFormat.class);
        
        // Return
        return xmlParser.waitForCompletion(true);
    }
    
    /**
     * Configure the 2nd job
     * Calculate new Page Rank
     * 
     * @param inputPath
     * @param outputPath
     * @return Boolean
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    private boolean runRankCalculation(String inputPath, String outputPath) throws IOException, ClassNotFoundException, InterruptedException {
    	// Init configuration
    	Configuration conf = new Configuration();
    	
    	// Init job
        Job rankCalculator = Job.getInstance(conf, "rankCalculator");
        rankCalculator.setJarByClass(PageRankDriver.class);
        
        // Input path
        FileInputFormat.setInputPaths(rankCalculator, new Path(inputPath));
        
        // Mapper
        rankCalculator.setMapperClass(Job2Mapper.class);
        
        // Reducer
        rankCalculator.setReducerClass(Job2Reducer.class);
        
        //Output : key/value
        rankCalculator.setOutputKeyClass(Text.class);
        rankCalculator.setOutputValueClass(Text.class);
        
        // Output path 
        FileOutputFormat.setOutputPath(rankCalculator, new Path(outputPath));
        
        // Return
        return rankCalculator.waitForCompletion(true);
    }
    
    /**
     * Configure the 3rd job
     * Order last run on PageRank (Descending)
     *  
     * @param inputPath
     * @param outputPath
     * @return Boolean
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    private boolean runRankOrdering(String inputPath, String outputPath) throws IOException, ClassNotFoundException, InterruptedException {
    	// Init configuration
    	Configuration conf = new Configuration();
    	
    	// Init job
        Job rankOrdering = Job.getInstance(conf, "rankOrdering");
        rankOrdering.setJarByClass(PageRankDriver.class);
        
        // Input path
        FileInputFormat.setInputPaths(rankOrdering, new Path(inputPath));
        rankOrdering.setInputFormatClass(TextInputFormat.class);
        
        // Mapper
        rankOrdering.setMapperClass(Job3Mapper.class);
        
        // Output : key/value
        rankOrdering.setOutputKeyClass(FloatWritable.class);
        rankOrdering.setOutputValueClass(Text.class);
        
        // Descending Sort
        rankOrdering.setSortComparatorClass(Job3SortingComparator.class);

        // Output path 
        FileOutputFormat.setOutputPath(rankOrdering, new Path(outputPath));
        rankOrdering.setOutputFormatClass(TextOutputFormat.class);
        
        // Return
        return rankOrdering.waitForCompletion(true);
    }

}
