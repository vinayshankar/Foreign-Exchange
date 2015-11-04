package personal.vinay.bigdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;

import personal.vinay.bigdata.DecisionTree.ForexTree;
import personal.vinay.bigdata.DecisionTree.Record;

public class RandomForest {

	public static final String COMMA = ",";
	public static final String NEW_LINE = "\n";
	public static final String FORWARD_SLASH = "/";

	private static ArrayList<File> fileNames = new ArrayList<File>();

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

			// Return when no samples
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
				HashSet<Integer> maskedFeatures = getRandomMaskedFeatures(this.records.get(0).NO_OF_FEATURES);
				randomForest.add(new ForexTree(recordsOfTree, maskedFeatures));
			}
		}
		
		private HashSet<Integer> getRandomMaskedFeatures(int numberOfFeatures){
			HashSet<Integer> maskedFeatures = new HashSet<Integer>();
			int min = 1;
			int max = numberOfFeatures;
			int range = (max - min) + 1;;
			boolean featuresSelected = false;
			while(!featuresSelected){
				int random = (int)(Math.random()*range) + min;
				if(!maskedFeatures.contains(random))
					maskedFeatures.add(random);
				if(maskedFeatures.size() == 3)
					break;
			}
			return maskedFeatures;
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

	private void getAllFiles(String inputPath) {
		File directory = new File(inputPath);
		File[] fList = directory.listFiles();
		assert fList != null;
		for (File file : fList) {
			if (file.isFile()) {
				String[] tokens = file.toString().split("\\.(?=[^\\.]+$)");
				if (tokens[1].equalsIgnoreCase("csv")) {
					if (tokens[0].toLowerCase().contains("prepared") && tokens[0].toLowerCase().contains("eurusd"))
						fileNames.add(file);
				}
			} else if (file.isDirectory()) {
				getAllFiles(file.getAbsolutePath());
			}
		}
	}

	public static void main(String args) {
		RandomForest runner = new RandomForest();
		ArrayList<Record> records = new ArrayList<Record>();
		String baseFolder = "C:/Users/Vinay Shankar/Documents/Vinay/CMU/2015Fall/11-676-BigData/Project/Data";
		runner.getAllFiles(baseFolder);
		BufferedReader br = null;
		String line = "";
		for (File file : fileNames) {
			try {
				System.out.println("Filename: " + file.getName());
				String[] data = null;
				br = new BufferedReader(new FileReader(file));
				while ((line = br.readLine()) != null) {
					data = line.split(COMMA);
					records.add(new DecisionTree.Record(data));
				}
			} catch (FileNotFoundException fnfe) {
				fnfe.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException pe) {
				pe.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		try {

			ForexForest forexForest = new ForexForest(records, 50);

			// Testing the random forest
			baseFolder = "C:/Users/Vinay Shankar/Documents/Vinay/CMU/2015Fall/11-676-BigData/Project/TestData";
			runner.getAllFiles(baseFolder);
			br = null;
			line = "";
			Record record = null;

			int correctYes = 0;
			int correctNo = 0;
			int incorrectYes = 0;
			int incorrectNo = 0;
			double predictedProbability = 0;

			for (File file : fileNames) {
				br = new BufferedReader(new FileReader(file));
				while ((line = br.readLine()) != null) {
					String[] data = line.split(COMMA);
					record = new DecisionTree.Record(data);
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
				br.close();
			}
			System.out.println("Total records tested: " + correctNo + correctYes + incorrectNo + incorrectYes);
			System.out.println("Correctly predicted ask price increase: " + correctYes);
			System.out.println("Incorrectly predicted ask price increase: " + incorrectYes);
			System.out.println("Correctly predicted ask price decrease: " + correctNo);
			System.out.println("Incorrectly predicted ask price decrease: " + incorrectNo);

			forexForest.writeObject("C:/Users/Vinay Shankar/Documents/Vinay/CMU/2015Fall/11-676-BigData/Project/forexforest.ser");
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException pe) {
			pe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
