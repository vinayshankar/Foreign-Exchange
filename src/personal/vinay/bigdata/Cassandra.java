package personal.vinay.bigdata;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import personal.vinay.bigdata.DecisionTree.ForexTree;
import personal.vinay.bigdata.DecisionTree.Record;

public class Cassandra {

	public static class ForexForest implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private static final double RECORDS_RATIO = 2.0 / 3;
		private static final double THRESHOLD = 0.5;

		private ArrayList<ForexTree> randomForest;
		private ArrayList<Record> records;
		private int numberOfTrees;

		public ForexForest(ArrayList<Record> records, int numberOfTrees) throws Exception {
			this.numberOfTrees = numberOfTrees;
			this.records = records;
			this.randomForest = new ArrayList<ForexTree>();

			if (this.records.isEmpty()) {
				return;
			}
			createForest();
		}

		private void createForest() throws Exception {
			for (int i = 0; i < this.numberOfTrees; i++) {
				ArrayList<Record> recordsOfTree = new ArrayList<Record>();
				System.out.println("Building Tree" + i + 1 + "...");
				for (Record record : this.records) {
					if (addRecord()) {
						recordsOfTree.add(record);
					}
				}
				//Set<Integer> mask = generateFeatureMask(this.records.get(0).NO_OF_FEATURES);
				randomForest.add(new ForexTree(recordsOfTree));
			}
		}

		private boolean addRecord() {
			return Math.random() < RECORDS_RATIO;
		}

		public int queryLabelValue(Record record) throws Exception {
			int positiveVotes = 0;
			int negativeVotes = 0;

			for (ForexTree tree : this.randomForest) {
				double prob = tree.queryLabelValue(record);
				if (prob >= THRESHOLD) {
					positiveVotes += 1;
				} else {
					negativeVotes += 1;
				}
			}

			if (positiveVotes >= negativeVotes) {
				return 1;
			} else {
				return -1;
			}
		}

		private void writeObject(String filename) throws IOException {
			FileOutputStream saveFile = new FileOutputStream(filename);
	        ObjectOutputStream save = new ObjectOutputStream(saveFile);
	        save.writeObject(this);
	        save.close();
		}

		private void readObject(FileInputStream input) throws ClassNotFoundException, IOException {
			ObjectInputStream restore = new ObjectInputStream(input);
	        ForexForest temp = (ForexForest) restore.readObject();
	        this.numberOfTrees = temp.numberOfTrees;
	        this.records = temp.records;
	        this.randomForest = temp.randomForest;
		}
	}
	
	private ArrayList<Record> getRecordsFromCassandra(String keyspace, String tableName) throws ParseException{
		Cluster cluster;
		Session session;
		
		ArrayList<Record> records = new ArrayList<Record>();
		Record record;
		String[] recordString;
		
		cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
		session = cluster.connect(keyspace);
		
		ResultSet results = session.execute("SELECT * FROM "+tableName);
		for (Row row : results) {
			recordString = new String[9];
			recordString[0] = row.getString("symbol");
			recordString[1] = row.getString("datetime");
			recordString[2] = row.getString("askprice");
			recordString[3] = row.getString("bidprice");
			recordString[4] = row.getString("avgaskprice");
			recordString[5] = row.getString("maxaskprice");
			recordString[6] = row.getString("minaskprice");
			recordString[7] = row.getString("avgbidprice");
			recordString[8] = row.getString("maxbidprice");
			recordString[9] = row.getString("minbidprice");
			recordString[10] = row.getString("avgspread");
			recordString[11] = row.getString("maxspread");
			recordString[12] = row.getString("minspread");
			recordString[13] = row.getString("askdirectionality");
			record = new Record(recordString);
			records.add(record);
		}
		return records;
	}

	public static void main(String args) {
		Cassandra runner = new Cassandra();
		
		try {
			ArrayList<Record> trainingRecords = runner.getRecordsFromCassandra("trainingdata","records");

			ForexForest forexForest = new ForexForest(trainingRecords, 50);

			int correctYes = 0;
			int correctNo = 0;
			int incorrectYes = 0;
			int incorrectNo = 0;
			double predictedProbability = 0;
			
			ArrayList<Record> testingRecords = runner.getRecordsFromCassandra("testingdata", "records");

			for (Record record : testingRecords) {
				predictedProbability = forexForest.queryLabelValue(record);
				if (predictedProbability > 0.5) {
					if (Integer.parseInt(record.getLabel()) == 1) {
						correctYes++;
					} else {
						incorrectYes++;
					}
				} else {
					if (Integer.parseInt(record.getLabel()) == 0) {
						correctNo++;
					} else {
						incorrectNo++;
					}
				}
			}
			System.out.println("Total records tested: " + correctNo + correctYes + incorrectNo + incorrectYes);
			System.out.println("Correctly predicted ask price increase: " + correctYes);
			System.out.println("Incorrectly predicted ask price increase: " + incorrectYes);
			System.out.println("Correctly predicted ask price decrease: " + correctNo);
			System.out.println("Incorrectly predicted ask price decrease: " + incorrectNo);

			forexForest.writeObject("C:/Users/Vinay Shankar/Documents/Vinay/CMU/2015Fall/11-676-BigData/Project/forexforest.ser");
		}  catch (ParseException pe) {
			pe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
