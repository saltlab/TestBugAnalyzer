package bugRepo;

import java.util.ArrayList;

import org.eclipse.jgit.diff.EditList;

public class Patch {

	String oldFilePath;
	String newFilePath;
	ArrayList<ArrayList<EditedLines>> editedLinesList;
	
	public Patch(String oldFilePath, String newFilePath, ArrayList<ArrayList<EditedLines>> editedLinesList)
	{
		this.oldFilePath = oldFilePath;
		this.newFilePath = newFilePath;
		this.editedLinesList = editedLinesList;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "file: " + newFilePath + ", editedLines: " + editedLinesList;
	}
	
}
