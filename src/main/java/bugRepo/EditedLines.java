package bugRepo;

import java.util.ArrayList;

import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.Edit.Type;
import org.eclipse.jgit.diff.RawText;

public class EditedLines {
	
	Edit edit;
	ArrayList<String> removedLines = new ArrayList<String>();
	ArrayList<String> addedLines = new ArrayList<String>();
	Patch patch;
	
	
	public EditedLines(Edit edit, RawText a, RawText b)
	{
		this.edit = edit;
		
		if(edit.getType() == Type.DELETE || edit.getType() == Type.REPLACE)
		{
			for(int i = edit.getBeginA(); i < edit.getEndA(); i++)
			{
				removedLines.add(a.getString(i));
			}
		}
		
		if(edit.getType() == Type.INSERT || edit.getType() == Type.REPLACE)
		{
			for(int i = edit.getBeginB(); i < edit.getEndB(); i++)
			{
				addedLines.add(b.getString(i));
			}
		}
	}
	
	
	
	


	public Patch getPatch() {
		return patch;
	}






	public void setPatch(Patch patch) {
		this.patch = patch;
	}






	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "removedLines: " + removedLines +", addedLines: "+addedLines;
	}
	

}
