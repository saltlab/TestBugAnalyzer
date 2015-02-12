package bugRepo;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;

import utils.Settings;

public class Commit {
	
	
	RevCommit revCommit;
	String message;
	Date date;
	ArrayList<Patch> patchs;
	String bugRepoXML;
	JiraBugReport jiraBugReport;
	String bugReportID;
	
	public Commit(RevCommit revCommit, String message, Date date, ArrayList<Patch> patchs)
	{
		this.revCommit = revCommit;
		this.message = message;
		this.date = date;
		this.patchs = patchs;
	}

	public boolean checkMessageForBugReport()
	{
		
		return message.matches(".*(^|[^A-Za-z\\d])" + Settings.getJiraProjectsRegex() + "[- ]*\\d{1,5}([^A-Za-z\\d\\.]|\\.[^\\d]|$)[\\S\\s]*") ;
	}
	
	
	
	public String getHTTPAddress()
	{
		getBugRepoID();
		
		return Settings.issuesApache + bugReportID + "/" + bugReportID + ".xml";
		
	}

	public String getBugRepoID() {
		Pattern p = Pattern.compile(Settings.getJiraProjectsRegex()+"[ ]*-[ ]*\\d+");
		Matcher m = p.matcher(message);

		if (m.find()) {
			bugReportID = m.group(0).replace(" ", "");
		}else
		{
			
			p = Pattern.compile(Settings.getJiraProjectsRegex()+"[ ]+\\d{1,5}");
			m = p.matcher(message);
			
			if (m.find()) {
				bugReportID = m.group(0).replaceFirst(" ", "-");
				bugReportID = bugReportID.replaceAll(" ", "");
			}
			else
			{
				p = Pattern.compile(Settings.getJiraProjectsRegex()+"\\d{1,5}");
				m = p.matcher(message);
				
				if (m.find()) {
					String id = m.group(0).replaceAll(" ", "");
					bugReportID = id.split("\\d+")[0]+"-"+id.split("[A-Za-z]+")[1];
				}
			}
		}
		
		return bugReportID;
	}
	

	public void setJiraBugReport()
	{
		if (bugRepoXML != null)
			this.jiraBugReport = new JiraBugReport(bugRepoXML);
	}
	
	public void extractJiraBugReport()
	{
		if(checkMessageForBugReport())
		{
			getBugRepoXML();
			if(bugRepoXML != null)
				setJiraBugReport();
		}
	}
	
	public int getNumberOfEditedLines()
	{
		int num = 0;
		for(Patch patch : this.patchs)
		{
			for(ArrayList<EditedLines> editedLinesList : patch.editedLinesList)
			{
				for(EditedLines editedLines : editedLinesList)
				{
					num += editedLines.addedLines.size();
					num += editedLines.removedLines.size();
				}
			}
		}
		
		return num;
	}
	
	
	public String getBugRepoXML()
	{
		
		URL url = null;
		try {
			
			BufferedReader in;
			
			url = new URL(getHTTPAddress());
			File bugReportFile = new File(Settings.allBugReportPath +bugReportID.split("-")[0] + File.separatorChar+ bugReportID + ".xml");
			if (bugReportFile.exists())
			{
				in = new BufferedReader(new FileReader(bugReportFile));
			}
			else
			{
				System.out.println(bugReportFile.getAbsolutePath() + " does not exists !!");
				URLConnection yc = url.openConnection();
				in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
			}
			
			
			StringBuffer xml = new StringBuffer();
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				xml.append(inputLine);
			in.close();
			this.bugRepoXML = xml.toString();
			
			if (!bugReportFile.exists())
				writeBugReportToFile(Settings.bugReportPath);
			
			return this.bugRepoXML;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("message : " + this.message);
			System.out.println("url : "+ url);
			System.out.println("id : " + bugReportID);
		}
		
		return "";
	}
	
	
	public void writeBugReportToFile(String path)
	{
		try {
			Formatter fr = new Formatter(path + bugReportID + ".xml");
			fr.format("%s\n", this.bugRepoXML);
			fr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "message: "+ message + ", patches: "+ patchs;
	}
	
	
	public String formatDiffs(Repository repo) {
		// TODO Auto-generated method stub
		
		
		StringBuffer result = new StringBuffer();
		result.append("message:" + revCommit.getFullMessage() + "\n");
		try {
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			DiffFormatter df = new DiffFormatter(out);
			df.setRepository(repo);
			df.setDiffComparator(RawTextComparator.DEFAULT);
			df.setDetectRenames(true);
			
			
			
			for(RevCommit parentCommit : revCommit.getParents())
			{
				
				List<DiffEntry> diffs = df.scan(revCommit.getTree(), parentCommit.getTree());
				
				for (DiffEntry diff : diffs) 
				{
					
//        		System.out.println("changeType=" + diff.getChangeType().name()
//        				+ " \n newMode=" + diff.getNewMode().getBits()
//        				+ " \nnewPath=" + diff.getNewPath()         
//        				+ " \nold path " + diff.getOldPath()
//        				+ " \nHash code " + diff.hashCode()
//        				+ " \nString  " + diff.toString()
//        				+ " \nchange " + diff.getChangeType().toString()
//        				);
//        		
//        		
//        		
        		df.format(diff);
        		
				String diffText = out.toString("UTF-8");
				result.append(diffText);
				}
				
			}
			
			
			df.release();
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
		return result.toString();
	}
}
