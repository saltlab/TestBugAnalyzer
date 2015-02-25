package bugRepo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Scanner;

import utils.Settings;

public class BugReportDownloader {

	
	static String prodBugRepPath = "prodBugReports/";
//	public ArrayList<String> openListFile(String path) throws FileNotFoundException
//	{
//		ArrayList<String> bugIDList = new ArrayList<String>();
//		Scanner sc = new Scanner(new File(path));
//		while(sc.hasNextLine())
//		{
//			bugIDList.add(sc.nextLine());
//		}
//		
//		return bugIDList;
//	}
//	
	
	public static void writeBugReportToFile(String folderPath, String bugReportID, String bugRepoXML)
	{
		try {
			Formatter fr = new Formatter(folderPath + bugReportID + ".xml");
			fr.format("%s\n", bugRepoXML);
			fr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static boolean downloadBugRepo(String folderPath, String bugReportID)
	{
		
			String bugRepoXML;
			try {
				URL url = new URL(Settings.issuesApache + bugReportID + "/" + bugReportID + ".xml");
				
				BufferedReader in;
				
				File bugReportFile = new File(folderPath + bugReportID +  ".xml");
				if (!bugReportFile.exists())
				{
				
					
					URLConnection yc = url.openConnection();
					in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
					StringBuffer xml = new StringBuffer();
					String inputLine;
					while ((inputLine = in.readLine()) != null)
						xml.append(inputLine);
					in.close();
					bugRepoXML = xml.toString();
					
					if (!bugReportFile.exists())
						writeBugReportToFile(folderPath, bugReportID,bugRepoXML);
				
				}
				return true;
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
	}
	
	
	
	public static String downloadBugRepo(String bugReportID)
	{
		
			String bugRepoXML = "";
			try {
				URL url = new URL(Settings.issuesApache + bugReportID + "/" + bugReportID + ".xml");
				
				BufferedReader in;
				
					URLConnection yc = url.openConnection();
					in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
					StringBuffer xml = new StringBuffer();
					String inputLine;
					while ((inputLine = in.readLine()) != null)
						xml.append(inputLine);
					in.close();
					bugRepoXML = xml.toString();
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return bugRepoXML;
			
	}
	
	public static void main(String[] args) {
			
		
		int numberOfFailedInARow = 0;
		for (String project : Settings.jiraProjects)
		{
			
			File folderPath = new File(prodBugRepPath + project + "/");
			if(!folderPath.exists())
				folderPath.mkdirs();
			
			
			numberOfFailedInARow = 0;
			try{
					int id = 1;
					while (numberOfFailedInARow < 100)
					{
						if(downloadBugRepo(prodBugRepPath + project + "/", project +"-" + id))
							numberOfFailedInARow = 0;
						else
							numberOfFailedInARow ++;
						
						id++;
						
					}
					
					System.out.println(project+"-"+id);
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
			
			
			
	}
	
	
	
}
