package bugRepo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Scanner;

import utils.Settings;

public class BugReportFrequencyAnalyzer {
	
	
	
	public static void main(String[] args)
	{
		
	}
	
	

	
	
	
	
	public static void readAndWriteProdBugReports(HashSet<String> testBugReports) throws FileNotFoundException
	{
		
		Formatter prodWriter = new Formatter("prodBugReportsProp.csv");
		Formatter testWriter = new Formatter("testBugReportsProp.csv");
		prodWriter.format("bugReport,timeToFix,timeEstimate,timeSpent,numberOfUniqueAuthors,numberOfComments,votes,watches\n");
		testWriter.format("bugReport,timeToFix,timeEstimate,timeSpent,numberOfUniqueAuthors,numberOfComments,votes,watches\n");
		
		File root = new File(Settings.allBugReportPath);
		File[] projects = root.listFiles();
		
		for (File project : projects)
		{
			File[] bugReports = project.listFiles();
			for (File bugReport : bugReports)
			{
				String bugRepoID = bugReport.getName().split(".xml")[0];
				if(!testBugReports.contains(bugRepoID))
				{
					JiraBugReport jBugReport = new JiraBugReport(readFile(bugReport.getAbsolutePath()));
					if(jBugReport.type.equals("Bug") && jBugReport.resolution.equals("Fixed") && !jBugReport.component.equals("test") )
						prodWriter.format("%s,%d,%d,%d,%d,%d,%d,%d\n", jBugReport.key, jBugReport.getMinutesToFix(), jBugReport.timeEstimate, jBugReport.timeSpent,
							jBugReport.numberOfAuthors, jBugReport.numberOfComments, jBugReport.numberOfVotes, jBugReport.numberOfWatcher);
				}
				else
				{
					JiraBugReport jBugReport = new JiraBugReport(readFile(bugReport.getAbsolutePath()));
					if(jBugReport.type.equals("Bug") && jBugReport.resolution.equals("Fixed"))
						testWriter.format("%s,%d,%d,%d,%d,%d,%d,%d\n", jBugReport.key, jBugReport.getMinutesToFix(), jBugReport.timeEstimate, jBugReport.timeSpent,
							jBugReport.numberOfAuthors, jBugReport.numberOfComments, jBugReport.numberOfVotes, jBugReport.numberOfWatcher);
				}
				
			}
			prodWriter.flush();
			testWriter.flush();
		}
		prodWriter.close();
		testWriter.close();
	}
	
	
	private static String readFile(String path)
	{
		Scanner sc = new Scanner(path);
		StringBuffer sb = new StringBuffer();
		while(sc.hasNext())
			sb.append(sc.nextLine());
		
		return sb.toString();
	}
	
	private static HashSet<String> readSet (String path) throws FileNotFoundException {
		
		Scanner prodSc = new Scanner(new File(path));
		HashSet<String> set = new HashSet<String>();
		while(prodSc.hasNext())
		{
			set.add(prodSc.next());
		}
		return set;
	}
	
	

}


