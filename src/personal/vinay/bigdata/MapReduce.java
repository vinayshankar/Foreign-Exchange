package personal.vinay.bigdata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import personal.vinay.bigdata.Cassandra.ForexForest;
import personal.vinay.bigdata.DecisionTree.ForexTree;
import personal.vinay.bigdata.DecisionTree.Record;

public class MapReduce {

	private static final String KEYSPACE = "trainingdata";
	private static final String TABLE_NAME = "records";
	private static final String OUTPUT_TABLE_NAME = "randomforest";

	public static class ForexForestMapper extends Mapper<Text, Text, Text, Text> {

		public void map(Text key, Text values, Context context) throws IOException, InterruptedException {
			try {
				ArrayList<Record> trainingRecords = Cassandra.getRecordsFromCassandra(KEYSPACE,TABLE_NAME);
				
				ArrayList<Record> recordsOfTree = new ArrayList<Record>();
				for (Record record : trainingRecords) {
					if (ForexForest.addRecord()) {
						recordsOfTree.add(record);
					}
				}
	
				HashSet<Integer> maskedFeatures = ForexForest.getRandomMaskedFeatures(Record.NO_OF_FEATURES);
				ForexTree forexTree = new ForexTree(recordsOfTree, maskedFeatures);
				Text keyOut = new Text("vinays_forexforest_forextree");
				Text valueOut = new Text(forexTree.toJsonString());
				context.write(keyOut, valueOut);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public class ForexForestReducer extends Reducer<Text, Text, NullWritable, Text> {

		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			ArrayList<ForexTree> trees = new ArrayList<ForexTree>();
			for (Text value : values) {
				trees.add(ForexTree.fromJsonString(value.toString()));
			}
			
			ForexForest forexForest = new ForexForest(trees);
			String forexForestJsonString = forexForest.toJsonString();
			Text valueOut = new Text(forexForestJsonString);
			Cassandra.writeForestToCassandra(KEYSPACE, OUTPUT_TABLE_NAME, forexForestJsonString);
			context.write(NullWritable.get(), valueOut);
		}
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "MapReduce");

		job.setJarByClass(MapReduce.class);
		job.setMapperClass(ForexForestMapper.class);
		job.setReducerClass(ForexForestReducer.class);

		job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

		System.exit(job.waitForCompletion(true) ? 0 : 1);

	}

}
