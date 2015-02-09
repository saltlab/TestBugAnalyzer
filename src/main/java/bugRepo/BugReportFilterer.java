package bugRepo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class BugReportFilterer {

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		
		HashSet<String> productionBugReport = readSet("/Users/Arash/Desktop/QuantitativeAnalysis/nonTestBugReports.txt");
		
		HashSet<String> ignoreList = readSet("/Users/Arash/Desktop/QuantitativeAnalysis/sampledNonProd.txt");
//		HashSet<String> ignoreList = new HashSet<String>();
		
		Scanner jiraSc = new Scanner(new File("/Users/Arash/Desktop/QuantitativeAnalysis/JiraTestComponentBugReports.txt"));
		
		ArrayList<String> testBugReports = new ArrayList<String>();
		ArrayList<String> jiraSampled = new ArrayList<String>();
		while(jiraSc.hasNext())
		{
			String bugID = jiraSc.next();
			jiraSampled.add(bugID);
			if (!productionBugReport.contains(bugID) || ignoreList.contains(bugID) )
				testBugReports.add(bugID);
		}
		
		String[] jiraArray = new String[jiraSampled.size()];
		BugReportCounter.countBugReports(jiraSampled.toArray(jiraArray));
		System.out.println("********");
		String[] testBugReportsArray = new String[testBugReports.size()];
		BugReportCounter.countBugReports(testBugReports.toArray(testBugReportsArray));
		
		

	}

	private static HashSet<String> readSet (String path)
			throws FileNotFoundException {
		
		Scanner prodSc = new Scanner(new File(path));
		HashSet<String> set = new HashSet<String>();
		while(prodSc.hasNext())
		{
			set.add(prodSc.next());
		}
		return set;
	}
	
	

}
