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
		
		public static HashSet<Integer> FEATURES_USED = new HashSet<Integer>();

		public ForexTree(ArrayList<Record> records) {
			this.nodeId = initAndGetBestFeatureId(records);
			FEATURES_USED.add(this.nodeId);
			
			if(this.askIncreasePorbability != 1 && this.askIncreasePorbability != 0){
				ArrayList<Record> leftNodeRecords = new ArrayList<Record>(this.leftNodeSize);
				ArrayList<Record> rightNodeRecords = new ArrayList<Record>(this.rightNodeSize);
				
				for(Record r : records){
					if(r.getFeature(this.nodeId)){
						leftNodeRecords.add(r);
					}else{
						rightNodeRecords.add(r);
					}
				}
			
				this.leftNode = new ForexTree(leftNodeRecords);
				this.rightNode = new ForexTree(rightNodeRecords);
			}
		}

		private int initAndGetBestFeatureId(ArrayList<Record> records) {
			double minRemainingEntropy = 1;
			int minRemainingEntropyFeatureId = -1;

			for (int i = 1; i <= Record.NO_OF_FEATURES && !FEATURES_USED.contains(i); i++) {
				HashMap<Integer, EntropyData> data = getDataForEntropy(records, i);
				// now calculate entropy
				double remaingingEntropy = getRemainingEntropy(data.get(1), data.get(0));
				if (remaingingEntropy < minRemainingEntropy) {
					minRemainingEntropy = remaingingEntropy;
					minRemainingEntropyFeatureId = i;
					this.askIncreasePorbability = ((double) (data.get(1).getYes() + data.get(0).getYes()))
							/ records.size();
					this.leftNodeSize = data.get(1).getYes() + data.get(1).getNo();
					this.rightNodeSize = data.get(0).getYes() + data.get(0).getNo();
				}
			}

			return minRemainingEntropyFeatureId;
		}

		private HashMap<Integer, EntropyData> getDataForEntropy(ArrayList<Record> records, int featureId) {
			EntropyData yesData = new EntropyData();
			EntropyData noData = new EntropyData();
			for(Record r : records){
				if(r.getFeature(featureId)){
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
			private int yes;
			private int no;

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
				return -1 * (this.yes / (this.yes + this.no)) * Math.log((double) (this.yes / (this.yes + this.no)))
						- 1 * (this.no / (this.yes + this.no)) * Math.log((double) (this.no / (this.yes + this.no)));
			}
		}

		public double queryLabelValue(Record record) {
			if (leftNode == null && rightNode == null) {
				return this.askIncreasePorbability;
			} else if (leftNode != null && rightNode != null) {
				if (record.getFeature(nodeId)) {
					return leftNode.queryLabelValue(record);
				} else {
					return rightNode.queryLabelValue(record);
				}
			} else {
				return -1;
			}
		}
		
	}

	private class Record {
		String symbol;
		Date tickTime;
		float askPrice;
		float bidPrice;

		// features
		float avgAskPrice;
		float maxAskPrice;
		float minAskPrice;
		float avgBidPrice;
		float maxBidPrice;
		float minBidPrice;
		float avgSpread;
		float maxSpread;
		float minSpread;
		// float eurJpyAvg; // Has not been implemented

		public static final int NO_OF_FEATURES = 9;

		private static final float AVG_ASK_PRICE_THRESHOLD = 0;
		private static final float MAX_ASK_PRICE_THRESHOLD = 0;
		private static final float MIN_ASK_PRICE_THRESHOLD = 0;
		private static final float AVG_BID_PRICE_THRESHOLD = 0;
		private static final float MAX_BID_PRICE_THRESHOLD = 0;
		private static final float MIN_BID_PRICE_THRESHOLD = 0;
		private static final float AVG_SPREAD_THRESHOLD = 0;
		private static final float MAX_SPREAD_THRESHOLD = 0;
		private static final float MIN_SPREAD_THRESHOLD = 0;
		// label
		String askDirectionality;

		DateFormat df = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");

		public Record(String[] record) throws ParseException {
			this.symbol = record[0];
			this.tickTime = df.parse(record[1]);
			this.askPrice = Float.parseFloat(record[2]);
			this.bidPrice = Float.parseFloat(record[3]);
			this.avgAskPrice = Float.parseFloat(record[4]);
			this.maxAskPrice = Float.parseFloat(record[5]);
			this.minAskPrice = Float.parseFloat(record[6]);
			this.avgBidPrice = Float.parseFloat(record[7]);
			this.maxBidPrice = Float.parseFloat(record[8]);
			this.minBidPrice = Float.parseFloat(record[9]);
			this.avgSpread = Float.parseFloat(record[10]);
			this.maxSpread = Float.parseFloat(record[11]);
			this.minSpread = Float.parseFloat(record[12]);
			this.askDirectionality = record[13];
		}

		public boolean getFeature(int id) {
			switch (id) {
			case 1:
				return this.avgAskPrice > AVG_ASK_PRICE_THRESHOLD;
			case 2:
				return this.maxAskPrice > MAX_ASK_PRICE_THRESHOLD;
			case 3:
				return this.minAskPrice > MIN_ASK_PRICE_THRESHOLD;
			case 4:
				return this.avgBidPrice > AVG_BID_PRICE_THRESHOLD;
			case 5:
				return this.maxBidPrice > MAX_BID_PRICE_THRESHOLD;
			case 6:
				return this.minBidPrice > MIN_BID_PRICE_THRESHOLD;
			case 7:
				return this.avgSpread > AVG_SPREAD_THRESHOLD;
			case 8:
				return this.maxSpread > MAX_SPREAD_THRESHOLD;
			case 9:
				return this.minSpread > MIN_SPREAD_THRESHOLD;
			default:
				return false;
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
					if (tokens[0].toLowerCase().contains("prepared") && tokens[0].toLowerCase().contains("EURUSD"))
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
		String baseFolder = "/Users/vinayshankar/CMU/2015Fall/11-676-BigData/Project/Data";
		decisionTree.getAllFiles(baseFolder);
		BufferedReader br = null;
		String line = "";
		for (File file : fileNames) {
			try {
				br = new BufferedReader(new FileReader(file));
				while ((line = br.readLine()) != null) {
					String[] data = line.split(COMMA);
					records.add(decisionTree.new Record(data));
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
		
		// Creating and training the decision tree
		ForexTree learnedTree = new DecisionTree.ForexTree(records);
		
		// Testing the decision tree
		baseFolder = "/Users/vinayshankar/CMU/2015Fall/11-676-BigData/Project/TestData";
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
			try {
				br = new BufferedReader(new FileReader(file));
				while ((line = br.readLine()) != null) {
					String[] data = line.split(COMMA);
					record = decisionTree.new Record(data);
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

}
