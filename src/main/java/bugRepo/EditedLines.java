package bugRepo;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.Edit.Type;
import org.eclipse.jgit.diff.RawText;

public class EditedLines {
	
	Edit edit;
	RawText a, b;
	ArrayList<String> removedLines = new ArrayList<String>();
	ArrayList<String> addedLines = new ArrayList<String>();
	
	ArrayList<LinePair> matches = new ArrayList<LinePair>();
	Patch patch;
	
	public ArrayList<String> addWholeStatementAndReturn(StringBuffer statements, int firstLine, RawText text)
	{
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_STATEMENTS);
		Block block = null;
		ArrayList<String> statementsArray = new ArrayList<String>();
		ArrayList<String> initialArray = new ArrayList<String>();
		initialArray.addAll(Arrays.asList(statements.toString().split("\n")));
		
		
		int lineNum = firstLine;
		
		try{
			while(block == null || block.statements().size() == 0)
			{
				parser.setSource(statements.toString().toCharArray());
				block = (Block) parser.createAST(null);
				
				if(block.statements().size() == 0)
				{
					statements.append(text.getString(lineNum));
					lineNum++;
				}
			}
			
			for( Object b : block.statements())
			{
				statementsArray.add(b.toString());
			}
			if(lineNum-firstLine != 0)
				System.out.println(lineNum - firstLine);
			return statementsArray;
			
		}catch(Exception e)
		{
			return initialArray;
		}
		
		
	}
	
	
	public EditedLines(Edit edit, RawText a, RawText b)
	{
		this.edit = edit;
		this.a = a;
		this.b = b;
		
		StringBuffer removedStatements = new StringBuffer();

		if(edit.getType() == Type.DELETE || edit.getType() == Type.REPLACE)
		{
			for(int i = edit.getBeginA(); i < edit.getEndA(); i++)
			{
				removedStatements.append(a.getString(i));
			}
		}
		
		removedLines.addAll(Arrays.asList(removedStatements.toString().split(";")));
		
		StringBuffer addedStatements = new StringBuffer();
		if(edit.getType() == Type.INSERT || edit.getType() == Type.REPLACE)
		{
			for(int i = edit.getBeginB(); i < edit.getEndB(); i++)
			{
				addedStatements.append(b.getString(i));
			}
		}
		
		addedLines.addAll(Arrays.asList(addedStatements.toString().split(";")));
		
		
		if(edit.getType() == Type.REPLACE)
		{
			matches = computeMatches();
		}
	}
	
	
	
	public ArrayList<LinePair> computeMatches()
	{
		ArrayList<String> added = (ArrayList<String>) addedLines.clone();
		ArrayList<String> removed = (ArrayList<String>) removedLines.clone();
		
		ArrayList<LinePair> matchResults = new ArrayList<LinePair>();
		
		for(String a : added )
		{
			int min = Integer.MAX_VALUE;
			String match = "";
			if (a.length() < 100)
			{
				for(String b : removed)
				{
					if(b.length() < 100 )
					{
						int minDist = minDistance(a, b);
						if(minDist < min)
						{
							min = minDist;
							match = b;
						}
					}
					
				}
				removed.remove(match);
			}
			matchResults.add(new LinePair(a, match));
			
		}
		for (String rmv : removed)
		{
			matchResults.add(new LinePair("",rmv));
		}
		
		return matchResults;
	}
	


	public Patch getPatch() {
		return patch;
	}




	
	private int minDistance(String word1, String word2) {
		// TODO with move operation
		int len1 = word1.length();
		int len2 = word2.length();
	 
		// len1+1, len2+1, because finally return dp[len1][len2]
		int[][] dp = new int[len1 + 1][len2 + 1];
	 
		for (int i = 0; i <= len1; i++) {
			dp[i][0] = i;
		}
	 
		for (int j = 0; j <= len2; j++) {
			dp[0][j] = j;
		}
	 
		//iterate though, and check last char
		for (int i = 0; i < len1; i++) {
			char c1 = word1.charAt(i);
			for (int j = 0; j < len2; j++) {
				char c2 = word2.charAt(j);
	 
				//if last two chars equal
				if (c1 == c2) {
					//update dp value for +1 length
					dp[i + 1][j + 1] = dp[i][j];
				} else {
					int replace = dp[i][j] + 1;
					int insert = dp[i][j + 1] + 1;
					int delete = dp[i + 1][j] + 1;
	 
					int min = replace > insert ? insert : replace;
					min = delete > min ? min : delete;
					dp[i + 1][j + 1] = min;
				}
			}
		}
	 
		return dp[len1][len2];
	}


	public void setPatch(Patch patch) {
		this.patch = patch;
	}




	class LinePair{
		String a, b;
		
		public LinePair(String a, String b)
		{
			this.a = a;
			this.b = b;
		}
	}


	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "removedLines: " + removedLines +", addedLines: "+addedLines;
	}
	

}
