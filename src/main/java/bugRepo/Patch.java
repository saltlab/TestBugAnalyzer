package bugRepo;

import java.util.ArrayList;

import org.eclipse.jgit.diff.EditList;

public class Patch {

	String oldFilePath;
	String newFilePath;
	ArrayList<ArrayList<EditedLines>> editedLinesList;
	Commit commit;
	
	public Patch(String oldFilePath, String newFilePath, ArrayList<ArrayList<EditedLines>> editedLinesList)
	{
		this.oldFilePath = oldFilePath;
		this.newFilePath = newFilePath;
		this.editedLinesList = editedLinesList;
	}
	
	
	
	public Commit getCommit() {
		return commit;
	}



	public void setCommit(Commit commit) {
		this.commit = commit;
	}



	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "file: " + newFilePath + ", editedLines: " + editedLinesList;
	}
	
}
