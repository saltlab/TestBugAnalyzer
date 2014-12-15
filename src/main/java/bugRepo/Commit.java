package bugRepo;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;

public class Commit {
	
	RevCommit revCommit;
	String message;
	Date date;
	ArrayList<Patch> patchs;
	String bugReport;
	
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
