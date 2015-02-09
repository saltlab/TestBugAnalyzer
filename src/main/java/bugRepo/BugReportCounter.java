package bugRepo;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class BugReportCounter {
	
	final static String folderPath = "savedBugReports";
	
	public static void main(String[] args){
		
		File folder = new File(folderPath);
		String[] xmlNames = folder.list();
		
		countBugReports(xmlNames);
		
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
