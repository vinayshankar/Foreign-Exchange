package personal.vinay.bigdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PrepData {

	public static final String COMMA = ",";
	public static final String NEW_LINE = "\n";
	public static final String FORWARD_SLASH = "/";
	static ArrayList<File> fileNames = new ArrayList<File>();

	public static void main(String[] args) {
		PrepData prepData = new PrepData();
		String baseFolder = "/Users/vinayshankar/CMU/2015Fall/11-676-BigData/Project/TestData";
		prepData.getAllFiles(baseFolder);
		for (File file : fileNames) {
			prepData.processData(file);
		}
	}

	@SuppressWarnings("deprecation")
	void processData(File fileName) {
		ArrayList<Record> records = new ArrayList<Record>();
		ArrayList<ArrayList<Record>> recordsGroupedByMinute = new ArrayList<ArrayList<Record>>();

		BufferedReader br = null;
		String line = "";
		String comma = ",";

		Record tempRecord;

		PrepData prepData = new PrepData();

		try {
			br = new BufferedReader(new FileReader(fileName));
			while ((line = br.readLine()) != null) {
				String[] record = line.split(comma);
				tempRecord = prepData.new Record(record);
				if (records.size() == 0) {
					records.add(tempRecord);
				} else {
					if (tempRecord.getTickTime().getMinutes() == records.get(records.size() - 1).getTickTime()
							.getMinutes()) {
						records.add(tempRecord);
					} else {
						records = prepData.setMinPrice(records);
						records = prepData.setMaxPrice(records);
						records = prepData.setAvgPrice(records);
						records = prepData.setMinSpread(records);
						records = prepData.setMaxSpread(records);
						records = prepData.setAvgSpread(records);
						recordsGroupedByMinute.add(records);
						if (recordsGroupedByMinute.size() == 2) {
							prepData.writeToFile(prepData.setDirectionality(recordsGroupedByMinute),
									fileName.getAbsolutePath().toString().replace(".csv", "-prepared.csv"));
							recordsGroupedByMinute.remove(0);
						}
						records.clear();
						records.add(tempRecord);
					}
				}
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

	ArrayList<Record> setMinPrice(ArrayList<Record> records) {
		float minAskPrice = 9999;
		float minBidPrice = 9999;
		for (Record record : records) {
			if (record.getAskPrice() < minAskPrice) {
				minAskPrice = record.getAskPrice();
			}
			if (record.getBidPrice() < minBidPrice) {
				minBidPrice = record.getBidPrice();
			}
		}

		for (Record record : records) {
			record.setMinAskPrice(minAskPrice);
			record.setMinBidPrice(minBidPrice);
		}
		// System.out.println("Min Ask Price:"+minAskPrice);
		// System.out.println("Min Bid Price:"+minBidPrice);
		return records;
	}

	ArrayList<Record> setMaxPrice(ArrayList<Record> records) {
		float maxAskPrice = 0;
		float maxBidPrice = 0;
		for (Record record : records) {
			if (record.getAskPrice() > maxAskPrice) {
				maxAskPrice = record.getAskPrice();
			}
			if (record.getBidPrice() > maxBidPrice) {
				maxBidPrice = record.getBidPrice();
			}
		}

		for (Record record : records) {
			record.setMaxAskPrice(maxAskPrice);
			record.setMaxBidPrice(maxBidPrice);
		}
		// System.out.println("Max Ask Price:"+maxAskPrice);
		// System.out.println("Max Bid Price:"+maxBidPrice);
		return records;
	}

	ArrayList<Record> setAvgPrice(ArrayList<Record> records) {
		float avgAskPrice = 0;
		float sumAskPrice = 0;
		float avgBidPrice = 0;
		float sumBidPrice = 0;
		for (Record record : records) {
			sumAskPrice += record.getAskPrice();
			sumBidPrice += record.getBidPrice();
		}

		avgAskPrice = sumAskPrice / records.size();
		avgBidPrice = sumBidPrice / records.size();

		for (Record record : records) {
			record.setAvgAskPrice(avgAskPrice);
			record.setAvgBidPrice(avgBidPrice);
		}
		// System.out.println("Avg Ask Price:"+avgAskPrice);
		// System.out.println("Avg Bid Price:"+avgBidPrice);
		return records;
	}

	ArrayList<Record> setMinSpread(ArrayList<Record> records) {
		float minSpread = 9999;
		for (Record record : records) {
			if ((record.getBidPrice() - record.getAskPrice()) < minSpread) {
				minSpread = record.getBidPrice() - record.getAskPrice();
			}
		}

		for (Record record : records) {
			record.setMinSpread(minSpread);
		}
		// System.out.println("Min Spread:"+minSpread);
		return records;
	}

	ArrayList<Record> setMaxSpread(ArrayList<Record> records) {
		float maxSpread = 0;
		for (Record record : records) {
			if ((record.getBidPrice() - record.getAskPrice()) > maxSpread) {
				maxSpread = record.getBidPrice() - record.getAskPrice();
			}
		}

		for (Record record : records) {
			record.setMaxSpread(maxSpread);
		}
		// System.out.println("Max Spread:"+maxSpread);
		return records;
	}

	ArrayList<Record> setAvgSpread(ArrayList<Record> records) {
		float avgSpread = 0;
		float sumSpread = 0;
		for (Record record : records) {
			sumSpread += (record.getBidPrice() - record.getAskPrice());
		}

		avgSpread = sumSpread / records.size();

		for (Record record : records) {
			record.setAvgSpread(avgSpread);
		}
		// System.out.println("Avg Spread:"+avgSpread);
		return records;
	}

	ArrayList<Record> setDirectionality(ArrayList<ArrayList<Record>> recordsGroupedByMinute) {
		ArrayList<Record> firstMinuteRecords = recordsGroupedByMinute.get(0);
		ArrayList<Record> secondMinuteRecords = recordsGroupedByMinute.get(1);
		int directionality = 0;
		if (firstMinuteRecords.get(0).getAskPrice() < secondMinuteRecords.get(0).getAskPrice()) {
			directionality = 1;
		}
		for (Record record : firstMinuteRecords) {
			record.setAskDirectionality(String.valueOf(directionality));
		}
		return firstMinuteRecords;
	}

	void getAllFiles(String inputPath) {
		File directory = new File(inputPath);
		File[] fList = directory.listFiles();
		assert fList != null;
		for (File file : fList) {
			if (file.isFile()) {
				String[] tokens = file.toString().split("\\.(?=[^\\.]+$)");
				if (tokens[1].equalsIgnoreCase("csv")) {
					if (!(tokens[0].toLowerCase().contains("prepared")))
						fileNames.add(file);
				}
			} else if (file.isDirectory()) {
				getAllFiles(file.getAbsolutePath());
			}
		}
	}

	boolean writeToFile(ArrayList<Record> records, String fileName) throws IOException {
		FileWriter fileWriter = new FileWriter(fileName, true);
		for (Record record : records) {
			fileWriter.append(record.toString());
			fileWriter.append(NEW_LINE);
		}
		fileWriter.close();
		return false;
	}

	class Record {
		String symbol;
		Date tickTime;
		float askPrice;
		float bidPrice;

		// new features
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

}
