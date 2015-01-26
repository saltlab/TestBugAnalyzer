package bugRepo;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class BugReportCounter {
	
	final static String folderPath = "savedBugReports";
	
	public static void main(String[] args){
		
		File folder = new File(folderPath);
		String[] xmlNames = folder.list();
		
		HashMap<String, Integer> bugRepoNum = new HashMap<String, Integer>();
		
		for (String bugRepo : xmlNames)
		{
			String projectName = bugRepo.split("-")[0];
			
			bugRepoNum.put(projectName, (bugRepoNum.get(projectName) != null ? bugRepoNum.get(projectName) : 0) + 1);
		}
		
		
		for(Map.Entry<String, Integer> entry : bugRepoNum.entrySet())
		{
			System.out.println(entry.getKey() + "," + entry.getValue());
		}
		
	}

}
