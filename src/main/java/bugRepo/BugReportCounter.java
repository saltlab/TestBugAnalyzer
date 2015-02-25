package bugRepo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import utils.Settings;

public class BugReportCounter {
	
	final static String folderPath = "savedBugReports";
	
	public static void main(String[] args) throws FileNotFoundException{
		
//		File folder = new File(folderPath);
//		String[] xmlNames = folder.list();
//		
//		countBugReports(xmlNames);
		
		writeToFile(readBugIDs());
		
	}
	
	public static ArrayList<String> readBugIDs() throws FileNotFoundException
	{
		Scanner sc = new Scanner(new File("/Users/Arash/Desktop/QuantitativeAnalysis/commitTests.txt"));
		ArrayList<String> bugIDs = new ArrayList<String>();
		while (sc.hasNext())
			bugIDs.add(sc.next());
		
		Collections.shuffle(bugIDs);
		return bugIDs;
			
	}
	
	
	public static void writeToFile(ArrayList<String> bugIDs) throws FileNotFoundException
	{
		Formatter fr = new Formatter("/Users/Arash/Desktop/QuantitativeAnalysis/shuffledCommitTests.txt");
		fr.format("ID,Link\n");
		for (String bugID : bugIDs)
		{
			String link = "https://issues.apache.org/jira/browse/" + bugID ;
			fr.format("%s,%s\n", bugID,link);
		}
		fr.flush();
		fr.close();
	}
	
	public static void countBugReports(String[] xmlNames) {
		HashMap<String, Integer> bugRepoNum = new HashMap<String, Integer>();
		
		for (String bugRepo : xmlNames)
		{
			try{
			String projectName = bugRepo.split("-")[0];
			
			bugRepoNum.put(projectName, (bugRepoNum.get(projectName) != null ? bugRepoNum.get(projectName) : 0) + 1);
			}catch(Exception e)
			{
				System.out.println(bugRepo);
//				e.printStackTrace();
			}
		}
		
		
		for(Map.Entry<String, Integer> entry : bugRepoNum.entrySet())
		{
			System.out.println(entry.getKey() + "," + entry.getValue());
		}
	}

}
