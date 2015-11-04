package personal.vinay.bigdata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SetupCassandra {

	private static ArrayList<File> fileNames = new ArrayList<File>();

	public static void main(String[] args) {

		SetupCassandra setup = new SetupCassandra();
		String baseFolder = "/mnt/hgfs/vinay-windows/CMU/2015Fall/11-676-BigData/Project/TestData";
		setup.getAllFiles(baseFolder);

		try {
			String sql;
			FileWriter foutstream = new FileWriter("/mnt/hgfs/vinay-windows/CMU/2015Fall/11-676-BigData/Project/testqueries.txt");
			BufferedWriter out = new BufferedWriter(foutstream);
			for (File file : fileNames) {
				System.out.println("Filename:"+file.getAbsolutePath());
				sql = "copy records (symbol, datetime, askprice, bidprice, avgaskprice, maxaskprice, minaskprice, avgbidprice, maxbidprice, minbidprice, avgspread, maxspread, minspread, askdirectionality) from '" + file.getAbsolutePath() + "';";
				out.write(sql + "\n");
			}
			out.close();
			
			String sysEnvStr = System.getenv("CASSANDRA_HOME");
			Process p = Runtime.getRuntime().exec(sysEnvStr + "/bin/cqlsh -k testingdata -f /mnt/hgfs/vinay-windows/CMU/2015Fall/11-676-BigData/Project/testqueries.txt");
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String s = "";
			while ((s = stdInput.readLine()) != null) {
				System.out.println(s);
			}
			while ((s = stdError.readLine()) != null) {
				System.out.println("Std ERROR : " + s);
			}

		} catch (Exception e) {
			e.printStackTrace();
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
					break;
				}
			} else if (file.isDirectory()) {
				getAllFiles(file.getAbsolutePath());
			}
		}
	}

}
