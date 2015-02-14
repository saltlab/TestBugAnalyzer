package bugRepo;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Scanner;

import utils.Settings;

public class BugReportFrequencyAnalyzer {
	
	
	
	public static void main(String[] args) throws FileNotFoundException
	{
		System.out.println("Execution started at " +  new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));
		readAndWriteProdBugReports(readSet("testAllBugReports.txt"));
		System.out.println("Execution finished at " +  new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));
		
		
	}
	
	

	
	
	
	
	public static void readAndWriteProdBugReports(HashSet<String> testBugReports) throws FileNotFoundException
	{
		
		Formatter prodWriter = new Formatter("prodBugReportsProp.txt");
		Formatter testWriter = new Formatter("testBugReportsProp.txt");
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
                try {
					

					if(!testBugReports.contains(bugRepoID))
					{
						String xml = readFile(bugReport);
//						System.out.println(bugRepoID);
						JiraBugReport jBugReport = new JiraBugReport(xml, bugRepoID);
//					System.out.println(xml);
						if(jBugReport.parsed && jBugReport.type.equals("Bug") && jBugReport.resolution.equals("Fixed") && !jBugReport.component.equals("test") )
							prodWriter.format("%s,%d,%d,%d,%d,%d,%d,%d\n", jBugReport.key, jBugReport.getMinutesToFix(), jBugReport.timeEstimate, jBugReport.timeSpent,
									jBugReport.numberOfAuthors, jBugReport.numberOfComments, jBugReport.numberOfVotes, jBugReport.numberOfWatcher);
					}
					else
					{
						JiraBugReport jBugReport = new JiraBugReport(readFile(bugReport), bugRepoID);
						if(jBugReport.parsed && jBugReport.type.equals("Bug") && jBugReport.resolution.equals("Fixed"))
							testWriter.format("%s,%d,%d,%d,%d,%d,%d,%d\n", jBugReport.key, jBugReport.getMinutesToFix(), jBugReport.timeEstimate, jBugReport.timeSpent,
									jBugReport.numberOfAuthors, jBugReport.numberOfComments, jBugReport.numberOfVotes, jBugReport.numberOfWatcher);
					}
				}catch(Exception e)
				{
					System.out.println(bugRepoID);
				}
				
			}
			prodWriter.flush();
			testWriter.flush();
		}
		prodWriter.close();
		testWriter.close();
	}
	
	
	private static String readFile(File file) throws FileNotFoundException
	{
		Scanner sc = new Scanner(file);
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


