package personal.vinay.bigdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

public class DecisionTree {

	public static final String COMMA = ",";
	public static final String NEW_LINE = "\n";
	public static final String FORWARD_SLASH = "/";
	
	private static ArrayList<File> fileNames = new ArrayList<File>();

	public static class ForexTree {

		private ForexTree leftNode = null;
		private ForexTree rightNode = null;

		private int nodeId = -1; // feature ID

		private int leftNodeSize = 0;
		private int rightNodeSize = 0;

		private double askIncreasePorbability = -1;
		
		private boolean isNodeADiscreteFeature = false;
		private HashMap<Integer,Double> continuousFeatureThresholdValues = new HashMap<Integer,Double>();
		private HashMap<Integer,Double> continuousFeatureEntropyValues = new HashMap<Integer,Double>();
		
		public static HashSet<Integer> FEATURES_USED = new HashSet<Integer>();

		public ForexTree(ArrayList<Record> records) throws Exception {
			calculateContinuousFeaturesThresholdEntropyValues(records);
			this.nodeId = initAndGetBestFeatureId(records);
			FEATURES_USED.add(this.nodeId);
			
			if(this.askIncreasePorbability != 1 && this.askIncreasePorbability != 0){
				ArrayList<Record> leftNodeRecords = new ArrayList<Record>(this.leftNodeSize);
				ArrayList<Record> rightNodeRecords = new ArrayList<Record>(this.rightNodeSize);
				
				for(Record r : records){
					if(getDecision(r,this.nodeId)){
						leftNodeRecords.add(r);
					}else{
						rightNodeRecords.add(r);
					}
				}
			
				this.leftNode = new ForexTree(leftNodeRecords);
				this.rightNode = new ForexTree(rightNodeRecords);
			}
		}
		
		private void calculateContinuousFeaturesThresholdEntropyValues(ArrayList<Record> records) throws Exception{
			double thresholdValue = -1;
			double entropy = -1;
			for (int i = 1; i <= Record.NO_OF_FEATURES && !FEATURES_USED.contains(i) && !Record.isFeatureDiscrete(i); i++) {
				// logic to calculate variance for the feature i
				ArrayList<Double> values = new ArrayList<Double>();
				// Add all the values of this feature that exist in the records into the list
				for(Record r : records){
					if(!values.contains(r.getContinousFeature(i))){
						values.add(r.getContinousFeature(i));
					}
				}
				// for each value, if it was threshold value, calculate remaining entropy in the tree. 
				// consider the value for which the remaining entropy is the least
				for(double v : values){
					EntropyData yesData = new EntropyData();
					EntropyData noData = new EntropyData();
					for(Record r : records){
						if(r.getContinousFeature(i) > v){
							if(Boolean.parseBoolean(r.getLabel())){
								yesData.setYes(yesData.getYes()+1);
							}else{
								yesData.setNo(yesData.getNo()+1);
							}
						}else{
							if(Boolean.parseBoolean(r.getLabel())){
								noData.setYes(yesData.getYes()+1);
							}else{
								noData.setNo(yesData.getNo()+1);
							}
						}
					}
					double ent = getRemainingEntropy(yesData, noData);
					if(ent < entropy){
						entropy = ent;
						thresholdValue = v;
					}
				}
				this.continuousFeatureEntropyValues.put(i, entropy);
				this.continuousFeatureThresholdValues.put(i, thresholdValue);
			}
		}
		
		private boolean getDecision(Record r, int featureId) throws Exception{
			if(this.isNodeADiscreteFeature){
				return r.getDiscreteFeature(featureId);
			}else{
				return r.getContinousFeature(featureId) > this.continuousFeatureThresholdValues.get(featureId);
			}
		}

		private int initAndGetBestFeatureId(ArrayList<Record> records) throws Exception {
			double minRemainingEntropy = 1;
			int minRemainingEntropyFeatureId = -1;

			for (int i = 1; i <= Record.NO_OF_FEATURES && !FEATURES_USED.contains(i); i++) {
				double remaingingEntropy = -1;
				HashMap<Integer, EntropyData> data = getDataForEntropy(records, i);
				if(Record.isFeatureDiscrete(i)){
					remaingingEntropy = getRemainingEntropy(data.get(1), data.get(0)); // for discrete features
				}else{
					remaingingEntropy = this.continuousFeatureEntropyValues.get(i); // for continuous features
				}
				if (remaingingEntropy < minRemainingEntropy) {
					minRemainingEntropy = remaingingEntropy;
					minRemainingEntropyFeatureId = i;
					this.isNodeADiscreteFeature = Record.isFeatureDiscrete(minRemainingEntropyFeatureId);
					this.askIncreasePorbability = ((double) (data.get(1).getYes() + data.get(0).getYes()))
							/ records.size();
					this.leftNodeSize = data.get(1).getYes() + data.get(1).getNo();
					this.rightNodeSize = data.get(0).getYes() + data.get(0).getNo();
				}
			}
			return minRemainingEntropyFeatureId;
		}

		private HashMap<Integer, EntropyData> getDataForEntropy(ArrayList<Record> records, int featureId) throws Exception {
			EntropyData yesData = new EntropyData();
			EntropyData noData = new EntropyData();
			for(Record r : records){
				if(getDecision(r, featureId)){
					if(Boolean.parseBoolean(r.getLabel())){
						yesData.setYes(yesData.getYes()+1);
					}else{
						yesData.setNo(yesData.getNo()+1);
					}
				}else{
					if(Boolean.parseBoolean(r.getLabel())){
						noData.setYes(yesData.getYes()+1);
					}else{
						noData.setNo(yesData.getNo()+1);
					}
				}
			}
			HashMap<Integer, EntropyData> data = new HashMap<Integer, EntropyData>();
			data.put(1, yesData);
			data.put(0, noData);
			return data;
		}

		private double getRemainingEntropy(EntropyData yesEntropyData, EntropyData noEntropyData) {
			return ((yesEntropyData.getNo() + yesEntropyData.getYes()) / (this.leftNodeSize + this.rightNodeSize))
					* yesEntropyData.getEntropy()
					+ ((noEntropyData.getNo() + noEntropyData.getYes()) / this.leftNodeSize + this.rightNodeSize)
							* noEntropyData.getEntropy();
		}

		private class EntropyData {
			private int yes = 0;
			private int no = 0;

			public int getYes() {
				return this.yes;
			}

			public int getNo() {
				return this.no;
			}

			public void setYes(int yes){
				this.yes = yes;
			}
			
			public void setNo(int no){
				this.no = no;
			}

			private double getEntropy() {
				return -1 * (this.yes / (this.yes + this.no)) * (Math.log((double) (this.yes / (this.yes + this.no)))/Math.log(2))
						- 1 * (this.no / (this.yes + this.no)) * (Math.log((double) (this.no / (this.yes + this.no)))/Math.log(2));
			}
		}

		public double queryLabelValue(Record record) throws Exception {
			if (this.leftNode == null && this.rightNode == null) {
				return this.askIncreasePorbability;
			} else if (this.leftNode != null && this.rightNode != null) {
				if (getDecision(record, this.nodeId)) {
					return this.leftNode.queryLabelValue(record);
				} else {
					return this.rightNode.queryLabelValue(record);
				}
			} else {
				return -1;
			}
		}
		
	}

	public static class Record {
		String symbol;
		Date tickTime;
		double askPrice;
		double bidPrice;

		// features
		double avgAskPrice;
		double maxAskPrice;
		double minAskPrice;
		double avgBidPrice;
		double maxBidPrice;
		double minBidPrice;
		double avgSpread;
		double maxSpread;
		double minSpread;
		// String usdJPYAvg; // Has not been implemented
		// String audUSDAvg; // Has not been implemented
		// String gbpUSDAvg; // Has not been implemented

		public static final int NO_OF_FEATURES = 9;

		// label
		String askDirectionality; // directionality of eurUSD

		static final DateFormat df = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");

		public Record(String[] record) throws ParseException {
			this.symbol = record[0];
			this.tickTime = df.parse(record[1]);
			this.askPrice = Double.parseDouble(record[2]);
			this.bidPrice = Double.parseDouble(record[3]);
			this.avgAskPrice = Double.parseDouble(record[4]);
			this.maxAskPrice = Double.parseDouble(record[5]);
			this.minAskPrice = Double.parseDouble(record[6]);
			this.avgBidPrice = Double.parseDouble(record[7]);
			this.maxBidPrice = Double.parseDouble(record[8]);
			this.minBidPrice = Double.parseDouble(record[9]);
			this.avgSpread = Double.parseDouble(record[10]);
			this.maxSpread = Double.parseDouble(record[11]);
			this.minSpread = Double.parseDouble(record[12]);
			//this.usdJPY = record[13];
			//this.audUSD = record[14];
			//this.gbpUSD = record[15];
			this.askDirectionality = record[13];
		}
		
		public static boolean isFeatureDiscrete(int id) throws Exception{
			if(id >= 1 && id <= 9){
				return false;
			}else if(id >= 10 && id <= 12){
				return true;
			}else{
				throw new Exception("Invalid id exception");
			}
		}

		public double getContinousFeature(int id) throws Exception {
			switch (id) {
			case 1:
				return this.avgAskPrice;
			case 2:
				return this.maxAskPrice;
			case 3:
				return this.minAskPrice;
			case 4:
				return this.avgBidPrice;
			case 5:
				return this.maxBidPrice;
			case 6:
				return this.minBidPrice;
			case 7:
				return this.avgSpread;
			case 8:
				return this.maxSpread;
			case 9:
				return this.minSpread;
			default:
				throw new Exception("Invalid id exception");
			}
		}
		
		public boolean getDiscreteFeature(int id) throws Exception {
			switch (id) {
			case 10:
				//return this.usdJPY;
				throw new Exception("Not implemented exception");
			case 11:
				//return this.audUSD;
				throw new Exception("Not implemented exception");
			case 12:
				//return this.gbpUSD;
				throw new Exception("Not implemented exception");
			default:
				throw new Exception("Invalid feature ID");
			}
		}

		public String getLabel() {
			return this.askDirectionality;
		}

		public String toString() {
			StringBuilder strBuilder = new StringBuilder();
			strBuilder.append(this.symbol).append(COMMA).append(df.format(this.tickTime)).append(COMMA)
					.append(this.askPrice).append(COMMA).append(this.bidPrice);
			if (this.avgAskPrice != 0)
				strBuilder.append(COMMA).append(this.avgAskPrice);
			if (this.maxAskPrice != 0)
				strBuilder.append(COMMA).append(this.maxAskPrice);
			if (this.minAskPrice != 0)
				strBuilder.append(COMMA).append(this.minAskPrice);
			if (this.avgBidPrice != 0)
				strBuilder.append(COMMA).append(this.avgBidPrice);
			if (this.maxBidPrice != 0)
				strBuilder.append(COMMA).append(this.maxBidPrice);
			if (this.minBidPrice != 0)
				strBuilder.append(COMMA).append(this.minBidPrice);
			if (this.avgSpread != 0)
				strBuilder.append(COMMA).append(this.avgSpread);
			if (this.maxSpread != 0)
				strBuilder.append(COMMA).append(this.maxSpread);
			if (this.minSpread != 0)
				strBuilder.append(COMMA).append(this.minSpread);
			if (this.askDirectionality != null)
				strBuilder.append(COMMA).append(this.askDirectionality);
			return strBuilder.toString();
		}
	}
	
	// get all the prepared EURUSD.csv files
	void getAllFiles(String inputPath) {
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

	public static void main(String[] args) {
		// Getting all the prepared data files
		DecisionTree decisionTree = new DecisionTree();
		ArrayList<Record> records = new ArrayList<Record>();
		String baseFolder = "C:/Users/Vinay Shankar/Documents/Vinay/CMU/2015Fall/11-676-BigData/Project/Data";
		decisionTree.getAllFiles(baseFolder);
		BufferedReader br = null;
		String line = "";
		for (File file : fileNames) {
			try {
				System.out.println("Filename: "+file.getName());
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
			// Creating and training the decision tree
			ForexTree learnedTree = new DecisionTree.ForexTree(records);
			
			// Testing the decision tree
			baseFolder = "C:/Users/Vinay Shankar/Documents/Vinay/CMU/2015Fall/11-676-BigData/Project/TestData";
			decisionTree.getAllFiles(baseFolder);
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
					predictedProbability = learnedTree.queryLabelValue(record);
					if(predictedProbability > 0.5){
						if(Integer.parseInt(record.getLabel()) == 1){
							correctYes++;
						}else{
							incorrectYes++;
						}
					}else{
						if(Integer.parseInt(record.getLabel()) == 0){
							correctNo++;
						}else{
							incorrectNo++;
						}
					}
				}
				br.close();
			}
			System.out.println("Total records tested: "+correctNo+correctYes+incorrectNo+incorrectYes);
			System.out.println("Correctly predicted ask price increase: "+correctYes);
			System.out.println("Incorrectly predicted ask price increase: "+incorrectYes);
			System.out.println("Correctly predicted ask price decrease: "+correctNo);
			System.out.println("Incorrectly predicted ask price decrease: "+incorrectNo);
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException pe) {
			pe.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}finally {
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
