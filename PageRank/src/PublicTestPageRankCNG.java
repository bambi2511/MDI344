/***
 * PublicTestPageRank class
 * Public tests for students
 * @author sgarouachi
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;


public class PublicTestPageRankCNG extends TestCase {	
	// Init
	MapDriver<LongWritable, Text, Text, Text> job1MapDriver;
	ReduceDriver<Text, Text, Text, Text> job1ReduceDriver;
	MapReduceDriver<LongWritable, Text, Text, Text, Text, Text> job1MapReduceDriver;

	MapDriver<LongWritable, Text, Text, Text> job2MapDriver;
	ReduceDriver<Text, Text, Text, Text> job2ReduceDriver;
	MapReduceDriver<LongWritable, Text, Text, Text, Text, Text> job2MapReduceDriver;

	MapDriver<LongWritable, Text, FloatWritable, Text> job3MapDriver;
	MapReduceDriver<LongWritable, Text, FloatWritable, Text, FloatWritable, Text> job3MapReduceDriver;

	@SuppressWarnings("unchecked")
	public void setUp() {
		// 1st job
		Job1Mapper job1Mapper = new Job1Mapper();
		job1MapDriver = new MapDriver<LongWritable, Text, Text, Text>();
		job1MapDriver.setMapper(job1Mapper);

		Job1Reducer job1Reducer = new Job1Reducer();
		job1ReduceDriver = new ReduceDriver<Text, Text, Text, Text>();
		job1ReduceDriver.setReducer(job1Reducer);

		job1MapReduceDriver = new MapReduceDriver<LongWritable, Text, Text, Text, Text, Text>();
		job1MapReduceDriver.setMapper(job1Mapper);
		job1MapReduceDriver.setReducer(job1Reducer);

		// 2nd job
		Job2Mapper job2Mapper = new Job2Mapper();
		job2MapDriver = new MapDriver<LongWritable, Text, Text, Text>();
		job2MapDriver.setMapper(job2Mapper);

		Job2Reducer job2Reducer = new Job2Reducer();
		job2ReduceDriver = new ReduceDriver<Text, Text, Text, Text>();
		job2ReduceDriver.setReducer(job2Reducer);

		job2MapReduceDriver = new MapReduceDriver<LongWritable, Text, Text, Text, Text, Text>();
		job2MapReduceDriver.setMapper(job2Mapper);
		job2MapReduceDriver.setReducer(job2Reducer);

		// 3rd job
		Job3Mapper job3Mapper = new Job3Mapper();
		job3MapDriver = new MapDriver<LongWritable, Text, FloatWritable, Text>();
		job3MapDriver.setMapper(job3Mapper);

		job3MapReduceDriver = new MapReduceDriver<LongWritable, Text, FloatWritable, Text, FloatWritable, Text>();
		job3MapReduceDriver.setMapper(job3Mapper);
		job3MapReduceDriver.setReducer(new Job3Reducer());
		job3MapReduceDriver.setKeyOrderComparator(new Job3SortingComparator());

	}

	// Test Job1 Reducer
	public void testJob1Reducer() throws IOException {
		// Add input
		List<Text> values = new ArrayList<Text>();
		values.add(new Text("PageA"));
		job1ReduceDriver.withInput(new Text("PageB"), values);
		job1ReduceDriver.withInput(new Text("PageC"), values);

		// Add output
		job1ReduceDriver.withOutput(new Text("PageB"), new Text("1.0\tPageA"));
		job1ReduceDriver.withOutput(new Text("PageC"),
						new Text("1.0\tPageA"));

		// Run & check
		job1ReduceDriver.runTest();
	}

    // Test Job1 Reducer
    public void testJob1Reducer2() throws IOException {
        // Add input
        List<Text> values1 = new ArrayList<Text>();
        values1.add(new Text("PageA"));
        values1.add(new Text("PageD"));
        job1ReduceDriver.withInput(new Text("PageB"), values1);
        job1ReduceDriver.withInput(new Text("PageC"), new ArrayList<Text>());

        // Add output
        job1ReduceDriver.withOutput(new Text("PageB"), new Text("1.0\tPageA,PageD"));
        job1ReduceDriver.withOutput(new Text("PageC"),
                new Text("1.0\t"));

        // Run & check
        job1ReduceDriver.runTest();
    }

    // Test Job1 Reducer
    public void testJob1Reducer3() throws IOException {
        // Add input
        List<Text> values1 = new ArrayList<Text>();
        values1.add(new Text("PageD"));
        values1.add(new Text("PageA"));
        values1.add(new Text("PageC"));
        values1.add(new Text("PageE"));
        values1.add(new Text("PageA"));
        values1.add(new Text("PageB"));
        job1ReduceDriver.withInput(new Text("PageB"), values1);
        job1ReduceDriver.withInput(new Text("PageC"), new ArrayList<Text>());

        // Add output
        job1ReduceDriver.withOutput(new Text("PageB"), new Text("1.0\tPageA,PageB,PageC,PageD,PageE"));
        job1ReduceDriver.withOutput(new Text("PageC"),
                new Text("1.0\t"));

        // Run & check
        job1ReduceDriver.runTest();
    }

	// Test Job2 Mapper
	public void testJob2Mapper() throws IOException {
		// Set input
		job2MapDriver.withInput(new LongWritable(1), new Text(
				"PageB\t1.0\tPageA"));
		job2MapDriver.withInput(new LongWritable(2), new Text(
				"PageC\t1.0\tPageA"));

		// Set output
		job2MapDriver.withOutput(new Text("PageB"), new Text("!"));
		job2MapDriver.withOutput(new Text("PageA"), new Text("PageB\t1.0\t1"));
		job2MapDriver.withOutput(new Text("PageB"), new Text("|PageA"));
		job2MapDriver.withOutput(new Text("PageC"), new Text("!"));
		job2MapDriver.withOutput(new Text("PageA"), new Text("PageC\t1.0\t1"));
		job2MapDriver.withOutput(new Text("PageC"), new Text("|PageA"));

		// Run & check
		job2MapDriver.runTest();
	}

	public void testJob2Mapper2() throws IOException {
        job2MapDriver.withInput(new LongWritable(3), new Text( "PageC\t1.0\tPageA,PageD"));
        job2MapDriver.withInput(new LongWritable(3), new Text( "PageB\t1.0\tPageA"));
        job2MapDriver.withOutput(new Text("PageC"), new Text("!"));
        job2MapDriver.withOutput(new Text("PageA"), new Text("PageC\t1.0\t2"));
        job2MapDriver.withOutput(new Text("PageD"), new Text("PageC\t1.0\t2"));
        job2MapDriver.withOutput(new Text("PageC"), new Text("|PageA,PageD"));
        job2MapDriver.withOutput(new Text("PageB"), new Text("!"));
        job2MapDriver.withOutput(new Text("PageA"), new Text("PageB\t1.0\t1"));
        job2MapDriver.withOutput(new Text("PageB"), new Text("|PageA"));

        // Run & check
        job2MapDriver.runTest();

    }

    public void testJob2Mapper3() throws IOException {
        job2MapDriver.withInput(new LongWritable(3), new Text( "PageC\t1.0\tPageD,PageA"));
        job2MapDriver.withInput(new LongWritable(3), new Text( "PageB\t1.0\tPageA"));
        job2MapDriver.withOutput(new Text("PageC"), new Text("!"));
        job2MapDriver.withOutput(new Text("PageD"), new Text("PageC\t1.0\t2"));
        job2MapDriver.withOutput(new Text("PageA"), new Text("PageC\t1.0\t2"));
        job2MapDriver.withOutput(new Text("PageC"), new Text("|PageA,PageD"));
        job2MapDriver.withOutput(new Text("PageB"), new Text("!"));
        job2MapDriver.withOutput(new Text("PageA"), new Text("PageB\t1.0\t1"));
        job2MapDriver.withOutput(new Text("PageB"), new Text("|PageA"));

        // Run & check
        job2MapDriver.runTest();
    }

    public void testJob2Mapper4() throws IOException {
        job2MapDriver.withInput(new LongWritable(3), new Text( "PageC\t1.0\tPageD,PageA"));
        job2MapDriver.withInput(new LongWritable(3), new Text( "PageB\t0.5\tPageA"));
        job2MapDriver.withInput(new LongWritable(3), new Text( "PageE\t1.0\t"));
        job2MapDriver.withOutput(new Text("PageC"), new Text("!"));
        job2MapDriver.withOutput(new Text("PageD"), new Text("PageC\t1.0\t2"));
        job2MapDriver.withOutput(new Text("PageA"), new Text("PageC\t1.0\t2"));
        job2MapDriver.withOutput(new Text("PageC"), new Text("|PageA,PageD"));
        job2MapDriver.withOutput(new Text("PageB"), new Text("!"));
        job2MapDriver.withOutput(new Text("PageA"), new Text("PageB\t0.5\t1"));
        job2MapDriver.withOutput(new Text("PageB"), new Text("|PageA"));
        job2MapDriver.withOutput(new Text("PageE"), new Text("!"));

        // Run & check
        job2MapDriver.runTest();
    }

    public void testJob2Mapper5() throws IOException {
        job2MapDriver.withInput(new LongWritable(3), new Text( "PageC\t1.0\tPageA,PageD"));
        job2MapDriver.withInput(new LongWritable(4), new Text( "PageB\t1.0\tPageA"));
        job2MapDriver.withInput(new LongWritable(5), new Text( "PageA\t1.0\t"));
        job2MapDriver.withOutput(new Text("PageC"), new Text("!"));
        job2MapDriver.withOutput(new Text("PageA"), new Text("PageC\t1.0\t2"));
        job2MapDriver.withOutput(new Text("PageD"), new Text("PageC\t1.0\t2"));
        job2MapDriver.withOutput(new Text("PageC"), new Text("|PageA,PageD"));
        job2MapDriver.withOutput(new Text("PageB"), new Text("!"));
        job2MapDriver.withOutput(new Text("PageA"), new Text("PageB\t1.0\t1"));
        job2MapDriver.withOutput(new Text("PageB"), new Text("|PageA"));
        job2MapDriver.withOutput(new Text("PageA"), new Text("!"));

        // Run & check
        job2MapDriver.runTest();
    }

	// Test Job2 Reducer
	public void testJob2Reducer() throws IOException {
		// Add input
		List<Text> values = new ArrayList<Text>();
		values.add(new Text("!"));
		values.add(new Text("|PageA"));
		job2ReduceDriver.withInput(new Text("PageB"), values);

		values = new ArrayList<Text>();
		values.add(new Text("!"));
		values.add(new Text("|PageA"));
		job2ReduceDriver.withInput(new Text("PageC"), values);

		// Add output
		job2ReduceDriver.withOutput(new Text("PageB"),
				new Text("0.1500\tPageA"));
		job2ReduceDriver.withOutput(new Text("PageC"), new Text(
				"0.1500\tPageA"));

		// Run & check
		job2ReduceDriver.runTest();
	}

    // Test Job2 Reducer
    public void testJob2Reducer2() throws IOException {
        // Add input
        List<Text> valuesA = new ArrayList<Text>();
        valuesA.add(new Text("!"));
        valuesA.add(new Text("PageC\t1.0\t2"));
        valuesA.add(new Text("PageB\t1.0\t1"));
        job2ReduceDriver.withInput(new Text("PageA"), valuesA);

        List<Text> valuesB = new ArrayList<Text>();
        valuesB.add(new Text("!"));
        valuesB.add(new Text("|PageA"));
        job2ReduceDriver.withInput(new Text("PageB"), valuesB);

        List<Text> valuesC = new ArrayList<Text>();
        valuesC.add(new Text("!"));
        valuesC.add(new Text("|PageA,PageD"));
        job2ReduceDriver.withInput(new Text("PageC"), valuesC);

        List<Text> valuesD = new ArrayList<Text>();
        valuesD.add(new Text("PageC\t1.0\t2"));
        job2ReduceDriver.withInput(new Text("PageD"), valuesD);

        // Add output
        job2ReduceDriver.withOutput(new Text("PageA"),
                new Text("1.4250"));
        job2ReduceDriver.withOutput(new Text("PageB"), new Text(
                "0.1500\tPageA"));
        job2ReduceDriver.withOutput(new Text("PageC"), new Text(
                "0.1500\tPageA,PageD"));

        // Run & check
        job2ReduceDriver.runTest();
    }

    // Test Job2 Reducer
    public void testJob2Reducer3() throws IOException {
        // Add input
        List<Text> valuesA = new ArrayList<Text>();
        valuesA.add(new Text("!"));
        valuesA.add(new Text("PageC\t0.15\t2"));
        valuesA.add(new Text("PageB\t0.15\t1"));
        job2ReduceDriver.withInput(new Text("PageA"), valuesA);

        List<Text> valuesB = new ArrayList<Text>();
        valuesB.add(new Text("!"));
        valuesB.add(new Text("|PageA"));
        job2ReduceDriver.withInput(new Text("PageB"), valuesB);

        List<Text> valuesC = new ArrayList<Text>();
        valuesC.add(new Text("!"));
        valuesC.add(new Text("|PageA,PageD"));
        job2ReduceDriver.withInput(new Text("PageC"), valuesC);

        List<Text> valuesD = new ArrayList<Text>();
        valuesD.add(new Text("PageC\t0.15\t2"));
        job2ReduceDriver.withInput(new Text("PageD"), valuesD);

        // Add output
        job2ReduceDriver.withOutput(new Text("PageA"),
                new Text("0.3413"));
        job2ReduceDriver.withOutput(new Text("PageB"), new Text(
                "0.1500\tPageA"));
        job2ReduceDriver.withOutput(new Text("PageC"), new Text(
                "0.1500\tPageA,PageD"));

        // Run & check
        job2ReduceDriver.runTest();
    }

    // Test Both Job2 Mapper and Reducer
    public void testJob2MapReduce() throws IOException {
        // Add input
        job2MapReduceDriver.withInput(new LongWritable(2), new Text(
                "PageB\t1.0\tPageA"));
        job2MapReduceDriver.withInput(new LongWritable(3), new Text(
                "PageC\t1.0\tPageA"));

        // Add output
        job2MapReduceDriver.withOutput(new Text("PageB"), new Text(
                "0.1500\tPageA"));
        job2MapReduceDriver.withOutput(new Text("PageC"), new Text(
                "0.1500\tPageA"));

        // Run & check
        job2MapReduceDriver.runTest();
    }

	// Test Both Job2 Mapper and Reducer
	public void testJob2MapReduce2() throws IOException {
		// Add input
		job2MapReduceDriver.withInput(new LongWritable(2), new Text(
				"PageC\t1.0\tPageA,PageD"));
		job2MapReduceDriver.withInput(new LongWritable(3), new Text(
				"PageB\t1.0\tPageA"));
		job2MapReduceDriver.withInput(new LongWritable(4), new Text(
				"PageA\t1.0\t"));

		// Add output
        job2MapReduceDriver.withOutput(new Text("PageA"),
                new Text("1.4250"));
        job2MapReduceDriver.withOutput(new Text("PageB"), new Text(
                "0.1500\tPageA"));
        job2MapReduceDriver.withOutput(new Text("PageC"), new Text(
                "0.1500\tPageA,PageD"));

		// Run & check
		job2MapReduceDriver.runTest();
	}

    // Test Both Job2 Mapper and Reducer
    public void testJob2MapReduce3() throws IOException {
        // Add input
        job2MapReduceDriver.withInput(new LongWritable(2), new Text(
                "PageC\t0.1500\tPageA,PageD"));
        job2MapReduceDriver.withInput(new LongWritable(3), new Text(
                "PageB\t0.1500\tPageA"));
        job2MapReduceDriver.withInput(new LongWritable(4), new Text(
                "PageA\t1.425\t"));

        // Add output
        job2MapReduceDriver.withOutput(new Text("PageA"),
                new Text("0.3413"));
        job2MapReduceDriver.withOutput(new Text("PageB"), new Text(
                "0.1500\tPageA"));
        job2MapReduceDriver.withOutput(new Text("PageC"), new Text(
                "0.1500\tPageA,PageD"));

        // Run & check
        job2MapReduceDriver.runTest();
    }

	// Test Job3 Mapper
	public void testJob3Mapper() throws IOException {
		// Set input
		job3MapDriver.withInput(new LongWritable(1), new Text("PageB\t0.1500\tPageA"));
		job3MapDriver.withInput(new LongWritable(2), new Text("PageC\t0.1200\tPageA"));
		
		// Set output
		job3MapDriver.withOutput(new FloatWritable(Float.parseFloat("0.1500")), new Text("PageB"));
		job3MapDriver.withOutput(new FloatWritable(Float.parseFloat("0.1200")), new Text("PageC"));
		// Run & check
		job3MapDriver.runTest();
	}

	// Test Job3 Mapper
	public void testJob3Mapper2() throws IOException {
		// Set input
		job3MapDriver.withInput(new LongWritable(1), new Text("PageB\t0.1500\tPageA"));
		job3MapDriver.withInput(new LongWritable(2), new Text("PageC\t0.2200\tPageA"));

		// Set output
		job3MapDriver.withOutput(new FloatWritable(Float.parseFloat("0.1500")), new Text("PageB"));
		job3MapDriver.withOutput(new FloatWritable(Float.parseFloat("0.2200")), new Text("PageC"));
		// Run & check
		job3MapDriver.runTest();
	}

	// Test Both Job3 Mapper and Sorting Comparator
	public void testJob3MapReduce() throws IOException {
		// Add input
		job3MapReduceDriver.withInput(new LongWritable(1), new Text("PageC\t0.1200\tPageA"));
		job3MapReduceDriver.withInput(new LongWritable(2), new Text("PageB\t0.1500\tPageA"));

		// Add output
		job3MapReduceDriver.withOutput(new FloatWritable(Float.parseFloat("0.1500")), new Text("PageB"));
		job3MapReduceDriver.withOutput(new FloatWritable(Float.parseFloat("0.1200")), new Text("PageC"));

		// Run & check
		job3MapReduceDriver.runTest();
	}

	// Test Both Job3 Mapper and Sorting Comparator
	public void testJob3MapReduce2() throws IOException {
		// Add input
		job3MapReduceDriver.withInput(new LongWritable(1), new Text("PageC\t0.2200\tPageA"));
		job3MapReduceDriver.withInput(new LongWritable(2), new Text("PageB\t0.1500\tPageA"));

		// Add output
		job3MapReduceDriver.withOutput(new FloatWritable(Float.parseFloat("0.2200")), new Text("PageC"));
		job3MapReduceDriver.withOutput(new FloatWritable(Float.parseFloat("0.1500")), new Text("PageB"));

		// Run & check
		job3MapReduceDriver.runTest();
	}
}