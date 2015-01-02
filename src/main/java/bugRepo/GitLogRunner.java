package bugRepo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.Edit.Type;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.DepthWalk.RevWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.xml.sax.SAXException;

import bugRepo.EditedLines.LinePair;
import utils.Project;
import utils.ProjectRunner;
import FindBug.Automater.MultipleProjectRunner;


public class GitLogRunner {
	
	
	
	public boolean checkFileIsInTestDir(ArrayList<String> testDirs, List<DiffEntry> changedFiles)
	{
		for(DiffEntry changedFile : changedFiles)
		{
			boolean flag = false;
			for(String testDir : testDirs)
			{
				if(changedFile.getNewPath().contains(testDir+ File.separatorChar))
				{
					flag = true;
					break;
				}
				
				if(flag == false)
					return false;
			}
			
		}
		
		return true;
		
	}
	
	
	public ArrayList<EditedLines> extractLines(EditList editList, RawText a, RawText b)
	{
		
		ArrayList<EditedLines> editedLinesList = new ArrayList<EditedLines>();
		
		for(Edit edit : editList)
		{
			EditedLines editedLines = new EditedLines(edit, a, b);
			editedLinesList.add(editedLines);
		}
		
		return editedLinesList;

	}
	
	
	public ArrayList<Commit> getTestCommits(Git git, ArrayList<String> testDirs) throws Exception
	{
		ArrayList<Commit> commits = new ArrayList<Commit>();
			
		Repository repo = git.getRepository();
        
        LogCommand log = git.log();
        
        ObjectId lastCommitId = repo.resolve(Constants.HEAD);
        RevWalk rw = new RevWalk(repo,1000);
        RevCommit parent = rw.parseCommit(lastCommitId);
        
        rw.sort(RevSort.COMMIT_TIME_DESC);
        rw.markStart(parent);
         
        
//        log.setMaxCount();
        Iterable<RevCommit> logMsgs = log.call();
        
        long numberOfCommits = 0;
        for (RevCommit revCommit : logMsgs) {
        	numberOfCommits++;
//            System.out.println("\n\n\n\n\n\n\n\n\n\n----------------------------------------");
//            System.out.println("commit    "  + revCommit);
//            System.out.println("commit.toObjectId()    "  + revCommit.toObjectId());
//            System.out.println(" commit.getAuthorIdent().getName()         "  + revCommit.getAuthorIdent().getName());
//            System.out.println(""  + revCommit.getAuthorIdent().getWhen());
//            System.out.println(" commit.getFullMessage())--- " + revCommit.getFullMessage());
//            System.out.println("---DIF STARTING ------------------------");
             
            
             
            ByteArrayOutputStream out = new ByteArrayOutputStream();
             
            DiffFormatter df = new DiffFormatter(out);
            df.setRepository(repo);
            df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
            df.setDetectRenames(true);
  
  
  
//            System.out.println(revCommit.getParentCount());
//            if(revCommit.getParentCount() <= 0 )
//            	continue;
            
            for(RevCommit parentCommit : revCommit.getParents())
            {
            	
            	List<DiffEntry> diffs = df.scan(revCommit.getTree(), parentCommit.getTree());
            	
            	
            	
            	
            	if(checkFileIsInTestDir(testDirs, diffs))
            	{
            		ArrayList<Patch> patches = new ArrayList<Patch>();
            		
            		for (DiffEntry diff : diffs) 
            		{
            			
            			DiffFormatter.FormatResult fr = df.createFormatResult(diff);
            			RawText rawTextA = fr.a;
            			RawText rawTextB = fr.b;
            			
            			ArrayList<ArrayList<EditedLines>> editedLineLists = new ArrayList<ArrayList<EditedLines>>();
            			FileHeader fh = df.toFileHeader(diff);
            			List<HunkHeader> hunkHeaders = (List<HunkHeader>) fh.getHunks();
            			for(HunkHeader hunkHeader : hunkHeaders)
            			{
            				EditList el = hunkHeader.toEditList();
            				editedLineLists.add(extractLines(el, rawTextA, rawTextB));
            			}
            			
            			Patch patch = new Patch(diff.getOldPath(), diff.getNewPath(), editedLineLists);
            			
            			patches.add(patch);
            			
//            		System.out.println("changeType=" + diff.getChangeType().name()
//            				+ " \n newMode=" + diff.getNewMode().getBits()
//            				+ " \nnewPath=" + diff.getNewPath()         
//            				+ " \nold path " + diff.getOldPath()
//            				+ " \nHash code " + diff.hashCode()
//            				+ " \nString  " + diff.toString()
//            				+ " \nchange " + diff.getChangeType().toString()
//            				);
//            		
//            		
//            		
//            		df.format(diff);
//            		
//					String diffText = out.toString("UTF-8");
//					System.out.println(diffText);
            		}
            		
            		
            		// setting double links
            		Commit commit = new Commit(revCommit, revCommit.getFullMessage(), revCommit.getAuthorIdent().getWhen(), patches);
            		for(Patch patch : patches)
            		{
            			patch.setCommit(commit);
            			for(ArrayList<EditedLines> editedLinesList : patch.editedLinesList)
            			{
            				for(EditedLines editedLines : editedLinesList)
            					editedLines.setPatch(patch);
            			}
            		}
            		
            		commits.add(commit);
            	}
            }
            
            
            df.release();
            out.close();
            parent =   revCommit;  
             
             
             
        }
        

        System.out.println("total number of commits : " + numberOfCommits);		
		return commits;
	}
	
	
	public void printCommits(ArrayList<Commit> commits, Repository repo)
	{
		for(Commit commit : commits)
		{
			System.out.println("*****commit start*****");
			System.out.println(commit.formatDiffs(repo));
		}
	}
	
	public void writeTofile(String fileName,ArrayList<Commit> commits, Repository repo) throws FileNotFoundException
	{
		Formatter fr = new Formatter(fileName);
		fr.format("number of commits : %d\n", commits.size());
		for(Commit commit : commits)
		{
			fr.format("*****commit start*****\n");
			fr.format("%s\n",commit.formatDiffs(repo));
		}
		
		fr.close();
	}
	
	
	
	
	public void runOnMultipleProjects() throws Exception
	{
		MultipleProjectRunner mpr = new MultipleProjectRunner();
		
		
		ArrayList<Project> projects = mpr.listProjects();
		for (Project project : projects) 
		{
			
			

	        	
	        	 ProjectRunner pr = new ProjectRunner(project);
	        	 ArrayList<String> testDirs = pr.findTestDirNames();
	             File gitWorkDir = new File(project.getPath());
	             Git git = null;
	             git = Git.open(gitWorkDir);
	             
	             ArrayList<Commit> commits = getTestCommits(git, testDirs);
//	             System.out.println("number of commits that only change test files : " + commits.size());
//	             ArrayList<Commit> assertionCommits = getCommitsThatHaveKeyword(commits, "assert");
//	             System.out.println("number of commits that change assertions : " + assertionCommits.size());
//	             writeTofile("commitsChangingAssertions.txt",assertionCommits, git.getRepository());
	             ArrayList<EditedLines> assertionFaults = checkForKeywordChanges(commits, ".*assert.*\\(.*");
	             writeEditedLines("assertionFaultsLess8.txt", assertionFaults);
//	             ArrayList<EditedLines> wrongControlFlow = checkForKeywordChanges(commits, "[ ]*for[ ]*\\(.*");
//	             wrongControlFlow.addAll(checkForKeywordChanges(commits, "[ ]*if[ ]*\\(.*\\).*"));
//	             writeEditedLines("wrongControlFlow.txt", wrongControlFlow);
//	             
//	             System.out.println("number of commits that edit assertions : " + assertionFaults.size());
	             ArrayList<Commit> bugReportCommits = getCommitsWithBugReport(commits);
////	             writeTofile("commitsWithBugReport.txt",bugReportCommits,git.getRepository());
	             System.out.println("number of commits that point to a bug report: " + bugReportCommits.size());
	             
	             for(Commit commit : bugReportCommits)
	             {
	            	 commit.extractJiraBugReport();
	            	 if(commit.jiraBugReport.type.equals("Bug"))
	            		 System.out.println(commit.jiraBugReport.link);
	             }
	             
	             String stats = getBugReportStatistics(bugReportCommits);
	             System.out.println("*************");
	             System.out.println(stats);
	             
	             
//	             numberOfFilesChangedMetric("numberOfFilesChangedMetric.txt", commits);
//	             writeCommitsWithZeroPatches("zero.txt", commits, git.getRepository());
	             
//	             numberOfEditedLinesPerFile("editedLinesPercommit.csv", commits);
	           
	      }
	}
	
	
	public void numberOfEditedLinesPerFile(String fileName, ArrayList<Commit> commits) throws FileNotFoundException
	{
		
		
		Formatter fr = new Formatter(fileName);
		
		for(Commit commit : commits)
		{
			int num = 0;
			for(Patch patch : commit.patchs)
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
			fr.format("%s,%d\n", commit.revCommit.getId(), num);
		}
		fr.close();
	}
	
	public void writeCommitsWithZeroPatches(String fileName, ArrayList<Commit> commits, Repository repo) throws FileNotFoundException
	{
		Formatter fr = new Formatter(fileName);
		
		for (Commit commit : commits)
		{
			if (commit.patchs.size() == 0)
				fr.format("%s\n", commit.formatDiffs(repo));
		}
		
		fr.close();
	}
	
	public void numberOfFilesChangedMetric(String fileName, ArrayList<Commit> commits) throws FileNotFoundException
	{
		Formatter fr = new Formatter(fileName);
		for(Commit commit : commits)
		{
			fr.format("%s ,%d\n",commit.revCommit.getId().toString() , commit.patchs.size());
		}
		fr.close();
	}
	
	
	public void collectMetric(ArrayList<Commit> commits)
	{
		
	}
	
	public String getBugReportStatistics(ArrayList<Commit> commits)
	{
		HashMap<String, HashMap<String,Integer>> statMap = new HashMap<String, HashMap<String,Integer>>();
		HashMap<String, Integer> componentMap = new HashMap<String, Integer>();
		StringBuffer sb = new StringBuffer();
		
		for ( Commit commit : commits)
		{
			if (!statMap.containsKey(commit.jiraBugReport.project))
			{
				statMap.put(commit.jiraBugReport.project, new HashMap<String, Integer>());
			}
			
			HashMap<String, Integer> typeMap = statMap.get(commit.jiraBugReport.project);
			
			if (!typeMap.containsKey(commit.jiraBugReport.type))
			{
				typeMap.put(commit.jiraBugReport.type, 1);
			}
			else
			{
				typeMap.put(commit.jiraBugReport.type, typeMap.get(commit.jiraBugReport.type) + 1);
			}
			
			if(commit.jiraBugReport.type.equals("Bug"))
			{
				componentMap.put(commit.jiraBugReport.component, (componentMap.get(commit.jiraBugReport.component) == null ? 0 : componentMap.get(commit.jiraBugReport.component)) + 1);
			}
		}
	
		for (Map.Entry<String, HashMap<String, Integer>> projectEntry : statMap.entrySet())
		{
			sb.append(projectEntry.getKey() + " : \n");
			
			for ( Map.Entry<String, Integer> typeEntry : projectEntry.getValue().entrySet() )
			{
				sb.append("\t" + typeEntry.getKey() + " : " + typeEntry.getValue() + "\n");
				
				if (typeEntry.getKey().equals("Bug"))
				{
					for (Map.Entry<String, Integer> entry : componentMap.entrySet())
					{
						sb.append("\t\t" + entry.getKey() + " : " + entry.getValue() + "\n");
					}
				}
			}
		}
		
		
		
		return sb.toString();
		
	}
	
	
	
	public ArrayList<Commit> getCommitsThatHaveKeyword(ArrayList<Commit> commits, String keyword)
	{
//		final String keyword = "assert";
		ArrayList<Commit> assertionCommits = new ArrayList<Commit>();
		for(Commit commit : commits)
		{
			outerloop:
			for(Patch patch : commit.patchs)
			{
				for(ArrayList<EditedLines> editedLinesList : patch.editedLinesList)
				{
					for(EditedLines editedLines : editedLinesList)
					{
						for(String addedLine : editedLines.addedLines)
						{
							if(addedLine.contains(keyword))
							{
								assertionCommits.add(commit);
								break outerloop;
								
							}
						}
						for(String removedLine : editedLines.removedLines)
						{
							if(removedLine.contains(keyword))
							{
								assertionCommits.add(commit);
								break outerloop;
							}
						}
						
					}
				}
			}
		}
		
		return assertionCommits;
	}
	
	
	
	public ArrayList<EditedLines> checkForKeywordChanges(ArrayList<Commit> commits, String regrex)
	{
//		final String keyword = "assert";
		ArrayList<EditedLines> editedKeywordLines = new ArrayList<EditedLines>();
		for(Commit commit : commits)
		{
			outerloop:
			for(Patch patch : commit.patchs)
			{
				for(ArrayList<EditedLines> editedLinesList : patch.editedLinesList)
				{
					for(EditedLines editedLines : editedLinesList)
					{
						boolean keywordRemoved = false, keywordAdded = false;
						for(String addedLine : editedLines.addedLines)
						{
							if(addedLine.matches(regrex))
							{
								keywordAdded = true;
								break;
							}
						}
						for(String removedLine : editedLines.removedLines)
						{
							if(removedLine.matches(regrex))
							{
								keywordRemoved = true;
								break;
							}
						}
						
						if(keywordRemoved & keywordAdded)
						{
							editedKeywordLines.add(editedLines);
						}
					}
				}
			}
		}
		
		return editedKeywordLines;
	}

	
	
	public void writeEditedLines(String fileName, ArrayList<EditedLines> editedLinesList) throws FileNotFoundException
	{
		Formatter fr = new Formatter(fileName);
		fr.format("number of edits : %d\n", editedLinesList.size());
		for(EditedLines editedLine : editedLinesList)
		{
			
			fr.format("***********\n file: %s\ncommit: %s\nnumber of editedLines : %d\n\n",editedLine.getPatch().newFilePath,editedLine.getPatch().getCommit().message, editedLine.getPatch().getCommit().getNumberOfEditedLines());
			
			
			for(LinePair pair : editedLine.matches)
			{
				fr.format("- %s\n",pair.a);
				fr.format("+ %s\n",pair.b);
			}
			
//			for(String removedLine : editedLine.removedLines)
//				fr.format("- %s\n",removedLine);
//			
//			for(String addedLine : editedLine.addedLines)
//				fr.format("+ %s\n",addedLine);
			
			fr.format("\n");
		}
		
		fr.close();
	}
	
	
	public ArrayList<Commit> getCommitsWithBugReport(ArrayList<Commit> commits)
	{
		ArrayList<Commit> bugReportCommits = new ArrayList<Commit>();
		for(Commit commit : commits)
		{
			if(commit.checkMessageForBugReport())
				bugReportCommits.add(commit);
		}
		
		return bugReportCommits;
	}
	
	
	public static void main(String[] args) throws Exception {
		GitLogRunner glr = new GitLogRunner();
		glr.runOnMultipleProjects();
		
	}
	
		
		
}

