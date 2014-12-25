package bugRepo;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.ArrayList;
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
	
	
	String[] Projects; //TODO for bug repo number
	RevCommit revCommit;
	String message;
	Date date;
	ArrayList<Patch> patchs;
	String bugReport;
	String bugRepoXML;
	JiraBugReport jiraBugReport;
	
	public Commit(RevCommit revCommit, String message, Date date, ArrayList<Patch> patchs)
	{
		this.revCommit = revCommit;
		this.message = message;
		this.date = date;
		this.patchs = patchs;
	}

	public boolean checkMessageForBugReport()
	{
		return message.matches(".*HBASE[- ]*\\d+[\\S\\s]*") || message.matches(".*HADOOP[- ]*\\d+[\\S\\s]*") ;
	}
	
	public String getHTTPAddress()
	{
		Pattern p = Pattern.compile("HBASE[- ]*\\d+");
		Matcher m = p.matcher(message);

		if (m.find()) {
			String bugNum = m.group(0).replace(" ", "");
		    return Settings.issuesApache + bugNum + "/" + bugNum + ".xml";
		}
		
		return "";
		
	}
	

	public void setJiraBugReport()
	{
		this.jiraBugReport = new JiraBugReport(bugRepoXML);
	}
	
	public void extractJiraBugReport()
	{
		if(checkMessageForBugReport())
		{
			getBugRepoXML();
			setJiraBugReport();
		}
	}
	
	
	public String getBugRepoXML()
	{
		URL url;
		try {
			url = new URL(getHTTPAddress());
			URLConnection yc = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
			StringBuffer xml = new StringBuffer();
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				xml.append(inputLine);
			in.close();
			this.bugRepoXML = xml.toString();
			return this.bugRepoXML;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
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
