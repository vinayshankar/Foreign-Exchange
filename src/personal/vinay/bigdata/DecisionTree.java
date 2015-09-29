package personal.vinay.bigdata;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class DecisionTree {
	
	public static final String COMMA = ",";
	public static final String NEW_LINE = "\n";
	public static final String FORWARD_SLASH = "/";

	public class ForexTree {

		private ForexTree leftNode;
		private ForexTree rightNode;

		private int nodeId; // feature ID

		private int leftNodeSize;
		private int rightNodeSize;

		public ForexTree(ArrayList<Record> records) {
			getBestFeature(records);
		}

		private int getBestFeature(ArrayList<Record> records) {
			// iterate over the records, check each feature value (yes or no)
			// and for yes and no, check number of label yes and no
			// now calculate entropy

			// repeat for all features

			// choose the feature with the most information gain
			return 0;
		}

		private HashMap<Integer, EntropyData> getDataForEntropy(ArrayList<Record> records) {
			
		}

		private double getRemainingEntropy(EntropyData yesEntropyData, EntropyData noEntropyData) {
			return ((yesEntropyData.getNo() + yesEntropyData.getYes()) / (leftNodeSize + rightNodeSize))
					* yesEntropyData.getEntropy()
					+ ((noEntropyData.getNo() + noEntropyData.getYes()) / leftNodeSize + rightNodeSize)
							* noEntropyData.getEntropy();
		}

		private class EntropyData {
			private int yes;
			private int no;

			public int getYes() {
				return yes;
			}

			public int getNo() {
				return no;
			}

			public EntropyData(int yes, int no) {
				this.yes = yes;
				this.no = no;
			}

			private double getEntropy() {
				return -1 * (yes / (yes + no)) * Math.log((double) (yes / (yes + no)))
						- 1 * (no / (yes + no)) * Math.log((double) (no / (yes + no)));
			}
		}
	}
	
	class Record {
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

		public Record() {
		}

		public String getSymbol() {
			return symbol;
		}

		public void setSymbol(String symbol) {
			this.symbol = symbol;
		}

		public Date getTickTime() {
			return tickTime;
		}

		public void setTickTime(Date tickTime) {
			this.tickTime = tickTime;
		}

		public float getAskPrice() {
			return askPrice;
		}

		public void setAskPrice(float askPrice) {
			this.askPrice = askPrice;
		}

		public float getBidPrice() {
			return bidPrice;
		}

		public void setBidPrice(float bidPrice) {
			this.bidPrice = bidPrice;
		}

		public float getAvgSpread() {
			return avgSpread;
		}

		public void setAvgSpread(float avgSpread) {
			this.avgSpread = avgSpread;
		}

		public float getMaxSpread() {
			return maxSpread;
		}

		public void setMaxSpread(float maxSpread) {
			this.maxSpread = maxSpread;
		}

		public float getMinSpread() {
			return minSpread;
		}

		public void setMinSpread(float minSpread) {
			this.minSpread = minSpread;
		}

		public String getAskDirectionality() {
			return askDirectionality;
		}

		public void setAskDirectionality(String askDirectionality) {
			this.askDirectionality = askDirectionality;
		}

		public float getAvgAskPrice() {
			return avgAskPrice;
		}

		public void setAvgAskPrice(float avgAskPrice) {
			this.avgAskPrice = avgAskPrice;
		}

		public float getMaxAskPrice() {
			return maxAskPrice;
		}

		public void setMaxAskPrice(float maxAskPrice) {
			this.maxAskPrice = maxAskPrice;
		}

		public float getMinAskPrice() {
			return minAskPrice;
		}

		public void setMinAskPrice(float minAskPrice) {
			this.minAskPrice = minAskPrice;
		}

		public float getAvgBidPrice() {
			return avgBidPrice;
		}

		public void setAvgBidPrice(float avgBidPrice) {
			this.avgBidPrice = avgBidPrice;
		}

		public float getMaxBidPrice() {
			return maxBidPrice;
		}

		public void setMaxBidPrice(float maxBidPrice) {
			this.maxBidPrice = maxBidPrice;
		}

		public float getMinBidPrice() {
			return minBidPrice;
		}

		public void setMinBidPrice(float minBidPrice) {
			this.minBidPrice = minBidPrice;
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

	public static void main(String[] args) {

	}

}
