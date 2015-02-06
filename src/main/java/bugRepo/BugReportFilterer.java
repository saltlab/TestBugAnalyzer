package bugRepo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;

public class BugReportFilterer {

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		
		HashSet<String> productionBugReport = new HashSet<String>();
		
		System.out.println("start");
		Scanner prodSc = new Scanner(new File("/Users/Arash/Desktop/QuantitativeAnalysis/nonTestBugReports.txt"));
		while(prodSc.hasNext())
		{
			productionBugReport.add(prodSc.next());
		}
		
		Scanner jiraSc = new Scanner(new File("/Users/Arash/Desktop/QuantitativeAnalysis/jiraTestBugReports.txt"));
		
		while(jiraSc.hasNext())
		{
			String bugID = jiraSc.next();
			if (!productionBugReport.contains(bugID))
				System.out.println(bugID);
		}
		
		

	}

}
